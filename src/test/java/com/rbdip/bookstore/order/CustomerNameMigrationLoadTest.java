package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbdip.bookstore.BookstoreApplication;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class CustomerNameMigrationLoadTest {

    private static final int LOAD_THREADS = 4;
    private static final Duration LOAD_DURATION = Duration.ofSeconds(4);

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bookstore_load")
            .withUsername("bookstore")
            .withPassword("bookstore");

    @Test
    void apiKeepsServingWhileNameSplitMigrationsRoll() throws Exception {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUser(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        // Состояние после ЛР2: есть full_name, есть "старый" клиент.
        flyway(dataSource, "3").migrate();
        jdbc.update("INSERT INTO customers (full_name) VALUES ('Иванов Иван')");

        // Expand до старта новой версии приложения (как в реальном деплое).
        flyway(dataSource, "4").migrate();

        try (ConfigurableWebServerApplicationContext app =
                (ConfigurableWebServerApplicationContext) new SpringApplicationBuilder(BookstoreApplication.class)
                        .run(
                                "--server.port=0",
                                "--spring.datasource.url=" + postgres.getJdbcUrl(),
                                "--spring.datasource.username=" + postgres.getUsername(),
                                "--spring.datasource.password=" + postgres.getPassword(),
                                // Миграциями управляет сам тест, а не автоконфигурация:
                                // Flyway у Boot приоритетнее defaultProperties, поэтому
                                // отключение передаётся через аргументы командной строки.
                                "--spring.flyway.enabled=false")) {
            String base = "http://localhost:" + app.getWebServer().getPort();
            RestTemplate rest = new RestTemplate();

            ResponseEntity<Map> product = rest.postForEntity(
                    base + "/products", Map.of("name", "Clean Code", "price", "35.00"), Map.class);
            assertThat(product.getStatusCode().value()).isEqualTo(201);
            ResponseEntity<Map> firstOrder = rest.postForEntity(
                    base + "/orders", orderRequest("Ivan Petrov"), Map.class);
            assertThat(firstOrder.getStatusCode().value()).isEqualTo(201);

            AtomicInteger errors = new AtomicInteger();
            AtomicInteger requests = new AtomicInteger();
            AtomicBoolean running = new AtomicBoolean(true);
            CountDownLatch started = new CountDownLatch(LOAD_THREADS);
            ExecutorService pool = Executors.newFixedThreadPool(LOAD_THREADS);
            String[] names = {"Ivan Petrov", "Anna Sidorova", "Petr Ivanov", "Olga Smirnova"};

            for (int i = 0; i < LOAD_THREADS; i++) {
                String name = names[i % names.length];
                boolean writer = i % 2 == 0;
                pool.submit(() -> {
                    started.countDown();
                    long deadline = System.nanoTime() + LOAD_DURATION.toNanos();
                    while (running.get() && System.nanoTime() < deadline) {
                        try {
                            ResponseEntity<?> response = writer
                                    ? rest.postForEntity(base + "/orders", orderRequest(name), Map.class)
                                    : rest.getForEntity(base + "/orders", List.class);
                            if (response.getStatusCode().is2xxSuccessful()) {
                                requests.incrementAndGet();
                            } else {
                                errors.incrementAndGet();
                            }
                        } catch (Exception e) {
                            errors.incrementAndGet();
                        }
                    }
                });
            }
            assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();

            // Основная фаза: под живой нагрузкой катятся backfill и contract.
            flyway(dataSource, "5").migrate();
            flyway(dataSource, null).migrate();

            running.set(false);
            pool.shutdown();
            assertThat(pool.awaitTermination(15, TimeUnit.SECONDS)).isTrue();

            assertThat(requests.get())
                    .as("Под нагрузкой должны были выполниться запросы к API")
                    .isPositive();
            assertThat(errors.get())
                    .as("Во время наката миграций API не должен возвращать ошибок")
                    .isZero();

            List<String> columns = jdbc.queryForList(
                    "SELECT column_name FROM information_schema.columns WHERE table_name = 'customers'",
                    String.class);
            assertThat(columns)
                    .doesNotContain("full_name")
                    .contains("first_name", "last_name");

            // Backfill для клиента, существовавшего до ЛР3: первое слово
            // стало first_name, остаток - last_name.
            Map<String, Object> seeded = jdbc.queryForMap(
                    "SELECT first_name, last_name FROM customers WHERE first_name = 'Иванов'");
            assertThat(seeded.get("last_name")).isEqualTo("Иван");
        }
    }

    private CreateOrderRequest orderRequest(String customerName) {
        return new CreateOrderRequest(
                customerName, "Test Address", null, "regular", null,
                List.of(new CreateOrderRequest.Item(1L, 1)));
    }

    private static Flyway flyway(PGSimpleDataSource dataSource, String target) {
        var config = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration");
        if (target != null) {
            config.target(target);
        }
        return config.load();
    }
}

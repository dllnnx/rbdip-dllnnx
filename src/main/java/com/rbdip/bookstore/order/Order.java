package com.rbdip.bookstore.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Намеренно денормализованная сущность: хранит "сырые" контактные данные
 * клиента прямо в заказе вместо ссылки на отдельную таблицу customers.
 * Это цель для нормализации схемы в ЛР2, а поле customerFullName - цель
 * expand-contract миграции в ЛР3 (разбить на firstName/lastName).
 */
@Entity
@Table(name = "orders")
// JPA-сущность по определению является носителем состояния, отдаваемым
// геттерами; добавлять к ней бизнес-поведение ради PMD DataClass не нужно.
@SuppressWarnings("PMD.DataClass")
public class Order {

    public static final String STATUS_NEW = "new";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_full_name", nullable = false)
    private String customerFullName;

    @Column(name = "customer_address")
    private String customerAddress;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(nullable = false)
    private String status;

    // final у персистентного поля запрещён JPA: Hibernate записывает его
    // при загрузке из БД.
    @Column(name = "created_at", nullable = false)
    @SuppressWarnings("PMD.ImmutableField")
    private Instant createdAt = Instant.now();

    protected Order() {
        // for JPA
    }

    public Order(String customerFullName, String customerAddress, String customerPhone, String status) {
        this.customerFullName = customerFullName;
        this.customerAddress = customerAddress;
        this.customerPhone = customerPhone;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerFullName() {
        return customerFullName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

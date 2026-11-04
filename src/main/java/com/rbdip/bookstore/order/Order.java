package com.rbdip.bookstore.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Заказ. Данные клиента нормализованы в ЛР2: вместо дублированных
 * колонок customer_full_name/address/phone - ссылка на customers.
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

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

    public Order(Customer customer, String status) {
        this.customer = customer;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

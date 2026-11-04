package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Позиция заказа: ссылка на товар из каталога вместо дублированных
 * названия/цены (нормализация ЛР2).
 */
@Entity
@Table(name = "order_items")
// JPA-сущность по определению является носителем состояния, отдаваемым
// геттерами; добавлять к ней бизнес-поведение ради PMD DataClass не нужно.
@SuppressWarnings("PMD.DataClass")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    protected OrderItem() {
        // for JPA
    }

    public OrderItem(Long orderId, Product product, Integer quantity) {
        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Product getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }
}

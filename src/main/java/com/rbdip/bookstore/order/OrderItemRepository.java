package com.rbdip.bookstore.order;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("select i from OrderItem i join fetch i.product where i.orderId in :orderIds")
    List<OrderItem> findAllByOrderIdIn(Collection<Long> orderIds);
}

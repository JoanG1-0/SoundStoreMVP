package com.soundstore.backend.repository;

import com.soundstore.backend.model.Order;
import com.soundstore.backend.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT COUNT(o) FROM Order o WHERE FUNCTION('YEAR', o.createdAt) = :year")
    long countByYear(@Param("year") int year);

    @Query("SELECT o FROM Order o WHERE o.status NOT IN :excluidos ORDER BY o.createdAt DESC")
    List<Order> findActivos(@Param("excluidos") List<OrderStatus> excluidos);

    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);
}

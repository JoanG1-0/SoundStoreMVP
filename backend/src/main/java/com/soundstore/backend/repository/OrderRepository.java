package com.soundstore.backend.repository;

import com.soundstore.backend.model.Order;
import com.soundstore.backend.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query(value = "SELECT COUNT(*) FROM orders o WHERE EXTRACT(YEAR FROM o.created_at) = :year", nativeQuery = true)
    long countByYear(@Param("year") int year);

    @Query("SELECT o FROM Order o WHERE o.status NOT IN :excluidos ORDER BY o.createdAt DESC")
    List<Order> findActivos(@Param("excluidos") List<OrderStatus> excluidos);

    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query(value = "SELECT COUNT(*) FROM orders WHERE created_at::date = CURRENT_DATE", nativeQuery = true)
    long countToday();

    long countByStatus(OrderStatus status);

    @Query(value = "SELECT COALESCE(SUM(total), 0) FROM orders " +
                   "WHERE EXTRACT(YEAR FROM created_at) = :year " +
                   "AND EXTRACT(MONTH FROM created_at) = :month " +
                   "AND status != 'CANCELLED'", nativeQuery = true)
    java.math.BigDecimal sumVentasMes(@Param("year") int year, @Param("month") int month);
}

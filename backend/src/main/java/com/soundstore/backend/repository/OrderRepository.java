package com.soundstore.backend.repository;

import com.soundstore.backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT COUNT(o) FROM Order o WHERE FUNCTION('YEAR', o.createdAt) = :year")
    long countByYear(@Param("year") int year);
}

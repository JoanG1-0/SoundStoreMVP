package com.soundstore.backend.repository;

import com.soundstore.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByActiveTrueAndStockGreaterThan(int stock);

    List<Product> findByGenreIgnoreCaseAndActiveTrueAndStockGreaterThan(String genre, int stock);

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stock > 0 " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.genre) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Product> searchByNameOrGenre(@Param("search") String search);
}

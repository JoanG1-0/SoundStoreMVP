package com.soundstore.backend.repository;

import com.soundstore.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByActiveTrueAndStockGreaterThan(int stock);

    List<Product> findByGenreIgnoreCaseAndActiveTrueAndStockGreaterThan(String genre, int stock);
}

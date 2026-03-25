package com.soundstore.backend.service;

import com.soundstore.backend.dto.product.CreateProductRequestDto;
import com.soundstore.backend.dto.product.ProductResponseDto;
import com.soundstore.backend.dto.product.UpdateProductRequestDto;
import com.soundstore.backend.exception.ProductNotFoundException;
import com.soundstore.backend.exception.UserNotFoundException;
import com.soundstore.backend.model.Product;
import com.soundstore.backend.model.User;
import com.soundstore.backend.repository.ProductRepository;
import com.soundstore.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getCatalog(String genre, String search) {
        List<Product> products;
        if (search != null && !search.isBlank()) {
            products = productRepository.searchByNameOrGenre(search.trim());
        } else if (genre != null && !genre.isBlank()) {
            products = productRepository.findByGenreIgnoreCaseAndActiveTrueAndStockGreaterThan(genre, 0);
        } else {
            products = productRepository.findByActiveTrueAndStockGreaterThan(0);
        }
        return products.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));
        return toDto(product);
    }

    @Transactional
    public ProductResponseDto create(CreateProductRequestDto request, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        Product product = Product.builder()
                .name(request.name().trim())
                .description(request.description().trim())
                .price(request.price())
                .genre(request.genre().trim())
                .stock(request.stock())
                .imageUrl(request.imageUrl())
                .active(true)
                .createdBy(creator)
                .build();

        return toDto(productRepository.save(product));
    }

    @Transactional
    public ProductResponseDto update(UUID id, UpdateProductRequestDto request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));

        product.setName(request.name().trim());
        product.setDescription(request.description().trim());
        product.setPrice(request.price());
        product.setGenre(request.genre().trim());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());

        return toDto(productRepository.save(product));
    }

    @Transactional
    public ProductResponseDto uploadImage(UUID id, MultipartFile file) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));

        String imageUrl = cloudinaryService.uploadImage(file);
        product.setImageUrl(imageUrl);
        return toDto(productRepository.save(product));
    }

    @Transactional
    public ProductResponseDto toggleStatus(UUID id, boolean active) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));

        product.setActive(active);
        return toDto(productRepository.save(product));
    }

    private ProductResponseDto toDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getGenre(),
                product.getStock(),
                product.getImageUrl(),
                product.isActive(),
                product.getCreatedBy().getId(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}

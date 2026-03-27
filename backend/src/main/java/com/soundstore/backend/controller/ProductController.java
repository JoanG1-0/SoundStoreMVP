package com.soundstore.backend.controller;

import com.soundstore.backend.dto.product.CreateProductRequestDto;
import com.soundstore.backend.dto.product.ProductResponseDto;
import com.soundstore.backend.dto.product.ToggleProductStatusRequestDto;
import com.soundstore.backend.dto.product.UpdateProductRequestDto;
import com.soundstore.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/gestion")
    public List<ProductResponseDto> getAllForManagement() {
        return productService.getAllForManagement();
    }

    @GetMapping
    public List<ProductResponseDto> getCatalog(
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String search) {
        return productService.getCatalog(genre, search);
    }

    @GetMapping("/{id}")
    public ProductResponseDto getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDto create(
            @Valid @RequestBody CreateProductRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return productService.create(request, userDetails.getUsername());
    }

    @PutMapping("/{id}")
    public ProductResponseDto update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequestDto request) {
        return productService.update(id, request);
    }

    @PostMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProductResponseDto uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        return productService.uploadImage(id, file);
    }

    @PatchMapping("/{id}/estado")
    public ProductResponseDto toggleStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ToggleProductStatusRequestDto request) {
        return productService.toggleStatus(id, request.active());
    }
}

package com.soundstore.backend.service;

import com.soundstore.backend.dto.product.CreateProductRequestDto;
import com.soundstore.backend.dto.product.ProductResponseDto;
import com.soundstore.backend.dto.product.UpdateProductRequestDto;
import com.soundstore.backend.exception.ProductNotFoundException;
import com.soundstore.backend.exception.UserNotFoundException;
import com.soundstore.backend.model.Product;
import com.soundstore.backend.model.User;
import com.soundstore.backend.model.UserRole;
import com.soundstore.backend.repository.ProductRepository;
import com.soundstore.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private CloudinaryService cloudinaryService;

    @InjectMocks
    private ProductService productService;

    private User buildSeller() {
        return User.builder()
                .id(UUID.randomUUID())
                .fullName("Vendedor Test")
                .email("vendedor@test.com")
                .passwordHash("$2a$10$hashed")
                .phone("3001234567")
                .role(UserRole.SELLER)
                .active(true)
                .emailVerified(true)
                .build();
    }

    private Product buildProduct(User seller) {
        return Product.builder()
                .id(UUID.randomUUID())
                .name("USB Salsa")
                .description("Memorias con salsa clásica")
                .price(new BigDecimal("25000.00"))
                .genre("Salsa")
                .stock(10)
                .imageUrl(null)
                .active(true)
                .createdBy(seller)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ─── getCatalog ───────────────────────────────────────────────────────────

    @Test
    void getCatalog_sinFiltro_retornaProductosActivosConStock() {
        User seller = buildSeller();
        Product product = buildProduct(seller);

        when(productRepository.findByActiveTrueAndStockGreaterThan(0)).thenReturn(List.of(product));

        List<ProductResponseDto> result = productService.getCatalog(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("USB Salsa");
    }

    @Test
    void getCatalog_conGenero_filtraPorGenero() {
        User seller = buildSeller();
        Product product = buildProduct(seller);

        when(productRepository.findByGenreIgnoreCaseAndActiveTrueAndStockGreaterThan("Salsa", 0))
                .thenReturn(List.of(product));

        List<ProductResponseDto> result = productService.getCatalog("Salsa");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).genre()).isEqualTo("Salsa");
    }

    @Test
    void getCatalog_generoVacio_retornaTodosLosProductos() {
        User seller = buildSeller();
        Product product = buildProduct(seller);

        when(productRepository.findByActiveTrueAndStockGreaterThan(0)).thenReturn(List.of(product));

        List<ProductResponseDto> result = productService.getCatalog("  ");

        assertThat(result).hasSize(1);
        verify(productRepository).findByActiveTrueAndStockGreaterThan(0);
        verify(productRepository, never()).findByGenreIgnoreCaseAndActiveTrueAndStockGreaterThan(anyString(), anyInt());
    }

    // ─── getById ─────────────────────────────────────────────────────────────

    @Test
    void getById_existente_retornaProducto() {
        User seller = buildSeller();
        Product product = buildProduct(seller);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        ProductResponseDto result = productService.getById(product.getId());

        assertThat(result.id()).isEqualTo(product.getId());
        assertThat(result.name()).isEqualTo("USB Salsa");
    }

    @Test
    void getById_noExiste_lanzaProductNotFoundException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getById(id));
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    void create_datosValidos_creaProductoCorrectamente() {
        User seller = buildSeller();
        CreateProductRequestDto request = new CreateProductRequestDto(
                "USB Rock", "Rock clásico", new BigDecimal("30000.00"), "Rock", 5, null);

        when(userRepository.findByEmail("vendedor@test.com")).thenReturn(Optional.of(seller));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p = Product.builder()
                    .id(UUID.randomUUID())
                    .name(p.getName())
                    .description(p.getDescription())
                    .price(p.getPrice())
                    .genre(p.getGenre())
                    .stock(p.getStock())
                    .imageUrl(p.getImageUrl())
                    .active(p.isActive())
                    .createdBy(p.getCreatedBy())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            return p;
        });

        ProductResponseDto result = productService.create(request, "vendedor@test.com");

        assertThat(result.name()).isEqualTo("USB Rock");
        assertThat(result.price()).isEqualByComparingTo("30000.00");
        assertThat(result.active()).isTrue();
        assertThat(result.createdBy()).isEqualTo(seller.getId());
    }

    @Test
    void create_nombreSeTrima() {
        User seller = buildSeller();
        CreateProductRequestDto request = new CreateProductRequestDto(
                "  USB Rock  ", "Desc", new BigDecimal("10000.00"), "Rock", 1, null);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(seller));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            p.setCreatedAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            return p;
        });

        productService.create(request, "vendedor@test.com");

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("USB Rock");
    }

    @Test
    void create_usuarioNoExiste_lanzaUserNotFoundException() {
        CreateProductRequestDto request = new CreateProductRequestDto(
                "USB Rock", "Desc", new BigDecimal("10000.00"), "Rock", 1, null);

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> productService.create(request, "noexiste@test.com"));
        verify(productRepository, never()).save(any());
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    void update_productoExistente_actualizaCampos() {
        User seller = buildSeller();
        Product product = buildProduct(seller);
        UpdateProductRequestDto request = new UpdateProductRequestDto(
                "USB Salsa Editada", "Nueva desc", new BigDecimal("28000.00"), "Salsa", 20, "http://img.url");

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponseDto result = productService.update(product.getId(), request);

        assertThat(result.name()).isEqualTo("USB Salsa Editada");
        assertThat(result.price()).isEqualByComparingTo("28000.00");
        assertThat(result.stock()).isEqualTo(20);
        assertThat(result.imageUrl()).isEqualTo("http://img.url");
    }

    @Test
    void update_productoNoExiste_lanzaProductNotFoundException() {
        UUID id = UUID.randomUUID();
        UpdateProductRequestDto request = new UpdateProductRequestDto(
                "X", "X", new BigDecimal("1000.00"), "X", 0, null);

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.update(id, request));
        verify(productRepository, never()).save(any());
    }

    // ─── toggleStatus ─────────────────────────────────────────────────────────

    @Test
    void toggleStatus_desactivaProducto() {
        User seller = buildSeller();
        Product product = buildProduct(seller);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponseDto result = productService.toggleStatus(product.getId(), false);

        assertThat(result.active()).isFalse();
    }

    @Test
    void toggleStatus_activaProducto() {
        User seller = buildSeller();
        Product product = buildProduct(seller);
        product.setActive(false);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponseDto result = productService.toggleStatus(product.getId(), true);

        assertThat(result.active()).isTrue();
    }

    @Test
    void toggleStatus_productoNoExiste_lanzaProductNotFoundException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.toggleStatus(id, false));
        verify(productRepository, never()).save(any());
    }

    // ─── uploadImage ──────────────────────────────────────────────────────────

    @Test
    void uploadImage_archivoValido_actualizaImageUrlEnProducto() {
        User seller = buildSeller();
        Product product = buildProduct(seller);
        MockMultipartFile file = new MockMultipartFile(
                "file", "img.jpg", "image/jpeg", "contenido".getBytes());

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cloudinaryService.uploadImage(file)).thenReturn("https://res.cloudinary.com/test.jpg");
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponseDto result = productService.uploadImage(product.getId(), file);

        assertThat(result.imageUrl()).isEqualTo("https://res.cloudinary.com/test.jpg");
    }

    @Test
    void uploadImage_productoNoExiste_lanzaProductNotFoundException() {
        UUID id = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "img.jpg", "image/jpeg", "contenido".getBytes());

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.uploadImage(id, file));
        verify(cloudinaryService, never()).uploadImage(any());
    }
}

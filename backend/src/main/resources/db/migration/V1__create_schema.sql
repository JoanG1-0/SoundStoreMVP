-- ============================================================
-- V1: Esquema inicial SoundStore
-- ============================================================

-- Tipos ENUM
CREATE TYPE user_role     AS ENUM ('BUYER', 'SELLER', 'ADMIN');
CREATE TYPE order_status  AS ENUM ('PENDING', 'CONFIRMED', 'PREPARING', 'READY_PICKUP', 'ON_THE_WAY', 'DELIVERED', 'CANCELLED');
CREATE TYPE delivery_type AS ENUM ('DELIVERY', 'PICKUP');
CREATE TYPE otp_type      AS ENUM ('REGISTRATION', 'PASSWORD_RESET');

-- ============================================================
-- Tabla: users
-- ============================================================
CREATE TABLE users (
    id             UUID         NOT NULL DEFAULT gen_random_uuid(),
    full_name      VARCHAR(120) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    phone          VARCHAR(20)  NOT NULL,
    address        TEXT,
    role           user_role    NOT NULL,
    is_active      BOOLEAN      NOT NULL DEFAULT true,
    email_verified BOOLEAN      NOT NULL DEFAULT false,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_users        PRIMARY KEY (id),
    CONSTRAINT uq_users_email  UNIQUE (email)
);

-- ============================================================
-- Tabla: products
-- ============================================================
CREATE TABLE products (
    id          UUID           NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(150)   NOT NULL,
    description TEXT           NOT NULL,
    price       DECIMAL(10,2)  NOT NULL,
    genre       VARCHAR(80)    NOT NULL,
    stock       INTEGER        NOT NULL DEFAULT 0,
    image_url   VARCHAR(500),
    is_active   BOOLEAN        NOT NULL DEFAULT true,
    created_by  UUID           NOT NULL,
    created_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_products              PRIMARY KEY (id),
    CONSTRAINT chk_products_price       CHECK (price > 0),
    CONSTRAINT chk_products_stock       CHECK (stock >= 0),
    CONSTRAINT fk_products_created_by   FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);

-- ============================================================
-- Tabla: orders
-- ============================================================
CREATE TABLE orders (
    id               UUID           NOT NULL DEFAULT gen_random_uuid(),
    order_number     VARCHAR(20)    NOT NULL,
    user_id          UUID           NOT NULL,
    status           order_status   NOT NULL DEFAULT 'PENDING',
    delivery_type    delivery_type  NOT NULL,
    delivery_address TEXT,
    total            DECIMAL(12,2)  NOT NULL,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_orders                PRIMARY KEY (id),
    CONSTRAINT uq_orders_order_number   UNIQUE (order_number),
    CONSTRAINT chk_orders_total         CHECK (total >= 0),
    CONSTRAINT fk_orders_user           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- ============================================================
-- Tabla: order_items
-- ============================================================
CREATE TABLE order_items (
    id          UUID           NOT NULL DEFAULT gen_random_uuid(),
    order_id    UUID           NOT NULL,
    product_id  UUID           NOT NULL,
    quantity    INTEGER        NOT NULL,
    unit_price  DECIMAL(10,2)  NOT NULL,
    subtotal    DECIMAL(12,2)  NOT NULL,
    CONSTRAINT pk_order_items               PRIMARY KEY (id),
    CONSTRAINT chk_order_items_quantity     CHECK (quantity > 0),
    CONSTRAINT chk_order_items_unit_price   CHECK (unit_price > 0),
    CONSTRAINT fk_order_items_order         FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product       FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

-- ============================================================
-- Tabla: otp_codes
-- ============================================================
CREATE TABLE otp_codes (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    email      VARCHAR(255) NOT NULL,
    code       VARCHAR(6)   NOT NULL,
    type       otp_type     NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT false,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_otp_codes PRIMARY KEY (id)
);

-- ============================================================
-- Indices
-- ============================================================
CREATE INDEX idx_users_email               ON users(email);
CREATE INDEX idx_products_active_stock     ON products(is_active, stock);
CREATE INDEX idx_products_genre            ON products(genre);
CREATE INDEX idx_orders_user_id            ON orders(user_id);
CREATE INDEX idx_orders_status             ON orders(status);
CREATE INDEX idx_order_items_order_id      ON order_items(order_id);
CREATE INDEX idx_otp_codes_email_type_used ON otp_codes(email, type, used);

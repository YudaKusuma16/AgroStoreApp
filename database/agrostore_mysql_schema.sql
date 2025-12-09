-- Database AgroStore
-- Created for Mobile App Programming Project
-- MySQL/MariaDB Schema

-- Create database
CREATE DATABASE IF NOT EXISTS agrostore;
USE agrostore;

-- Users table
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('pembeli', 'penjual') NOT NULL DEFAULT 'pembeli',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE products (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    description TEXT,
    stock INT NOT NULL DEFAULT 0,
    image_url VARCHAR(500),
    seller_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Orders table
CREATE TABLE orders (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    status ENUM('Menunggu Pembayaran', 'Dibayar', 'Dikemas', 'Dikirim', 'Selesai', 'Dibatalkan') NOT NULL DEFAULT 'Menunggu Pembayaran',
    shipping_address TEXT,
    payment_method VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Order items table (junction table for orders and products)
CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL, -- Price at time of purchase
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Reviews table
CREATE TABLE reviews (
    id VARCHAR(50) PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_review (product_id, user_id) -- One review per user per product
);

-- Cart table (for persistent cart across sessions)
CREATE TABLE cart (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    product_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_cart_item (user_id, product_id) -- One cart item per user per product
);

-- Create indexes for better performance
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_seller ON products(seller_id);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);
CREATE INDEX idx_cart_user ON cart(user_id);

-- Insert sample sellers (penjual)
INSERT INTO users (id, name, email, password, role) VALUES
('seller1', 'Toko Pertanian Makmur', 'toko@agro.com', '$2y$10$rOKd7WzcxRNZB7AWo.7RweUeMZsxqnYIr1tkw0JkJhyxuNUEW.3my', 'penjual'),
('seller2', 'Kios Benih Unggul', 'benih@agro.com', '$2y$10$rOKd7WzcxRNZB7AWo.7RweUeMZsxqnYIr1tkw0JkJhyxuNUEW.3my', 'penjual'),
('seller3', 'Alat Tani Indonesia', 'alat@agro.com', '$2y$10$rOKd7WzcxRNZB7AWo.7RweUeMZsxqnYIr1tkw0JkJhyxuNUEW.3my', 'penjual');

-- Insert sample buyers (pembeli)
INSERT INTO users (id, name, email, password, role) VALUES
('user1', 'Petani Joko', 'joko@agro.com', '$2y$10$rOKd7WzcxRNZB7AWo.7RweUeMZsxqnYIr1tkw0JkJhyxuNUEW.3my', 'pembeli'),
('user2', 'Petani Siti', 'siti@agro.com', '$2y$10$rOKd7WzcxRNZB7AWo.7RweUeMZsxqnYIr1tkw0JkJhyxuNUEW.3my', 'pembeli');

-- Insert sample products with sequential IDs
INSERT INTO products (id, name, category, price, description, stock, seller_id) VALUES
('prod1', 'Pupuk Urea 50kg', 'Pupuk', 250000.00, 'Pupuk urea berkualitas tinggi untuk tanaman padi dan palawija', 100, 'seller1'),
('prod2', 'Cangkul Baja Prima', 'Alat Pertanian', 85000.00, 'Cangkul berkualitas dengan gagang kayu jati yang kuat', 50, 'seller1'),
('prod3', 'Benih Jagung Hibrida NK-22', 'Benih', 150000.00, 'Benih jagung hibrida unggul dengan hasil panen tinggi', 200, 'seller2'),
('prod4', 'Sprayer Elektrik 16L', 'Alat Semprot', 450000.00, 'Sprayer elektrik untuk penyemprotan pestisida dan pupuk cair', 30, 'seller2'),
('prod5', 'Sabit Panen Tajam', 'Alat Panen', 35000.00, 'Sabit tajam untuk memanen padi dan rumput', 75, 'seller1'),
('prod6', 'Pupuk NPK 16-16-16', 'Pupuk', 180000.00, 'Pupuk NPK lengkap untuk semua jenis tanaman', 80, 'seller3'),
('prod7', 'Terpal Plastik 8x10m', 'Perlengkapan', 125000.00, 'Terpal plastik kuat untuk menjemur hasil panen', 40, 'seller3'),
('prod8', 'Sekop Mini Taman', 'Alat Pertanian', 45000.00, 'Sekop mini untuk perawatan kebun dan taman', 60, 'seller1'),
('prod9', 'Benih Padi IR-64', 'Benih', 75000.00, 'Benih padi IR-64 unggul dengan umur panen 115 hari', 150, 'seller2'),
('prod10', 'Pestisida Organik 500ml', 'Pestisida', 55000.00, 'Pestisida organik aman untuk lingkungan dan hasil panen', 90, 'seller3');

-- Note:
-- 1. Password for all sample users is '123456' (hashed with bcrypt)
-- 2. ID Format:
--    - Users: user[1-9...] for pembeli, seller[1-9...] for penjual
--    - Products: prod[1-9...]
--    - Orders: order[1-9...]
--    - Reviews: rev[1-9...]
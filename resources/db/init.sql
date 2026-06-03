-- Create database if not exists
CREATE DATABASE IF NOT EXISTS campus_auction CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_auction;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'user',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'normal'
);

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    price DOUBLE NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    shipping_method VARCHAR(50) NOT NULL,
    after_sales VARCHAR(100) NOT NULL,
    product_condition VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'for_sale',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    price DOUBLE NOT NULL,
    status VARCHAR(20) DEFAULT 'pending_payment',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create messages table
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create favorites table
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_product (user_id, product_id)
);

-- Create auctions table
CREATE TABLE IF NOT EXISTS auctions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    starting_price DOUBLE NOT NULL,
    current_price DOUBLE NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'ongoing',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Create bids table
CREATE TABLE IF NOT EXISTS bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    auction_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    price DOUBLE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Insert initial user
INSERT INTO users (username, password, role, created_at, status)
VALUES ('peter', '123456', 'user', CURRENT_TIMESTAMP, 'normal')
ON DUPLICATE KEY UPDATE username = username;

-- Insert admin user
INSERT INTO users (username, password, role, created_at, status)
VALUES ('admin', '123456', 'admin', CURRENT_TIMESTAMP, 'normal')
ON DUPLICATE KEY UPDATE username = username;

-- Insert initial products
INSERT INTO products (user_id, category, title, price, image_url, shipping_method, after_sales, product_condition, description, status, created_at)
VALUES 
(1, 'Electronics', 'iPhone 13 Pro', 5999.00, 'https://example.com/iphone13.jpg', 'Express Shipping', '30-day return policy', 'Like new', 'Used iPhone 13 Pro with 128GB storage, in excellent condition.', 'for_sale', CURRENT_TIMESTAMP),
(1, 'Clothing', 'Nike Air Max Shoes', 799.00, 'https://example.com/nike.jpg', 'Standard Shipping', '14-day return policy', 'New with tags', 'Brand new Nike Air Max shoes, size 42.', 'for_sale', CURRENT_TIMESTAMP),
(1, 'Books', 'Spring Boot in Action', 89.00, 'https://example.com/springboot.jpg', 'Standard Shipping', '7-day return policy', 'Like new', 'Used Spring Boot in Action book, in great condition.', 'for_sale', CURRENT_TIMESTAMP),
(1, 'Electronics', 'MacBook Air M2', 8999.00, 'https://example.com/macbook.jpg', 'Express Shipping', '30-day return policy', 'New', 'Brand new MacBook Air M2 with 8GB RAM and 256GB SSD.', 'for_sale', CURRENT_TIMESTAMP),
(1, 'Home', 'IKEA Desk', 499.00, 'https://example.com/ikea.jpg', 'Local Pickup', 'No return policy', 'Used', 'Used IKEA desk in good condition.', 'for_sale', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE title = title;

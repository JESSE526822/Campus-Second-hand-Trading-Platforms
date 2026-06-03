package com.campus.auction.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<Map<String, Object>> productRowMapper = new RowMapper<Map<String, Object>>() {
        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> product = new HashMap<>();
            product.put("id", rs.getInt("id"));
            product.put("userId", rs.getInt("user_id"));
            product.put("ownerName", rs.getString("username"));
            product.put("category", rs.getString("category"));
            product.put("title", rs.getString("title"));
            product.put("price", rs.getDouble("price"));
            product.put("imageUrl", rs.getString("image_url"));
            product.put("shippingMethod", rs.getString("shipping_method"));
            product.put("afterSales", rs.getString("after_sales"));
            product.put("condition", rs.getString("product_condition"));
            product.put("description", rs.getString("description"));
            product.put("status", rs.getString("status"));
            product.put("createdAt", rs.getTimestamp("created_at"));
            return product;
        }
    };

    @GetMapping("/search")
    public Map<String, Object> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sortBy) {
        try {
            StringBuilder sql = new StringBuilder("SELECT p.*, u.username FROM products p JOIN users u ON p.user_id = u.id WHERE 1=1");
            List<Object> params = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                sql.append(" AND (p.title LIKE ? OR p.description LIKE ?)");
                params.add("%" + keyword + "%");
                params.add("%" + keyword + "%");
            }

            if (category != null && !category.isEmpty()) {
                sql.append(" AND p.category = ?");
                params.add(category);
            }

            if (sortBy != null) {
                switch (sortBy) {
                    case "price_asc":
                        sql.append(" ORDER BY p.price ASC");
                        break;
                    case "price_desc":
                        sql.append(" ORDER BY p.price DESC");
                        break;
                    default:
                        sql.append(" ORDER BY p.created_at DESC");
                }
            } else {
                sql.append(" ORDER BY p.created_at DESC");
            }

            List<Map<String, Object>> products = jdbcTemplate.query(sql.toString(), params.toArray(), productRowMapper);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("products", products);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error searching products: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/info/{id}")
    public Map<String, Object> getProductInfo(@PathVariable String id) {
        try {
            String sql = "SELECT p.*, u.username FROM products p JOIN users u ON p.user_id = u.id WHERE p.id = ?";
            List<Map<String, Object>> products = jdbcTemplate.query(sql, new Object[]{id}, productRowMapper);

            if (products.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Product not found");
                return response;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("product", products.get(0));
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error getting product info: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/categories")
    public Map<String, Object> getCategories() {
        try {
            String sql = "SELECT DISTINCT category FROM products";
            List<String> categories = jdbcTemplate.queryForList(sql, String.class);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("categories", categories);
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error getting categories: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/import")
    public Map<String, Object> importProducts() {
        try {
            // 确保用户表中有对应的用户记录
            String[] users = {"nick", "peter", "bill", "robin"};
            for (String user : users) {
                String userSql = "INSERT INTO users (username, password, created_at, status) VALUES (?, ?, CURRENT_TIMESTAMP, 'normal') ON DUPLICATE KEY UPDATE username = username";
                jdbcTemplate.update(userSql, user, "123456");
            }

            // 确保收藏表存在
            String createFavoriteTableSql = "CREATE TABLE IF NOT EXISTS favorites (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "product_id INT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE KEY unique_user_product (user_id, product_id)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")";
            jdbcTemplate.execute(createFavoriteTableSql);

            // 清空现有商品数据
            jdbcTemplate.update("DELETE FROM products");

            // 读取CSV文件并插入数据
            String csvFile = "商品数据.csv";
            java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(csvFile));
            String line;
            br.readLine(); // 跳过表头
            int count = 0;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 8) {
                    String owner = data[0];
                    String category = data[1];
                    String title = data[2];
                    double price = Double.parseDouble(data[3]);
                    String imageUrl = data[4];
                    String shippingMethod = data[5];
                    String afterSales = data[6];
                    String condition = data[7];

                    // 获取用户ID
                    String userIdSql = "SELECT id FROM users WHERE username = ?";
                    Integer userId = jdbcTemplate.queryForObject(userIdSql, new Object[]{owner}, Integer.class);

                    // 插入商品数据
                    String productSql = "INSERT INTO products (user_id, category, title, price, image_url, shipping_method, after_sales, product_condition, description, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
                    jdbcTemplate.update(productSql, userId, category, title, price, imageUrl, shippingMethod, afterSales, condition, title, "for_sale");
                    count++;
                }
            }
            br.close();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "导入成功，共导入 " + count + " 条商品数据");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "导入失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/favorite/check")
    public Map<String, Object> checkFavorite(
            @RequestParam int userId,
            @RequestParam int productId) {
        try {
            // 确保收藏表存在
            String createFavoriteTableSql = "CREATE TABLE IF NOT EXISTS favorites (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "product_id INT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE KEY unique_user_product (user_id, product_id)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")";
            jdbcTemplate.execute(createFavoriteTableSql);

            String sql = "SELECT COUNT(*) FROM favorites WHERE user_id = ? AND product_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, new Object[]{userId, productId}, Integer.class);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isFavorite", count > 0);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "检查收藏状态失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/favorite/add")
    public Map<String, Object> addFavorite(
            @RequestParam int userId,
            @RequestParam int productId) {
        try {
            // 确保收藏表存在
            String createFavoriteTableSql = "CREATE TABLE IF NOT EXISTS favorites (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "product_id INT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE KEY unique_user_product (user_id, product_id)," +
                    "FOREIGN KEY (user_id) REFERENCES users(id)," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")";
            jdbcTemplate.execute(createFavoriteTableSql);

            String sql = "INSERT IGNORE INTO favorites (user_id, product_id) VALUES (?, ?)";
            int rowsAffected = jdbcTemplate.update(sql, userId, productId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", rowsAffected > 0 ? "收藏成功" : "已经收藏过了");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "添加收藏失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/favorite/remove")
    public Map<String, Object> removeFavorite(
            @RequestParam int userId,
            @RequestParam int productId) {
        try {
            String sql = "DELETE FROM favorites WHERE user_id = ? AND product_id = ?";
            int rowsAffected = jdbcTemplate.update(sql, userId, productId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", rowsAffected > 0 ? "取消收藏成功" : "未找到收藏记录");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "取消收藏失败: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/create")
    public Map<String, Object> createProduct(@RequestBody Map<String, Object> productData) {
        try {
            Integer userId = (Integer) productData.get("userId");
            String title = (String) productData.get("title");
            String category = (String) productData.get("category");
            Double price = null;
            Object priceObj = productData.get("price");
            if (priceObj instanceof Integer) {
                price = ((Integer) priceObj).doubleValue();
            } else if (priceObj instanceof Double) {
                price = (Double) priceObj;
            }
            String imageUrl = (String) productData.get("imageUrl");
            String condition = (String) productData.get("condition");
            String shippingMethod = (String) productData.get("shippingMethod");
            String afterSales = (String) productData.get("afterSales");
            String description = (String) productData.get("description");

            // 验证必填字段
            if (userId == null || title == null || category == null || price == null || imageUrl == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请填写必填字段");
                return response;
            }

            // 插入商品数据
            String sql = "INSERT INTO products (user_id, owner_id, category, title, name, price, image_url, shipping_method, after_sales, product_condition, description, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            int rowsAffected = jdbcTemplate.update(sql, userId, userId, category, title, title, price, imageUrl, shippingMethod, afterSales, condition, description, "for_sale");

            if (rowsAffected > 0) {
                // 获取刚插入的商品ID
                String getIdSql = "SELECT LAST_INSERT_ID() as id";
                Integer productId = jdbcTemplate.queryForObject(getIdSql, Integer.class);

                // 获取商品详情（连接users表获取用户名）
                String getProductSql = "SELECT p.*, u.username FROM products p JOIN users u ON p.user_id = u.id WHERE p.id = ?";
                List<Map<String, Object>> products = jdbcTemplate.query(getProductSql, new Object[]{productId}, productRowMapper);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "商品发布成功");
                response.put("product", products.get(0));
                return response;
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "商品发布失败");
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "发布失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/favorite/user/{userId}")
    public Map<String, Object> getUserFavorites(@PathVariable int userId) {
        try {
            String sql = "SELECT f.id, f.product_id, p.* FROM favorites f JOIN products p ON f.product_id = p.id WHERE f.user_id = ?";
            List<Map<String, Object>> favorites = jdbcTemplate.query(sql, new Object[]{userId}, new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String, Object> favorite = new HashMap<>();
                    favorite.put("id", rs.getInt("id"));
                    favorite.put("productId", rs.getInt("product_id"));
                    
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", rs.getInt("id"));
                    product.put("userId", rs.getInt("user_id"));
                    product.put("category", rs.getString("category"));
                    product.put("title", rs.getString("title"));
                    product.put("price", rs.getDouble("price"));
                    product.put("imageUrl", rs.getString("image_url"));
                    product.put("shippingMethod", rs.getString("shipping_method"));
                    product.put("afterSales", rs.getString("after_sales"));
                    product.put("condition", rs.getString("product_condition"));
                    product.put("description", rs.getString("description"));
                    product.put("status", rs.getString("status"));
                    product.put("createdAt", rs.getTimestamp("created_at"));
                    
                    favorite.put("product", product);
                    return favorite;
                }
            });

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("favorites", favorites);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取收藏列表失败: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/review/create")
    public Map<String, Object> createReview(@RequestBody Map<String, Object> reviewData) {
        try {
            Integer productId = (Integer) reviewData.get("productId");
            Integer buyerId = (Integer) reviewData.get("buyerId");
            Integer sellerId = (Integer) reviewData.get("sellerId");
            Integer rating = (Integer) reviewData.get("rating");
            String comment = (String) reviewData.get("comment");

            // 验证必填字段
            if (productId == null || buyerId == null || sellerId == null || rating == null || comment == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请填写必填字段");
                return response;
            }

            // 确保评价表存在
            String createReviewTableSql = "CREATE TABLE IF NOT EXISTS reviews (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "product_id INT NOT NULL," +
                    "buyer_id INT NOT NULL," +
                    "seller_id INT NOT NULL," +
                    "rating INT NOT NULL," +
                    "comment TEXT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)," +
                    "FOREIGN KEY (buyer_id) REFERENCES users(id)," +
                    "FOREIGN KEY (seller_id) REFERENCES users(id)" +
                    ")";
            jdbcTemplate.execute(createReviewTableSql);

            // 插入评价数据
            String sql = "INSERT INTO reviews (product_id, buyer_id, seller_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
            int rowsAffected = jdbcTemplate.update(sql, productId, buyerId, sellerId, rating, comment);

            if (rowsAffected > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "评价提交成功");
                return response;
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "评价提交失败");
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "评价提交失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/review/product/{productId}")
    public Map<String, Object> getProductReviews(@PathVariable int productId) {
        try {
            // 确保评价表存在
            String createReviewTableSql = "CREATE TABLE IF NOT EXISTS reviews (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "product_id INT NOT NULL," +
                    "buyer_id INT NOT NULL," +
                    "seller_id INT NOT NULL," +
                    "rating INT NOT NULL," +
                    "comment TEXT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)," +
                    "FOREIGN KEY (buyer_id) REFERENCES users(id)," +
                    "FOREIGN KEY (seller_id) REFERENCES users(id)" +
                    ")";
            jdbcTemplate.execute(createReviewTableSql);

            String sql = "SELECT * FROM reviews WHERE product_id = ? ORDER BY created_at DESC";
            List<Map<String, Object>> reviews = jdbcTemplate.query(sql, new Object[]{productId}, new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String, Object> review = new HashMap<>();
                    review.put("id", rs.getInt("id"));
                    review.put("productId", rs.getInt("product_id"));
                    review.put("buyerId", rs.getInt("buyer_id"));
                    review.put("sellerId", rs.getInt("seller_id"));
                    review.put("rating", rs.getInt("rating"));
                    review.put("comment", rs.getString("comment"));
                    review.put("createdAt", rs.getTimestamp("created_at"));
                    return review;
                }
            });

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reviews", reviews);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取评价列表失败: " + e.getMessage());
            return response;
        }
    }

    @DeleteMapping("/review/delete/{reviewId}")
    public Map<String, Object> deleteReview(@PathVariable int reviewId) {
        try {
            String sql = "DELETE FROM reviews WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(sql, reviewId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", rowsAffected > 0 ? "评价删除成功" : "未找到评价记录");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除评价失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/review/user/{userId}")
    public Map<String, Object> getUserReviews(@PathVariable int userId) {
        try {
            // 确保评价表存在
            String createReviewTableSql = "CREATE TABLE IF NOT EXISTS reviews (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "product_id INT NOT NULL," +
                    "buyer_id INT NOT NULL," +
                    "seller_id INT NOT NULL," +
                    "rating INT NOT NULL," +
                    "comment TEXT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)," +
                    "FOREIGN KEY (buyer_id) REFERENCES users(id)," +
                    "FOREIGN KEY (seller_id) REFERENCES users(id)" +
                    ")";
            jdbcTemplate.execute(createReviewTableSql);

            String sql = "SELECT r.*, p.* FROM reviews r JOIN products p ON r.product_id = p.id WHERE r.buyer_id = ? ORDER BY r.created_at DESC";
            List<Map<String, Object>> reviews = jdbcTemplate.query(sql, new Object[]{userId}, new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String, Object> review = new HashMap<>();
                    review.put("id", rs.getInt("id"));
                    review.put("productId", rs.getInt("product_id"));
                    review.put("buyerId", rs.getInt("buyer_id"));
                    review.put("sellerId", rs.getInt("seller_id"));
                    review.put("rating", rs.getInt("rating"));
                    review.put("comment", rs.getString("comment"));
                    review.put("createdAt", rs.getTimestamp("created_at"));
                    
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", rs.getInt("id"));
                    product.put("userId", rs.getInt("user_id"));
                    product.put("category", rs.getString("category"));
                    product.put("title", rs.getString("title"));
                    product.put("price", rs.getDouble("price"));
                    product.put("imageUrl", rs.getString("image_url"));
                    product.put("shippingMethod", rs.getString("shipping_method"));
                    product.put("afterSales", rs.getString("after_sales"));
                    product.put("condition", rs.getString("product_condition"));
                    product.put("description", rs.getString("description"));
                    product.put("status", rs.getString("status"));
                    product.put("createdAt", rs.getTimestamp("created_at"));
                    
                    review.put("product", product);
                    return review;
                }
            });

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reviews", reviews);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取评价列表失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/review/all")
    public Map<String, Object> getAllReviews() {
        try {
            // 确保评价表存在
            String createReviewTableSql = "CREATE TABLE IF NOT EXISTS reviews (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "product_id INT NOT NULL," +
                    "buyer_id INT NOT NULL," +
                    "seller_id INT NOT NULL," +
                    "rating INT NOT NULL," +
                    "comment TEXT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            jdbcTemplate.execute(createReviewTableSql);

            // 确保评价表有外键约束
            try {
                jdbcTemplate.execute("ALTER TABLE reviews ADD CONSTRAINT reviews_ibfk_1 FOREIGN KEY (product_id) REFERENCES products(id)");
                jdbcTemplate.execute("ALTER TABLE reviews ADD CONSTRAINT reviews_ibfk_2 FOREIGN KEY (buyer_id) REFERENCES users(id)");
                jdbcTemplate.execute("ALTER TABLE reviews ADD CONSTRAINT reviews_ibfk_3 FOREIGN KEY (seller_id) REFERENCES users(id)");
            } catch (Exception e) {
                // 外键约束可能已经存在，忽略错误
            }

            // 使用左连接确保即使没有对应的用户或商品，也能返回评论数据
            String sql = "SELECT r.*, COALESCE(u.username, '未知用户') as buyerName, COALESCE(p.title, '未知商品') as productTitle FROM reviews r LEFT JOIN users u ON r.buyer_id = u.id LEFT JOIN products p ON r.product_id = p.id ORDER BY r.created_at DESC";
            List<Map<String, Object>> reviews = jdbcTemplate.query(sql, new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String, Object> review = new HashMap<>();
                    review.put("id", rs.getInt("id"));
                    review.put("productId", rs.getInt("product_id"));
                    review.put("buyerId", rs.getInt("buyer_id"));
                    review.put("sellerId", rs.getInt("seller_id"));
                    review.put("rating", rs.getInt("rating"));
                    review.put("comment", rs.getString("comment"));
                    review.put("createdAt", rs.getTimestamp("created_at"));
                    review.put("buyerName", rs.getString("buyerName"));
                    review.put("productTitle", rs.getString("productTitle"));
                    return review;
                }
            });

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reviews", reviews);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取评价列表失败: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/cart/add")
    public Map<String, Object> addToCart(@RequestBody Map<String, Object> cartData) {
        try {
            Integer userId = (Integer) cartData.get("userId");
            Integer productId = (Integer) cartData.get("productId");
            Integer quantity = (Integer) cartData.get("quantity");

            // 验证必填字段
            if (userId == null || productId == null || quantity == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请填写必填字段");
                return response;
            }

            // 确保购物车表存在
            String createCartTableSql = "CREATE TABLE IF NOT EXISTS cart (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "product_id INT NOT NULL," +
                    "quantity INT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE KEY unique_user_product (user_id, product_id)" +
                    ")";
            jdbcTemplate.execute(createCartTableSql);

            // 检查商品是否存在且在售
            String checkProductSql = "SELECT status FROM products WHERE id = ?";
            String productStatus = jdbcTemplate.queryForObject(checkProductSql, new Object[]{productId}, String.class);
            if (!"for_sale".equals(productStatus)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "商品已下架或已售出");
                return response;
            }

            // 检查购物车中是否已有该商品
            String checkCartSql = "SELECT id, quantity FROM cart WHERE user_id = ? AND product_id = ?";
            List<Map<String, Object>> cartItems = jdbcTemplate.query(checkCartSql, new Object[]{userId, productId}, new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", rs.getInt("id"));
                    item.put("quantity", rs.getInt("quantity"));
                    return item;
                }
            });

            if (!cartItems.isEmpty()) {
                // 更新数量
                Map<String, Object> item = cartItems.get(0);
                Integer cartId = (Integer) item.get("id");
                Integer currentQuantity = (Integer) item.get("quantity");
                Integer newQuantity = currentQuantity + quantity;
                
                String updateSql = "UPDATE cart SET quantity = ? WHERE id = ?";
                jdbcTemplate.update(updateSql, newQuantity, cartId);
            } else {
                // 添加新商品
                String insertSql = "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
                jdbcTemplate.update(insertSql, userId, productId, quantity);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "商品已添加到购物车");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "添加到购物车失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/cart/user/{userId}")
    public Map<String, Object> getUserCart(@PathVariable int userId) {
        try {
            // 确保购物车表存在
            String createCartTableSql = "CREATE TABLE IF NOT EXISTS cart (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "user_id INT NOT NULL," +
                    "product_id INT NOT NULL," +
                    "quantity INT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "UNIQUE KEY unique_user_product (user_id, product_id)" +
                    ")";
            jdbcTemplate.execute(createCartTableSql);

            String sql = "SELECT c.id, c.product_id, c.quantity, p.* FROM cart c JOIN products p ON c.product_id = p.id WHERE c.user_id = ?";
            List<Map<String, Object>> cartItems = jdbcTemplate.query(sql, new Object[]{userId}, new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String, Object> cartItem = new HashMap<>();
                    cartItem.put("id", rs.getInt("id"));
                    cartItem.put("productId", rs.getInt("product_id"));
                    cartItem.put("quantity", rs.getInt("quantity"));
                    
                    Map<String, Object> product = new HashMap<>();
                    product.put("id", rs.getInt("id"));
                    product.put("userId", rs.getInt("user_id"));
                    product.put("category", rs.getString("category"));
                    product.put("title", rs.getString("title"));
                    product.put("price", rs.getDouble("price"));
                    product.put("imageUrl", rs.getString("image_url"));
                    product.put("shippingMethod", rs.getString("shipping_method"));
                    product.put("afterSales", rs.getString("after_sales"));
                    product.put("condition", rs.getString("product_condition"));
                    product.put("description", rs.getString("description"));
                    product.put("status", rs.getString("status"));
                    product.put("createdAt", rs.getTimestamp("created_at"));
                    
                    cartItem.put("product", product);
                    return cartItem;
                }
            });

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cartItems", cartItems);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取购物车失败: " + e.getMessage());
            return response;
        }
    }

    @DeleteMapping("/cart/remove/{cartId}")
    public Map<String, Object> removeFromCart(@PathVariable int cartId) {
        try {
            String sql = "DELETE FROM cart WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(sql, cartId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", rowsAffected > 0 ? "商品已从购物车移除" : "未找到购物车商品");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "移除商品失败: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/update-status/{productId}")
    public Map<String, Object> updateProductStatus(@PathVariable int productId, @RequestBody Map<String, Object> statusData) {
        try {
            String status = (String) statusData.get("status");
            if (status == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请提供状态值");
                return response;
            }

            String sql = "UPDATE products SET status = ? WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(sql, status, productId);

            if (rowsAffected > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "商品状态更新成功");
                return response;
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "商品不存在或更新失败");
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新商品状态失败: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/cart/update/{cartId}")
    public Map<String, Object> updateCartItem(@PathVariable int cartId, @RequestBody Map<String, Object> updateData) {
        try {
            Integer quantity = (Integer) updateData.get("quantity");

            if (quantity == null || quantity <= 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请输入有效的数量");
                return response;
            }

            String sql = "UPDATE cart SET quantity = ? WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(sql, quantity, cartId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", rowsAffected > 0 ? "购物车商品数量已更新" : "未找到购物车商品");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新商品数量失败: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/cart/checkout")
    public Map<String, Object> checkoutCart(@RequestBody Map<String, Object> checkoutData) {
        try {
            Integer userId = (Integer) checkoutData.get("userId");
            List<Integer> cartIds = (List<Integer>) checkoutData.get("cartIds");

            if (userId == null || cartIds == null || cartIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请选择要购买的商品");
                return response;
            }

            // 确保交易表存在
            String createTransactionTableSql = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "buyer_id INT NOT NULL," +
                    "seller_id INT NOT NULL," +
                    "product_id INT NOT NULL," +
                    "price DOUBLE NOT NULL," +
                    "quantity INT NOT NULL DEFAULT 1," +
                    "status VARCHAR(50) NOT NULL DEFAULT 'completed'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (buyer_id) REFERENCES users(id)," +
                    "FOREIGN KEY (seller_id) REFERENCES users(id)," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")";
            jdbcTemplate.execute(createTransactionTableSql);
            
            // 检查并添加缺失的quantity列（分步操作以确保成功）
            try {
                // 1. 先添加可空列
                String addQuantityColumnSql = "ALTER TABLE transactions ADD COLUMN IF NOT EXISTS quantity INT";
                jdbcTemplate.execute(addQuantityColumnSql);
                
                // 2. 更新现有行的默认值
                String updateDefaultSql = "UPDATE transactions SET quantity = 1 WHERE quantity IS NULL";
                jdbcTemplate.update(updateDefaultSql);
                
                // 3. 修改列为非空
                String modifyColumnSql = "ALTER TABLE transactions MODIFY COLUMN quantity INT NOT NULL DEFAULT 1";
                jdbcTemplate.execute(modifyColumnSql);
            } catch (Exception e) {
                // 忽略添加列失败的错误
                System.out.println("添加quantity列失败: " + e.getMessage());
            }

            // 开始事务
            jdbcTemplate.execute("START TRANSACTION");

            try {
                for (Integer cartId : cartIds) {
                    // 获取购物车商品信息
                    String getCartItemSql = "SELECT c.product_id, c.quantity, p.status, p.user_id as seller_id, p.price FROM cart c JOIN products p ON c.product_id = p.id WHERE c.id = ? AND c.user_id = ?";
                    List<Map<String, Object>> cartItems = jdbcTemplate.query(getCartItemSql, new Object[]{cartId, userId}, new RowMapper<Map<String, Object>>() {
                        @Override
                        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                            Map<String, Object> item = new HashMap<>();
                            item.put("productId", rs.getInt("product_id"));
                            item.put("quantity", rs.getInt("quantity"));
                            item.put("status", rs.getString("status"));
                            item.put("sellerId", rs.getInt("seller_id"));
                            item.put("price", rs.getDouble("price"));
                            return item;
                        }
                    });

                    if (cartItems.isEmpty()) {
                        throw new Exception("购物车商品不存在或已被移除");
                    }

                    Map<String, Object> item = cartItems.get(0);
                    Integer productId = (Integer) item.get("productId");
                    Integer quantity = (Integer) item.get("quantity");
                    String status = (String) item.get("status");
                    Integer sellerId = (Integer) item.get("sellerId");
                    Double price = null;
                    Object priceObj = item.get("price");
                    if (priceObj instanceof Integer) {
                        price = ((Integer) priceObj).doubleValue();
                    } else if (priceObj instanceof Double) {
                        price = (Double) priceObj;
                    } else if (priceObj instanceof String) {
                        try {
                            price = Double.parseDouble((String) priceObj);
                        } catch (NumberFormatException e) {
                            throw new Exception("价格格式错误");
                        }
                    }
                    
                    if (price == null) {
                        throw new Exception("价格信息缺失");
                    }

                    if (!"for_sale".equals(status)) {
                        throw new Exception("商品已下架或已售出");
                    }

                    // 更新商品状态为已售
                    String updateProductSql = "UPDATE products SET status = 'sold' WHERE id = ?";
                    jdbcTemplate.update(updateProductSql, productId);

                    // 创建交易记录
                    String createTransactionSql = "INSERT INTO transactions (buyer_id, seller_id, product_id, price, quantity, status) VALUES (?, ?, ?, ?, ?, 'completed')";
                    jdbcTemplate.update(createTransactionSql, userId, sellerId, productId, price, quantity);

                    // 从购物车移除商品
                    String removeCartSql = "DELETE FROM cart WHERE id = ?";
                    jdbcTemplate.update(removeCartSql, cartId);
                }

                // 提交事务
                jdbcTemplate.execute("COMMIT");

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "购物车结算成功");
                return response;
            } catch (Exception e) {
                // 回滚事务
                jdbcTemplate.execute("ROLLBACK");
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "结算失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserProducts(@PathVariable int userId) {
        try {
            String sql = "SELECT p.*, u.username FROM products p JOIN users u ON p.user_id = u.id WHERE p.user_id = ? ORDER BY p.created_at DESC";
            List<Map<String, Object>> products = jdbcTemplate.query(sql, new Object[]{userId}, productRowMapper);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("products", products);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取用户商品失败: " + e.getMessage());
            return response;
        }
    }

    @DeleteMapping("/delete/{productId}")
    public Map<String, Object> deleteProduct(@PathVariable int productId) {
        try {
            String sql = "DELETE FROM products WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(sql, productId);

            if (rowsAffected > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "商品删除成功");
                return response;
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "商品不存在或删除失败");
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除商品失败: " + e.getMessage());
            return response;
        }
    }
}

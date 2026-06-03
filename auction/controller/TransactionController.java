package com.campus.auction.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/buyer/{userId}")
    public Map<String, Object> getBuyerTransactions(@PathVariable int userId) {
        try {
            // 确保交易表存在
            String createTransactionTableSql = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "buyer_id INT NOT NULL," +
                    "seller_id INT NOT NULL," +
                    "product_id INT NOT NULL," +
                    "price DOUBLE NOT NULL," +
                    "quantity INT NOT NULL," +
                    "status VARCHAR(50) NOT NULL DEFAULT 'completed'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (buyer_id) REFERENCES users(id)," +
                    "FOREIGN KEY (seller_id) REFERENCES users(id)," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")";
            jdbcTemplate.execute(createTransactionTableSql);

            String sql = "SELECT * FROM transactions WHERE buyer_id = ? ORDER BY created_at DESC";
            List<Map<String, Object>> transactions = jdbcTemplate.query(sql, new Object[]{userId}, new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String, Object> transaction = new HashMap<>();
                    transaction.put("id", rs.getInt("id"));
                    transaction.put("buyerId", rs.getInt("buyer_id"));
                    transaction.put("sellerId", rs.getInt("seller_id"));
                    transaction.put("productId", rs.getInt("product_id"));
                    transaction.put("price", rs.getDouble("price"));
                    transaction.put("quantity", rs.getInt("quantity"));
                    transaction.put("status", rs.getString("status"));
                    transaction.put("createdAt", rs.getTimestamp("created_at"));
                    return transaction;
                }
            });

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactions", transactions);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取交易记录失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/seller/{userId}")
    public Map<String, Object> getSellerTransactions(@PathVariable int userId) {
        try {
            // 确保交易表存在
            String createTransactionTableSql = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "buyer_id INT NOT NULL," +
                    "seller_id INT NOT NULL," +
                    "product_id INT NOT NULL," +
                    "price DOUBLE NOT NULL," +
                    "quantity INT NOT NULL," +
                    "status VARCHAR(50) NOT NULL DEFAULT 'completed'," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (buyer_id) REFERENCES users(id)," +
                    "FOREIGN KEY (seller_id) REFERENCES users(id)," +
                    "FOREIGN KEY (product_id) REFERENCES products(id)" +
                    ")";
            jdbcTemplate.execute(createTransactionTableSql);

            String sql = "SELECT * FROM transactions WHERE seller_id = ? ORDER BY created_at DESC";
            List<Map<String, Object>> transactions = jdbcTemplate.query(sql, new Object[]{userId}, new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String, Object> transaction = new HashMap<>();
                    transaction.put("id", rs.getInt("id"));
                    transaction.put("buyerId", rs.getInt("buyer_id"));
                    transaction.put("sellerId", rs.getInt("seller_id"));
                    transaction.put("productId", rs.getInt("product_id"));
                    transaction.put("price", rs.getDouble("price"));
                    transaction.put("quantity", rs.getInt("quantity"));
                    transaction.put("status", rs.getString("status"));
                    transaction.put("createdAt", rs.getTimestamp("created_at"));
                    return transaction;
                }
            });

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactions", transactions);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取交易记录失败: " + e.getMessage());
            return response;
        }
    }
}

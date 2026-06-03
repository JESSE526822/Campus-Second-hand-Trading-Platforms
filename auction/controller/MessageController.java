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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/create")
    public Map<String, Object> createMessage(@RequestBody Map<String, Object> messageData) {
        try {
            Integer senderId = (Integer) messageData.get("senderId");
            Integer receiverId = (Integer) messageData.get("receiverId");
            String content = (String) messageData.get("content");

            // 验证必填字段
            if (senderId == null || receiverId == null || content == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请填写必填字段");
                return response;
            }

            // 确保消息表存在
            String createMessageTableSql = "CREATE TABLE IF NOT EXISTS messages (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "sender_id INT NOT NULL," +
                    "receiver_id INT NOT NULL," +
                    "content TEXT NOT NULL," +
                    "is_read BOOLEAN DEFAULT FALSE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (sender_id) REFERENCES users(id)," +
                    "FOREIGN KEY (receiver_id) REFERENCES users(id)" +
                    ")";
            jdbcTemplate.execute(createMessageTableSql);

            // 插入消息数据
            String sql = "INSERT INTO messages (sender_id, receiver_id, content) VALUES (?, ?, ?)";
            int rowsAffected = jdbcTemplate.update(sql, senderId, receiverId, content);

            if (rowsAffected > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "消息发送成功");
                return response;
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "消息发送失败");
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "发送消息失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserMessages(@PathVariable int userId) {
        try {
            // 确保消息表存在
            String createMessageTableSql = "CREATE TABLE IF NOT EXISTS messages (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "sender_id INT NOT NULL," +
                    "receiver_id INT NOT NULL," +
                    "content TEXT NOT NULL," +
                    "is_read BOOLEAN DEFAULT FALSE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (sender_id) REFERENCES users(id)," +
                    "FOREIGN KEY (receiver_id) REFERENCES users(id)" +
                    ")";
            jdbcTemplate.execute(createMessageTableSql);

            String sql = "SELECT m.*, u.username as senderName FROM messages m JOIN users u ON m.sender_id = u.id WHERE m.receiver_id = ? ORDER BY m.created_at DESC";
            List<Map<String, Object>> messages = jdbcTemplate.query(sql, new Object[]{userId}, new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String, Object> message = new HashMap<>();
                    message.put("id", rs.getInt("id"));
                    message.put("senderId", rs.getInt("sender_id"));
                    message.put("senderName", rs.getString("senderName"));
                    message.put("receiverId", rs.getInt("receiver_id"));
                    message.put("content", rs.getString("content"));
                    message.put("isRead", rs.getBoolean("is_read"));
                    message.put("createdAt", rs.getTimestamp("created_at"));
                    return message;
                }
            });

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messages", messages);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取消息失败: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/mark-read/{messageId}")
    public Map<String, Object> markMessageAsRead(@PathVariable int messageId) {
        try {
            String sql = "UPDATE messages SET is_read = TRUE WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(sql, messageId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", rowsAffected > 0 ? "消息已标记为已读" : "未找到消息");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "标记消息失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/conversation/{userId1}/{userId2}")
    public Map<String, Object> getConversation(@PathVariable int userId1, @PathVariable int userId2) {
        try {
            // 确保消息表存在
            String createMessageTableSql = "CREATE TABLE IF NOT EXISTS messages (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "sender_id INT NOT NULL," +
                    "receiver_id INT NOT NULL," +
                    "content TEXT NOT NULL," +
                    "is_read BOOLEAN DEFAULT FALSE," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (sender_id) REFERENCES users(id)," +
                    "FOREIGN KEY (receiver_id) REFERENCES users(id)" +
                    ")";
            jdbcTemplate.execute(createMessageTableSql);

            String sql = "SELECT * FROM messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY created_at ASC";
            List<Map<String, Object>> messages = jdbcTemplate.query(sql, new Object[]{userId1, userId2, userId2, userId1}, new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Map<String, Object> message = new HashMap<>();
                    message.put("id", rs.getInt("id"));
                    message.put("senderId", rs.getInt("sender_id"));
                    message.put("receiverId", rs.getInt("receiver_id"));
                    message.put("content", rs.getString("content"));
                    message.put("isRead", rs.getBoolean("is_read"));
                    message.put("createdAt", rs.getTimestamp("created_at"));
                    return message;
                }
            });

            // 标记接收的消息为已读
            String markReadSql = "UPDATE messages SET is_read = TRUE WHERE sender_id = ? AND receiver_id = ? AND is_read = FALSE";
            jdbcTemplate.update(markReadSql, userId2, userId1);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messages", messages);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取对话失败: " + e.getMessage());
            return response;
        }
    }
}

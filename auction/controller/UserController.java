package com.campus.auction.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");

            if (username == null || password == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户名和密码不能为空");
                return response;
            }

            // 查询用户
            String sql = "SELECT id, username, role, status FROM users WHERE username = ? AND password = ?";
            Map<String, Object> user = jdbcTemplate.queryForMap(sql, username, password);

            // 更新用户最后登录时间
            String updateLoginTimeSql = "UPDATE users SET last_login_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(updateLoginTimeSql, user.get("id"));

            // 记录登录日志
            String insertLogSql = "INSERT INTO logs (action, user, ip, details) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(insertLogSql, "用户登录", username, "127.0.0.1", "用户 " + username + " 登录系统");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "用户名或密码错误");
            return response;
        }
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> registerData) {
        try {
            String username = registerData.get("username");
            String password = registerData.get("password");

            if (username == null || password == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户名和密码不能为空");
                return response;
            }

            // 检查用户是否已存在
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, new Object[]{username}, Integer.class);
            if (count > 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户名已存在");
                return response;
            }

            // 注册新用户
            String insertSql = "INSERT INTO users (username, password, role, created_at, status) VALUES (?, ?, 'user', CURRENT_TIMESTAMP, 'normal')";
            jdbcTemplate.update(insertSql, username, password);

            // 查询新用户信息
            String selectSql = "SELECT id, username, role, status FROM users WHERE username = ?";
            Map<String, Object> user = jdbcTemplate.queryForMap(selectSql, username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "注册失败: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestBody Map<String, String> logoutData) {
        try {
            String username = logoutData.get("username");
            
            // 记录登出日志
            if (username != null) {
                String insertLogSql = "INSERT INTO logs (action, user, ip, details) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(insertLogSql, "用户登出", username, "127.0.0.1", "用户 " + username + " 退出系统");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "登出成功");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "登出失败: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/info/{userId}")
    public Map<String, Object> getUserInfo(@PathVariable int userId) {
        try {
            String sql = "SELECT id, username, role, status FROM users WHERE id = ?";
            Map<String, Object> user = jdbcTemplate.queryForMap(sql, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", user);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取用户信息失败");
            return response;
        }
    }
}

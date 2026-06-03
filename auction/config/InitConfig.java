package com.campus.auction.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@Component
public class InitConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            // 1. 创建基础表（无外键依赖）
            createUsersTable();
            
            // 2. 确保users表结构完整
            ensureUsersTableHasRoleColumn();
            ensureUsersTableHasLastLoginColumn();
            
            // 3. 初始化用户数据
            initUsers();
            
            // 4. 创建商品表（依赖users表）
            createProductsTable();
            
            // 5. 初始化商品数据
            initProducts();
            
            // 6. 创建其他表（依赖users和products表）
            initFavoritesTable();
            initTransactionsTable();
            initCartTable();
            initMessagesTable();
            initLogsTable();
            
            System.out.println("初始化完成");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("初始化失败: " + e.getMessage());
        }
    }
    
    private void createUsersTable() {
        String createUserTableSql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) NOT NULL UNIQUE," +
                "password VARCHAR(50) NOT NULL," +
                "email VARCHAR(100)," +
                "phone VARCHAR(20)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "status VARCHAR(20) DEFAULT 'normal'" +
                ")";
        jdbcTemplate.execute(createUserTableSql);
        
        System.out.println("用户表创建完成");
    }
    
    private void createProductsTable() {
        // 使用CREATE TABLE IF NOT EXISTS确保表存在且结构正确
        String createProductTableSql = "CREATE TABLE IF NOT EXISTS products (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "description TEXT," +
                "price DOUBLE NOT NULL," +
                "category VARCHAR(50) NOT NULL," +
                "status VARCHAR(20) DEFAULT 'for_sale'," +
                "image_url VARCHAR(255)," +
                "owner_id INT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "FOREIGN KEY (owner_id) REFERENCES users(id)" +
                ")";
        jdbcTemplate.execute(createProductTableSql);
        
        System.out.println("商品表创建完成");
    }
    
    private void ensureProductsTableStructure() {
        try {
            // 检查并添加缺失的列
            String[] columns = {
                "title VARCHAR(100) NOT NULL",
                "name VARCHAR(100) NOT NULL",
                "description TEXT",
                "price DOUBLE NOT NULL",
                "category VARCHAR(50) NOT NULL",
                "status VARCHAR(20) DEFAULT 'for_sale'",
                "shipping_method VARCHAR(50) DEFAULT 'self_pickup'",
                "after_sales VARCHAR(50) DEFAULT 'no_return'",
                "product_condition VARCHAR(50) DEFAULT 'used'",
                "image_url VARCHAR(255)",
                "user_id INT NOT NULL",
                "owner_id INT NOT NULL",
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP",
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
            };
            
            for (String columnDef : columns) {
                String columnName = columnDef.split(" ")[0];
                String checkSql = "SHOW COLUMNS FROM products LIKE ?";
                List<Map<String, Object>> result = jdbcTemplate.queryForList(checkSql, columnName);
                
                if (result.isEmpty()) {
                    String addColumnSql = "ALTER TABLE products ADD COLUMN " + columnDef;
                    jdbcTemplate.execute(addColumnSql);
                    System.out.println("已为products表添加列: " + columnName);
                }
            }
            
            // 确保外键约束存在
            try {
                String addForeignKeySql = "ALTER TABLE products ADD FOREIGN KEY (user_id) REFERENCES users(id)";
                jdbcTemplate.execute(addForeignKeySql);
                System.out.println("已为products表添加外键约束(user_id)");
            } catch (Exception e) {
                // 外键约束可能已经存在，忽略错误
                System.out.println("外键约束可能已经存在(user_id): " + e.getMessage());
            }
            
            try {
                String addForeignKeySql = "ALTER TABLE products ADD FOREIGN KEY (owner_id) REFERENCES users(id)";
                jdbcTemplate.execute(addForeignKeySql);
                System.out.println("已为products表添加外键约束(owner_id)");
            } catch (Exception e) {
                // 外键约束可能已经存在，忽略错误
                System.out.println("外键约束可能已经存在(owner_id): " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("检查products表结构时出错: " + e.getMessage());
        }
    }
    
    private void initProducts() {
        try {
            // 确保products表结构正确
            ensureProductsTableStructure();
            
            // 禁用外键约束
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // 清空商品表
            String truncateSql = "TRUNCATE TABLE products";
            jdbcTemplate.execute(truncateSql);
            System.out.println("商品表已清空");
            
            // 重新启用外键约束
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            
            // 获取用户映射（用户名 -> ID）
            Map<String, Long> userMap = getUserMap();
            if (userMap.isEmpty()) {
                System.out.println("警告: 未找到用户数据，无法初始化商品");
                return;
            }
            
            System.out.println("用户映射: " + userMap);
            
            // 从CSV文件导入商品数据
            importProductsFromCsv(userMap);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("初始化商品数据时出错: " + e.getMessage());
        }
    }
    
    private Map<String, Long> getUserMap() {
        Map<String, Long> userMap = new HashMap<>();
        try {
            String sql = "SELECT id, username FROM users";
            List<Map<String, Object>> users = jdbcTemplate.queryForList(sql);
            for (Map<String, Object> user : users) {
                String username = (String) user.get("username");
                long id = ((Number) user.get("id")).longValue();
                userMap.put(username, id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取用户映射时出错: " + e.getMessage());
        }
        return userMap;
    }
    
    private void importProductsFromCsv(Map<String, Long> userMap) {
        String csvFile = "商品数据.csv";
        String line = "";
        String csvSplitBy = ",";
        int count = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // 跳过表头
            br.readLine();
            
            while ((line = br.readLine()) != null) {
                // 处理CSV行，注意处理包含逗号的字段
                List<String> fields = parseCsvLine(line);
                if (fields.size() < 8) {
                    System.out.println("跳过格式不正确的行: " + line);
                    continue;
                }
                
                String ownerUsername = fields.get(0).trim();
                String category = fields.get(1).trim();
                String title = fields.get(2).trim();
                String priceStr = fields.get(3).trim();
                String imageUrl = fields.get(4).trim();
                String shippingMethod = fields.get(5).trim();
                String afterSales = fields.get(6).trim();
                String productCondition = fields.get(7).trim();
                
                // 转换价格
                double price;
                try {
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    System.out.println("跳过价格格式不正确的行: " + line);
                    continue;
                }
                
                // 获取用户ID
                Long userId = userMap.get(ownerUsername);
                if (userId == null) {
                    System.out.println("跳过未找到用户的行 (" + ownerUsername + "): " + line);
                    continue;
                }
                
                // 插入商品数据
                String insertSql = "INSERT INTO products (title, name, description, price, category, status, shipping_method, after_sales, product_condition, image_url, user_id, owner_id) " +
                        "VALUES (?, ?, ?, ?, ?, 'for_sale', ?, ?, ?, ?, ?, ?)";
                
                try {
                    jdbcTemplate.update(insertSql, 
                        title, // title
                        title, // name (使用标题作为名称)
                        title, // description (使用标题作为描述)
                        price, 
                        category, 
                        shippingMethod, 
                        afterSales, 
                        productCondition, 
                        imageUrl,
                        userId, // user_id
                        userId  // owner_id
                    );
                    count++;
                    System.out.println("成功插入商品: " + title + " (用户: " + ownerUsername + ")");
                } catch (Exception e) {
                    System.out.println("插入商品失败: " + title + ", 错误: " + e.getMessage());
                }
            }
            
            System.out.println("商品数据导入完成，共导入 " + count + " 个商品");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("读取CSV文件时出错: " + e.getMessage());
        }
    }
    
    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString());
        return fields;
    }
    
    private void ensureUsersTableHasRoleColumn() {
        try {
            // 检查users表是否存在role字段
            String checkSql = "SHOW COLUMNS FROM users LIKE 'role'";
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(checkSql);
            
            if (columns.isEmpty()) {
                // 添加role字段
                String addColumnSql = "ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'user'";
                jdbcTemplate.update(addColumnSql);
                System.out.println("已为users表添加role字段");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ensureUsersTableHasLastLoginColumn() {
        try {
            // 检查users表是否存在last_login_at字段
            String checkSql = "SHOW COLUMNS FROM users LIKE 'last_login_at'";
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(checkSql);
            
            if (columns.isEmpty()) {
                // 添加last_login_at字段
                String addColumnSql = "ALTER TABLE users ADD COLUMN last_login_at DATETIME";
                jdbcTemplate.update(addColumnSql);
                System.out.println("已为users表添加last_login_at字段");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUsers() {
        // 根据产品表的物主列创建用户表
        // 从CSV文件中提取的用户名
        String[] users = {"nick", "peter", "bill", "robin"};
        
        for (String user : users) {
            String userSql = "INSERT INTO users (username, password, role, created_at, status) VALUES (?, ?, 'user', CURRENT_TIMESTAMP, 'normal') ON DUPLICATE KEY UPDATE username = username";
            jdbcTemplate.update(userSql, user, "123456");
            System.out.println("初始化用户: " + user);
        }
        
        // 添加管理员用户
        String adminSql = "INSERT INTO users (username, password, role, created_at, status) VALUES (?, ?, 'admin', CURRENT_TIMESTAMP, 'normal') ON DUPLICATE KEY UPDATE username = username";
        jdbcTemplate.update(adminSql, "admin", "123456");
        System.out.println("初始化管理员用户: admin");
        
        System.out.println("用户初始化完成，共初始化 " + (users.length + 1) + " 个用户（包含1个管理员）");
    }

    private void initFavoritesTable() {
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
        
        System.out.println("收藏表初始化完成");
    }
    
    private void initTransactionsTable() {
        // 先创建基本表结构
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
        
        // 检查并添加quantity字段（如果不存在）
        try {
            String checkSql = "SHOW COLUMNS FROM transactions LIKE 'quantity'";
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(checkSql);
            
            if (columns.isEmpty()) {
                // 添加quantity列
                String addQuantityColumnSql = "ALTER TABLE transactions ADD COLUMN quantity INT NOT NULL DEFAULT 1";
                jdbcTemplate.execute(addQuantityColumnSql);
                System.out.println("已为transactions表添加quantity列");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("添加quantity列失败: " + e.getMessage());
        }
        
        System.out.println("交易表初始化完成");
    }
    
    private void initCartTable() {
        String createCartTableSql = "CREATE TABLE IF NOT EXISTS cart (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL," +
                "product_id INT NOT NULL," +
                "quantity INT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE KEY unique_user_product (user_id, product_id)," +
                "FOREIGN KEY (user_id) REFERENCES users(id)," +
                "FOREIGN KEY (product_id) REFERENCES products(id)" +
                ")";
        jdbcTemplate.execute(createCartTableSql);
        
        System.out.println("购物车表初始化完成");
    }

    private void initMessagesTable() {
        String createMessageTableSql = "CREATE TABLE IF NOT EXISTS messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "sender_id INT NOT NULL," +
                "receiver_id INT NOT NULL," +
                "content TEXT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '消息是否已读：false未读，true已读'," + // 新增的核心字段
                "FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE," + // 加了ON DELETE CASCADE，删除用户时同步删消息，更健壮
                "FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE" + // 同上
                ")";
        jdbcTemplate.execute(createMessageTableSql);

        System.out.println("消息表初始化完成（已包含is_read已读标识字段）");
    }
    
    private void initLogsTable() {
        String createLogTableSql = "CREATE TABLE IF NOT EXISTS logs (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "action VARCHAR(50) NOT NULL," +
                "user VARCHAR(50) NOT NULL," +
                "ip VARCHAR(50)," +
                "details TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        jdbcTemplate.execute(createLogTableSql);
        
        System.out.println("日志表初始化完成");
    }
}

package com.campus.auction.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CsvDataImporter {

    public static void main(String[] args) {
        String csvFile = "商品数据.csv";
        String jdbcUrl = "jdbc:mysql://localhost:3306/campus_auction?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "lwj20040527";

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            // 确保用户表中有对应的用户记录
            String[] users = {"nick", "peter", "bill", "robin"};
            for (String user : users) {
                String userSql = "INSERT INTO users (username, password, created_at, status) VALUES (?, ?, CURRENT_TIMESTAMP, 'normal') ON DUPLICATE KEY UPDATE username = username";
                PreparedStatement userStmt = connection.prepareStatement(userSql);
                userStmt.setString(1, user);
                userStmt.setString(2, "123456");
                userStmt.executeUpdate();
            }

            // 清空现有商品数据
            String clearSql = "DELETE FROM products";
            PreparedStatement clearStmt = connection.prepareStatement(clearSql);
            clearStmt.executeUpdate();

            // 读取CSV文件并插入数据
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
                    PreparedStatement userIdStmt = connection.prepareStatement(userIdSql);
                    userIdStmt.setString(1, owner);
                    ResultSet rs = userIdStmt.executeQuery();
                    if (rs.next()) {
                        long userId = rs.getLong(1);

                        // 插入商品数据
                        String productSql = "INSERT INTO products (user_id, category, title, price, image_url, shipping_method, after_sales, product_condition, description, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
                        PreparedStatement productStmt = connection.prepareStatement(productSql);
                        productStmt.setLong(1, userId);
                        productStmt.setString(2, category);
                        productStmt.setString(3, title);
                        productStmt.setDouble(4, price);
                        productStmt.setString(5, imageUrl);
                        productStmt.setString(6, shippingMethod);
                        productStmt.setString(7, afterSales);
                        productStmt.setString(8, condition);
                        productStmt.setString(9, title); // 使用标题作为描述
                        productStmt.setString(10, "for_sale");
                        productStmt.executeUpdate();
                        count++;
                    }
                }
            }

            System.out.println("成功导入 " + count + " 条商品数据");

            // 查看导入结果
            String countSql = "SELECT COUNT(*) AS total FROM products";
            PreparedStatement countStmt = connection.prepareStatement(countSql);
            ResultSet rs = countStmt.executeQuery();
            if (rs.next()) {
                System.out.println("数据库中共有 " + rs.getInt("total") + " 条商品记录");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

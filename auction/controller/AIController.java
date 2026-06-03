package com.campus.auction.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/ai")
public class AIController {

    @Value("${wenxin.api.key}")
    private String apiKey;

    // 引入Jackson的ObjectMapper来安全解析JSON（Spring项目默认已引入）
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> request) {
        try {
            String message = (String) request.get("message");
            List<Map<String, String>> history = (List<Map<String, String>>) request.get("history");

            // 参数校验
            if (message == null || message.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "消息不能为空");
                return response;
            }

            String reply = callWenxinAPI(message, history);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reply", reply);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "AI服务暂时不可用: " + e.getMessage());
            return response;
        }
    }

    private String callWenxinAPI(String message, List<Map<String, String>> history) throws Exception {
        System.out.println("AI 助手收到消息: " + message);
        System.out.println("调用文心一言 API...");

        String apiUrl = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions";

        // ========== 修复1：安全构建请求体（支持历史消息） ==========
        Map<String, Object> requestBodyMap = new HashMap<>();
        List<Map<String, String>> messages = new ArrayList<>();

        // 处理历史消息（如果有）
        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }

        // 添加当前用户消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messages.add(userMessage);

        // 设置请求参数
        requestBodyMap.put("messages", messages);
        requestBodyMap.put("temperature", 0.7);
        requestBodyMap.put("top_p", 0.8);
        requestBodyMap.put("penalty_score", 1.0);

        // 用ObjectMapper转为JSON字符串，避免手动转义错误
        String requestBody = objectMapper.writeValueAsString(requestBodyMap);
        System.out.println("请求体: " + requestBody);

        // 建立连接
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(60000);
        conn.setDoOutput(true);

        // 发送请求体
        OutputStream os = conn.getOutputStream();
        byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
        os.flush();
        os.close();

        // 获取响应码和响应内容
        int responseCode = conn.getResponseCode();
        System.out.println("API 响应码: " + responseCode);

        BufferedReader in;
        if (responseCode >= 200 && responseCode < 300) {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }

        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        String jsonResponse = response.toString();
        System.out.println("API 响应: " + jsonResponse);

        // ========== 修复2：安全解析JSON响应，优先处理错误 ==========
        JsonNode responseNode = objectMapper.readTree(jsonResponse);

        // 第一步：检查是否有错误码（即使响应码是200，也可能有业务错误）
        if (responseNode.has("error_code")) {
            int errorCode = responseNode.get("error_code").asInt();
            String errorMsg = responseNode.get("error_msg").asText();

            // 针对常见错误给出友好提示
            String errorMessage;
            switch (errorCode) {
                case 17:
                    errorMessage = "AI接口每日调用次数已达上限，请明日再试";
                    break;
                case 18:
                    errorMessage = "AI接口调用频率过高，请稍后再试";
                    break;
                case 282000:
                    errorMessage = "请求参数错误：" + errorMsg;
                    break;
                default:
                    errorMessage = String.format("AI接口调用失败（错误码：%d）：%s", errorCode, errorMsg);
            }
            throw new RuntimeException(errorMessage);
        }

        // 第二步：检查是否有result字段
        if (!responseNode.has("result")) {
            throw new RuntimeException("AI接口响应格式异常，未找到返回结果：" + jsonResponse);
        }

        // 第三步：获取并处理结果
        String result = responseNode.get("result").asText();
        // 清理特殊字符，还原换行和引号
        result = result.replaceAll("[$*#]+", "")
                .replace("\\n", "\n")
                .replace("\\\"", "\"");
        return result;
    }
}
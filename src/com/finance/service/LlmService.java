package com.finance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.finance.model.DoubaoChatResponse; // 引入 Doubao API 响应 DTO

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LlmService {

    private String prompt;
    private String extractedAnswer;
    private String apiResponse; // 存储原始API响应

    // --- 火山引擎 Doubao API 配置 ---
    // 请替换为你的实际 API_KEY
    private static final String API_KEY = "d3d268fb-f434-43a1-ab47-e8865ab30da8"; // <-- 将你找到的API Key放在这里
    private static final String HOST = "ark.cn-beijing.volces.com"; // API 主机地址，根据文档可能不同
    private static final String PATH = "/api/v3/chat/completions"; // Doubao 1.5 Lite 的路径
    private static final String MODEL_NAME = "doubao-1-5-lite-32k-250115"; // 使用的 Doubao 模型名称

    private static final long TIMEOUT_SECONDS = 60; // 请求超时时间

    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper 实例

    /**
     * 构造方法，用于传入 prompt。
     *
     * @param prompt 传给 Doubao API 的输入文本。
     */
    public LlmService(String prompt) {
        this.prompt = prompt;
        this.extractedAnswer = null;
        this.apiResponse = null; // 初始化
    }

    /**
     * 执行对 Doubao API 的调用，并将结果存储在内部变量中。
     * 该方法处理请求的构建、发送以及响应的解析。
     *
     * @throws IOException            如果发生 IO 错误。
     * @throws InterruptedException   如果线程在等待响应时被中断。
     * @throws LlmServiceException    如果 API 返回了错误状态码或解析响应失败。
     */
    public void callLlmApi() throws IOException, InterruptedException, LlmServiceException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();

        // 1. 构建请求体 (根据 Doubao API 文档)
        ObjectNode requestBodyJson = objectMapper.createObjectNode();
        requestBodyJson.put("model", MODEL_NAME);

        ArrayNode messages = requestBodyJson.putArray("messages");
        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", this.prompt);

        String requestBody = objectMapper.writeValueAsString(requestBodyJson);

        // 2. 构建 HTTP 请求 (使用 API Key 认证)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://" + HOST + PATH))
                .header("Content-Type", "application/json")
                // 使用你的 API_KEY 放在 Authorization 头中
                .header("Authorization", "Bearer " + API_KEY) // <--- 这里是关键修改
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // 3. 发送请求并获取响应
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            this.apiResponse = response.body();
            parseLlmApiResponse(); // 解析API响应，提取回答
        } else {
            throw new LlmServiceException("Doubao API 调用失败，状态码: " + statusCode + ", 响应体: " + response.body());
        }
    }

    /**
     * 从 Doubao API 的原始 JSON 响应中解析出回答。
     * 现在直接解析到 DoubaoChatResponse，因为其结构已匹配API返回。
     *
     * @throws LlmServiceException 如果无法解析 API 响应或找不到回答字段。
     */
    private void parseLlmApiResponse() throws LlmServiceException {
        if (this.apiResponse == null || this.apiResponse.isEmpty()) {
            throw new LlmServiceException("Doubao API 响应为空，无法解析。");
        }

        try {
            // 直接将API响应的JSON字符串反序列化为 DoubaoChatResponse 对象
            DoubaoChatResponse doubaoResponse = objectMapper.readValue(this.apiResponse, DoubaoChatResponse.class);

            // 根据新的DTO结构获取回答
            // 检查 choices 列表是否存在且不为空
            if (doubaoResponse.getChoices() != null &&
                    !doubaoResponse.getChoices().isEmpty() &&
                    doubaoResponse.getChoices().get(0).getMessage() != null) { // 确保第一个 choice 的 message 不为空

                this.extractedAnswer = doubaoResponse.getChoices().get(0).getMessage().getContent();

                if (this.extractedAnswer == null || this.extractedAnswer.isEmpty()) {
                    throw new LlmServiceException("从 Doubao API 响应中提取的回答为空或不存在。");
                }
            } else {
                throw new LlmServiceException("Doubao API 响应结构不符合预期，无法找到回答。响应体: " + this.apiResponse);
            }

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new LlmServiceException("解析 Doubao API 响应失败: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new LlmServiceException("解析 Doubao API 响应时发生未知错误: " + e.getMessage(), e);
        }
    }

    /**
     * 获取 Doubao API 返回的回答。在调用 callLlmApi() 之后才能获取。
     *
     * @return Doubao API 的回答，如果尚未调用 API 或解析失败，则返回 null。
     */
    public String getAnswer() {
        return extractedAnswer;
    }

    /**
     * 自定义异常类，用于表示 Doubao API 调用或解析失败。
     */
    public static class LlmServiceException extends Exception {
        public LlmServiceException(String message) {
            super(message);
        }

        public LlmServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

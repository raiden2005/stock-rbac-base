package com.stock.rbac.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kms.domain.aggregation.knowledge.port.IllmClientPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * LLM调用适配器
 * 实现IllmClientPort，HTTP调用第三方LLM API
 */
@Component
public class LlmClientAdapter implements IllmClientPort {

    private static final Logger log = LoggerFactory.getLogger(LlmClientAdapter.class);

    @Value("${llm.api.url:http://127.0.0.1:8000/v1/chat/completions}")
    private String apiUrl;

    @Value("${llm.api.key:}")
    private String apiKey;

    @Value("${llm.api.model:gpt-3.5-turbo}")
    private String model;

    @Value("${llm.api.timeout:30000}")
    private int timeout;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String chat(String prompt) {
        return chat("你是一个专业的股票分析助手，请基于提供的私有知识库内容回答用户问题。如果知识库中没有相关信息，请如实告知。", prompt);
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.setBearerAuth(apiKey);
            }

            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", Arrays.asList(systemMsg, userMsg));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode choices = root.get("choices");
                if (choices != null && choices.isArray() && !choices.isEmpty()) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null) {
                        String content = message.get("content").asText();
                        log.info("LLM调用成功，返回长度: {}", content.length());
                        return content;
                    }
                }
            }

            log.warn("LLM返回异常状态: {}", response.getStatusCode());
            return generateFallbackReply(userMessage);
        } catch (Exception e) {
            log.error("LLM调用失败，使用降级回复: {}", e.getMessage());
            return generateFallbackReply(userMessage);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.setBearerAuth(apiKey);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl.replace("/chat/completions", "/models"), HttpMethod.GET, entity, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.warn("LLM服务不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 降级回复(当LLM不可用时)
     */
    private String generateFallbackReply(String userMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append("[系统提示] 当前AI服务暂时不可用，已为您展示知识库检索结果。\n\n");
        sb.append("您的问题是: ").append(userMessage != null && userMessage.length() > 100
                ? userMessage.substring(0, 100) + "..." : userMessage).append("\n\n");
        sb.append("请稍后重试，或联系管理员检查AI服务配置。");
        return sb.toString();
    }
}

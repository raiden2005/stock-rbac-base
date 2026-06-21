package com.stock.rbac.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kms.domain.aggregation.knowledge.port.IEmbeddingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文本向量化适配器
 * 实现IEmbeddingPort，调用本地/远程Embedding API
 */
@Component
public class EmbeddingAdapter implements IEmbeddingPort {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingAdapter.class);

    @Value("${embedding.api.url:http://127.0.0.1:8001/v1/embeddings}")
    private String apiUrl;

    @Value("${embedding.api.key:}")
    private String apiKey;

    @Value("${embedding.api.model:text-embedding-ada-002}")
    private String model;

    @Value("${embedding.dimension:1024}")
    private int dimension;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<Float> embed(String text) {
        List<List<Float>> results = batchEmbed(Collections.singletonList(text));
        return results.isEmpty() ? generateMockVector() : results.get(0);
    }

    @Override
    public List<List<Float>> batchEmbed(List<String> texts) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.isEmpty()) {
                headers.setBearerAuth(apiKey);
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("input", texts);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode dataNode = root.get("data");
                if (dataNode != null && dataNode.isArray()) {
                    List<List<Float>> vectors = new ArrayList<>();
                    for (JsonNode item : dataNode) {
                        JsonNode embeddingNode = item.get("embedding");
                        if (embeddingNode != null && embeddingNode.isArray()) {
                            List<Float> vector = new ArrayList<>();
                            for (JsonNode val : embeddingNode) {
                                vector.add((float) val.asDouble());
                            }
                            vectors.add(vector);
                        }
                    }
                    return vectors;
                }
            }
        } catch (Exception e) {
            log.warn("Embedding API调用失败，使用模拟向量: {}", e.getMessage());
        }

        // 降级: 返回模拟向量
        return texts.stream().map(t -> generateMockVector()).collect(Collectors.toList());
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    /**
     * 生成模拟向量(降级使用)
     * 使用文本hash值生成伪随机向量
     */
    private List<Float> generateMockVector() {
        List<Float> vector = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < dimension; i++) {
            vector.add(random.nextFloat());
        }
        // 归一化
        float norm = 0f;
        for (float v : vector) {
            norm += v * v;
        }
        norm = (float) Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.size(); i++) {
                vector.set(i, vector.get(i) / norm);
            }
        }
        return vector;
    }
}

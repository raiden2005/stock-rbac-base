package com.stock.rbac.service;

import com.kms.domain.aggregation.knowledge.entity.VectorSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG增强Prompt组装服务
 * 三段式结构: 系统指令 + 私有知识 + 用户问题
 */
@Service
public class PromptBuildService {

    private static final Logger log = LoggerFactory.getLogger(PromptBuildService.class);

    /** 默认系统指令 */
    private static final String DEFAULT_SYSTEM_PROMPT =
            "你是一个专业的股票投资分析助手。请严格基于以下【私有知识库】中的内容回答用户问题。\n" +
            "回答要求:\n" +
            "1. 仅使用知识库中提供的信息，不要编造或推测\n" +
            "2. 如果知识库中没有相关信息，请明确告知用户\n" +
            "3. 回答要条理清晰、重点突出\n" +
            "4. 引用知识来源时标注知识标题\n";

    /**
     * 组装RAG增强Prompt
     * 三段式: 系统指令 + 私有知识 + 用户问题
     *
     * @param searchResults 检索到的知识片段
     * @param userQuestion  用户问题
     * @return 完整的RAG Prompt
     */
    public String buildRagPrompt(List<VectorSearchResult> searchResults, String userQuestion) {
        return buildRagPrompt(DEFAULT_SYSTEM_PROMPT, searchResults, userQuestion);
    }

    /**
     * 组装RAG增强Prompt(自定义系统指令)
     *
     * @param systemPrompt 系统指令
     * @param searchResults 检索到的知识片段
     * @param userQuestion  用户问题
     * @return 完整的RAG Prompt
     */
    public String buildRagPrompt(String systemPrompt, List<VectorSearchResult> searchResults, String userQuestion) {
        StringBuilder prompt = new StringBuilder();

        // 第一段: 系统指令
        prompt.append("【系统指令】\n");
        prompt.append(systemPrompt).append("\n\n");

        // 第二段: 私有知识
        prompt.append("【私有知识库】\n");
        if (searchResults == null || searchResults.isEmpty()) {
            prompt.append("（暂无相关知识）\n");
        } else {
            for (int i = 0; i < searchResults.size(); i++) {
                VectorSearchResult result = searchResults.get(i);
                prompt.append("知识片段").append(i + 1).append(":\n");
                prompt.append("来源: ").append(result.getTitle()).append("\n");
                prompt.append("分类: ").append(result.getCategory() != null ? result.getCategory() : "未分类").append("\n");
                prompt.append("内容: ").append(result.getSegmentContent()).append("\n");
                prompt.append("相似度: ").append(String.format("%.2f", result.getScore())).append("\n");
                prompt.append("---\n");
            }
        }
        prompt.append("\n");

        // 第三段: 用户问题
        prompt.append("【用户问题】\n");
        prompt.append(userQuestion).append("\n\n");
        prompt.append("请基于上述知识库内容，回答用户的问题:");

        String fullPrompt = prompt.toString();
        log.info("RAG Prompt组装完成, 长度: {}, 知识片段数: {}",
                fullPrompt.length(), searchResults != null ? searchResults.size() : 0);

        return fullPrompt;
    }

    /**
     * 生成知识溯源JSON
     *
     * @param searchResults 检索结果列表
     * @return JSON格式溯源信息
     */
    public String buildSourceJson(List<VectorSearchResult> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return "[]";
        }

        return searchResults.stream()
                .map(r -> String.format(
                        "{\"knowledgeId\":\"%s\",\"title\":\"%s\",\"category\":\"%s\",\"score\":%.2f,\"weight\":%.2f}",
                        r.getKnowledgeId(),
                        escapeJson(r.getTitle()),
                        escapeJson(r.getCategory()),
                        r.getScore(),
                        r.getWeight()
                ))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}

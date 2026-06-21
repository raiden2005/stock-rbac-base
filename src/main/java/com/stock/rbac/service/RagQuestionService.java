package com.stock.rbac.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kms.domain.aggregation.knowledge.entity.VectorSearchResult;
import com.kms.domain.aggregation.knowledge.port.IllmClientPort;
import com.stock.rbac.entity.StockKnowledge;
import com.stock.rbac.entity.StockKnowledgeSlice;
import com.stock.rbac.mapper.StockKnowledgeMapper;
import com.stock.rbac.mapper.StockKnowledgeSliceMapper;
import com.stock.rbac.vo.RagAnswerVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * RAG问答增强服务
 * 整合向量检索 + Prompt组装 + LLM调用 + 降级策略
 */
@Service
public class RagQuestionService {

    private static final Logger log = LoggerFactory.getLogger(RagQuestionService.class);

    @Autowired
    private VectorSearchService vectorSearchService;

    @Autowired
    private PromptBuildService promptBuildService;

    @Autowired
    private IllmClientPort llmClientPort;

    @Autowired
    private StockKnowledgeMapper knowledgeMapper;

    @Autowired
    private StockKnowledgeSliceMapper sliceMapper;

    /**
     * RAG增强问答
     * 完整流程: 向量检索 -> Prompt组装 -> LLM调用
     * 降级策略: 向量检索异常自动降级旧版固定Prompt
     *
     * @param userQuestion 用户问题
     * @return RAG回答VO
     */
    public RagAnswerVO ragAnswer(String userQuestion) {
        RagAnswerVO answer = new RagAnswerVO();
        List<VectorSearchResult> searchResults = null;
        String fullRagPrompt = null;

        try {
            // 1. 向量检索
            searchResults = vectorSearchService.search(userQuestion);

            // 2. 组装RAG Prompt
            fullRagPrompt = promptBuildService.buildRagPrompt(searchResults, userQuestion);

            // 3. 调用LLM
            String llmReply = llmClientPort.chat(fullRagPrompt);

            answer.setAnswer(llmReply);
            answer.setRagEnabled(true);
            answer.setFullRagPrompt(fullRagPrompt);

            // 4. 构建知识溯源
            answer.setSources(buildSources(searchResults));

            // 5. 更新命中次数
            incrementHitCount(searchResults);

            log.info("RAG问答完成: 问题长度={}, 检索结果数={}",
                    userQuestion.length(), searchResults != null ? searchResults.size() : 0);

        } catch (Exception e) {
            log.error("RAG问答异常，降级处理: {}", e.getMessage(), e);
            // 降级: 使用旧版固定Prompt
            answer.setAnswer(generateFallbackAnswer(userQuestion));
            answer.setRagEnabled(false);
            answer.setSources(new ArrayList<>());
        }

        return answer;
    }

    /**
     * 仅检索知识(不调用LLM)
     * 用于预览检索结果
     */
    public List<VectorSearchResult> searchOnly(String queryText) {
        return vectorSearchService.search(queryText);
    }

    /**
     * 构建知识溯源列表
     */
    private List<RagAnswerVO.KnowledgeSourceVO> buildSources(List<VectorSearchResult> searchResults) {
        List<RagAnswerVO.KnowledgeSourceVO> sources = new ArrayList<>();
        if (searchResults == null || searchResults.isEmpty()) {
            return sources;
        }

        for (VectorSearchResult r : searchResults) {
            RagAnswerVO.KnowledgeSourceVO source = new RagAnswerVO.KnowledgeSourceVO();
            source.setKnowledgeId(r.getKnowledgeId());
            source.setTitle(r.getTitle());
            source.setCategory(r.getCategory());
            source.setSegmentContent(r.getSegmentContent());
            source.setScore(r.getScore() != null ? r.getScore().doubleValue() : 0);
            source.setWeight(r.getWeight() != null ? r.getWeight().doubleValue() : 1.0);
            sources.add(source);
        }
        return sources;
    }

    /**
     * 更新命中次数
     */
    private void incrementHitCount(List<VectorSearchResult> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return;
        }

        for (VectorSearchResult r : searchResults) {
            try {
                // 更新知识命中次数
                StockKnowledge knowledge = knowledgeMapper.selectById(r.getKnowledgeId());
                if (knowledge != null) {
                    knowledge.setHitCount((knowledge.getHitCount() != null ? knowledge.getHitCount() : 0) + 1);
                    knowledgeMapper.updateById(knowledge);
                }
            } catch (Exception e) {
                log.warn("更新命中次数失败: knowledgeId={}, error={}", r.getKnowledgeId(), e.getMessage());
            }
        }
    }

    /**
     * 降级回答(旧版固定Prompt)
     */
    private String generateFallbackAnswer(String userQuestion) {
        StringBuilder sb = new StringBuilder();
        sb.append("[系统提示] 当前知识库增强服务暂时不可用，已降级为基础回复模式。\n\n");
        sb.append("您的问题是: ").append(userQuestion).append("\n\n");
        sb.append("建议稍后重试，或联系管理员检查向量检索和AI服务配置。");
        return sb.toString();
    }
}

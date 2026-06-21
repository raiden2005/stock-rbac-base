package com.stock.rbac.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kms.domain.aggregation.knowledge.entity.VectorSearchResult;
import com.kms.domain.aggregation.knowledge.port.IVectorSearchPort;
import com.stock.rbac.config.MilvusConfig;
import com.stock.rbac.entity.StockKnowledge;
import com.stock.rbac.entity.StockKnowledgeSlice;
import com.stock.rbac.mapper.StockKnowledgeMapper;
import com.stock.rbac.mapper.StockKnowledgeSliceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 向量检索适配器
 * 实现IVectorSearchPort，负责与Milvus向量数据库交互
 * 本地开发模式下使用模拟检索
 */
@Component
public class VectorSearchAdapter implements IVectorSearchPort {

    private static final Logger log = LoggerFactory.getLogger(VectorSearchAdapter.class);

    @Autowired
    private MilvusConfig milvusConfig;

    @Autowired
    private StockKnowledgeMapper knowledgeMapper;

    @Autowired
    private StockKnowledgeSliceMapper sliceMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<VectorSearchResult> search(String queryText, int topN) {
        return search(queryText, topN, milvusConfig.getSimilarityThreshold());
    }

    @Override
    public List<VectorSearchResult> search(String queryText, int topN, double threshold) {
        if (!milvusConfig.isEnabled()) {
            return mockSearch(queryText, topN, threshold);
        }

        try {
            // TODO: 实际Milvus SDK调用
            // 1. 将queryText通过EmbeddingPort转为向量
            // 2. 调用milvusClient.search()进行相似度检索
            // 3. 根据返回的milvusVectorId查询sliceMapper获取segmentContent
            // 4. 根据knowledgeId查询knowledgeMapper获取title/category/weight
            // 5. 组装VectorSearchResult列表
            log.info("Milvus向量检索: queryText={}, topN={}, threshold={}", queryText, topN, threshold);
            return mockSearch(queryText, topN, threshold);
        } catch (Exception e) {
            log.error("Milvus向量检索异常，降级为模拟检索: {}", e.getMessage(), e);
            return mockSearch(queryText, topN, threshold);
        }
    }

    @Override
    public boolean insert(String vectorId, List<Float> vector, Map<String, String> metadata) {
        if (!milvusConfig.isEnabled()) {
            log.info("Milvus未启用，跳过向量插入: vectorId={}", vectorId);
            return true;
        }
        try {
            // TODO: 实际Milvus SDK调用
            log.info("Milvus向量插入: vectorId={}", vectorId);
            return true;
        } catch (Exception e) {
            log.error("Milvus向量插入失败: vectorId={}, error={}", vectorId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean batchInsert(List<String> vectorIds, List<List<Float>> vectors, List<Map<String, String>> metadataList) {
        if (!milvusConfig.isEnabled()) {
            log.info("Milvus未启用，跳过批量向量插入: count={}", vectorIds.size());
            return true;
        }
        try {
            // TODO: 实际Milvus SDK调用
            log.info("Milvus批量向量插入: count={}", vectorIds.size());
            return true;
        } catch (Exception e) {
            log.error("Milvus批量向量插入失败: count={}, error={}", vectorIds.size(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String vectorId) {
        if (!milvusConfig.isEnabled()) {
            log.info("Milvus未启用，跳过向量删除: vectorId={}", vectorId);
            return true;
        }
        try {
            // TODO: 实际Milvus SDK调用
            log.info("Milvus向量删除: vectorId={}", vectorId);
            return true;
        } catch (Exception e) {
            log.error("Milvus向量删除失败: vectorId={}, error={}", vectorId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isHealthy() {
        if (!milvusConfig.isEnabled()) {
            return true;
        }
        try {
            // TODO: 实际Milvus健康检查
            return true;
        } catch (Exception e) {
            log.error("Milvus健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 模拟向量检索(本地开发/降级使用)
     * 从数据库中随机选取上架知识的切片作为模拟结果
     */
    private List<VectorSearchResult> mockSearch(String queryText, int topN, double threshold) {
        log.info("使用模拟向量检索: queryText={}", queryText);

        List<VectorSearchResult> results = new ArrayList<>();

        // 查询上架知识
        var queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StockKnowledge>();
        queryWrapper.eq(StockKnowledge::getStatus, 1)
                .orderByDesc(StockKnowledge::getHitCount)
                .last("LIMIT " + Math.min(topN, 5));
        List<StockKnowledge> knowledges = knowledgeMapper.selectList(queryWrapper);

        if (knowledges == null || knowledges.isEmpty()) {
            return results;
        }

        for (StockKnowledge k : knowledges) {
            // 查询该知识的第一个切片
            var sliceWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StockKnowledgeSlice>();
            sliceWrapper.eq(StockKnowledgeSlice::getKnowledgeId, k.getId())
                    .last("LIMIT 1");
            StockKnowledgeSlice slice = sliceMapper.selectOne(sliceWrapper);

            if (slice == null) {
                continue;
            }

            VectorSearchResult result = new VectorSearchResult();
            result.setKnowledgeId(k.getId());
            result.setTitle(k.getTitle());
            result.setCategory(k.getCategory());
            result.setSegmentContent(slice.getSegmentContent());
            // 模拟相似度分数: 基于权重生成一个0.7~0.99的随机分数
            double mockScore = 0.7 + (k.getWeight().doubleValue() / 10.0) * 0.29;
            mockScore = Math.min(mockScore, 0.99);
            result.setScore(BigDecimal.valueOf(mockScore));
            result.setWeight(k.getWeight());
            result.calculateWeightedScore();

            if (mockScore >= threshold) {
                results.add(result);
            }
        }

        // 按加权得分排序
        results.sort((a, b) -> b.getWeightedScore().compareTo(a.getWeightedScore()));

        return results.stream().limit(topN).collect(Collectors.toList());
    }
}

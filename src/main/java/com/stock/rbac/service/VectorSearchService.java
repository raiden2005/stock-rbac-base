package com.stock.rbac.service;

import com.kms.domain.aggregation.knowledge.entity.VectorSearchResult;
import com.kms.domain.aggregation.knowledge.port.IVectorSearchPort;
import com.stock.rbac.config.MilvusConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Milvus向量检索服务
 * 调用IVectorSearchPort进行相似度检索，过滤+排序
 */
@Service
public class VectorSearchService {

    private static final Logger log = LoggerFactory.getLogger(VectorSearchService.class);

    @Autowired
    private IVectorSearchPort vectorSearchPort;

    @Autowired
    private MilvusConfig milvusConfig;

    /**
     * 向量相似度检索
     * 相似度>=0.7过滤，Top3~5，权重加权排序
     *
     * @param queryText 查询文本
     * @param topN      返回条数(3~5)
     * @return 检索结果列表(已排序)
     */
    public List<VectorSearchResult> search(String queryText, int topN) {
        // 限制topN范围: 3~5
        int actualTopN = Math.max(3, Math.min(topN, 5));

        double threshold = milvusConfig.getSimilarityThreshold();
        log.info("向量检索: queryText长度={}, topN={}, threshold={}",
                queryText != null ? queryText.length() : 0, actualTopN, threshold);

        try {
            List<VectorSearchResult> results = vectorSearchPort.search(queryText, actualTopN, threshold);

            if (results == null || results.isEmpty()) {
                log.info("向量检索无结果");
                return List.of();
            }

            // 计算加权得分并排序
            for (VectorSearchResult r : results) {
                r.calculateWeightedScore();
            }

            List<VectorSearchResult> sorted = results.stream()
                    .sorted((a, b) -> b.getWeightedScore().compareTo(a.getWeightedScore()))
                    .collect(Collectors.toList());

            log.info("向量检索完成: 返回{}条结果", sorted.size());
            return sorted;

        } catch (Exception e) {
            log.error("向量检索异常: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 使用默认参数检索
     */
    public List<VectorSearchResult> search(String queryText) {
        return search(queryText, milvusConfig.getTopN());
    }
}

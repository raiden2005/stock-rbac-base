package com.kms.domain.aggregation.knowledge.domain;

import com.kms.domain.aggregation.knowledge.entity.KnowledgeEntity;
import com.kms.domain.aggregation.knowledge.entity.KnowledgeSliceEntity;
import com.kms.domain.aggregation.knowledge.entity.VectorSearchResult;
import com.kms.domain.common.port.Port;

import java.util.List;

/**
 * 知识库领域服务接口
 * 定义知识库的核心领域业务逻辑
 * 由主应用bridge包实现
 */
public interface IKnowledgeDomainService extends Port {

    /**
     * 创建知识并完成切片和向量化
     *
     * @param entity   知识实体
     * @param rawText  原始文本内容
     * @param sliceLen 切片长度
     * @param overlap  切片重叠长度
     * @return 切片后的实体列表
     */
    List<KnowledgeSliceEntity> createKnowledgeWithSlices(KnowledgeEntity entity, String rawText, int sliceLen, int overlap);

    /**
     * 智能文本切片
     * 按字数切片，保留重叠区域
     *
     * @param text     原始文本
     * @param sliceLen 切片长度
     * @param overlap  重叠长度
     * @return 切片文本列表
     */
    List<String> sliceText(String text, int sliceLen, int overlap);

    /**
     * 文本清洗
     * 去除多余空白、特殊字符等
     *
     * @param text 原始文本
     * @return 清洗后文本
     */
    String cleanText(String text);

    /**
     * 向量检索并排序
     * 调用向量检索端口，按相似度过滤，权重加权排序
     *
     * @param queryText 查询文本
     * @param topN      返回条数
     * @param threshold 相似度阈值
     * @return 检索结果列表(已排序)
     */
    List<VectorSearchResult> searchAndRank(String queryText, int topN, double threshold);

    /**
     * 组装RAG增强Prompt
     * 三段式: 系统指令 + 私有知识 + 用户问题
     *
     * @param systemPrompt 系统指令
     * @param searchResults 检索到的知识片段
     * @param userQuestion  用户问题
     * @return 完整的RAG Prompt
     */
    String buildRagPrompt(String systemPrompt, List<VectorSearchResult> searchResults, String userQuestion);

    /**
     * 更新知识状态(上架/下架)
     *
     * @param knowledgeId 知识ID
     * @param status       目标状态
     */
    void updateKnowledgeStatus(String knowledgeId, int status);

    /**
     * 删除知识及其所有切片
     *
     * @param knowledgeId 知识ID
     */
    void deleteKnowledgeAndSlices(String knowledgeId);
}

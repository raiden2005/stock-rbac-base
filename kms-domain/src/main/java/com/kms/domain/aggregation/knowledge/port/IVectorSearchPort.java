package com.kms.domain.aggregation.knowledge.port;

import com.kms.domain.aggregation.knowledge.entity.VectorSearchResult;
import com.kms.domain.common.port.Port;

import java.util.List;

/**
 * 向量检索端口接口
 * 领域层定义，基础设施层实现
 * 负责与Milvus向量数据库交互
 */
public interface IVectorSearchPort extends Port {

    /**
     * 向量相似度检索
     *
     * @param queryText 查询文本(会先进行向量化)
     * @param topN      返回条数
     * @return 检索结果列表
     */
    List<VectorSearchResult> search(String queryText, int topN);

    /**
     * 向量相似度检索(带阈值过滤)
     *
     * @param queryText 查询文本
     * @param topN      返回条数
     * @param threshold 相似度阈值(0~1)
     * @return 过滤后的检索结果列表
     */
    List<VectorSearchResult> search(String queryText, int topN, double threshold);

    /**
     * 将向量插入Milvus
     *
     * @param vectorId 向量ID
     * @param vector   向量数据(float数组)
     * @param metadata 元数据(如knowledgeId, segmentContent等)
     * @return 是否成功
     */
    boolean insert(String vectorId, List<Float> vector, java.util.Map<String, String> metadata);

    /**
     * 批量插入向量
     *
     * @param vectorIds 向量ID列表
     * @param vectors   向量数据列表
     * @param metadataList 元数据列表
     * @return 是否成功
     */
    boolean batchInsert(List<String> vectorIds, List<List<Float>> vectors, List<java.util.Map<String, String>> metadataList);

    /**
     * 删除指定向量
     *
     * @param vectorId 向量ID
     * @return 是否成功
     */
    boolean delete(String vectorId);

    /**
     * 检查Milvus连接是否正常
     *
     * @return 是否连接正常
     */
    boolean isHealthy();
}

package com.kms.domain.aggregation.knowledge.port;

import com.kms.domain.common.port.Port;

import java.util.List;

/**
 * 向量化端口接口
 * 领域层定义，基础设施层实现
 * 负责将文本转换为向量表示
 */
public interface IEmbeddingPort extends Port {

    /**
     * 单条文本向量化
     *
     * @param text 输入文本
     * @return 向量数据(float数组)
     */
    List<Float> embed(String text);

    /**
     * 批量文本向量化
     *
     * @param texts 输入文本列表
     * @return 向量数据列表
     */
    List<List<Float>> batchEmbed(List<String> texts);

    /**
     * 获取向量维度
     *
     * @return 维度数
     */
    int getDimension();
}

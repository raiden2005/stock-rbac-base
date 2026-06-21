package com.stock.rbac.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Milvus连接配置类
 */
@Component
@ConfigurationProperties(prefix = "milvus")
public class MilvusConfig {

    /** Milvus服务器地址 */
    private String host = "127.0.0.1";

    /** Milvus服务器端口 */
    private int port = 19530;

    /** 集合名称 */
    private String collectionName = "stock_private_knowledge";

    /** 向量维度 */
    private int dimension = 1024;

    /** 相似度阈值 */
    private double similarityThreshold = 0.7;

    /** 默认返回条数 */
    private int topN = 5;

    /** 连接超时(秒) */
    private int connectTimeout = 10;

    /** 是否启用(本地开发可关闭) */
    private boolean enabled = true;

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }

    public int getDimension() { return dimension; }
    public void setDimension(int dimension) { this.dimension = dimension; }

    public double getSimilarityThreshold() { return similarityThreshold; }
    public void setSimilarityThreshold(double similarityThreshold) { this.similarityThreshold = similarityThreshold; }

    public int getTopN() { return topN; }
    public void setTopN(int topN) { this.topN = topN; }

    public int getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(int connectTimeout) { this.connectTimeout = connectTimeout; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}

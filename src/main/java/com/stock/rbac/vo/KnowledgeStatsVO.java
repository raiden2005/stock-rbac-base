package com.stock.rbac.vo;

/**
 * 知识库统计看板VO
 */
public class KnowledgeStatsVO {

    /** 知识总数 */
    private long totalKnowledge;

    /** 上架知识数 */
    private long onlineKnowledge;

    /** 总切片数 */
    private long totalSlices;

    /** 总命中次数 */
    private long totalHits;

    public long getTotalKnowledge() { return totalKnowledge; }
    public void setTotalKnowledge(long totalKnowledge) { this.totalKnowledge = totalKnowledge; }

    public long getOnlineKnowledge() { return onlineKnowledge; }
    public void setOnlineKnowledge(long onlineKnowledge) { this.onlineKnowledge = onlineKnowledge; }

    public long getTotalSlices() { return totalSlices; }
    public void setTotalSlices(long totalSlices) { this.totalSlices = totalSlices; }

    public long getTotalHits() { return totalHits; }
    public void setTotalHits(long totalHits) { this.totalHits = totalHits; }
}

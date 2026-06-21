package com.stock.rbac.vo;

import java.util.List;

/**
 * RAG回答VO
 * 包含回答内容和知识溯源列表
 */
public class RagAnswerVO {

    /** 回答内容 */
    private String answer;

    /** 是否使用RAG增强 */
    private boolean ragEnabled;

    /** 知识溯源列表 */
    private List<KnowledgeSourceVO> sources;

    /** 完整RAG Prompt(调试用) */
    private String fullRagPrompt;

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public boolean isRagEnabled() { return ragEnabled; }
    public void setRagEnabled(boolean ragEnabled) { this.ragEnabled = ragEnabled; }

    public List<KnowledgeSourceVO> getSources() { return sources; }
    public void setSources(List<KnowledgeSourceVO> sources) { this.sources = sources; }

    public String getFullRagPrompt() { return fullRagPrompt; }
    public void setFullRagPrompt(String fullRagPrompt) { this.fullRagPrompt = fullRagPrompt; }

    /**
     * 知识溯源条目
     */
    public static class KnowledgeSourceVO {

        /** 知识ID */
        private String knowledgeId;

        /** 知识标题 */
        private String title;

        /** 知识分类 */
        private String category;

        /** 命中的切片内容 */
        private String segmentContent;

        /** 相似度分数 */
        private double score;

        /** 权重 */
        private double weight;

        public String getKnowledgeId() { return knowledgeId; }
        public void setKnowledgeId(String knowledgeId) { this.knowledgeId = knowledgeId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getSegmentContent() { return segmentContent; }
        public void setSegmentContent(String segmentContent) { this.segmentContent = segmentContent; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }

        public double getWeight() { return weight; }
        public void setWeight(double weight) { this.weight = weight; }
    }
}

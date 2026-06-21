package com.stock.rbac.dto;

import java.math.BigDecimal;

/**
 * 新增知识DTO
 */
public class KnowledgeAddDTO {

    /** 知识标题 */
    private String title;

    /** 知识分类 */
    private String category;

    /** 来源类型: text文本 / file文件 */
    private String sourceType;

    /** 文本内容(sourceType=text时填写) */
    private String content;

    /** 权重(0.01~10.00) */
    private BigDecimal weight;

    /** 文件URL(sourceType=file时填写，由上传接口返回) */
    private String fileUrl;

    /** 文件名(sourceType=file时填写) */
    private String fileName;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}

package com.stock.rbac.dto;

/**
 * 知识库查询条件DTO
 */
public class KnowledgeQueryDTO {

    /** 知识分类 */
    private String category;

    /** 关键词(标题模糊搜索) */
    private String keyword;

    /** 状态: 1上架 0下架 null全部 */
    private Integer status;

    /** 当前页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }

    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}

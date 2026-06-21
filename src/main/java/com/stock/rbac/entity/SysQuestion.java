package com.stock.rbac.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户提交的提问记录（用户页面：历史问题列表用）
 */
@TableName("sys_question")
public class SysQuestion {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 租户ID */
    private String tenantId;

    /** 提交用户GUID */
    private String userGuid;

    /** 提交用户账号（冗余，方便查询） */
    private String userAccount;

    /** 问题标题 */
    private String title;

    /** 问题内容（正文） */
    private String questionContent;

    /** 回复内容（当前为占位回答） */
    private String replyContent;

    /** 状态：0-等待回复 1-已回复 */
    private Integer status;

    /** 来源是否走付费额度：0-免费 1-付费 */
    private Integer payType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getUserGuid() { return userGuid; }
    public void setUserGuid(String userGuid) { this.userGuid = userGuid; }

    public String getUserAccount() { return userAccount; }
    public void setUserAccount(String userAccount) { this.userAccount = userAccount; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }

    public String getReplyContent() { return replyContent; }
    public void setReplyContent(String replyContent) { this.replyContent = replyContent; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getPayType() { return payType; }
    public void setPayType(Integer payType) { this.payType = payType; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}

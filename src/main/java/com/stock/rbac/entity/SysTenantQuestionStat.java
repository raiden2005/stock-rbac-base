package com.stock.rbac.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

@TableName("sys_tenant_question_stat")
public class SysTenantQuestionStat {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String tenantId;

    private String statMonth;

    private Integer freeUseNum;

    private Integer payUseNum;

    private Integer surplusPayQuestion;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getStatMonth() {
        return statMonth;
    }

    public void setStatMonth(String statMonth) {
        this.statMonth = statMonth;
    }

    public Integer getFreeUseNum() {
        return freeUseNum;
    }

    public void setFreeUseNum(Integer freeUseNum) {
        this.freeUseNum = freeUseNum;
    }

    public Integer getPayUseNum() {
        return payUseNum;
    }

    public void setPayUseNum(Integer payUseNum) {
        this.payUseNum = payUseNum;
    }

    public Integer getSurplusPayQuestion() {
        return surplusPayQuestion;
    }

    public void setSurplusPayQuestion(Integer surplusPayQuestion) {
        this.surplusPayQuestion = surplusPayQuestion;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}

package com.stock.rbac.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("sys_plan_config")
public class SysPlanConfig {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String planType;

    private BigDecimal yearSubPrice;

    private Integer monthlyFreeQuestionNum;

    private BigDecimal overQuestionUnitPrice;

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

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public BigDecimal getYearSubPrice() {
        return yearSubPrice;
    }

    public void setYearSubPrice(BigDecimal yearSubPrice) {
        this.yearSubPrice = yearSubPrice;
    }

    public Integer getMonthlyFreeQuestionNum() {
        return monthlyFreeQuestionNum;
    }

    public void setMonthlyFreeQuestionNum(Integer monthlyFreeQuestionNum) {
        this.monthlyFreeQuestionNum = monthlyFreeQuestionNum;
    }

    public BigDecimal getOverQuestionUnitPrice() {
        return overQuestionUnitPrice;
    }

    public void setOverQuestionUnitPrice(BigDecimal overQuestionUnitPrice) {
        this.overQuestionUnitPrice = overQuestionUnitPrice;
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

package com.kms.domain.aggregation.directoryright.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 可见性规则实体
 * 定义目录的可见性配置规则，包括可见用户、部门以及继承策略
 *
 * @author kms-domain-team
 * @version 1.0
 * @since Java 21
 */
@Getter
@Setter
public class VisibilityRule {

    /**
     * 规则ID
     */
    private String ruleId;

    /**
     * 关联的目录ID
     */
    private String directoryId;

    /**
     * 可见用户ID列表
     */
    private List<String> visibleUserIds;

    /**
     * 可见部门ID列表
     */
    private List<String> visibleDeptIds;

    /**
     * 继承父目录可见性
     */
    private boolean inheritFromParent;

    /**
     * 是否为公开规则
     */
    private boolean publicRule;

    /**
     * 规则优先级
     */
    private int priority;

    /**
     * 规则生效开始时间
     */
    private LocalDateTime effectiveStartTime;

    /**
     * 规则生效结束时间
     */
    private LocalDateTime effectiveEndTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 检查规则是否在有效期内
     *
     * @return 是否有效
     */
    public boolean isEffective() {
        LocalDateTime now = LocalDateTime.now();
        if (effectiveStartTime != null && now.isBefore(effectiveStartTime)) {
            return false;
        }
        if (effectiveEndTime != null && now.isAfter(effectiveEndTime)) {
            return false;
        }
        return true;
    }

    /**
     * 检查用户是否在可见规则范围内
     *
     * @param userId 用户ID
     * @param userDeptIds 用户所属部门ID列表
     * @return 是否可见
     */
    public boolean isVisibleTo(String userId, List<String> userDeptIds) {
        if (publicRule) {
            return true;
        }
        if (visibleUserIds != null && visibleUserIds.contains(userId)) {
            return true;
        }
        if (visibleDeptIds != null && userDeptIds != null) {
            for (String deptId : userDeptIds) {
                if (visibleDeptIds.contains(deptId)) {
                    return true;
                }
            }
        }
        return false;
    }
}

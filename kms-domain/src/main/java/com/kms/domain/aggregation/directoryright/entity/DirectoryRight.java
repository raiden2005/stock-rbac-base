package com.kms.domain.aggregation.directoryright.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 目录权限实体
 * 表示目录的权限配置信息，包含可见性规则和管理员配置
 *
 * @author kms-domain-team
 * @version 1.0
 * @since Java 21
 */
@Getter
@Setter
public class DirectoryRight {

    /**
     * 目录ID
     */
    private String directoryId;

    /**
     * 目录名称
     */
    private String directoryName;

    /**
     * 父目录ID
     */
    private String parentId;

    /**
     * 目录路径
     */
    private String path;

    /**
     * 可见性规则
     */
    private VisibilityRule visibilityRule;

    /**
     * 所有者ID列表
     */
    private List<String> ownerIds;

    /**
     * 管理员ID列表
     */
    private List<String> managerIds;

    /**
     * 可见用户ID列表
     */
    private List<String> visibleUserIds;

    /**
     * 可见部门ID列表
     */
    private List<String> visibleDeptIds;

    /**
     * 可见性类型
     */
    private RightType rightType;

    /**
     * 是否为公开目录
     */
    private boolean publicFlag;

    /**
     * 是否继承父目录权限
     */
    private boolean inheritParent;

    /**
     * 创建者ID
     */
    private String createUserId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新者ID
     */
    private String updateUserId;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    private boolean deleted;

    /**
     * 获取所有管理员和所有者ID集合
     *
     * @return 管理员和所有者的合并ID集合
     */
    public Set<String> getAllAdminIds() {
        return Set.of();
    }

    /**
     * 检查用户是否为所有者
     *
     * @param userId 用户ID
     * @return 是否为所有者
     */
    public boolean isOwner(String userId) {
        return ownerIds != null && ownerIds.contains(userId);
    }

    /**
     * 检查用户是否为管理员
     *
     * @param userId 用户ID
     * @return 是否为管理员
     */
    public boolean isManager(String userId) {
        return managerIds != null && managerIds.contains(userId);
    }

    /**
     * 检查用户是否在可见范围内
     *
     * @param userId 用户ID
     * @return 是否可见
     */
    public boolean isVisibleToUser(String userId) {
        if (publicFlag) {
            return true;
        }
        return visibleUserIds != null && visibleUserIds.contains(userId);
    }
}

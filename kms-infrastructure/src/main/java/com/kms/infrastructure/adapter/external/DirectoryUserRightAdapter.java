package com.kms.infrastructure.adapter.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 外部用户权限调用适配器
 * <p>
 * 对接用户中心，获取用户角色、人员GUID集合
 * 所有外部调用封闭在适配器中，上层业务无感知
 */
@Component
public class DirectoryUserRightAdapter {

    private static final Logger log = LoggerFactory.getLogger(DirectoryUserRightAdapter.class);

    private static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MANAGER = "MANAGER";

    // ==================== 对外接口方法 ====================

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    public List<String> getUserRoles(String userId) {
        try {
            // TODO: 调用用户中心获取用户角色
            // 示例：userCenterClient.getUserRoles(userId)
            List<String> roles = new ArrayList<>();
            // 临时返回空列表，实际实现时替换为真实调用
            return roles;
        } catch (Exception e) {
            log.error("获取用户角色失败, userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据角色获取用户GUID列表
     *
     * @param roleCode 角色编码
     * @return 用户GUID集合
     */
    public Set<String> getUserGuidsByRole(String roleCode) {
        try {
            // TODO: 调用用户中心获取角色下的用户列表
            // 示例：userCenterClient.getUserGuidsByRole(roleCode)
            Set<String> userGuids = new HashSet<>();
            // 临时返回空集合，实际实现时替换为真实调用
            return userGuids;
        } catch (Exception e) {
            log.error("根据角色获取用户列表失败, roleCode={}", roleCode, e);
            return new HashSet<>();
        }
    }

    /**
     * 根据部门获取用户GUID列表
     *
     * @param deptId 部门ID
     * @return 用户GUID集合
     */
    public Set<String> getUserGuidsByDept(String deptId) {
        try {
            // TODO: 调用用户中心获取部门下的用户列表
            // 示例：userCenterClient.getUserGuidsByDept(deptId)
            Set<String> userGuids = new HashSet<>();
            // 临时返回空集合，实际实现时替换为真实调用
            return userGuids;
        } catch (Exception e) {
            log.error("根据部门获取用户列表失败, deptId={}", deptId, e);
            return new HashSet<>();
        }
    }

    /**
     * 判断是否超级管理员
     *
     * @param userId 用户ID
     * @return 是否超级管理员
     */
    public boolean isSuperAdmin(String userId) {
        List<String> roles = getUserRoles(userId);
        return roles.contains(ROLE_SUPER_ADMIN);
    }

    /**
     * 判断是否管理员
     *
     * @param userId 用户ID
     * @return 是否管理员
     */
    public boolean isAdmin(String userId) {
        List<String> roles = getUserRoles(userId);
        return roles.contains(ROLE_ADMIN) || roles.contains(ROLE_SUPER_ADMIN);
    }

    /**
     * 判断是否经理角色
     *
     * @param userId 用户ID
     * @return 是否经理
     */
    public boolean isManager(String userId) {
        List<String> roles = getUserRoles(userId);
        return roles.contains(ROLE_MANAGER);
    }

    /**
     * 获取用户在指定目录的管理角色
     *
     * @param userId      用户ID
     * @param directoryId 目录ID
     * @return 管理角色类型：OWNER/MANAGER/VIEWER/空
     */
    public String getUserDirectoryRole(String userId, String directoryId) {
        // TODO: 实现获取用户在目录中的具体角色
        // 示例：userCenterClient.getUserDirectoryRole(userId, directoryId)
        // 临时返回空，实际实现时替换为真实调用
        return "";
    }

    /**
     * 批量获取用户在多个目录的管理角色
     *
     * @param userId       用户ID
     * @param directoryIds  目录ID列表
     * @return 目录ID -> 角色类型 映射
     */
    public java.util.Map<String, String> batchGetUserDirectoryRoles(String userId, List<String> directoryIds) {
        // TODO: 实现批量获取用户在多个目录中的角色
        // 临时返回空Map，实际实现时替换为真实调用
        return new java.util.HashMap<>();
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 检查用户是否拥有指定角色
     */
    private boolean hasRole(String userId, String roleCode) {
        List<String> roles = getUserRoles(userId);
        return roles.contains(roleCode);
    }

    /**
     * 检查用户是否拥有任一指定角色
     */
    private boolean hasAnyRole(String userId, String... roleCodes) {
        List<String> userRoles = getUserRoles(userId);
        for (String role : roleCodes) {
            if (userRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }
}

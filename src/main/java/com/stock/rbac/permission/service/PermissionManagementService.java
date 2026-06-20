package com.stock.rbac.permission.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.entity.BizVisibleRule;
import com.stock.rbac.entity.SysPermission;
import com.stock.rbac.entity.SysRole;
import com.stock.rbac.entity.SysUser;
import com.stock.rbac.vo.PermissionTreeVO;

import java.util.List;

public interface PermissionManagementService {

    void addUser(SysUser user, List<String> roleIds);

    void updateUser(SysUser user);

    void deleteUser(String userGuid);

    void updateUserStatus(String userGuid, Integer status);

    Page<SysUser> pageUsers(long current, long size, String keyword);

    void assignUserRoles(String userGuid, List<String> roleIds);

    List<String> getUserRoleIds(String userGuid);

    void addRole(SysRole role, List<String> permIds);

    void updateRole(SysRole role);

    void deleteRole(String roleId);

    void updateRoleStatus(String roleId, Integer status);

    Page<SysRole> pageRoles(long current, long size, String keyword);

    void assignRolePermissions(String roleId, List<String> permIds);

    List<String> getRolePermIds(String roleId);

    List<SysRole> listAllRoles();

    void addPermission(SysPermission perm);

    void updatePermission(SysPermission perm);

    void deletePermission(String permId);

    List<PermissionTreeVO> listPermissionTree();

    List<SysPermission> listPermissionsByType(String permType);

    void addVisibleRule(BizVisibleRule rule);

    void batchAddVisibleRules(List<BizVisibleRule> rules);

    void deleteVisibleRule(String ruleId);

    Page<BizVisibleRule> pageVisibleRules(long current, long size, String userGuid, String resourceType);

    void refreshGlobalPermissionCache();

    long getCurrentPermVersion();

    void incrPermVersion(String permType);
}

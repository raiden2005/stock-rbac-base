package com.stock.rbac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.annotation.AuditLog;
import com.stock.rbac.annotation.RequirePermission;
import com.stock.rbac.entity.SysUser;
import com.stock.rbac.permission.dto.AssignUserRoleDTO;
import com.stock.rbac.permission.service.PermissionManagementService;
import com.stock.rbac.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Autowired
    private PermissionManagementService permissionManagementService;

    @PostMapping("/add")
    @RequirePermission("system:user:add")
    @AuditLog(module = "用户管理", operation = "新增用户")
    public Result<?> add(@RequestBody SysUser user,
                         @RequestParam(required = false) List<String> roleIds) {
        permissionManagementService.addUser(user, roleIds);
        return Result.success();
    }

    @PostMapping("/update")
    @RequirePermission("system:user:edit")
    @AuditLog(module = "用户管理", operation = "编辑用户")
    public Result<?> update(@RequestBody SysUser user) {
        permissionManagementService.updateUser(user);
        return Result.success();
    }

    @PostMapping("/delete/{userGuid}")
    @RequirePermission("system:user:delete")
    @AuditLog(module = "用户管理", operation = "删除用户")
    public Result<?> delete(@PathVariable String userGuid) {
        permissionManagementService.deleteUser(userGuid);
        return Result.success();
    }

    @PostMapping("/status")
    @RequirePermission("system:user:edit")
    @AuditLog(module = "用户管理", operation = "修改用户状态")
    public Result<?> updateStatus(@RequestParam String userGuid,
                                  @RequestParam Integer status) {
        permissionManagementService.updateUserStatus(userGuid, status);
        return Result.success();
    }

    @GetMapping("/page")
    @RequirePermission("system:user")
    public Result<Page<SysUser>> page(@RequestParam long current,
                                      @RequestParam long size,
                                      @RequestParam(required = false) String keyword) {
        Page<SysUser> page = permissionManagementService.pageUsers(current, size, keyword);
        return Result.success(page);
    }

    @PostMapping("/assign-roles")
    @RequirePermission("system:user:edit")
    @AuditLog(module = "用户管理", operation = "分配角色")
    public Result<?> assignRoles(@RequestBody AssignUserRoleDTO dto) {
        permissionManagementService.assignUserRoles(dto.getUserGuid(), dto.getRoleIds());
        return Result.success();
    }

    @GetMapping("/roles/{userGuid}")
    @RequirePermission("system:user")
    public Result<List<String>> getUserRoleIds(@PathVariable String userGuid) {
        List<String> roleIds = permissionManagementService.getUserRoleIds(userGuid);
        return Result.success(roleIds);
    }
}

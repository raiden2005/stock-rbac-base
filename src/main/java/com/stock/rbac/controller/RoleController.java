package com.stock.rbac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.annotation.AuditLog;
import com.stock.rbac.annotation.RequirePermission;
import com.stock.rbac.entity.SysRole;
import com.stock.rbac.permission.dto.AssignRolePermissionDTO;
import com.stock.rbac.permission.service.PermissionManagementService;
import com.stock.rbac.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
@Slf4j
public class RoleController {

    @Autowired
    private PermissionManagementService permissionManagementService;

    @PostMapping("/add")
    @RequirePermission("system:role:add")
    @AuditLog(module = "角色管理", operation = "新增角色")
    public Result<?> add(@RequestBody SysRole role,
                         @RequestParam(required = false) List<String> permIds) {
        permissionManagementService.addRole(role, permIds);
        return Result.success();
    }

    @PostMapping("/update")
    @RequirePermission("system:role:add")
    @AuditLog(module = "角色管理", operation = "编辑角色")
    public Result<?> update(@RequestBody SysRole role) {
        permissionManagementService.updateRole(role);
        return Result.success();
    }

    @PostMapping("/delete/{roleId}")
    @RequirePermission("system:role:add")
    @AuditLog(module = "角色管理", operation = "删除角色")
    public Result<?> delete(@PathVariable String roleId) {
        permissionManagementService.deleteRole(roleId);
        return Result.success();
    }

    @PostMapping("/status")
    @RequirePermission("system:role:assign")
    @AuditLog(module = "角色管理", operation = "修改角色状态")
    public Result<?> updateStatus(@RequestParam String roleId,
                                  @RequestParam Integer status) {
        permissionManagementService.updateRoleStatus(roleId, status);
        return Result.success();
    }

    @GetMapping("/page")
    @RequirePermission("system:role")
    public Result<Page<SysRole>> page(@RequestParam long current,
                                      @RequestParam long size,
                                      @RequestParam(required = false) String keyword) {
        Page<SysRole> page = permissionManagementService.pageRoles(current, size, keyword);
        return Result.success(page);
    }

    @PostMapping("/assign-permissions")
    @RequirePermission("system:role:assign")
    @AuditLog(module = "角色管理", operation = "分配权限")
    public Result<?> assignPermissions(@RequestBody AssignRolePermissionDTO dto) {
        permissionManagementService.assignRolePermissions(dto.getRoleId(), dto.getPermIds());
        return Result.success();
    }

    @GetMapping("/permissions/{roleId}")
    @RequirePermission("system:role")
    public Result<List<String>> getRolePermIds(@PathVariable String roleId) {
        List<String> permIds = permissionManagementService.getRolePermIds(roleId);
        return Result.success(permIds);
    }

    @GetMapping("/list")
    @RequirePermission("system:role")
    public Result<List<SysRole>> list() {
        List<SysRole> roles = permissionManagementService.listAllRoles();
        return Result.success(roles);
    }
}

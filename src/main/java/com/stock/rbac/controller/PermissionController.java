package com.stock.rbac.controller;

import com.stock.rbac.annotation.AuditLog;
import com.stock.rbac.annotation.RequirePermission;
import com.stock.rbac.annotation.RequireRole;
import com.stock.rbac.entity.SysPermission;
import com.stock.rbac.permission.service.PermissionManagementService;
import com.stock.rbac.vo.PermissionTreeVO;
import com.stock.rbac.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permission")
@Slf4j
public class PermissionController {

    @Autowired
    private PermissionManagementService permissionManagementService;

    @PostMapping("/add")
    @RequirePermission("system:perm")
    @AuditLog(module = "权限管理", operation = "新增权限")
    public Result<?> add(@RequestBody SysPermission perm) {
        permissionManagementService.addPermission(perm);
        return Result.success();
    }

    @PostMapping("/update")
    @RequirePermission("system:perm")
    @AuditLog(module = "权限管理", operation = "编辑权限")
    public Result<?> update(@RequestBody SysPermission perm) {
        permissionManagementService.updatePermission(perm);
        return Result.success();
    }

    @PostMapping("/delete/{permId}")
    @RequirePermission("system:perm")
    @AuditLog(module = "权限管理", operation = "删除权限")
    public Result<?> delete(@PathVariable String permId) {
        permissionManagementService.deletePermission(permId);
        return Result.success();
    }

    @GetMapping("/tree")
    @RequirePermission("system:perm")
    public Result<List<PermissionTreeVO>> tree() {
        List<PermissionTreeVO> tree = permissionManagementService.listPermissionTree();
        return Result.success(tree);
    }

    @GetMapping("/list-by-type")
    @RequirePermission("system:perm")
    public Result<List<SysPermission>> listByType(@RequestParam String type) {
        List<SysPermission> permissions = permissionManagementService.listPermissionsByType(type);
        return Result.success(permissions);
    }

    @PostMapping("/refresh-cache")
    @RequireRole({"SUPER_ADMIN"})
    @AuditLog(module = "权限管理", operation = "刷新缓存")
    public Result<?> refreshCache() {
        permissionManagementService.refreshGlobalPermissionCache();
        return Result.success();
    }

    @GetMapping("/version")
    public Result<Long> version() {
        long version = permissionManagementService.getCurrentPermVersion();
        return Result.success(version);
    }
}

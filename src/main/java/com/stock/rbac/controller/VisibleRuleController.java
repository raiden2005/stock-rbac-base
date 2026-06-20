package com.stock.rbac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.annotation.AuditLog;
import com.stock.rbac.annotation.RequirePermission;
import com.stock.rbac.entity.BizVisibleRule;
import com.stock.rbac.permission.dto.BatchVisibleRuleDTO;
import com.stock.rbac.permission.service.PermissionManagementService;
import com.stock.rbac.util.UserContext;
import com.stock.rbac.vo.Result;
import com.stock.rbac.visibility.service.VisibilityPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/visible")
@Slf4j
public class VisibleRuleController {

    @Autowired
    private PermissionManagementService permissionManagementService;

    @Autowired
    private VisibilityPermissionService visibilityPermissionService;

    @PostMapping("/add")
    @RequirePermission("system:visible")
    @AuditLog(module = "数据可见权限", operation = "新增规则")
    public Result<?> add(@RequestBody BizVisibleRule rule) {
        permissionManagementService.addVisibleRule(rule);
        return Result.success();
    }

    @PostMapping("/batch-add")
    @RequirePermission("system:visible")
    @AuditLog(module = "数据可见权限", operation = "批量新增规则")
    public Result<?> batchAdd(@RequestBody BatchVisibleRuleDTO dto) {
        List<BizVisibleRule> rules = new ArrayList<>();
        if (dto.getResourceIds() != null) {
            for (String resourceId : dto.getResourceIds()) {
                BizVisibleRule rule = new BizVisibleRule();
                rule.setUserGuid(dto.getUserGuid());
                rule.setResourceType(dto.getResourceType());
                rule.setResourceId(resourceId);
                rule.setVisibleType(dto.getVisibleType());
                rules.add(rule);
            }
        }
        permissionManagementService.batchAddVisibleRules(rules);
        return Result.success();
    }

    @PostMapping("/delete/{ruleId}")
    @RequirePermission("system:visible")
    @AuditLog(module = "数据可见权限", operation = "删除规则")
    public Result<?> delete(@PathVariable String ruleId) {
        permissionManagementService.deleteVisibleRule(ruleId);
        return Result.success();
    }

    @GetMapping("/page")
    @RequirePermission("system:visible")
    public Result<Page<BizVisibleRule>> page(@RequestParam long current,
                                             @RequestParam long size,
                                             @RequestParam(required = false) String userGuid,
                                             @RequestParam(required = false) String resourceType) {
        Page<BizVisibleRule> page = permissionManagementService.pageVisibleRules(current, size, userGuid, resourceType);
        return Result.success(page);
    }

    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam String resourceType,
                                 @RequestParam String resourceId) {
        String userGuid = UserContext.getUserGuid();
        boolean visible = visibilityPermissionService.checkUserVisibility(userGuid, resourceType, resourceId);
        return Result.success(visible);
    }

    @GetMapping("/my-resources")
    public Result<List<String>> myResources(@RequestParam String resourceType) {
        String userGuid = UserContext.getUserGuid();
        List<String> resourceIds = visibilityPermissionService.selectVisibilityPermission(userGuid, resourceType);
        return Result.success(resourceIds);
    }
}

package com.stock.rbac.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.annotation.AuditLog;
import com.stock.rbac.entity.SysTenant;
import com.stock.rbac.mapper.SysTenantMapper;
import com.stock.rbac.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 平台侧 - 租户管理（新增/编辑/冻结/延期/列表）
 */
@RestController
@RequestMapping("/api/tenant")
public class TenantController {

    @Autowired
    private SysTenantMapper sysTenantMapper;

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) String tenantName,
                          @RequestParam(required = false) Integer tenantStatus) {
        LambdaQueryWrapper<SysTenant> query = new LambdaQueryWrapper<>();
        if (tenantName != null && !tenantName.trim().isEmpty()) {
            query.like(SysTenant::getTenantName, tenantName);
        }
        if (tenantStatus != null) {
            query.eq(SysTenant::getTenantStatus, tenantStatus);
        }
        query.orderByDesc(SysTenant::getCreateTime);
        Page<SysTenant> result = sysTenantMapper.selectPage(new Page<>(pageNum, pageSize), query);
        return Result.success(result);
    }

    @AuditLog(module = "租户管理", operation = "新增租户")
    @PostMapping("/add")
    public Result<?> add(@RequestBody SysTenant tenant) {
        if (tenant.getTenantName() == null || tenant.getTenantName().trim().isEmpty()) {
            return Result.error(400, "租户名称不能为空");
        }
        if (tenant.getTenantId() == null || tenant.getTenantId().trim().isEmpty()) {
            tenant.setTenantId("TENANT_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        }
        if (tenant.getTenantCode() == null || tenant.getTenantCode().trim().isEmpty()) {
            tenant.setTenantCode(tenant.getTenantId());
        }
        if (tenant.getValidStart() == null) {
            tenant.setValidStart(LocalDateTime.now());
        }
        if (tenant.getValidEnd() == null) {
            tenant.setValidEnd(LocalDateTime.now().plusYears(1));
        }
        if (tenant.getTenantStatus() == null) {
            tenant.setTenantStatus(1);
        }
        if (tenant.getPlanType() == null) {
            tenant.setPlanType("STANDARD");
        }
        if (tenant.getMaxAccountNum() == null) {
            tenant.setMaxAccountNum(50);
        }
        sysTenantMapper.insert(tenant);
        return Result.success(tenant);
    }

    @AuditLog(module = "租户管理", operation = "编辑租户")
    @PutMapping("/edit")
    public Result<?> edit(@RequestBody SysTenant tenant) {
        if (tenant.getTenantId() == null || tenant.getTenantId().isEmpty()) {
            return Result.error(400, "租户ID不能为空");
        }
        SysTenant existing = sysTenantMapper.selectById(tenant.getTenantId());
        if (existing == null) {
            return Result.error(404, "租户不存在");
        }
        sysTenantMapper.updateById(tenant);
        return Result.success(tenant);
    }

    @AuditLog(module = "租户管理", operation = "更新租户状态")
    @PutMapping("/status")
    public Result<?> status(@RequestParam String tenantId, @RequestParam Integer status) {
        SysTenant tenant = sysTenantMapper.selectById(tenantId);
        if (tenant == null) {
            return Result.error(404, "租户不存在");
        }
        if (status == null || (status != 0 && status != 1)) {
            return Result.error(400, "状态值非法：0=冻结 1=激活");
        }
        SysTenant update = new SysTenant();
        update.setTenantId(tenantId);
        update.setTenantStatus(status);
        sysTenantMapper.updateById(update);
        return Result.success();
    }

    @AuditLog(module = "租户管理", operation = "租户延期")
    @PutMapping("/delay")
    public Result<?> delay(@RequestParam String tenantId,
                           @RequestParam(defaultValue = "12") Integer addMonths) {
        SysTenant tenant = sysTenantMapper.selectById(tenantId);
        if (tenant == null) {
            return Result.error(404, "租户不存在");
        }
        if (addMonths == null || addMonths <= 0) {
            return Result.error(400, "延期月数必须大于0");
        }
        LocalDateTime base = (tenant.getValidEnd() != null && tenant.getValidEnd().isAfter(LocalDateTime.now()))
                ? tenant.getValidEnd()
                : LocalDateTime.now();
        LocalDateTime newEnd = base.plusMonths(addMonths);
        SysTenant update = new SysTenant();
        update.setTenantId(tenantId);
        update.setValidEnd(newEnd);
        // 若之前为冻结且尚未激活的状态，仍保持原状态；此处仅变更结束日期
        sysTenantMapper.updateById(update);
        return Result.success(newEnd);
    }

    @GetMapping("/{tenantId}")
    public Result<?> getById(@PathVariable String tenantId) {
        SysTenant tenant = sysTenantMapper.selectById(tenantId);
        if (tenant == null) {
            return Result.error(404, "租户不存在");
        }
        return Result.success(tenant);
    }
}

package com.stock.rbac.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.annotation.AuditLog;
import com.stock.rbac.entity.SysAuditLog;
import com.stock.rbac.mapper.SysAuditLogMapper;
import com.stock.rbac.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    @Autowired
    private SysAuditLogMapper auditLogMapper;

    /**
     * 分页查询审计日志
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize,
                          @RequestParam(required = false) String userAccount,
                          @RequestParam(required = false) String operModule,
                          @RequestParam(required = false) String operType) {
        LambdaQueryWrapper<SysAuditLog> query = new LambdaQueryWrapper<>();
        if (userAccount != null && !userAccount.trim().isEmpty()) {
            query.like(SysAuditLog::getUserAccount, userAccount);
        }
        if (operModule != null && !operModule.trim().isEmpty()) {
            query.eq(SysAuditLog::getOperModule, operModule);
        }
        if (operType != null && !operType.trim().isEmpty()) {
            query.eq(SysAuditLog::getOperType, operType);
        }
        query.orderByDesc(SysAuditLog::getCreateTime);

        Page<SysAuditLog> page = new Page<>(pageNum, pageSize);
        Page<SysAuditLog> result = auditLogMapper.selectPage(page, query);
        return Result.success(result);
    }

    /**
     * 根据用户GUID查询审计日志
     */
    @GetMapping("/user/{userGuid}")
    public Result<?> listByUser(@PathVariable String userGuid,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<SysAuditLog> query = new LambdaQueryWrapper<>();
        query.eq(SysAuditLog::getUserGuid, userGuid);
        query.orderByDesc(SysAuditLog::getCreateTime);

        Page<SysAuditLog> page = new Page<>(pageNum, pageSize);
        Page<SysAuditLog> result = auditLogMapper.selectPage(page, query);
        return Result.success(result);
    }

    /**
     * 根据ID查询单条审计日志
     */
    @GetMapping("/{logId}")
    public Result<?> getById(@PathVariable String logId) {
        SysAuditLog log = auditLogMapper.selectById(logId);
        return Result.success(log);
    }
}

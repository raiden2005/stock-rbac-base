package com.stock.rbac.controller;

import com.stock.rbac.annotation.AuditLog;
import com.stock.rbac.entity.SysPlanConfig;
import com.stock.rbac.saas.service.PlanConfigService;
import com.stock.rbac.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plan/config")
public class PlanConfigController {

    @Autowired
    private PlanConfigService planConfigService;

    @GetMapping("/get")
    public Result<?> get() {
        return Result.success(planConfigService.getDefaultConfig());
    }

    @AuditLog(module = "套餐配置", operation = "更新配置")
    @PutMapping("/update")
    public Result<?> update(@RequestBody SysPlanConfig config) {
        planConfigService.updateConfig(config);
        return Result.success();
    }
}

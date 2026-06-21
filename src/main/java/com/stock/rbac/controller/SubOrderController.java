package com.stock.rbac.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.annotation.AuditLog;
import com.stock.rbac.entity.SysSubOrder;
import com.stock.rbac.mapper.SysSubOrderMapper;
import com.stock.rbac.saas.service.SubOrderService;
import com.stock.rbac.util.UserContext;
import com.stock.rbac.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order/sub")
public class SubOrderController {

    private final SubOrderService subOrderService;
    private final SysSubOrderMapper subOrderMapper;

    public SubOrderController(SubOrderService subOrderService, SysSubOrderMapper subOrderMapper) {
        this.subOrderService = subOrderService;
        this.subOrderMapper = subOrderMapper;
    }

    @GetMapping("/platform/list")
    public Result<?> platformList(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String tenantId) {
        LambdaQueryWrapper<SysSubOrder> query = new LambdaQueryWrapper<>();
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            query.eq(SysSubOrder::getTenantId, tenantId);
        }
        query.orderByDesc(SysSubOrder::getCreateTime);
        Page<SysSubOrder> result = subOrderMapper.selectPage(new Page<>(pageNum, pageSize), query);
        return Result.success(result);
    }

    @AuditLog(module = "订阅订单", operation = "平台手工创建并支付")
    @PostMapping("/manual")
    public Result<?> manualCreate(@RequestBody SysSubOrder body) {
        if (body.getTenantId() == null || body.getTenantId().isEmpty()) {
            return Result.error(400, "租户ID不能为空");
        }
        int years = (body.getSubscribeYear() == null || body.getSubscribeYear() <= 0)
                ? 1 : body.getSubscribeYear();
        SysSubOrder created = subOrderService.createOrder(body.getTenantId(), years);
        subOrderService.payOrder(created.getId(), null);
        return Result.success(created);
    }

    /** 租户自助创建订阅订单 */
    @AuditLog(module = "订阅订单", operation = "租户自助创建订单")
    @PostMapping("/create")
    public Result<?> create(@RequestBody Map<String, Integer> body) {
        Integer years = body.get("subscribeYear");
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = "TENANT_DEFAULT";
        }
        SysSubOrder order = subOrderService.createOrder(tenantId, years);
        return Result.success(order);
    }

    /** 租户自助支付 */
    @AuditLog(module = "订阅订单", operation = "租户支付订单并顺延有效期")
    @PostMapping("/pay/{orderId}")
    public Result<?> pay(@PathVariable String orderId) {
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = "TENANT_DEFAULT";
        }
        SysSubOrder paid = subOrderService.payOrder(orderId, tenantId);
        return Result.success(paid);
    }

    /** 租户自助取消 */
    @AuditLog(module = "订阅订单", operation = "租户取消订单")
    @PostMapping("/cancel/{orderId}")
    public Result<?> cancel(@PathVariable String orderId) {
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = "TENANT_DEFAULT";
        }
        SysSubOrder order = subOrderService.cancelOrder(orderId, tenantId);
        return Result.success(order);
    }
}

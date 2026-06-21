package com.stock.rbac.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.entity.SysSubBill;
import com.stock.rbac.mapper.SysSubBillMapper;
import com.stock.rbac.util.UserContext;
import com.stock.rbac.vo.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bill")
public class SubBillController {

    private final SysSubBillMapper subBillMapper;

    public SubBillController(SysSubBillMapper subBillMapper) {
        this.subBillMapper = subBillMapper;
    }

    @GetMapping("/platform/list")
    public Result<?> platformList(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String tenantId) {
        LambdaQueryWrapper<SysSubBill> query = new LambdaQueryWrapper<>();
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            query.eq(SysSubBill::getTenantId, tenantId);
        }
        query.orderByDesc(SysSubBill::getCreateTime);
        Page<SysSubBill> result = subBillMapper.selectPage(new Page<>(pageNum, pageSize), query);
        return Result.success(result);
    }

    @GetMapping("/self/list")
    public Result<?> selfList(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = "TENANT_DEFAULT";
        }
        LambdaQueryWrapper<SysSubBill> query = new LambdaQueryWrapper<>();
        query.eq(SysSubBill::getTenantId, tenantId);
        query.orderByDesc(SysSubBill::getCreateTime);
        Page<SysSubBill> result = subBillMapper.selectPage(new Page<>(pageNum, pageSize), query);
        return Result.success(result);
    }
}

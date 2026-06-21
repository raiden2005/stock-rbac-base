package com.stock.rbac.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.vo.Result;
import com.stock.rbac.entity.SysTenant;
import com.stock.rbac.entity.SysTenantQuestionStat;
import com.stock.rbac.mapper.SysTenantMapper;
import com.stock.rbac.saas.service.QuestionQuotaService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员 - 额度管理
 */
@RestController
@RequestMapping("/api/quota")
public class QuotaController {

    private final QuestionQuotaService questionQuotaService;
    private final SysTenantMapper tenantMapper;

    public QuotaController(QuestionQuotaService questionQuotaService,
                          SysTenantMapper tenantMapper) {
        this.questionQuotaService = questionQuotaService;
        this.tenantMapper = tenantMapper;
    }

    /**
     * 查询所有租户的额度列表
     * GET /api/quota/list?pageNum=1&pageSize=10&keyword=
     */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {

        // 分页查询所有租户
        Page<SysTenant> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysTenant> qw = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            qw.like(SysTenant::getTenantName, keyword)
              .or().like(SysTenant::getTenantCode, keyword);
        }
        qw.orderByDesc(SysTenant::getCreateTime);
        Page<SysTenant> tenantPage = tenantMapper.selectPage(page, qw);

        // 每个租户组装额度信息
        String currentMonth = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
        List<Map<String, Object>> records = tenantPage.getRecords().stream().map(tenant -> {
            Map<String, Object> item = new HashMap<>();
            item.put("tenantId", tenant.getTenantId());
            item.put("tenantName", tenant.getTenantName());
            item.put("tenantCode", tenant.getTenantCode());
            item.put("contactPerson", tenant.getContactPerson());
            item.put("validEnd", tenant.getValidEnd());

            // 查本月额度
            SysTenantQuestionStat stat = questionQuotaService.getStat(tenant.getTenantId(), currentMonth);
            item.put("statMonth", currentMonth);
            item.put("freeUsed", stat == null || stat.getFreeUseNum() == null ? 0 : stat.getFreeUseNum());
            item.put("payUsed", stat == null || stat.getPayUseNum() == null ? 0 : stat.getPayUseNum());
            item.put("surplusPay", stat == null || stat.getSurplusPayQuestion() == null ? 0 : stat.getSurplusPayQuestion());
            item.put("statId", stat == null ? null : stat.getId());
            return item;
        }).toList();

        Map<String, Object> result = new HashMap<>();
        result.put("total", tenantPage.getTotal());
        result.put("records", records);
        return Result.success(result);
    }

    /**
     * 查询指定租户的额度详情
     * GET /api/quota/{tenantId}
     */
    @GetMapping("/{tenantId}")
    public Result<?> detail(@PathVariable String tenantId) {
        SysTenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            return Result.error(500, "租户不存在");
        }
        String currentMonth = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
        Map<String, Object> quota = questionQuotaService.getQuotaInfo(tenantId);
        Map<String, Object> data = new HashMap<>();
        data.put("tenantId", tenant.getTenantId());
        data.put("tenantName", tenant.getTenantName());
        data.put("tenantCode", tenant.getTenantCode());
        data.putAll(quota);
        return Result.success(data);
    }

    /**
     * 调整租户付费存量额度（管理员操作）
     * PUT /api/quota/{tenantId}/surplus
     * body: { "adjust": +5 或 -3, "remark": "管理员赠送" }
     */
    @PutMapping("/{tenantId}/surplus")
    public Result<?> adjustSurplus(@PathVariable String tenantId,
                                   @RequestBody Map<String, Object> body) {
        Integer adjust = body.get("adjust") == null ? 0 : Integer.parseInt(body.get("adjust").toString());
        String remark = body.get("remark") == null ? "" : body.get("remark").toString();

        boolean ok = questionQuotaService.adjustSurplus(tenantId, adjust, remark);
        if (!ok) {
            return Result.error(500, "调整失败，请检查租户是否有效");
        }
        return Result.success(questionQuotaService.getQuotaInfo(tenantId));
    }

    /**
     * 重置租户当月免费额度（管理员操作）
     * PUT /api/quota/{tenantId}/reset
     */
    @PutMapping("/{tenantId}/reset")
    public Result<?> resetFree(@PathVariable String tenantId) {
        boolean ok = questionQuotaService.resetFreeUse(tenantId);
        if (!ok) {
            return Result.error(500, "重置失败");
        }
        return Result.success(questionQuotaService.getQuotaInfo(tenantId));
    }
}

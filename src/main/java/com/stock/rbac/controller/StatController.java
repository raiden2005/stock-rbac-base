package com.stock.rbac.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stock.rbac.entity.SysQuestionOrder;
import com.stock.rbac.entity.SysSubOrder;
import com.stock.rbac.entity.SysTenant;
import com.stock.rbac.entity.SysTenantQuestionStat;
import com.stock.rbac.mapper.SysQuestionOrderMapper;
import com.stock.rbac.mapper.SysSubOrderMapper;
import com.stock.rbac.mapper.SysTenantMapper;
import com.stock.rbac.mapper.SysTenantQuestionStatMapper;
import com.stock.rbac.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 平台数据统计 - PRD 第5.2/5.6 节
 * - /api/stat/home  仪表盘总览
 * - /api/stat/question/overview  提问总量/付费收入
 * - /api/stat/tenant/question/{tenantId}  指定租户提问明细
 */
@RestController
@RequestMapping("/api/stat")
public class StatController {

    private final SysTenantMapper tenantMapper;
    private final SysSubOrderMapper subOrderMapper;
    private final SysQuestionOrderMapper questionOrderMapper;
    private final SysTenantQuestionStatMapper tenantStatMapper;

    public StatController(SysTenantMapper tenantMapper,
                          SysSubOrderMapper subOrderMapper,
                          SysQuestionOrderMapper questionOrderMapper,
                          SysTenantQuestionStatMapper tenantStatMapper) {
        this.tenantMapper = tenantMapper;
        this.subOrderMapper = subOrderMapper;
        this.questionOrderMapper = questionOrderMapper;
        this.tenantStatMapper = tenantStatMapper;
    }

    @GetMapping("/home")
    public Result<?> home() {
        long totalTenants = tenantMapper.selectCount(null);
        long activeTenants = tenantMapper.selectCount(new LambdaQueryWrapper<SysTenant>()
                .eq(SysTenant::getTenantStatus, 1));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

        // 订阅收入
        List<SysSubOrder> subOrders = subOrderMapper.selectList(new LambdaQueryWrapper<SysSubOrder>()
                .eq(SysSubOrder::getOrderStatus, 1));
        BigDecimal subscriptionRevenueTotal = subOrders.stream()
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subscriptionRevenueToday = subOrders.stream()
                .filter(o -> o.getPayTime() != null && !o.getPayTime().isBefore(startOfDay))
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subscriptionRevenueMonth = subOrders.stream()
                .filter(o -> o.getPayTime() != null && !o.getPayTime().isBefore(startOfMonth))
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 增值订单收入
        List<SysQuestionOrder> qOrders = questionOrderMapper.selectList(new LambdaQueryWrapper<SysQuestionOrder>()
                .eq(SysQuestionOrder::getOrderStatus, 1));
        BigDecimal questionRevenueTotal = qOrders.stream()
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal questionRevenueToday = qOrders.stream()
                .filter(o -> o.getPayTime() != null && !o.getPayTime().isBefore(startOfDay))
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal questionRevenueMonth = qOrders.stream()
                .filter(o -> o.getPayTime() != null && !o.getPayTime().isBefore(startOfMonth))
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 今日新增租户
        long newToday = tenantMapper.selectCount(new LambdaQueryWrapper<SysTenant>()
                .ge(SysTenant::getCreateTime, startOfDay));

        // 到期租户（30天内或已过期）
        long expiring = tenantMapper.selectCount(new LambdaQueryWrapper<SysTenant>()
                .lt(SysTenant::getValidEnd, now.plusDays(30)));

        Map<String, Object> data = new HashMap<>();
        data.put("totalTenants", totalTenants);
        data.put("activeTenants", activeTenants);
        data.put("expiringTenants", expiring);
        data.put("newTenantsToday", newToday);
        data.put("subscriptionRevenueToday", subscriptionRevenueToday);
        data.put("subscriptionRevenueMonth", subscriptionRevenueMonth);
        data.put("subscriptionRevenueTotal", subscriptionRevenueTotal);
        data.put("questionRevenueToday", questionRevenueToday);
        data.put("questionRevenueMonth", questionRevenueMonth);
        data.put("questionRevenueTotal", questionRevenueTotal);
        data.put("totalRevenueToday", subscriptionRevenueToday.add(questionRevenueToday));
        data.put("totalRevenueMonth", subscriptionRevenueMonth.add(questionRevenueMonth));
        data.put("totalRevenueTotal", subscriptionRevenueTotal.add(questionRevenueTotal));
        return Result.success(data);
    }

    @GetMapping("/question/overview")
    public Result<?> questionOverview() {
        List<SysTenantQuestionStat> stats = tenantStatMapper.selectList(null);
        long freeTotal = 0;
        long payUsedTotal = 0;
        long surplusPayTotal = 0;
        for (SysTenantQuestionStat s : stats) {
            freeTotal += s.getFreeUseNum() == null ? 0 : s.getFreeUseNum();
            payUsedTotal += s.getPayUseNum() == null ? 0 : s.getPayUseNum();
            surplusPayTotal += s.getSurplusPayQuestion() == null ? 0 : s.getSurplusPayQuestion();
        }

        List<SysQuestionOrder> qOrders = questionOrderMapper.selectList(
                new LambdaQueryWrapper<SysQuestionOrder>().eq(SysQuestionOrder::getOrderStatus, 1));
        BigDecimal revenue = qOrders.stream()
                .map(o -> o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("totalQuestions", freeTotal + payUsedTotal);
        data.put("freeUsedTotal", freeTotal);
        data.put("paidUsedTotal", payUsedTotal);
        data.put("surplusPayTotal", surplusPayTotal);
        data.put("paidRevenueTotal", revenue);
        data.put("paidOrderCount", qOrders.size());
        return Result.success(data);
    }

    @GetMapping("/tenant/question/{tenantId}")
    public Result<?> tenantQuestionDetail(@PathVariable String tenantId) {
        List<SysTenantQuestionStat> stats = tenantStatMapper.selectList(
                new LambdaQueryWrapper<SysTenantQuestionStat>()
                        .eq(SysTenantQuestionStat::getTenantId, tenantId)
                        .orderByDesc(SysTenantQuestionStat::getStatMonth));
        return Result.success(stats);
    }
}

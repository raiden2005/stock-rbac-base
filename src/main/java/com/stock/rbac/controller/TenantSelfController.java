package com.stock.rbac.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.entity.SysPlanConfig;
import com.stock.rbac.entity.SysQuestion;
import com.stock.rbac.entity.SysSubBill;
import com.stock.rbac.entity.SysSubOrder;
import com.stock.rbac.entity.SysTenant;
import com.stock.rbac.mapper.SysQuestionMapper;
import com.stock.rbac.mapper.SysSubBillMapper;
import com.stock.rbac.mapper.SysSubOrderMapper;
import com.stock.rbac.entity.SysTenantQuestionStat;
import com.stock.rbac.mapper.SysPlanConfigMapper;
import com.stock.rbac.mapper.SysTenantMapper;
import com.stock.rbac.mapper.SysTenantQuestionStatMapper;
import com.stock.rbac.saas.service.PlanConfigService;
import com.stock.rbac.util.UserContext;
import com.stock.rbac.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tenant/self")
public class TenantSelfController {

    @Autowired
    private SysTenantMapper sysTenantMapper;

    @Autowired
    private SysTenantQuestionStatMapper tenantQuestionStatMapper;

    @Autowired
    private SysPlanConfigMapper sysPlanConfigMapper;

    @Autowired
    private PlanConfigService planConfigService;

    @Autowired
    private SysQuestionMapper questionMapper;

    @Autowired
    private SysSubOrderMapper subOrderMapper;

    @Autowired
    private SysSubBillMapper subBillMapper;

    /** 获取当前租户基础信息 + 到期提醒 */
    @GetMapping("/info")
    public Result<?> info() {
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            tenantId = "TENANT_DEFAULT";
        }
        SysTenant tenant = sysTenantMapper.selectById(tenantId);
        if (tenant == null) {
            return Result.error(404, "租户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("tenant", tenant);

        // 计算到期剩余天数
        if (tenant.getValidEnd() != null) {
            long days = ChronoUnit.DAYS.between(LocalDateTime.now(), tenant.getValidEnd());
            data.put("remainingDays", days);
            if (days < 30) {
                data.put("expireHint", "即将到期，请尽快续费");
            } else if (days < 0) {
                data.put("expireHint", "租户已到期，请续费");
            }
        }

        // 套餐配置
        SysPlanConfig plan = planConfigService.getDefaultConfig();
        data.put("yearSubPrice", plan.getYearSubPrice());
        data.put("monthlyFreeQuestionNum", plan.getMonthlyFreeQuestionNum());
        data.put("overQuestionUnitPrice", plan.getOverQuestionUnitPrice());
        return Result.success(data);
    }

    /** 租户提问额度（免费剩余 + 付费存量） */
    @GetMapping("/question/quota")
    public Result<?> questionQuota() {
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            tenantId = "TENANT_DEFAULT";
        }
        String statMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        SysTenantQuestionStat stat = tenantQuestionStatMapper.selectOne(
                new LambdaQueryWrapper<SysTenantQuestionStat>()
                        .eq(SysTenantQuestionStat::getTenantId, tenantId)
                        .eq(SysTenantQuestionStat::getStatMonth, statMonth)
                        .last("LIMIT 1"));

        SysPlanConfig config = sysPlanConfigMapper.selectOne(
                new LambdaQueryWrapper<SysPlanConfig>()
                        .eq(SysPlanConfig::getPlanType, PlanConfigService.PLAN_TYPE_STANDARD)
                        .last("LIMIT 1"));
        int monthlyTotal = config != null && config.getMonthlyFreeQuestionNum() != null
                ? config.getMonthlyFreeQuestionNum() : 3;

        Map<String, Object> data = new HashMap<>();
        data.put("monthlyFreeTotal", monthlyTotal);
        data.put("freeUsed", stat != null && stat.getFreeUseNum() != null ? stat.getFreeUseNum() : 0);
        data.put("surplusPay", stat != null && stat.getSurplusPayQuestion() != null ? stat.getSurplusPayQuestion() : 0);
        data.put("payUsed", stat != null && stat.getPayUseNum() != null ? stat.getPayUseNum() : 0);
        data.put("statMonth", statMonth);
        data.put("unitPrice", config != null ? config.getOverQuestionUnitPrice() : 20);
        return Result.success(data);
    }

    /** 更新本租户联系人信息 */
    @PutMapping("/edit")
    public Result<?> edit(@RequestBody SysTenant tenant) {
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            tenantId = "TENANT_DEFAULT";
        }
        SysTenant update = new SysTenant();
        update.setTenantId(tenantId);
        update.setContactPerson(tenant.getContactPerson());
        update.setContactPhone(tenant.getContactPhone());
        update.setContactEmail(tenant.getContactEmail());
        sysTenantMapper.updateById(update);
        return Result.success();
    }

    /**
     * 用户首页聚合信息：租户信息 + 提问额度 + 订阅状态 + 最近5条提问
     */
    @GetMapping("/home")
    public Result<?> home() {
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            tenantId = "TENANT_DEFAULT";
        }

        Map<String, Object> data = new HashMap<>();

        // 1. 租户基本信息 + 到期提醒
        SysTenant tenant = sysTenantMapper.selectById(tenantId);
        Map<String, Object> tenantInfo = new HashMap<>();
        if (tenant != null) {
            tenantInfo.put("tenantId", tenant.getTenantId());
            tenantInfo.put("tenantName", tenant.getTenantName());
            tenantInfo.put("tenantCode", tenant.getTenantCode());
            tenantInfo.put("tenantStatus", tenant.getTenantStatus());
            tenantInfo.put("planType", tenant.getPlanType());
            tenantInfo.put("validStart", tenant.getValidStart());
            tenantInfo.put("validEnd", tenant.getValidEnd());
            if (tenant.getValidEnd() != null) {
                long days = ChronoUnit.DAYS.between(LocalDateTime.now(), tenant.getValidEnd());
                tenantInfo.put("remainingDays", days);
                if (days < 30) {
                    tenantInfo.put("expireHint", "即将到期，请尽快续费");
                } else if (days < 0) {
                    tenantInfo.put("expireHint", "租户已到期，请续费");
                }
            }
            tenantInfo.put("contactPerson", tenant.getContactPerson());
            tenantInfo.put("contactPhone", tenant.getContactPhone());
            tenantInfo.put("contactEmail", tenant.getContactEmail());
        }
        data.put("tenant", tenantInfo);

        // 2. 套餐配置
        SysPlanConfig plan = planConfigService.getDefaultConfig();
        Map<String, Object> planInfo = new HashMap<>();
        planInfo.put("planType", plan.getPlanType());
        planInfo.put("yearSubPrice", plan.getYearSubPrice());
        planInfo.put("monthlyFreeQuestionNum", plan.getMonthlyFreeQuestionNum());
        planInfo.put("overQuestionUnitPrice", plan.getOverQuestionUnitPrice());
        data.put("plan", planInfo);

        // 3. 提问额度
        String statMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        SysTenantQuestionStat stat = tenantQuestionStatMapper.selectOne(
                new LambdaQueryWrapper<SysTenantQuestionStat>()
                        .eq(SysTenantQuestionStat::getTenantId, tenantId)
                        .eq(SysTenantQuestionStat::getStatMonth, statMonth)
                        .last("LIMIT 1"));
        int monthlyTotal = plan.getMonthlyFreeQuestionNum() == null ? 3 : plan.getMonthlyFreeQuestionNum();
        Map<String, Object> quota = new HashMap<>();
        quota.put("monthlyFreeTotal", monthlyTotal);
        quota.put("freeUsed", stat != null && stat.getFreeUseNum() != null ? stat.getFreeUseNum() : 0);
        quota.put("surplusPay", stat != null && stat.getSurplusPayQuestion() != null ? stat.getSurplusPayQuestion() : 0);
        quota.put("payUsed", stat != null && stat.getPayUseNum() != null ? stat.getPayUseNum() : 0);
        quota.put("statMonth", statMonth);
        data.put("quota", quota);

        // 4. 最近5条提问历史
        Page<SysQuestion> qPage = questionMapper.selectPage(new Page<>(1, 5),
                new LambdaQueryWrapper<SysQuestion>()
                        .eq(SysQuestion::getTenantId, tenantId)
                        .orderByDesc(SysQuestion::getCreateTime));
        data.put("recentQuestions", qPage.getRecords());
        data.put("totalQuestions", qPage.getTotal());

        // 5. 订阅状态（最近1条有效订阅）
        SysSubOrder lastSub = subOrderMapper.selectOne(
                new LambdaQueryWrapper<SysSubOrder>()
                        .eq(SysSubOrder::getTenantId, tenantId)
                        .eq(SysSubOrder::getOrderStatus, 1)
                        .orderByDesc(SysSubOrder::getPayTime)
                        .last("LIMIT 1"));
        Map<String, Object> subInfo = new HashMap<>();
        if (lastSub != null) {
            subInfo.put("hasActiveSubscription", true);
            subInfo.put("subscribeYear", lastSub.getSubscribeYear());
            subInfo.put("totalAmount", lastSub.getTotalAmount());
            subInfo.put("payTime", lastSub.getPayTime());
        } else {
            subInfo.put("hasActiveSubscription", false);
        }
        data.put("subscription", subInfo);

        return Result.success(data);
    }

    /**
     * 我的订阅订单列表
     */
    @GetMapping("/order/sub/list")
    public Result<?> subOrderList(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            tenantId = "TENANT_DEFAULT";
        }
        Page<SysSubOrder> page = subOrderMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysSubOrder>()
                        .eq(SysSubOrder::getTenantId, tenantId)
                        .orderByDesc(SysSubOrder::getCreateTime));
        return Result.success(page);
    }

    /**
     * 我的账单列表
     */
    @GetMapping("/bill/list")
    public Result<?> billList(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            tenantId = "TENANT_DEFAULT";
        }
        Page<SysSubBill> page = subBillMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysSubBill>()
                        .eq(SysSubBill::getTenantId, tenantId)
                        .orderByDesc(SysSubBill::getCreateTime));
        return Result.success(page);
    }
}

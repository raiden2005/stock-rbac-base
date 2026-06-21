package com.stock.rbac.saas.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stock.rbac.entity.SysPlanConfig;
import com.stock.rbac.entity.SysTenantQuestionStat;
import com.stock.rbac.mapper.SysPlanConfigMapper;
import com.stock.rbac.mapper.SysTenantQuestionStatMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提问额度服务
 * 消耗规则（PRD第2.3条）：当月免费额度 → 付费存量额度 → 额度耗尽禁止提问
 */
@Service
public class QuestionQuotaService {

    private final SysTenantQuestionStatMapper tenantQuestionStatMapper;
    private final SysPlanConfigMapper planConfigMapper;

    public QuestionQuotaService(SysTenantQuestionStatMapper tenantQuestionStatMapper,
                                SysPlanConfigMapper planConfigMapper) {
        this.tenantQuestionStatMapper = tenantQuestionStatMapper;
        this.planConfigMapper = planConfigMapper;
    }

    /** 针对并发提问做简单级别的内存锁（不影响正确性，仅提升并发下的稳定性） */
    private static final ConcurrentHashMap<String, Object> LOCK_MAP = new ConcurrentHashMap<>();

    /**
     * 检查并消耗一次提问额度
     * @return true 表示已扣减成功（放行），false 表示额度耗尽（拦截）
     */
    public boolean checkAndConsume(String tenantId) {
        if (tenantId == null || tenantId.isEmpty()) {
            return false;
        }
        Object lock = LOCK_MAP.computeIfAbsent(tenantId, k -> new Object());
        synchronized (lock) {
            try {
                SysPlanConfig config = planConfigMapper.selectOne(
                        new LambdaQueryWrapper<SysPlanConfig>()
                                .eq(SysPlanConfig::getPlanType, PlanConfigService.PLAN_TYPE_STANDARD)
                                .last("LIMIT 1"));
                int monthlyFree = config != null && config.getMonthlyFreeQuestionNum() != null
                        ? config.getMonthlyFreeQuestionNum() : 0;

                String statMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                SysTenantQuestionStat stat = tenantQuestionStatMapper.selectOne(
                        new LambdaQueryWrapper<SysTenantQuestionStat>()
                                .eq(SysTenantQuestionStat::getTenantId, tenantId)
                                .eq(SysTenantQuestionStat::getStatMonth, statMonth)
                                .last("LIMIT 1"));
                if (stat == null) {
                    stat = new SysTenantQuestionStat();
                    stat.setTenantId(tenantId);
                    stat.setStatMonth(statMonth);
                    stat.setFreeUseNum(0);
                    stat.setPayUseNum(0);
                    stat.setSurplusPayQuestion(0);
                    tenantQuestionStatMapper.insert(stat);
                }

                int freeUsed = stat.getFreeUseNum() == null ? 0 : stat.getFreeUseNum();
                // PRD: 免费额度优先
                if (freeUsed < monthlyFree) {
                    stat.setFreeUseNum(freeUsed + 1);
                    tenantQuestionStatMapper.updateById(stat);
                    return true;
                }

                int surplusPay = stat.getSurplusPayQuestion() == null ? 0 : stat.getSurplusPayQuestion();
                if (surplusPay > 0) {
                    stat.setSurplusPayQuestion(surplusPay - 1);
                    stat.setPayUseNum((stat.getPayUseNum() == null ? 0 : stat.getPayUseNum()) + 1);
                    tenantQuestionStatMapper.updateById(stat);
                    return true;
                }

                return false;
            } finally {
                // 防止Map无限增长：超过一定时间后清理
                if (LOCK_MAP.size() > 1000) {
                    LOCK_MAP.clear();
                }
            }
        }
    }

    public Map<String, Object> getQuotaInfo(String tenantId) {
        Map<String, Object> result = new HashMap<>();
        SysPlanConfig config = planConfigMapper.selectOne(
                new LambdaQueryWrapper<SysPlanConfig>()
                        .eq(SysPlanConfig::getPlanType, PlanConfigService.PLAN_TYPE_STANDARD)
                        .last("LIMIT 1"));
        int monthlyFree = config == null || config.getMonthlyFreeQuestionNum() == null
                ? 0 : config.getMonthlyFreeQuestionNum();

        String statMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        SysTenantQuestionStat stat = tenantQuestionStatMapper.selectOne(
                new LambdaQueryWrapper<SysTenantQuestionStat>()
                        .eq(SysTenantQuestionStat::getTenantId, tenantId)
                        .eq(SysTenantQuestionStat::getStatMonth, statMonth)
                        .last("LIMIT 1"));

        result.put("monthlyFreeTotal", monthlyFree);
        result.put("statMonth", statMonth);
        result.put("freeUsed", stat == null || stat.getFreeUseNum() == null ? 0 : stat.getFreeUseNum());
        result.put("surplusPay", stat == null || stat.getSurplusPayQuestion() == null ? 0 : stat.getSurplusPayQuestion());
        result.put("payUsed", stat == null || stat.getPayUseNum() == null ? 0 : stat.getPayUseNum());
        return result;
    }

    public void resetMonthlyFree() {
        // 月度重置由按月统计实现：新月份首次提问时自动创建新记录，无需额外操作
        System.out.println("[QuestionQuotaService] 月度重置任务已触发（按月份分表自动生效）");
    }
}

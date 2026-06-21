package com.stock.rbac.saas.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stock.rbac.entity.SysPlanConfig;
import com.stock.rbac.mapper.SysPlanConfigMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PlanConfigService {

    public static final String PLAN_TYPE_STANDARD = "STANDARD";

    private final SysPlanConfigMapper planConfigMapper;

    public PlanConfigService(SysPlanConfigMapper planConfigMapper) {
        this.planConfigMapper = planConfigMapper;
    }

    public SysPlanConfig getDefaultConfig() {
        return getPlanConfig(PLAN_TYPE_STANDARD);
    }

    public SysPlanConfig getPlanConfig(String planType) {
        SysPlanConfig config = planConfigMapper.selectOne(
                new LambdaQueryWrapper<SysPlanConfig>()
                        .eq(SysPlanConfig::getPlanType, planType)
                        .last("LIMIT 1"));
        if (config == null) {
            // 返回不可变默认（不改动数据库），避免依赖调用顺序造成的副作用
            config = new SysPlanConfig();
            config.setId("PC_" + planType);
            config.setPlanType(planType);
            config.setYearSubPrice(BigDecimal.valueOf(500));
            config.setMonthlyFreeQuestionNum(3);
            config.setOverQuestionUnitPrice(BigDecimal.valueOf(20));
        }
        return config;
    }

    public void updateConfig(SysPlanConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("配置对象不能为空");
        }
        // 防止越权：必须指定有效的 planType
        if (config.getPlanType() == null || config.getPlanType().isEmpty()) {
            config.setPlanType(PLAN_TYPE_STANDARD);
        }
        if (config.getId() != null && !config.getId().isEmpty()) {
            planConfigMapper.updateById(config);
        } else {
            SysPlanConfig existing = planConfigMapper.selectOne(
                    new LambdaQueryWrapper<SysPlanConfig>()
                            .eq(SysPlanConfig::getPlanType, config.getPlanType())
                            .last("LIMIT 1"));
            if (existing != null) {
                config.setId(existing.getId());
                planConfigMapper.updateById(config);
            } else {
                planConfigMapper.insert(config);
            }
        }
    }
}

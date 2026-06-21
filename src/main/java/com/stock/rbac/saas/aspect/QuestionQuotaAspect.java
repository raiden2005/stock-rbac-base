package com.stock.rbac.saas.aspect;

import com.stock.rbac.exception.BusinessException;
import com.stock.rbac.saas.service.QuestionQuotaService;
import com.stock.rbac.util.UserContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 提问额度AOP切面：拦截标注 @QuestionQuota(check=true) 的方法
 * PRD 第7.3节：免费额度优先 → 付费存量 → 额度耗尽禁止提问
 */
@Aspect
@Component
public class QuestionQuotaAspect {

    private final QuestionQuotaService questionQuotaService;

    public QuestionQuotaAspect(QuestionQuotaService questionQuotaService) {
        this.questionQuotaService = questionQuotaService;
    }

    @Around("@annotation(com.stock.rbac.annotation.QuestionQuota) && @annotation(quota)")
    public Object checkQuota(ProceedingJoinPoint pjp,
                             com.stock.rbac.annotation.QuestionQuota quota) throws Throwable {
        // 关闭额度校验时直接放行
        if (quota == null || !quota.check()) {
            return pjp.proceed();
        }

        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) {
            tenantId = "TENANT_DEFAULT";
        }

        boolean ok = questionQuotaService.checkAndConsume(tenantId);
        if (!ok) {
            throw new BusinessException("提问次数已用完，请购买增值提问次数");
        }
        return pjp.proceed();
    }
}

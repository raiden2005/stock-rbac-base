package com.stock.rbac.saas.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.stock.rbac.entity.SysQuestionOrder;
import com.stock.rbac.entity.SysTenant;
import com.stock.rbac.mapper.SysQuestionOrderMapper;
import com.stock.rbac.mapper.SysTenantMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TenantScheduleTasks {

    private final SysQuestionOrderMapper questionOrderMapper;
    private final SysTenantMapper tenantMapper;

    public TenantScheduleTasks(SysQuestionOrderMapper questionOrderMapper,
                               SysTenantMapper tenantMapper) {
        this.questionOrderMapper = questionOrderMapper;
        this.tenantMapper = tenantMapper;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void closeExpiredOrders() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(30);
        LambdaUpdateWrapper<SysQuestionOrder> update = new LambdaUpdateWrapper<>();
        update.eq(SysQuestionOrder::getOrderStatus, 0)
                .lt(SysQuestionOrder::getCreateTime, expireTime)
                .set(SysQuestionOrder::getOrderStatus, 2);
        int rows = questionOrderMapper.update(null, update);
        System.out.println("[TenantScheduleTasks] 关闭过期订单数量: " + rows);
    }

    @Scheduled(cron = "0 5 0 1 * ?")
    public void resetMonthlyFree() {
        System.out.println("[TenantScheduleTasks] 月度免费额度重置任务执行（新月份首次提问时自动创建新记录）");
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void freezeExpiredTenants() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<SysTenant> query = new LambdaQueryWrapper<>();
        query.lt(SysTenant::getValidEnd, now)
                .eq(SysTenant::getTenantStatus, 1);
        java.util.List<SysTenant> expiredTenants = tenantMapper.selectList(query);
        int count = 0;
        for (SysTenant t : expiredTenants) {
            t.setTenantStatus(0);
            tenantMapper.updateById(t);
            count++;
        }
        System.out.println("[TenantScheduleTasks] 冻结过期租户数量: " + count);
    }
}

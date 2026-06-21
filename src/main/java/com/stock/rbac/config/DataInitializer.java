package com.stock.rbac.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stock.rbac.entity.SysPlanConfig;
import com.stock.rbac.entity.SysTenant;
import com.stock.rbac.entity.SysUser;
import com.stock.rbac.entity.SysUserRole;
import com.stock.rbac.mapper.SysPlanConfigMapper;
import com.stock.rbac.mapper.SysTenantMapper;
import com.stock.rbac.mapper.SysUserMapper;
import com.stock.rbac.mapper.SysUserRoleMapper;
import com.stock.rbac.util.PasswordUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysTenantMapper sysTenantMapper;
    private final SysPlanConfigMapper sysPlanConfigMapper;
    private final PasswordUtil passwordUtil;

    public DataInitializer(SysUserMapper sysUserMapper,
                           SysUserRoleMapper sysUserRoleMapper,
                           SysTenantMapper sysTenantMapper,
                           SysPlanConfigMapper sysPlanConfigMapper,
                           PasswordUtil passwordUtil) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysTenantMapper = sysTenantMapper;
        this.sysPlanConfigMapper = sysPlanConfigMapper;
        this.passwordUtil = passwordUtil;
    }

    @Override
    public void run(String... args) {
        SysPlanConfig existingPlan = sysPlanConfigMapper.selectOne(
                new LambdaQueryWrapper<SysPlanConfig>()
                        .eq(SysPlanConfig::getPlanType, "STANDARD")
                        .last("LIMIT 1")
        );
        if (existingPlan == null) {
            SysPlanConfig plan = new SysPlanConfig();
            plan.setId("PC001");
            plan.setPlanType("STANDARD");
            plan.setYearSubPrice(BigDecimal.valueOf(500));
            plan.setMonthlyFreeQuestionNum(3);
            plan.setOverQuestionUnitPrice(BigDecimal.valueOf(20));
            sysPlanConfigMapper.insert(plan);
        }

        SysTenant existingTenant = sysTenantMapper.selectOne(
                new LambdaQueryWrapper<SysTenant>()
                        .eq(SysTenant::getTenantCode, "DEFAULT")
                        .last("LIMIT 1")
        );
        if (existingTenant == null) {
            SysTenant tenant = new SysTenant();
            tenant.setTenantId("TENANT_DEFAULT");
            tenant.setTenantName("默认租户");
            tenant.setTenantCode("DEFAULT");
            tenant.setPlanType("STANDARD");
            tenant.setMaxAccountNum(50);
            tenant.setTenantStatus(1);
            tenant.setValidStart(LocalDateTime.now());
            tenant.setValidEnd(LocalDateTime.now().plusYears(1));
            sysTenantMapper.insert(tenant);
        } else if (existingTenant.getValidEnd() == null
                || existingTenant.getValidEnd().equals(existingTenant.getValidStart())
                || existingTenant.getValidEnd().isBefore(LocalDateTime.now())) {
            // 修复 SQL 初始化遗留：validStart == validEnd（未设置有效期）
            existingTenant.setValidStart(LocalDateTime.now());
            existingTenant.setValidEnd(LocalDateTime.now().plusYears(1));
            if (existingTenant.getTenantStatus() == null) {
                existingTenant.setTenantStatus(1);
            }
            sysTenantMapper.updateById(existingTenant);
        }

        SysUser existingAdmin = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserAccount, "admin")
                        .last("LIMIT 1")
        );

        if (existingAdmin == null) {
            SysUser admin = new SysUser();
            admin.setUserGuid("USER_ADMIN_001");
            admin.setUserAccount("admin");
            admin.setUserName("系统管理员");
            admin.setUserPwdBcrypt(passwordUtil.encode("admin123"));
            admin.setUserStatus(1);
            admin.setUserType("admin");
            admin.setDeptId("DEPT_ROOT");
            sysUserMapper.insert(admin);

            SysUserRole ur = new SysUserRole();
            ur.setId("UR001");
            ur.setUserGuid("USER_ADMIN_001");
            ur.setRoleId("ROLE_SUPER_ADMIN");
            sysUserRoleMapper.insert(ur);

            SysUser user1 = new SysUser();
            user1.setUserGuid("USER_TEST_001");
            user1.setUserAccount("user1");
            user1.setUserName("测试用户1");
            user1.setUserPwdBcrypt(passwordUtil.encode("user123"));
            user1.setUserStatus(1);
            user1.setUserType("normal");
            sysUserMapper.insert(user1);

            SysUserRole ur2 = new SysUserRole();
            ur2.setId("UR002");
            ur2.setUserGuid("USER_TEST_001");
            ur2.setRoleId("ROLE_NORMAL_USER");
            sysUserRoleMapper.insert(ur2);
        }
    }
}

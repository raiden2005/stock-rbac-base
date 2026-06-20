package com.stock.rbac.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stock.rbac.entity.SysUser;
import com.stock.rbac.mapper.SysUserMapper;
import com.stock.rbac.util.PasswordUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final PasswordUtil passwordUtil;

    public DataInitializer(SysUserMapper sysUserMapper, PasswordUtil passwordUtil) {
        this.sysUserMapper = sysUserMapper;
        this.passwordUtil = passwordUtil;
    }

    @Override
    public void run(String... args) {
        // 检查是否已存在admin用户
        SysUser existingAdmin = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserAccount, "admin")
                        .last("LIMIT 1")
        );

        if (existingAdmin == null) {
            // 创建测试用户
            SysUser admin = new SysUser();
            admin.setUserGuid("U001");
            admin.setUserAccount("admin");
            admin.setUserName("系统管理员");
            admin.setUserPwdBcrypt(passwordUtil.encode("admin123"));
            admin.setUserStatus(1);
            admin.setUserType("SUPER_ADMIN");
            sysUserMapper.insert(admin);

            SysUser user1 = new SysUser();
            user1.setUserGuid("U002");
            user1.setUserAccount("user1");
            user1.setUserName("测试用户1");
            user1.setUserPwdBcrypt(passwordUtil.encode("user123"));
            user1.setUserStatus(1);
            user1.setUserType("USER");
            sysUserMapper.insert(user1);
        }
    }
}

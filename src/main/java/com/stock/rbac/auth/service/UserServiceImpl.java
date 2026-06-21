package com.stock.rbac.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stock.rbac.config.LocalCacheConfig;
import com.stock.rbac.constant.RedisConstant;
import com.stock.rbac.constant.SsoConstants;
import com.stock.rbac.entity.SysPermission;
import com.stock.rbac.entity.SysRole;
import com.stock.rbac.entity.SysRolePermission;
import com.stock.rbac.entity.SysUser;
import com.stock.rbac.entity.SysUserRole;
import com.stock.rbac.entity.SysTenant;
import com.stock.rbac.exception.BusinessException;
import com.stock.rbac.mapper.SysPermissionMapper;
import com.stock.rbac.mapper.SysRoleMapper;
import com.stock.rbac.mapper.SysRolePermissionMapper;
import com.stock.rbac.mapper.SysUserMapper;
import com.stock.rbac.mapper.SysUserRoleMapper;
import com.stock.rbac.mapper.SysTenantMapper;
import com.stock.rbac.util.JwtUtil;
import com.stock.rbac.util.PasswordUtil;
import com.stock.rbac.util.RbacConfigUtil;
import com.stock.rbac.util.UserContext;
import com.stock.rbac.vo.LoginRequestVO;
import com.stock.rbac.vo.LoginResponseVO;
import com.stock.rbac.vo.UserInfoBean;
import com.stock.rbac.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysTenantMapper sysTenantMapper;
    private final LocalCacheConfig.RedisTemplateFallback redisTemplate;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;
    private final RbacConfigUtil rbacConfigUtil;

    public UserServiceImpl(SysUserMapper sysUserMapper,
                           SysUserRoleMapper sysUserRoleMapper,
                           SysRoleMapper sysRoleMapper,
                           SysRolePermissionMapper sysRolePermissionMapper,
                           SysPermissionMapper sysPermissionMapper,
                           SysTenantMapper sysTenantMapper,
                           LocalCacheConfig.RedisTemplateFallback redisTemplate,
                           PasswordUtil passwordUtil,
                           JwtUtil jwtUtil,
                           RbacConfigUtil rbacConfigUtil) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysTenantMapper = sysTenantMapper;
        this.redisTemplate = redisTemplate;
        this.passwordUtil = passwordUtil;
        this.jwtUtil = jwtUtil;
        this.rbacConfigUtil = rbacConfigUtil;
    }

    @Override
    public UserInfoBean getUserInfoByGuid(String userGuid) {
        if (userGuid == null) {
            return null;
        }

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserGuid, userGuid)
                        .last("LIMIT 1")
        );
        if (user == null) {
            return null;
        }

        List<String> roleCodes = getRoleCodesByUserGuid(userGuid);
        List<String> permCodes = getPermCodesByUserGuid(userGuid, roleCodes);

        return UserInfoBean.builder()
                .userGuid(user.getUserGuid())
                .userAccount(user.getUserAccount())
                .userName(user.getUserName())
                .userStatus(user.getUserStatus())
                .userType(user.getUserType())
                .roles(roleCodes)
                .permCodes(permCodes)
                .loginTime(System.currentTimeMillis())
                .build();
    }

    private List<String> getRoleCodesByUserGuid(String userGuid) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserGuid, userGuid)
        );
        if (userRoles == null || userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());

        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getRoleId, roleIds)
                        .eq(SysRole::getStatus, 1)
        );
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }

        return roles.stream()
                .map(SysRole::getRoleCode)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> getPermCodesByUserGuid(String userGuid, List<String> roleCodes) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserGuid, userGuid)
        );
        if (userRoles == null || userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());

        List<SysRolePermission> rolePermissions = sysRolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>()
                        .in(SysRolePermission::getRoleId, roleIds)
        );
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> permIds = rolePermissions.stream()
                .map(SysRolePermission::getPermId)
                .distinct()
                .collect(Collectors.toList());

        List<SysPermission> permissions = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .in(SysPermission::getPermId, permIds)
                        .eq(SysPermission::getStatus, 1)
        );
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }

        return permissions.stream()
                .map(SysPermission::getPermCode)
                .filter(code -> code != null && !code.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public LoginResponseVO login(LoginRequestVO vo, HttpServletRequest request) {
        if (vo == null || vo.getUserAccount() == null || vo.getUserPwd() == null
                || vo.getUserAccount().trim().isEmpty()) {
            throw new BusinessException(400, "账号或密码不能为空");
        }

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserAccount, vo.getUserAccount())
                        .last("LIMIT 1"));
        if (user == null) {
            throw new BusinessException(400, "账号或密码错误");
        }

        if (user.getUserStatus() != null && user.getUserStatus() == 0) {
            throw new BusinessException(400, "账号已禁用");
        }

        // PRD 第7.1节：租户到期冻结，全员禁止登录（admin 类型用户豁免，便于平台侧运维）
        String tenantId = "TENANT_DEFAULT";
        SysTenant tenant = sysTenantMapper.selectById(tenantId);
        if (tenant != null && !"admin".equalsIgnoreCase(user.getUserType())) {
            if (tenant.getTenantStatus() != null && tenant.getTenantStatus() == 0) {
                throw new BusinessException(403, "租户已冻结，请续费后登录");
            }
            if (tenant.getValidEnd() != null && tenant.getValidEnd().isBefore(java.time.LocalDateTime.now())) {
                throw new BusinessException(403, "租户服务已到期，请续费后登录");
            }
        }

        if (!passwordUtil.matches(vo.getUserPwd(), user.getUserPwdBcrypt())) {
            throw new BusinessException(400, "账号或密码错误");
        }

        List<String> roleCodes = getRoleCodesByUserGuid(user.getUserGuid());
        List<String> permCodes = getPermCodesByUserGuid(user.getUserGuid(), roleCodes);

        UserInfoBean userInfo = UserInfoBean.builder()
                .userGuid(user.getUserGuid())
                .userAccount(user.getUserAccount())
                .userName(user.getUserName())
                .userStatus(user.getUserStatus())
                .userType(user.getUserType())
                .roles(roleCodes)
                .permCodes(permCodes)
                .loginTime(System.currentTimeMillis())
                .build();

        // 登录成功后绑定 UserContext，确保 AuditLogAspect 能获取用户信息
        UserContext.setUserGuid(userInfo.getUserGuid());
        UserContext.setUserAccount(userInfo.getUserAccount());
        UserContext.setUserName(userInfo.getUserName());
        UserContext.setUserType(userInfo.getUserType());
        UserContext.setRoles(userInfo.getRoles());
        UserContext.setPermCodes(userInfo.getPermCodes());
        // 默认租户（单租户场景下）
        UserContext.setTenantId("TENANT_DEFAULT");

        LoginResponseVO response = new LoginResponseVO();
        response.setUserGuid(user.getUserGuid());
        response.setUserAccount(user.getUserAccount());
        response.setUserName(user.getUserName());
        response.setUserType(user.getUserType());
        response.setRoles(roleCodes);

        String loginClient = vo.getLoginClient();
        if (SsoConstants.USER_TYPE_MOBILE.equalsIgnoreCase(loginClient)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("user_guid", user.getUserGuid());
            claims.put("account", user.getUserAccount());

            String token = jwtUtil.generateToken(user.getUserGuid(), claims);
            response.setToken(token);
            response.setExpireTime(jwtUtil.getExpireTime());

            String redisKey = RedisConstant.AUTH_JWT_PREFIX + token;
            redisTemplate.set(redisKey, user.getUserGuid(),
                    RedisConstant.JWT_CACHE_EXPIRE, TimeUnit.SECONDS);
        } else {
            HttpSession session = request.getSession(true);
            session.setAttribute(SsoConstants.SESSION_USER_KEY, userInfo);

            String sessionKey = RedisConstant.AUTH_PC_SESSION_PREFIX + session.getId();
            redisTemplate.set(sessionKey, user.getUserGuid(),
                    RedisConstant.RBAC_CACHE_EXPIRE, TimeUnit.SECONDS);
        }

        return response;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            try {
                redisTemplate.delete(RedisConstant.AUTH_PC_SESSION_PREFIX + session.getId());
            } catch (Exception ignored) {
            }
            try {
                session.invalidate();
            } catch (Exception ignored) {
            }
        }

        String authHeader = request.getHeader(SsoConstants.HEADER_AUTH);
        if (authHeader != null && authHeader.startsWith(SsoConstants.TOKEN_PREFIX)) {
            String token = authHeader.substring(SsoConstants.TOKEN_PREFIX.length());
            try {
                redisTemplate.delete(RedisConstant.AUTH_JWT_PREFIX + token);
            } catch (Exception ignored) {
            }
        }

        String userGuid = UserContext.getUserGuid();
        if (userGuid != null) {
            try {
                redisTemplate.delete(RedisConstant.RBAC_USER_ROLE_PREFIX + userGuid);
            } catch (Exception ignored) {
            }
        }

        UserContext.clear();
    }

    @Override
    public UserInfoVO getCurrentUserInfo() {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserGuid(UserContext.getUserGuid());
        vo.setUserAccount(UserContext.getUserAccount());
        vo.setUserName(UserContext.getUserName());
        vo.setUserType(UserContext.getUserType());
        vo.setRoles(UserContext.getRoles());
        vo.setPermCodes(UserContext.getPermCodes());
        return vo;
    }
}

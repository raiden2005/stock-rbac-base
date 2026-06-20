package com.stock.rbac.visibility.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stock.rbac.config.LocalCacheConfig;
import com.stock.rbac.constant.RedisConstant;
import com.stock.rbac.entity.BizVisibleRule;
import com.stock.rbac.entity.SysPermissionVersion;
import com.stock.rbac.entity.SysRole;
import com.stock.rbac.entity.SysUserRole;
import com.stock.rbac.mapper.BizVisibleRuleMapper;
import com.stock.rbac.mapper.SysPermissionMapper;
import com.stock.rbac.mapper.SysPermissionVersionMapper;
import com.stock.rbac.mapper.SysRoleMapper;
import com.stock.rbac.mapper.SysRolePermissionMapper;
import com.stock.rbac.mapper.SysUserMapper;
import com.stock.rbac.mapper.SysUserRoleMapper;
import com.stock.rbac.permission.service.PermissionManagementService;
import com.stock.rbac.visibility.vo.VisibilityCacheData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VisibilityPermissionServiceImpl implements VisibilityPermissionService {

    private static final Logger log = LoggerFactory.getLogger(VisibilityPermissionServiceImpl.class);

    private static final String SUPER_ADMIN = "SUPER_ADMIN";

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final BizVisibleRuleMapper bizVisibleRuleMapper;
    private final SysPermissionVersionMapper sysPermissionVersionMapper;
    private final LocalCacheConfig.RedisTemplateFallback redisTemplate;
    private final PermissionManagementService permissionManagementService;

    public VisibilityPermissionServiceImpl(SysUserMapper sysUserMapper,
                                           SysUserRoleMapper sysUserRoleMapper,
                                           SysRoleMapper sysRoleMapper,
                                           SysRolePermissionMapper sysRolePermissionMapper,
                                           SysPermissionMapper sysPermissionMapper,
                                           BizVisibleRuleMapper bizVisibleRuleMapper,
                                           SysPermissionVersionMapper sysPermissionVersionMapper,
                                           LocalCacheConfig.RedisTemplateFallback redisTemplate,
                                           PermissionManagementService permissionManagementService) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysPermissionMapper = sysPermissionMapper;
        this.bizVisibleRuleMapper = bizVisibleRuleMapper;
        this.sysPermissionVersionMapper = sysPermissionVersionMapper;
        this.redisTemplate = redisTemplate;
        this.permissionManagementService = permissionManagementService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> loadGlobalPermissionMap() {
        try {
            Object cached = redisTemplate.get(RedisConstant.RBAC_GLOBAL_PERMISSION);
            if (cached instanceof Map) {
                return (Map<String, List<String>>) cached;
            }
        } catch (Exception e) {
            log.warn("读取全局权限缓存失败，将从数据库重载", e);
        }
        permissionManagementService.refreshGlobalPermissionCache();
        try {
            Object cached = redisTemplate.get(RedisConstant.RBAC_GLOBAL_PERMISSION);
            if (cached instanceof Map) {
                return (Map<String, List<String>>) cached;
            }
        } catch (Exception e) {
            log.warn("重载后读取全局权限缓存失败", e);
        }
        return new java.util.HashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getUserRoles(String userGuid) {
        if (userGuid == null || userGuid.isEmpty()) {
            return new ArrayList<>();
        }
        String cacheKey = RedisConstant.RBAC_USER_ROLE_PREFIX + userGuid;
        try {
            Object cached = redisTemplate.get(cacheKey);
            if (cached instanceof List) {
                return (List<String>) cached;
            }
        } catch (Exception e) {
            log.warn("读取用户角色缓存失败 userGuid={}", userGuid, e);
        }

        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserGuid, userGuid)
        );
        List<String> roleCodes = new ArrayList<>();
        if (userRoles != null && !userRoles.isEmpty()) {
            List<String> roleIds = userRoles.stream()
                    .map(SysUserRole::getRoleId)
                    .distinct()
                    .collect(Collectors.toList());
            List<SysRole> roles = sysRoleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>().in(SysRole::getRoleId, roleIds).eq(SysRole::getStatus, 1)
            );
            if (roles != null) {
                roleCodes = roles.stream()
                        .map(SysRole::getRoleCode)
                        .distinct()
                        .collect(Collectors.toList());
            }
        }

        try {
            redisTemplate.set(cacheKey, roleCodes, RedisConstant.RBAC_CACHE_EXPIRE, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入用户角色缓存失败 userGuid={}", userGuid, e);
        }
        return roleCodes;
    }

    @Override
    public List<String> getUserPermCodes(String userGuid) {
        if (userGuid == null || userGuid.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> roleCodes = getUserRoles(userGuid);
        if (roleCodes.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, List<String>> globalMap = loadGlobalPermissionMap();
        Set<String> permCodeSet = new HashSet<>();
        for (String roleCode : roleCodes) {
            List<String> perms = globalMap.get(roleCode);
            if (perms != null) {
                permCodeSet.addAll(perms);
            }
        }
        return new ArrayList<>(permCodeSet);
    }

    @Override
    public boolean checkUserFunctionPerm(String userGuid, String permCode) {
        if (userGuid == null || permCode == null) {
            return false;
        }
        List<String> roleCodes = getUserRoles(userGuid);
        if (roleCodes.contains(SUPER_ADMIN)) {
            return true;
        }
        List<String> userPermCodes = getUserPermCodes(userGuid);
        return userPermCodes.contains(permCode);
    }

    @Override
    public boolean checkUserRole(String userGuid, String roleCode) {
        if (userGuid == null || roleCode == null) {
            return false;
        }
        List<String> roleCodes = getUserRoles(userGuid);
        return roleCodes.contains(roleCode);
    }

    @Override
    public List<String> selectVisibilityPermission(String userGuid, String resourceType) {
        if (userGuid == null || resourceType == null) {
            return new ArrayList<>();
        }
        String cacheKey = RedisConstant.VISIBILITY_PREFIX + resourceType + ":" + userGuid;
        long currentVersion = getCurrentVisibilityVersion();

        try {
            Object cached = redisTemplate.get(cacheKey);
            if (cached instanceof VisibilityCacheData) {
                VisibilityCacheData cacheData = (VisibilityCacheData) cached;
                if (cacheData.getVersionNo() != null && cacheData.getVersionNo() >= currentVersion) {
                    return cacheData.getResourceIds() != null ? cacheData.getResourceIds() : new ArrayList<>();
                }
            }
        } catch (Exception e) {
            log.warn("读取可见权限缓存失败 userGuid={}, resourceType={}", userGuid, resourceType, e);
        }

        List<String> roles = getUserRoles(userGuid);
        List<String> permCodes = getUserPermCodes(userGuid);
        if (roles.isEmpty() && permCodes.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<BizVisibleRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizVisibleRule::getUserGuid, userGuid)
                .eq(BizVisibleRule::getResourceType, resourceType)
                .eq(BizVisibleRule::getStatus, 1)
                .and(w -> w.isNull(BizVisibleRule::getValidStart).or().le(BizVisibleRule::getValidStart, now))
                .and(w -> w.isNull(BizVisibleRule::getValidEnd).or().ge(BizVisibleRule::getValidEnd, now));
        List<BizVisibleRule> rules = bizVisibleRuleMapper.selectList(wrapper);

        Set<String> resourceIds = new HashSet<>();
        if (rules != null) {
            for (BizVisibleRule rule : rules) {
                if (rule.getResourceId() != null && !rule.getResourceId().isEmpty()) {
                    resourceIds.add(rule.getResourceId());
                }
            }
        }

        List<String> result = new ArrayList<>(resourceIds);
        try {
            VisibilityCacheData cacheData = new VisibilityCacheData(currentVersion, result, System.currentTimeMillis());
            redisTemplate.set(cacheKey, cacheData, RedisConstant.VISIBILITY_CACHE_EXPIRE, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入可见权限缓存失败 userGuid={}, resourceType={}", userGuid, resourceType, e);
        }
        return result;
    }

    @Override
    public boolean checkUserVisibility(String userGuid, String resourceType, String resourceId) {
        if (userGuid == null || resourceType == null || resourceId == null) {
            return false;
        }
        if (!checkUserFunctionPerm(userGuid, resourceType)) {
            return false;
        }
        List<String> resourceIds = selectVisibilityPermission(userGuid, resourceType);
        return resourceIds.contains(resourceId);
    }

    @Override
    public boolean isCacheValid(long cachedVersion) {
        long latestVersion = permissionManagementService.getCurrentPermVersion();
        return cachedVersion >= latestVersion;
    }

    private long getCurrentVisibilityVersion() {
        try {
            SysPermissionVersion version = sysPermissionVersionMapper.selectOne(
                    new LambdaQueryWrapper<SysPermissionVersion>()
                            .in(SysPermissionVersion::getPermType, "function", "visible", "visibility")
                            .orderByDesc(SysPermissionVersion::getVersionNo)
                            .last("LIMIT 1")
            );
            if (version != null && version.getVersionNo() != null) {
                return version.getVersionNo();
            }
        } catch (Exception e) {
            log.warn("读取权限版本号失败", e);
        }
        return permissionManagementService.getCurrentPermVersion();
    }
}

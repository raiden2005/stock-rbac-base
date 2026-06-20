package com.stock.rbac.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.config.LocalCacheConfig;
import com.stock.rbac.constant.RedisConstant;
import com.stock.rbac.entity.BizVisibleRule;
import com.stock.rbac.entity.SysPermission;
import com.stock.rbac.entity.SysPermissionVersion;
import com.stock.rbac.entity.SysRole;
import com.stock.rbac.entity.SysRolePermission;
import com.stock.rbac.entity.SysUser;
import com.stock.rbac.entity.SysUserRole;
import com.stock.rbac.event.PermissionConfigChangedEvent;
import com.stock.rbac.exception.BusinessException;
import com.stock.rbac.mapper.BizVisibleRuleMapper;
import com.stock.rbac.mapper.SysPermissionMapper;
import com.stock.rbac.mapper.SysPermissionVersionMapper;
import com.stock.rbac.mapper.SysRoleMapper;
import com.stock.rbac.mapper.SysRolePermissionMapper;
import com.stock.rbac.mapper.SysUserMapper;
import com.stock.rbac.mapper.SysUserRoleMapper;
import com.stock.rbac.util.PasswordUtil;
import com.stock.rbac.util.UserContext;
import com.stock.rbac.vo.PermissionTreeVO;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PermissionManagementServiceImpl implements PermissionManagementService {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PermissionManagementServiceImpl.class);

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final BizVisibleRuleMapper bizVisibleRuleMapper;
    private final SysPermissionVersionMapper sysPermissionVersionMapper;
    private final LocalCacheConfig.RedisTemplateFallback redisTemplate;
    private final PasswordUtil passwordUtil;
    private final ApplicationContext applicationContext;

    public PermissionManagementServiceImpl(SysUserMapper sysUserMapper,
                                           SysUserRoleMapper sysUserRoleMapper,
                                           SysRoleMapper sysRoleMapper,
                                           SysPermissionMapper sysPermissionMapper,
                                           SysRolePermissionMapper sysRolePermissionMapper,
                                           BizVisibleRuleMapper bizVisibleRuleMapper,
                                           SysPermissionVersionMapper sysPermissionVersionMapper,
                                           LocalCacheConfig.RedisTemplateFallback redisTemplate,
                                           PasswordUtil passwordUtil,
                                           ApplicationContext applicationContext) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.bizVisibleRuleMapper = bizVisibleRuleMapper;
        this.sysPermissionVersionMapper = sysPermissionVersionMapper;
        this.redisTemplate = redisTemplate;
        this.passwordUtil = passwordUtil;
        this.applicationContext = applicationContext;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(SysUser user, List<String> roleIds) {
        if (user == null || user.getUserAccount() == null) {
            throw new BusinessException("用户账号不能为空");
        }

        SysUser exist = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUserAccount, user.getUserAccount())
                        .last("LIMIT 1")
        );
        if (exist != null) {
            throw new BusinessException("账号【" + user.getUserAccount() + "】已存在");
        }

        if (user.getUserGuid() == null || user.getUserGuid().isEmpty()) {
            user.setUserGuid(UUID.randomUUID().toString().replace("-", ""));
        }

        if (user.getUserPwdBcrypt() != null && !user.getUserPwdBcrypt().isEmpty()) {
            user.setUserPwdBcrypt(passwordUtil.encode(user.getUserPwdBcrypt()));
        }

        sysUserMapper.insert(user);

        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> userRoleList = new ArrayList<>();
            for (String roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserGuid(user.getUserGuid());
                ur.setRoleId(roleId);
                userRoleList.add(ur);
            }
            for (SysUserRole ur : userRoleList) {
                sysUserRoleMapper.insert(ur);
            }
        }

        incrPermVersion("function");
        refreshGlobalPermissionCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "ADD_USER", UserContext.getUserGuid()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
        if (user == null || user.getUserGuid() == null) {
            throw new BusinessException("用户guid不能为空");
        }
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(String userGuid) {
        if (userGuid == null || userGuid.isEmpty()) {
            throw new BusinessException("用户guid不能为空");
        }

        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserGuid, userGuid)
        );

        sysUserMapper.deleteById(userGuid);

        incrPermVersion("function");
        refreshGlobalPermissionCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "DELETE_USER", UserContext.getUserGuid()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(String userGuid, Integer status) {
        if (userGuid == null || userGuid.isEmpty()) {
            throw new BusinessException("用户guid不能为空");
        }
        SysUser user = new SysUser();
        user.setUserGuid(userGuid);
        user.setUserStatus(status);
        sysUserMapper.updateById(user);

        incrPermVersion("function");
        refreshGlobalPermissionCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "UPDATE_USER_STATUS", UserContext.getUserGuid()));
    }

    @Override
    public Page<SysUser> pageUsers(long current, long size, String keyword) {
        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getUserAccount, keyword)
                    .or().like(SysUser::getUserName, keyword));
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return sysUserMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignUserRoles(String userGuid, List<String> roleIds) {
        if (userGuid == null || userGuid.isEmpty()) {
            throw new BusinessException("用户guid不能为空");
        }

        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserGuid, userGuid)
        );

        if (roleIds != null && !roleIds.isEmpty()) {
            for (String roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserGuid(userGuid);
                ur.setRoleId(roleId);
                sysUserRoleMapper.insert(ur);
            }
        }

        incrPermVersion("function");
        refreshGlobalPermissionCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "ASSIGN_USER_ROLES", UserContext.getUserGuid()));
    }

    @Override
    public List<String> getUserRoleIds(String userGuid) {
        if (userGuid == null || userGuid.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserGuid, userGuid)
        );
        if (userRoles == null || userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        return userRoles.stream()
                .map(SysUserRole::getRoleId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(SysRole role, List<String> permIds) {
        if (role == null || role.getRoleCode() == null) {
            throw new BusinessException("角色代码不能为空");
        }

        SysRole exist = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, role.getRoleCode())
                        .last("LIMIT 1")
        );
        if (exist != null) {
            throw new BusinessException("角色代码【" + role.getRoleCode() + "】已存在");
        }

        if (role.getRoleId() == null || role.getRoleId().isEmpty()) {
            role.setRoleId(UUID.randomUUID().toString().replace("-", ""));
        }

        sysRoleMapper.insert(role);

        if (permIds != null && !permIds.isEmpty()) {
            for (String permId : permIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(role.getRoleId());
                rp.setPermId(permId);
                sysRolePermissionMapper.insert(rp);
            }
        }

        incrPermVersion("function");
        refreshGlobalPermissionCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "ADD_ROLE", UserContext.getUserGuid()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRole role) {
        if (role == null || role.getRoleId() == null) {
            throw new BusinessException("角色id不能为空");
        }
        sysRoleMapper.updateById(role);

        incrPermVersion("function");
        refreshGlobalPermissionCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "UPDATE_ROLE", UserContext.getUserGuid()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(String roleId) {
        if (roleId == null || roleId.isEmpty()) {
            throw new BusinessException("角色id不能为空");
        }

        List<SysUserRole> bindUsers = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId)
        );
        if (bindUsers != null && !bindUsers.isEmpty()) {
            throw new BusinessException("该角色仍有用户绑定，无法删除");
        }

        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId)
        );

        sysRoleMapper.deleteById(roleId);

        incrPermVersion("function");
        refreshGlobalPermissionCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "DELETE_ROLE", UserContext.getUserGuid()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoleStatus(String roleId, Integer status) {
        if (roleId == null || roleId.isEmpty()) {
            throw new BusinessException("角色id不能为空");
        }
        SysRole role = new SysRole();
        role.setRoleId(roleId);
        role.setStatus(status);
        sysRoleMapper.updateById(role);

        incrPermVersion("function");
        refreshGlobalPermissionCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "UPDATE_ROLE_STATUS", UserContext.getUserGuid()));
    }

    @Override
    public Page<SysRole> pageRoles(long current, long size, String keyword) {
        Page<SysRole> page = new Page<>(current, size);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, keyword)
                    .or().like(SysRole::getRoleName, keyword));
        }
        wrapper.orderByAsc(SysRole::getSort).orderByDesc(SysRole::getCreateTime);
        return sysRoleMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolePermissions(String roleId, List<String> permIds) {
        if (roleId == null || roleId.isEmpty()) {
            throw new BusinessException("角色id不能为空");
        }

        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId)
        );

        if (permIds != null && !permIds.isEmpty()) {
            for (String permId : permIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermId(permId);
                sysRolePermissionMapper.insert(rp);
            }
        }

        incrPermVersion("function");
        refreshGlobalPermissionCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "ASSIGN_ROLE_PERMISSIONS", UserContext.getUserGuid()));
    }

    @Override
    public List<String> getRolePermIds(String roleId) {
        if (roleId == null || roleId.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysRolePermission> rolePermissions = sysRolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId)
        );
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return new ArrayList<>();
        }
        return rolePermissions.stream()
                .map(SysRolePermission::getPermId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<SysRole> listAllRoles() {
        return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSort)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPermission(SysPermission perm) {
        if (perm == null || perm.getPermCode() == null) {
            throw new BusinessException("权限代码不能为空");
        }

        SysPermission exist = sysPermissionMapper.selectOne(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getPermCode, perm.getPermCode())
                        .last("LIMIT 1")
        );
        if (exist != null) {
            throw new BusinessException("权限代码【" + perm.getPermCode() + "】已存在");
        }

        if (perm.getPermId() == null || perm.getPermId().isEmpty()) {
            perm.setPermId(UUID.randomUUID().toString().replace("-", ""));
        }

        sysPermissionMapper.insert(perm);

        incrPermVersion("function");
        refreshGlobalPermissionCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "ADD_PERMISSION", UserContext.getUserGuid()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(SysPermission perm) {
        if (perm == null || perm.getPermId() == null) {
            throw new BusinessException("权限id不能为空");
        }
        sysPermissionMapper.updateById(perm);

        incrPermVersion("function");
        refreshGlobalPermissionCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "UPDATE_PERMISSION", UserContext.getUserGuid()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(String permId) {
        if (permId == null || permId.isEmpty()) {
            throw new BusinessException("权限id不能为空");
        }

        List<SysPermission> children = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getParentId, permId)
        );
        if (children != null && !children.isEmpty()) {
            throw new BusinessException("存在子权限，禁止删除");
        }

        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getPermId, permId)
        );

        sysPermissionMapper.deleteById(permId);

        incrPermVersion("function");
        refreshGlobalPermissionCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "DELETE_PERMISSION", UserContext.getUserGuid()));
    }

    @Override
    public List<PermissionTreeVO> listPermissionTree() {
        List<SysPermission> allPerms = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getSort)
        );
        if (allPerms == null || allPerms.isEmpty()) {
            return new ArrayList<>();
        }

        List<PermissionTreeVO> allVos = new ArrayList<>();
        for (SysPermission perm : allPerms) {
            PermissionTreeVO vo = new PermissionTreeVO();
            vo.setPermId(perm.getPermId());
            vo.setParentId(perm.getParentId());
            vo.setPermName(perm.getPermName());
            vo.setPermCode(perm.getPermCode());
            vo.setPermType(perm.getPermType());
            vo.setPermUrl(perm.getPermUrl());
            vo.setSort(perm.getSort());
            vo.setIcon(perm.getIcon());
            vo.setStatus(perm.getStatus());
            vo.setChildren(new ArrayList<>());
            allVos.add(vo);
        }

        Map<String, PermissionTreeVO> voMap = new HashMap<>();
        for (PermissionTreeVO vo : allVos) {
            voMap.put(vo.getPermId(), vo);
        }

        List<PermissionTreeVO> roots = new ArrayList<>();
        for (PermissionTreeVO vo : allVos) {
            String parentId = vo.getParentId();
            if (parentId == null || parentId.isEmpty() || "0".equals(parentId)) {
                roots.add(vo);
            } else {
                PermissionTreeVO parent = voMap.get(parentId);
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }

        return roots;
    }

    @Override
    public List<SysPermission> listPermissionsByType(String permType) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        if (permType != null && !permType.isEmpty()) {
            wrapper.eq(SysPermission::getPermType, permType);
        }
        wrapper.orderByAsc(SysPermission::getSort);
        return sysPermissionMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addVisibleRule(BizVisibleRule rule) {
        if (rule == null) {
            throw new BusinessException("可见规则不能为空");
        }
        if (rule.getRuleId() == null || rule.getRuleId().isEmpty()) {
            rule.setRuleId(UUID.randomUUID().toString().replace("-", ""));
        }
        bizVisibleRuleMapper.insert(rule);

        incrPermVersion("visibility");
        clearAllVisibilityCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "ADD_VISIBLE_RULE", UserContext.getUserGuid()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAddVisibleRules(List<BizVisibleRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return;
        }
        for (BizVisibleRule rule : rules) {
            if (rule.getRuleId() == null || rule.getRuleId().isEmpty()) {
                rule.setRuleId(UUID.randomUUID().toString().replace("-", ""));
            }
            bizVisibleRuleMapper.insert(rule);
        }

        incrPermVersion("visibility");
        clearAllVisibilityCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "BATCH_ADD_VISIBLE_RULES", UserContext.getUserGuid()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVisibleRule(String ruleId) {
        if (ruleId == null || ruleId.isEmpty()) {
            throw new BusinessException("规则id不能为空");
        }
        bizVisibleRuleMapper.deleteById(ruleId);

        incrPermVersion("visibility");
        clearAllVisibilityCache();
        applicationContext.publishEvent(new PermissionConfigChangedEvent(this, "DELETE_VISIBLE_RULE", UserContext.getUserGuid()));
    }

    @Override
    public Page<BizVisibleRule> pageVisibleRules(long current, long size, String userGuid, String resourceType) {
        Page<BizVisibleRule> page = new Page<>(current, size);
        LambdaQueryWrapper<BizVisibleRule> wrapper = new LambdaQueryWrapper<>();
        if (userGuid != null && !userGuid.isEmpty()) {
            wrapper.eq(BizVisibleRule::getUserGuid, userGuid);
        }
        if (resourceType != null && !resourceType.isEmpty()) {
            wrapper.eq(BizVisibleRule::getResourceType, resourceType);
        }
        wrapper.orderByDesc(BizVisibleRule::getCreateTime);
        return bizVisibleRuleMapper.selectPage(page, wrapper);
    }

    @Override
    public void refreshGlobalPermissionCache() {
        try {
            Map<String, List<String>> roleToPermCodes = new HashMap<>();

            List<SysRole> allRoles = sysRoleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1)
            );
            if (allRoles == null || allRoles.isEmpty()) {
                redisTemplate.set(RedisConstant.RBAC_GLOBAL_PERMISSION, roleToPermCodes,
                        RedisConstant.RBAC_CACHE_EXPIRE, TimeUnit.SECONDS);
                clearAllUserRoleCache();
                clearAllVisibilityCache();
                return;
            }

            List<String> roleIds = allRoles.stream()
                    .map(SysRole::getRoleId)
                    .distinct()
                    .collect(Collectors.toList());

            List<SysRolePermission> allRolePerms = sysRolePermissionMapper.selectList(
                    new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds)
            );

            Map<String, List<String>> roleIdToPermIdsMap = new HashMap<>();
            if (allRolePerms != null && !allRolePerms.isEmpty()) {
                List<String> permIds = allRolePerms.stream()
                        .map(SysRolePermission::getPermId)
                        .distinct()
                        .collect(Collectors.toList());

                List<SysPermission> allPerms = sysPermissionMapper.selectList(
                        new LambdaQueryWrapper<SysPermission>()
                                .in(SysPermission::getPermId, permIds)
                                .eq(SysPermission::getStatus, 1)
                );
                Map<String, String> permIdToCodeMap = new HashMap<>();
                if (allPerms != null) {
                    for (SysPermission perm : allPerms) {
                        permIdToCodeMap.put(perm.getPermId(), perm.getPermCode());
                    }
                }

                for (SysRolePermission rp : allRolePerms) {
                    String permCode = permIdToCodeMap.get(rp.getPermId());
                    if (permCode != null && !permCode.isEmpty()) {
                        roleIdToPermIdsMap.computeIfAbsent(rp.getRoleId(), k -> new ArrayList<>()).add(permCode);
                    }
                }
            }

            for (SysRole role : allRoles) {
                List<String> permCodes = roleIdToPermIdsMap.get(role.getRoleId());
                if (permCodes == null) {
                    permCodes = new ArrayList<>();
                }
                roleToPermCodes.put(role.getRoleCode(), permCodes);
            }

            redisTemplate.set(RedisConstant.RBAC_GLOBAL_PERMISSION, roleToPermCodes,
                    RedisConstant.RBAC_CACHE_EXPIRE, TimeUnit.SECONDS);

            clearAllUserRoleCache();
            clearAllVisibilityCache();

            log.info("全局权限缓存刷新完成，共刷新 {} 个角色", roleToPermCodes.size());
        } catch (Exception e) {
            log.error("刷新全局权限缓存失败", e);
            throw new BusinessException("刷新全局权限缓存失败: " + e.getMessage());
        }
    }

    @Override
    public long getCurrentPermVersion() {
        SysPermissionVersion version = sysPermissionVersionMapper.selectOne(
                new LambdaQueryWrapper<SysPermissionVersion>().last("LIMIT 1")
        );
        if (version == null || version.getVersionNo() == null) {
            return 0L;
        }
        return version.getVersionNo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrPermVersion(String permType) {
        SysPermissionVersion current = sysPermissionVersionMapper.selectOne(
                new LambdaQueryWrapper<SysPermissionVersion>()
                        .eq(SysPermissionVersion::getPermType, permType)
                        .last("LIMIT 1")
        );
        if (current == null) {
            SysPermissionVersion newVersion = new SysPermissionVersion();
            newVersion.setPermType(permType);
            newVersion.setVersionNo(1L);
            sysPermissionVersionMapper.insert(newVersion);
        } else {
            Long newVersionNo = (current.getVersionNo() == null ? 0L : current.getVersionNo()) + 1;
            SysPermissionVersion update = new SysPermissionVersion();
            update.setId(current.getId());
            update.setPermType(permType);
            update.setVersionNo(newVersionNo);
            sysPermissionVersionMapper.updateById(update);
        }
    }

    private void clearAllUserRoleCache() {
        try {
            java.util.Set<String> keys = redisTemplate.keys(RedisConstant.RBAC_USER_ROLE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    redisTemplate.delete(key);
                }
            }
        } catch (Exception e) {
            log.warn("批量清理用户角色缓存失败: {}", e.getMessage());
        }
    }

    private void clearAllVisibilityCache() {
        try {
            java.util.Set<String> keys = redisTemplate.keys(RedisConstant.VISIBILITY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                for (String key : keys) {
                    redisTemplate.delete(key);
                }
            }
        } catch (Exception e) {
            log.warn("批量清理可见权限缓存失败: {}", e.getMessage());
        }
    }
}

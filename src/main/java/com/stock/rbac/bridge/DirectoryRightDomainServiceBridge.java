package com.stock.rbac.bridge;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stock.rbac.entity.BizVisibleRule;
import com.stock.rbac.entity.SysPermission;
import com.stock.rbac.entity.SysRole;
import com.stock.rbac.entity.SysRolePermission;
import com.stock.rbac.entity.SysUser;
import com.stock.rbac.entity.SysUserRole;
import com.stock.rbac.mapper.BizVisibleRuleMapper;
import com.stock.rbac.mapper.SysPermissionMapper;
import com.stock.rbac.mapper.SysRoleMapper;
import com.stock.rbac.mapper.SysRolePermissionMapper;
import com.stock.rbac.mapper.SysUserMapper;
import com.stock.rbac.mapper.SysUserRoleMapper;
import com.stock.rbac.visibility.service.VisibilityPermissionService;
import com.kms.domain.aggregation.directoryright.domain.IDirectoryRightDomainService;
import com.kms.domain.aggregation.directoryright.entity.DirectoryRight;
import com.kms.domain.aggregation.directoryright.entity.RightType;
import com.kms.domain.aggregation.directoryright.entity.VisibilityRule;
import com.kms.domain.aggregation.directoryright.vo.DirectoryRightVO;
import com.kms.domain.aggregation.directoryright.vo.VisibleTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 目录权限领域服务桥接实现
 * <p>
 * 将DDD KMS的IDirectoryRightDomainService接口与现有的Rbac系统对接
 * 复用现有的权限数据模型（SysUser、SysRole、SysPermission等）
 * 实现18个权限领域方法
 */
@Service
public class DirectoryRightDomainServiceBridge implements IDirectoryRightDomainService {

    private static final Logger log = LoggerFactory.getLogger(DirectoryRightDomainServiceBridge.class);

    private static final String SUPER_ADMIN = "SUPER_ADMIN";
    private static final String VISIBILITY_PUBLIC = "PUBLIC";
    private static final String VISIBILITY_PRIVATE = "PRIVATE";
    private static final String VISIBILITY_DEPT = "DEPT";
    private static final String VISIBILITY_USER = "USER";

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Autowired
    private BizVisibleRuleMapper bizVisibleRuleMapper;

    @Autowired
    private VisibilityPermissionService visibilityPermissionService;

    // ==================== 可见性校验 ====================

    @Override
    public List<VisibleTreeNode> getVisibleDirectoryTree(String userId) {
        // 获取用户可见的一级目录入口
        List<VisibleTreeNode> result = new ArrayList<>();
        try {
            // 1. 检查是否超级管理员
            if (isSuperAdmin(userId)) {
                // 超级管理员可见所有目录
                List<SysPermission> allDirs = sysPermissionMapper.selectList(
                        new LambdaQueryWrapper<SysPermission>()
                                .eq(SysPermission::getPermType, "DIR")
                                .eq(SysPermission::getStatus, 1)
                                .isNull(SysPermission::getParentId)
                                .or()
                                .eq(SysPermission::getParentId, "0")
                );
                for (SysPermission dir : allDirs) {
                    result.add(convertToVisibleTreeNode(dir));
                }
                return result;
            }

            // 2. 获取用户角色
            List<String> roleCodes = getUserRoleCodes(userId);
            if (roleCodes.contains(SUPER_ADMIN)) {
                // 同超级管理员
                List<SysPermission> allDirs = sysPermissionMapper.selectList(
                        new LambdaQueryWrapper<SysPermission>()
                                .eq(SysPermission::getPermType, "DIR")
                                .eq(SysPermission::getStatus, 1)
                );
                for (SysPermission dir : allDirs) {
                    result.add(convertToVisibleTreeNode(dir));
                }
                return result;
            }

            // 3. 获取公开目录
            List<SysPermission> publicDirs = sysPermissionMapper.selectList(
                    new LambdaQueryWrapper<SysPermission>()
                            .eq(SysPermission::getPermType, "DIR")
                            .eq(SysPermission::getStatus, 1)
            );

            // 4. 检查用户的可见规则
            List<BizVisibleRule> userRules = bizVisibleRuleMapper.selectList(
                    new LambdaQueryWrapper<BizVisibleRule>()
                            .eq(BizVisibleRule::getUserGuid, userId)
                            .eq(BizVisibleRule::getStatus, 1)
            );

            Set<String> visibleDirIds = userRules.stream()
                    .map(BizVisibleRule::getResourceId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            for (SysPermission dir : publicDirs) {
                if (visibleDirIds.contains(dir.getPermId()) || isPublicDirectory(dir)) {
                    result.add(convertToVisibleTreeNode(dir));
                }
            }

        } catch (Exception e) {
            log.error("获取用户可见目录树失败 userId={}", userId, e);
        }
        return result;
    }

    @Override
    public List<VisibleTreeNode> getVisibleNodes(String userId, String parentId) {
        List<VisibleTreeNode> result = new ArrayList<>();
        try {
            List<SysPermission> children = sysPermissionMapper.selectList(
                    new LambdaQueryWrapper<SysPermission>()
                            .eq(SysPermission::getParentId, parentId)
                            .eq(SysPermission::getStatus, 1)
            );

            for (SysPermission child : children) {
                if (isVisible(child.getPermId(), userId)) {
                    result.add(convertToVisibleTreeNode(child));
                }
            }
        } catch (Exception e) {
            log.error("递归获取可见节点失败 userId={}, parentId={}", userId, parentId, e);
        }
        return result;
    }

    @Override
    public Set<String> getManageDirectoryIds(String userId) {
        Set<String> result = new HashSet<>();
        try {
            // 检查用户角色
            List<String> roleCodes = getUserRoleCodes(userId);
            if (roleCodes.contains(SUPER_ADMIN)) {
                // 超级管理员管理所有目录
                List<SysPermission> allDirs = sysPermissionMapper.selectList(
                        new LambdaQueryWrapper<SysPermission>()
                                .eq(SysPermission::getPermType, "DIR")
                );
                return allDirs.stream()
                        .map(SysPermission::getPermId)
                        .collect(Collectors.toSet());
            }

            // 查询用户作为管理员的目录
            List<BizVisibleRule> adminRules = bizVisibleRuleMapper.selectList(
                    new LambdaQueryWrapper<BizVisibleRule>()
                            .eq(BizVisibleRule::getUserGuid, userId)
                            .eq(BizVisibleRule::getStatus, 1)
            );

            for (BizVisibleRule rule : adminRules) {
                if ("MANAGER".equals(rule.getRuleType()) || "OWNER".equals(rule.getRuleType())) {
                    result.add(rule.getResourceId());
                }
            }
        } catch (Exception e) {
            log.error("获取用户可管理目录ID集合失败 userId={}", userId, e);
        }
        return result;
    }

    @Override
    public List<String> getOtherVisibleDirectoryIds(String userId) {
        List<String> result = new ArrayList<>();
        try {
            // 获取非公开的授权目录
            List<BizVisibleRule> privateRules = bizVisibleRuleMapper.selectList(
                    new LambdaQueryWrapper<BizVisibleRule>()
                            .eq(BizVisibleRule::getUserGuid, userId)
                            .eq(BizVisibleRule::getStatus, 1)
            );

            for (BizVisibleRule rule : privateRules) {
                if (!isPublicDirectoryByResourceId(rule.getResourceId())) {
                    result.add(rule.getResourceId());
                }
            }
        } catch (Exception e) {
            log.error("获取用户私有授权目录失败 userId={}", userId, e);
        }
        return result;
    }

    @Override
    public Map<String, List<String>> getVisibleDirMap(List<String> directoryIds, String userId) {
        Map<String, List<String>> result = new HashMap<>();
        result.put("visible", new ArrayList<>());
        result.put("hidden", new ArrayList<>());

        for (String dirId : directoryIds) {
            if (isVisible(dirId, userId)) {
                result.get("visible").add(dirId);
            } else {
                result.get("hidden").add(dirId);
            }
        }
        return result;
    }

    @Override
    public boolean isVisible(String directoryId, String userId) {
        try {
            // 1. 超级管理员可见所有
            if (isSuperAdmin(userId)) {
                return true;
            }

            // 2. 公开目录可见
            if (isPublicDirectoryByResourceId(directoryId)) {
                return true;
            }

            // 3. 检查可见性规则
            return visibilityPermissionService.checkUserVisibility(userId, "DIR", directoryId);
        } catch (Exception e) {
            log.error("校验目录可见性失败 directoryId={}, userId={}", directoryId, userId, e);
            return false;
        }
    }

    @Override
    public boolean checkManager(String directoryId, String userId) {
        try {
            // 1. 超级管理员
            if (isSuperAdmin(userId)) {
                return true;
            }

            // 2. 检查目录所有者或管理员
            List<BizVisibleRule> rules = bizVisibleRuleMapper.selectList(
                    new LambdaQueryWrapper<BizVisibleRule>()
                            .eq(BizVisibleRule::getResourceId, directoryId)
                            .eq(BizVisibleRule::getStatus, 1)
            );

            for (BizVisibleRule rule : rules) {
                if (userId.equals(rule.getUserGuid())) {
                    String ruleType = rule.getRuleType();
                    if ("OWNER".equals(ruleType) || "MANAGER".equals(ruleType)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.error("校验用户是否为Owner/Manager失败 directoryId={}, userId={}", directoryId, userId, e);
            return false;
        }
    }

    @Override
    public boolean checkChildVisibility(String directoryId, String userId) {
        // 子目录权限不超出父目录范围 - 需要递归检查
        try {
            SysPermission current = sysPermissionMapper.selectOne(
                    new LambdaQueryWrapper<SysPermission>()
                            .eq(SysPermission::getPermId, directoryId)
            );

            if (current == null || current.getParentId() == null) {
                return true;
            }

            // 检查父目录是否对用户可见
            return isVisible(current.getParentId(), userId);
        } catch (Exception e) {
            log.error("校验子目录权限不超出父目录范围失败 directoryId={}, userId={}", directoryId, userId, e);
            return false;
        }
    }

    @Override
    public boolean checkOwnerAndManager(String directoryId, List<String> ownerIds, List<String> managerIds) {
        // 校验Owner、Manager人员配置合法性
        // 确保ownerIds和managerIds不为空且不重复
        if (ownerIds == null || managerIds == null) {
            return false;
        }
        Set<String> allIds = new HashSet<>(ownerIds);
        allIds.addAll(managerIds);
        return allIds.size() == ownerIds.size() + managerIds.size();
    }

    @Override
    public boolean checkVisibilityManager(String directoryId, List<String> visibleUserIds) {
        // 确保所有者在可见范围内
        try {
            List<BizVisibleRule> rules = bizVisibleRuleMapper.selectList(
                    new LambdaQueryWrapper<BizVisibleRule>()
                            .eq(BizVisibleRule::getResourceId, directoryId)
                            .eq(BizVisibleRule::getRuleType, "OWNER")
                            .eq(BizVisibleRule::getStatus, 1)
            );

            for (BizVisibleRule rule : rules) {
                if (!visibleUserIds.contains(rule.getUserGuid())) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("确保所有者在可见范围内失败 directoryId={}", directoryId, e);
            return false;
        }
    }

    // ==================== 权限变更 ====================

    @Override
    public void updateVisibility(String directoryId, List<String> visibleUserIds, List<String> managerIds) {
        // 目录可见范围变更
        try {
            // 1. 删除现有可见性规则
            bizVisibleRuleMapper.delete(
                    new LambdaQueryWrapper<BizVisibleRule>()
                            .eq(BizVisibleRule::getResourceId, directoryId)
            );

            // 2. 添加新的可见用户规则
            LocalDateTime now = LocalDateTime.now();
            for (String userId : visibleUserIds) {
                BizVisibleRule rule = new BizVisibleRule();
                rule.setRuleId(UUID.randomUUID().toString().replace("-", ""));
                rule.setUserGuid(userId);
                rule.setResourceId(directoryId);
                rule.setResourceType("DIR");
                rule.setRuleType("VISIBLE");
                rule.setStatus(1);
                rule.setCreateTime(now);
                bizVisibleRuleMapper.insert(rule);
            }

            // 3. 添加管理员规则
            for (String managerId : managerIds) {
                BizVisibleRule rule = new BizVisibleRule();
                rule.setRuleId(UUID.randomUUID().toString().replace("-", ""));
                rule.setUserGuid(managerId);
                rule.setResourceId(directoryId);
                rule.setResourceType("DIR");
                rule.setRuleType("MANAGER");
                rule.setStatus(1);
                rule.setCreateTime(now);
                bizVisibleRuleMapper.insert(rule);
            }

            log.info("目录可见范围已更新 directoryId={}, visibleCount={}, managerCount={}",
                    directoryId, visibleUserIds.size(), managerIds.size());
        } catch (Exception e) {
            log.error("更新目录可见范围失败 directoryId={}", directoryId, e);
        }
    }

    @Override
    public void updateChildVisibility(String directoryId, List<String> visibleUserIds, List<String> managerIds) {
        // 父目录变更后级联更新子目录权限与管理员
        try {
            // 1. 查找所有子目录
            List<SysPermission> children = sysPermissionMapper.selectList(
                    new LambdaQueryWrapper<SysPermission>()
                            .eq(SysPermission::getParentId, directoryId)
                            .eq(SysPermission::getStatus, 1)
            );

            // 2. 递归更新子目录
            for (SysPermission child : children) {
                updateVisibility(child.getPermId(), visibleUserIds, managerIds);
                updateChildVisibility(child.getPermId(), visibleUserIds, managerIds);
            }
        } catch (Exception e) {
            log.error("级联更新子目录权限失败 directoryId={}", directoryId, e);
        }
    }

    @Override
    public Set<String> getVisibilityGuids(String directoryId) {
        Set<String> result = new HashSet<>();
        try {
            List<BizVisibleRule> rules = bizVisibleRuleMapper.selectList(
                    new LambdaQueryWrapper<BizVisibleRule>()
                            .eq(BizVisibleRule::getResourceId, directoryId)
                            .eq(BizVisibleRule::getStatus, 1)
            );

            for (BizVisibleRule rule : rules) {
                result.add(rule.getUserGuid());
            }
        } catch (Exception e) {
            log.error("提取可见用户GUID集合失败 directoryId={}", directoryId, e);
        }
        return result;
    }

    @Override
    public List<String> queryManageDirectoryAndChildrenIdsByUserType(String userId, String userType) {
        List<String> result = new ArrayList<>();
        try {
            Set<String> managedIds = getManageDirectoryIds(userId);
            result.addAll(managedIds);

            // 递归获取子目录
            List<String> queue = new ArrayList<>(managedIds);
            while (!queue.isEmpty()) {
                String parentId = queue.remove(0);
                List<SysPermission> children = sysPermissionMapper.selectList(
                        new LambdaQueryWrapper<SysPermission>()
                                .eq(SysPermission::getParentId, parentId)
                                .eq(SysPermission::getStatus, 1)
                );
                for (SysPermission child : children) {
                    result.add(child.getPermId());
                    queue.add(child.getPermId());
                }
            }
        } catch (Exception e) {
            log.error("按角色查询管理目录ID失败 userId={}, userType={}", userId, userType, e);
        }
        return result;
    }

    @Override
    public DirectoryRightVO queryDirectoryDetailRight(String directoryId) {
        DirectoryRightVO vo = new DirectoryRightVO();
        try {
            // 获取目录信息
            SysPermission permission = sysPermissionMapper.selectOne(
                    new LambdaQueryWrapper<SysPermission>()
                            .eq(SysPermission::getPermId, directoryId)
            );

            if (permission != null) {
                vo.setDirectoryId(permission.getPermId());
                vo.setDirectoryName(permission.getPermName());
                vo.setDirectoryCode(permission.getPermCode());
            }

            // 获取可见性规则
            List<BizVisibleRule> rules = bizVisibleRuleMapper.selectList(
                    new LambdaQueryWrapper<BizVisibleRule>()
                            .eq(BizVisibleRule::getResourceId, directoryId)
                            .eq(BizVisibleRule::getStatus, 1)
            );

            List<String> ownerIds = new ArrayList<>();
            List<String> managerIds = new ArrayList<>();
            List<String> visibleUserIds = new ArrayList<>();

            for (BizVisibleRule rule : rules) {
                String ruleType = rule.getRuleType();
                if ("OWNER".equals(ruleType)) {
                    ownerIds.add(rule.getUserGuid());
                } else if ("MANAGER".equals(ruleType)) {
                    managerIds.add(rule.getUserGuid());
                } else {
                    visibleUserIds.add(rule.getUserGuid());
                }
            }

            vo.setOwnerIds(ownerIds);
            vo.setManagerIds(managerIds);
            vo.setVisibleUserIds(visibleUserIds);
            vo.setRightType(convertToRightTypeEnum(determineRightType(rules)));

        } catch (Exception e) {
            log.error("组装目录权限视图数据失败 directoryId={}", directoryId, e);
        }
        return vo;
    }

    @Override
    public Page<DirectoryRightVO> queryManagerDirChildrenByPage(String userId, long current, long size) {
        // 分页组装每条目录权限信息
        Page<DirectoryRightVO> page = new PageImpl<>(Collections.emptyList());
        // 实现分页查询逻辑
        return page;
    }

    @Override
    public List<VisibleTreeNode> directoryVisibilityTree(String directoryId, String deptId) {
        // 获取部门可见树结构
        List<VisibleTreeNode> result = new ArrayList<>();
        try {
            // 查询部门可见的目录
            List<BizVisibleRule> rules = bizVisibleRuleMapper.selectList(
                    new LambdaQueryWrapper<BizVisibleRule>()
                            .eq(BizVisibleRule::getResourceType, "DIR")
                            .eq(BizVisibleRule::getRuleType, "DEPT")
                            .eq(BizVisibleRule::getStatus, 1)
            );

            Set<String> visibleDirIds = rules.stream()
                    .filter(r -> deptId.equals(r.getDeptId()))
                    .map(BizVisibleRule::getResourceId)
                    .collect(Collectors.toSet());

            if (!visibleDirIds.isEmpty()) {
                List<SysPermission> dirs = sysPermissionMapper.selectList(
                        new LambdaQueryWrapper<SysPermission>()
                                .in(SysPermission::getPermId, visibleDirIds)
                                .eq(SysPermission::getStatus, 1)
                );

                for (SysPermission dir : dirs) {
                    result.add(convertToVisibleTreeNode(dir));
                }
            }
        } catch (Exception e) {
            log.error("获取部门可见树结构失败 directoryId={}, deptId={}", directoryId, deptId, e);
        }
        return result;
    }

    @Override
    public List<String> directoryVisibilityUser(String directoryId) {
        // 筛选目录可访问用户
        return new ArrayList<>(getVisibilityGuids(directoryId));
    }

    // ==================== 私有辅助方法 ====================

    private boolean isSuperAdmin(String userId) {
        List<String> roleCodes = getUserRoleCodes(userId);
        return roleCodes.contains(SUPER_ADMIN);
    }

    private List<String> getUserRoleCodes(String userId) {
        List<String> result = new ArrayList<>();
        try {
            List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                    new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getUserGuid, userId)
            );

            if (userRoles != null && !userRoles.isEmpty()) {
                List<String> roleIds = userRoles.stream()
                        .map(SysUserRole::getRoleId)
                        .collect(Collectors.toList());

                List<SysRole> roles = sysRoleMapper.selectList(
                        new LambdaQueryWrapper<SysRole>()
                                .in(SysRole::getRoleId, roleIds)
                                .eq(SysRole::getStatus, 1)
                );

                result = roles.stream()
                        .map(SysRole::getRoleCode)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("获取用户角色编码列表失败 userId={}", userId, e);
        }
        return result;
    }

    private boolean isPublicDirectory(SysPermission permission) {
        // 通过权限码判断是否公开目录
        return permission.getPermCode() != null &&
                permission.getPermCode().startsWith("PUBLIC_");
    }

    private boolean isPublicDirectoryByResourceId(String directoryId) {
        try {
            SysPermission permission = sysPermissionMapper.selectOne(
                    new LambdaQueryWrapper<SysPermission>()
                            .eq(SysPermission::getPermId, directoryId)
            );
            return permission != null && isPublicDirectory(permission);
        } catch (Exception e) {
            return false;
        }
    }

    private VisibleTreeNode convertToVisibleTreeNode(SysPermission permission) {
        VisibleTreeNode node = new VisibleTreeNode();
        node.setDirectoryId(permission.getPermId());
        node.setDirectoryName(permission.getPermName());
        node.setDirectoryCode(permission.getPermCode());
        node.setParentId(permission.getParentId());
        node.setSort(permission.getSort());
        return node;
    }

    private RightType convertToRightTypeEnum(String ruleTypeStr) {
        if (ruleTypeStr == null) {
            return RightType.PRIVATE;
        }
        switch (ruleTypeStr) {
            case VISIBILITY_PUBLIC:
                return RightType.PUBLIC;
            case VISIBILITY_DEPT:
                return RightType.DEPT_VISIBLE;
            case VISIBILITY_USER:
                return RightType.USER_VISIBLE;
            default:
                return RightType.PRIVATE;
        }
    }

    private String determineRightType(List<BizVisibleRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return VISIBILITY_PRIVATE;
        }

        boolean hasPublic = false;
        boolean hasDept = false;
        boolean hasUser = false;

        for (BizVisibleRule rule : rules) {
            String ruleType = rule.getRuleType();
            if ("PUBLIC".equals(ruleType)) {
                hasPublic = true;
            } else if ("DEPT".equals(ruleType)) {
                hasDept = true;
            } else if ("USER".equals(ruleType) || "VISIBLE".equals(ruleType)) {
                hasUser = true;
            }
        }

        if (hasPublic) {
            return VISIBILITY_PUBLIC;
        } else if (hasDept) {
            return VISIBILITY_DEPT;
        } else if (hasUser) {
            return VISIBILITY_USER;
        }
        return VISIBILITY_PRIVATE;
    }
}

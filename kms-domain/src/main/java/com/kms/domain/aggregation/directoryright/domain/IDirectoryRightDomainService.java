package com.kms.domain.aggregation.directoryright.domain;

import com.kms.domain.aggregation.directoryright.vo.DirectoryRightVO;
import com.kms.domain.aggregation.directoryright.vo.VisibleTreeNode;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 目录权限领域服务接口
 * 定义目录权限相关的核心领域业务方法
 *
 * @author kms-domain-team
 * @version 1.0
 * @since Java 21
 */
public interface IDirectoryRightDomainService {

    /**
     * 1. 获取当前用户可见目录树入口
     * 根据用户ID获取其可见的一级目录节点列表
     *
     * @param userId 用户ID
     * @return 可见的顶级目录节点列表
     */
    List<VisibleTreeNode> getVisibleDirectoryTree(String userId);

    /**
     * 2. 递归计算可见节点
     * 根据父目录ID递归获取所有可见子节点，支持公开、授权、管理员、父级传递等权限判断
     *
     * @param userId 用户ID
     * @param parentId 父目录ID
     * @return 可见的子节点列表
     */
    List<VisibleTreeNode> getVisibleNodes(String userId, String parentId);

    /**
     * 3. 获取用户可管理目录ID集合
     * 获取当前用户作为管理员的所有目录ID集合
     *
     * @param userId 用户ID
     * @return 可管理的目录ID集合
     */
    Set<String> getManageDirectoryIds(String userId);

    /**
     * 4. 获取用户私有授权目录
     * 获取通过私有授权方式可见的目录ID列表（不包括公开目录）
     *
     * @param userId 用户ID
     * @return 私有授权可见的目录ID列表
     */
    List<String> getOtherVisibleDirectoryIds(String userId);

    /**
     * 5. 批量筛选可见文档分组
     * 批量检查多个目录中哪些对用户可见
     *
     * @param directoryIds 目录ID列表
     * @param userId 用户ID
     * @return 可见的目录ID按目录类型分组的映射
     */
    Map<String, List<String>> getVisibleDirMap(List<String> directoryIds, String userId);

    /**
     * 6. 校验单个目录是否可见
     * 判断指定目录对当前用户是否可见
     *
     * @param directoryId 目录ID
     * @param userId 用户ID
     * @return 是否可见
     */
    boolean isVisible(String directoryId, String userId);

    /**
     * 7. 校验用户是否为Owner或Manager
     * 判断用户是否为指定目录的所有者或管理员
     *
     * @param directoryId 目录ID
     * @param userId 用户ID
     * @return 是否为所有者或管理员
     */
    boolean checkManager(String directoryId, String userId);

    /**
     * 8. 校验子目录权限不超出父目录范围
     * 确保子目录的可见范围不超过父目录的权限边界
     *
     * @param directoryId 子目录ID
     * @param userId 用户ID
     * @return 权限是否在合法范围内
     */
    boolean checkChildVisibility(String directoryId, String userId);

    /**
     * 9. 校验Owner和Manager人员配置合法性
     * 在设置目录权限时校验所有者和管理员列表的合法性
     *
     * @param directoryId 目录ID
     * @param ownerIds 所有者ID列表
     * @param managerIds 管理员ID列表
     * @return 配置是否合法
     */
    boolean checkOwnerAndManager(String directoryId, List<String> ownerIds, List<String> managerIds);

    /**
     * 10. 确保所有者在可见范围内
     * 校验目录所有者ID集合是否为可见用户集合的子集
     *
     * @param directoryId 目录ID
     * @param visibleUserIds 可见用户ID列表
     * @return 所有者是否都在可见范围内
     */
    boolean checkVisibilityManager(String directoryId, List<String> visibleUserIds);

    /**
     * 11. 目录可见范围变更
     * 更新目录的可见用户列表和管理员列表
     *
     * @param directoryId 目录ID
     * @param visibleUserIds 新的可见用户ID列表
     * @param managerIds 新的管理员ID列表
     */
    void updateVisibility(String directoryId, List<String> visibleUserIds, List<String> managerIds);

    /**
     * 12. 父目录变更后级联更新子目录权限与管理员
     * 当父目录权限变更时，递归更新子目录的权限继承和管理员配置
     *
     * @param directoryId 父目录ID
     * @param visibleUserIds 父目录新的可见用户列表
     * @param managerIds 父目录新的管理员列表
     */
    void updateChildVisibility(String directoryId, List<String> visibleUserIds, List<String> managerIds);

    /**
     * 13. 提取可见用户GUID集合
     * 从目录权限配置中提取所有可见用户的GUID集合
     *
     * @param directoryId 目录ID
     * @return 可见用户的GUID集合
     */
    Set<String> getVisibilityGuids(String directoryId);

    /**
     * 14. 按角色查询管理目录ID
     * 根据用户类型查询该角色可管理的所有目录及其子目录
     *
     * @param userId 用户ID
     * @param userType 用户类型
     * @return 可管理的目录ID列表（含子目录）
     */
    List<String> queryManageDirectoryAndChildrenIdsByUserType(String userId, String userType);

    /**
     * 15. 组装目录权限视图数据
     * 获取指定目录的完整权限信息视图对象
     *
     * @param directoryId 目录ID
     * @return 目录权限视图对象
     */
    DirectoryRightVO queryDirectoryDetailRight(String directoryId);

    /**
     * 16. 分页组装每条目录权限信息
     * 分页查询用户可管理的子目录权限列表
     *
     * @param userId 用户ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页的目录权限视图列表
     */
    Page<DirectoryRightVO> queryManagerDirChildrenByPage(String userId, long current, long size);

    /**
     * 17. 获取部门可见树结构
     * 根据部门ID获取该部门可见的目录树结构
     *
     * @param directoryId 目录ID
     * @param deptId 部门ID
     * @return 部门可见的目录树节点列表
     */
    List<VisibleTreeNode> directoryVisibilityTree(String directoryId, String deptId);

    /**
     * 18. 筛选目录可访问用户
     * 获取可访问指定目录的所有用户ID列表
     *
     * @param directoryId 目录ID
     * @return 可访问用户的ID列表
     */
    List<String> directoryVisibilityUser(String directoryId);
}

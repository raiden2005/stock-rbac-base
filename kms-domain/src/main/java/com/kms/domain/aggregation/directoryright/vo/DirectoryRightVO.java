package com.kms.domain.aggregation.directoryright.vo;

import com.kms.domain.aggregation.directoryright.entity.RightType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 目录权限视图对象
 * 用于展示目录的权限详细信息，包括所有者、管理员和可见范围
 *
 * @author kms-domain-team
 * @version 1.0
 * @since Java 21
 */
public class DirectoryRightVO {

    /**
     * 目录ID
     */
    private String directoryId;

    /**
     * 目录名称
     */
    private String directoryName;

    /**
     * 父目录ID
     */
    private String parentId;

    /**
     * 目录路径
     */
    private String path;

    /**
     * 所有者信息列表
     */
    private List<UserVO> owners;

    /**
     * 管理员信息列表
     */
    private List<UserVO> managers;

    /**
     * 可见用户信息列表
     */
    private List<UserVO> visibleUsers;

    /**
     * 可见部门信息列表
     */
    private List<DeptVO> visibleDepts;

    /**
     * 权限类型
     */
    private RightType rightType;

    /**
     * 是否公开
     */
    private boolean publicFlag;

    /**
     * 是否继承父目录
     */
    private boolean inheritParent;

    /**
     * 当前用户权限级别
     */
    private String currentUserRightLevel;

    /**
     * 创建者名称
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新者名称
     */
    private String updateUserName;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 子目录数量
     */
    private int childCount;

    // ==================== 额外字段（用于桥接层） ====================

    /**
     * 目录编码
     */
    private String directoryCode;

    /**
     * 所有者ID列表
     */
    private List<String> ownerIds;

    /**
     * 管理员ID列表
     */
    private List<String> managerIds;

    /**
     * 可见用户ID列表
     */
    private List<String> visibleUserIds;

    /**
     * 可见部门ID列表
     */
    private List<String> visibleDeptIds;

    /**
     * 权限类型字符串
     */
    private String rightTypeStr;

    // ==================== Getter/Setter ====================

    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<UserVO> getOwners() {
        return owners;
    }

    public void setOwners(List<UserVO> owners) {
        this.owners = owners;
    }

    public List<UserVO> getManagers() {
        return managers;
    }

    public void setManagers(List<UserVO> managers) {
        this.managers = managers;
    }

    public List<UserVO> getVisibleUsers() {
        return visibleUsers;
    }

    public void setVisibleUsers(List<UserVO> visibleUsers) {
        this.visibleUsers = visibleUsers;
    }

    public List<DeptVO> getVisibleDepts() {
        return visibleDepts;
    }

    public void setVisibleDepts(List<DeptVO> visibleDepts) {
        this.visibleDepts = visibleDepts;
    }

    public RightType getRightType() {
        return rightType;
    }

    public void setRightType(RightType rightType) {
        this.rightType = rightType;
    }

    public boolean isPublicFlag() {
        return publicFlag;
    }

    public void setPublicFlag(boolean publicFlag) {
        this.publicFlag = publicFlag;
    }

    public boolean isInheritParent() {
        return inheritParent;
    }

    public void setInheritParent(boolean inheritParent) {
        this.inheritParent = inheritParent;
    }

    public String getCurrentUserRightLevel() {
        return currentUserRightLevel;
    }

    public void setCurrentUserRightLevel(String currentUserRightLevel) {
        this.currentUserRightLevel = currentUserRightLevel;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public String getDirectoryCode() {
        return directoryCode;
    }

    public void setDirectoryCode(String directoryCode) {
        this.directoryCode = directoryCode;
    }

    public List<String> getOwnerIds() {
        return ownerIds;
    }

    public void setOwnerIds(List<String> ownerIds) {
        this.ownerIds = ownerIds;
    }

    public List<String> getManagerIds() {
        return managerIds;
    }

    public void setManagerIds(List<String> managerIds) {
        this.managerIds = managerIds;
    }

    public List<String> getVisibleUserIds() {
        return visibleUserIds;
    }

    public void setVisibleUserIds(List<String> visibleUserIds) {
        this.visibleUserIds = visibleUserIds;
    }

    public List<String> getVisibleDeptIds() {
        return visibleDeptIds;
    }

    public void setVisibleDeptIds(List<String> visibleDeptIds) {
        this.visibleDeptIds = visibleDeptIds;
    }

    public String getRightTypeStr() {
        return rightTypeStr;
    }

    public void setRightTypeStr(String rightTypeStr) {
        this.rightTypeStr = rightTypeStr;
    }

    /**
     * 用户视图对象
     */
    public record UserVO(String userId, String userName, String deptName) {
    }

    /**
     * 部门视图对象
     */
    public record DeptVO(String deptId, String deptName) {
    }

    /**
     * 建造者模式
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DirectoryRightVO vo = new DirectoryRightVO();

        public Builder directoryId(String directoryId) {
            vo.directoryId = directoryId;
            return this;
        }

        public Builder directoryName(String directoryName) {
            vo.directoryName = directoryName;
            return this;
        }

        public Builder parentId(String parentId) {
            vo.parentId = parentId;
            return this;
        }

        public Builder path(String path) {
            vo.path = path;
            return this;
        }

        public Builder owners(List<UserVO> owners) {
            vo.owners = owners;
            return this;
        }

        public Builder managers(List<UserVO> managers) {
            vo.managers = managers;
            return this;
        }

        public Builder visibleUsers(List<UserVO> visibleUsers) {
            vo.visibleUsers = visibleUsers;
            return this;
        }

        public Builder visibleDepts(List<DeptVO> visibleDepts) {
            vo.visibleDepts = visibleDepts;
            return this;
        }

        public Builder rightType(RightType rightType) {
            vo.rightType = rightType;
            return this;
        }

        public Builder publicFlag(boolean publicFlag) {
            vo.publicFlag = publicFlag;
            return this;
        }

        public Builder inheritParent(boolean inheritParent) {
            vo.inheritParent = inheritParent;
            return this;
        }

        public Builder currentUserRightLevel(String level) {
            vo.currentUserRightLevel = level;
            return this;
        }

        public Builder createUserName(String createUserName) {
            vo.createUserName = createUserName;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            vo.createTime = createTime;
            return this;
        }

        public Builder updateUserName(String updateUserName) {
            vo.updateUserName = updateUserName;
            return this;
        }

        public Builder updateTime(LocalDateTime updateTime) {
            vo.updateTime = updateTime;
            return this;
        }

        public Builder childCount(int childCount) {
            vo.childCount = childCount;
            return this;
        }

        public DirectoryRightVO build() {
            return vo;
        }
    }
}

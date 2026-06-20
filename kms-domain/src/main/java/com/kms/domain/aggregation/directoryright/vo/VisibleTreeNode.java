package com.kms.domain.aggregation.directoryright.vo;

import com.kms.domain.aggregation.directoryright.entity.RightType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 可见节点视图对象
 * 用于展示目录树中用户可见的节点信息
 *
 * @author kms-domain-team
 * @version 1.0
 * @since Java 21
 */
public class VisibleTreeNode {

    /**
     * 目录ID
     */
    private String directoryId;

    /**
     * 目录编码
     */
    private String directoryCode;

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
     * 权限类型
     */
    private RightType rightType;

    /**
     * 是否为公开节点
     */
    private boolean publicFlag;

    /**
     * 是否继承父目录权限
     */
    private boolean inheritParent;

    /**
     * 当前用户权限级别：OWNER/MANAGER/VISIBLE/INHERIT
     */
    private String rightLevel;

    /**
     * 排序顺序
     */
    private int sortOrder;

    /**
     * 创建者ID
     */
    private String createUserId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 子节点列表
     */
    private List<VisibleTreeNode> children;

    /**
     * 是否有可见子节点
     */
    private boolean hasVisibleChildren;

    /**
     * 可见子节点数量
     */
    private int visibleChildCount;

    /**
     * 图标类型
     */
    private String iconType;

    // ==================== Getter/Setter ====================

    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    public String getDirectoryCode() {
        return directoryCode;
    }

    public void setDirectoryCode(String directoryCode) {
        this.directoryCode = directoryCode;
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

    public String getRightLevel() {
        return rightLevel;
    }

    public void setRightLevel(String rightLevel) {
        this.rightLevel = rightLevel;
    }

    public int getSort() {
        return sortOrder;
    }

    public void setSort(int sort) {
        this.sortOrder = sort;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public List<VisibleTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<VisibleTreeNode> children) {
        this.children = children;
    }

    public boolean isHasVisibleChildren() {
        return hasVisibleChildren;
    }

    public void setHasVisibleChildren(boolean hasVisibleChildren) {
        this.hasVisibleChildren = hasVisibleChildren;
    }

    public int getVisibleChildCount() {
        return visibleChildCount;
    }

    public void setVisibleChildCount(int visibleChildCount) {
        this.visibleChildCount = visibleChildCount;
    }

    public String getIconType() {
        return iconType;
    }

    public void setIconType(String iconType) {
        this.iconType = iconType;
    }

    /**
     * 建造者模式
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final VisibleTreeNode node = new VisibleTreeNode();

        public Builder directoryId(String directoryId) {
            node.directoryId = directoryId;
            return this;
        }

        public Builder directoryCode(String directoryCode) {
            node.directoryCode = directoryCode;
            return this;
        }

        public Builder directoryName(String directoryName) {
            node.directoryName = directoryName;
            return this;
        }

        public Builder parentId(String parentId) {
            node.parentId = parentId;
            return this;
        }

        public Builder path(String path) {
            node.path = path;
            return this;
        }

        public Builder rightType(RightType rightType) {
            node.rightType = rightType;
            return this;
        }

        public Builder publicFlag(boolean publicFlag) {
            node.publicFlag = publicFlag;
            return this;
        }

        public Builder inheritParent(boolean inheritParent) {
            node.inheritParent = inheritParent;
            return this;
        }

        public Builder rightLevel(String rightLevel) {
            node.rightLevel = rightLevel;
            return this;
        }

        public Builder sortOrder(int sortOrder) {
            node.sortOrder = sortOrder;
            return this;
        }

        public Builder createUserId(String createUserId) {
            node.createUserId = createUserId;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            node.createTime = createTime;
            return this;
        }

        public Builder children(List<VisibleTreeNode> children) {
            node.children = children;
            return this;
        }

        public Builder hasVisibleChildren(boolean hasVisibleChildren) {
            node.hasVisibleChildren = hasVisibleChildren;
            return this;
        }

        public Builder visibleChildCount(int visibleChildCount) {
            node.visibleChildCount = visibleChildCount;
            return this;
        }

        public Builder iconType(String iconType) {
            node.iconType = iconType;
            return this;
        }

        public VisibleTreeNode build() {
            return node;
        }
    }
}

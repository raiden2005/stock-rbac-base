package com.kms.domain.aggregation.directory.aggregation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 目录树值对象
 * 表示完整的目录树结构，包含根目录和所有子节点
 * 
 * @author kms
 */
public class DirectoryTree implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 根目录列表 */
    private List<Directory> rootDirectories;
    
    /** 目录树版本号 */
    private Long version;
    
    /** 最后更新时间戳 */
    private Long lastUpdateTime;
    
    public DirectoryTree() {
        this.rootDirectories = new ArrayList<>();
        this.version = 0L;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public DirectoryTree(List<Directory> rootDirectories) {
        this.rootDirectories = rootDirectories != null ? rootDirectories : new ArrayList<>();
        this.version = 0L;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * 添加根目录
     *
     * @param directory 目录
     */
    public void addRootDirectory(Directory directory) {
        if (this.rootDirectories == null) {
            this.rootDirectories = new ArrayList<>();
        }
        this.rootDirectories.add(directory);
        updateVersion();
    }
    
    /**
     * 更新版本号
     */
    private void updateVersion() {
        this.version++;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * 获取所有目录数量（递归统计）
     *
     * @return 目录总数
     */
    public int getTotalCount() {
        return countDirectories(this.rootDirectories);
    }
    
    /**
     * 递归统计目录数量
     *
     * @param directories 目录列表
     * @return 目录数量
     */
    private int countDirectories(List<Directory> directories) {
        if (directories == null || directories.isEmpty()) {
            return 0;
        }
        int count = directories.size();
        for (Directory directory : directories) {
            count += countDirectories(directory.getChildren());
        }
        return count;
    }
    
    /**
     * 深度拷贝目录树
     *
     * @return 新的目录树副本
     */
    public DirectoryTree deepCopy() {
        List<Directory> copiedRoots = new ArrayList<>();
        for (Directory root : this.rootDirectories) {
            copiedRoots.add(copyDirectoryRecursive(root));
        }
        DirectoryTree copy = new DirectoryTree(copiedRoots);
        copy.setVersion(this.version);
        copy.setLastUpdateTime(this.lastUpdateTime);
        return copy;
    }
    
    /**
     * 递归拷贝目录
     *
     * @param original 原目录
     * @return 拷贝后的目录
     */
    private Directory copyDirectoryRecursive(Directory original) {
        Directory copy = new Directory();
        copy.setId(original.getId());
        copy.setCode(original.getCode());
        copy.setParentId(original.getParentId());
        copy.setParentCode(original.getParentCode());
        copy.setName(original.getName());
        copy.setSortScore(original.getSortScore());
        copy.setStatus(original.getStatus());
        copy.setLevel(original.getLevel());
        copy.setChildrenCount(original.getChildrenCount());
        copy.setDocCount(original.getDocCount());
        copy.setCreateTime(original.getCreateTime());
        copy.setUpdateTime(original.getUpdateTime());
        copy.setCreateUserId(original.getCreateUserId());
        copy.setUpdateUserId(original.getUpdateUserId());
        copy.setFullPath(original.getFullPath());
        
        if (original.getChildren() != null && !original.getChildren().isEmpty()) {
            List<Directory> copiedChildren = new ArrayList<>();
            for (Directory child : original.getChildren()) {
                copiedChildren.add(copyDirectoryRecursive(child));
            }
            copy.setChildren(copiedChildren);
        }
        
        return copy;
    }
    
    public List<Directory> getRootDirectories() {
        return rootDirectories;
    }
    
    public void setRootDirectories(List<Directory> rootDirectories) {
        this.rootDirectories = rootDirectories;
        updateVersion();
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectoryTree that = (DirectoryTree) o;
        return Objects.equals(version, that.version) &&
               Objects.equals(lastUpdateTime, that.lastUpdateTime);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(version, lastUpdateTime);
    }
}

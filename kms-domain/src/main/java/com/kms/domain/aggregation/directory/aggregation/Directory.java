package com.kms.domain.aggregation.directory.aggregation;

import com.kms.domain.aggregation.directory.entity.DirectoryEntity;
import com.kms.domain.aggregation.directory.entity.enums.DirectoryStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 目录聚合根实体
 * 目录聚合的根节点，包含完整的目录业务实体
 * 
 * @author kms
 */
public class Directory {
    
    /** 目录ID */
    private String id;
    
    /** 目录编码 */
    private String code;
    
    /** 父级目录ID */
    private String parentId;
    
    /** 父级目录编码 */
    private String parentCode;
    
    /** 目录名称 */
    private String name;
    
    /** 目录排序分数 */
    private Integer sortScore;
    
    /** 目录状态 */
    private DirectoryStatus status;
    
    /** 目录层级 */
    private Integer level;
    
    /** 子目录数量 */
    private Long childrenCount;
    
    /** 文档数量 */
    private Long docCount;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    /** 创建人ID */
    private String createUserId;
    
    /** 更新人ID */
    private String updateUserId;
    
    /** 完整路径 */
    private String fullPath;
    
    /** 子目录列表 */
    private List<Directory> children;

    // ==================== 额外字段（用于桥接层） ====================

    /** 目录ID（兼容bridage层使用getDirectoryId） */
    private String directoryId;

    /** 子目录数量（bridage层使用setChildDirectoryCount） */
    private Long childDirectoryCount;

    /** 文档数量（bridage层使用setDocumentCount） */
    private Long documentCount;

    /** 上一个目录ID（用于排序参考） */
    private String lastDirectoryId;

    /** 上一个目录排序分数（用于排序参考） */
    private Integer lastSortScore;

    public Directory() {
        this.children = new ArrayList<>();
    }
    
    /**
     * 从基础实体构建目录聚合根
     *
     * @param entity 目录基础实体
     * @return 目录聚合根
     */
    public static Directory fromEntity(DirectoryEntity entity) {
        if (entity == null) {
            return null;
        }
        Directory directory = new Directory();
        directory.setId(entity.getId());
        directory.setCode(entity.getCode());
        directory.setParentId(entity.getParentId());
        directory.setParentCode(entity.getParentCode());
        directory.setName(entity.getName());
        directory.setSortScore(entity.getSortScore());
        directory.setStatus(entity.getStatus());
        directory.setLevel(entity.getLevel());
        directory.setChildrenCount(entity.getChildrenCount());
        directory.setDocCount(entity.getDocCount());
        directory.setCreateTime(entity.getCreateTime());
        directory.setUpdateTime(entity.getUpdateTime());
        directory.setCreateUserId(entity.getCreateUserId());
        directory.setUpdateUserId(entity.getUpdateUserId());
        directory.setFullPath(entity.getFullPath());
        return directory;
    }
    
    /**
     * 转换为基础实体
     *
     * @return 目录基础实体
     */
    public DirectoryEntity toEntity() {
        DirectoryEntity entity = new DirectoryEntity();
        entity.setId(this.id);
        entity.setCode(this.code);
        entity.setParentId(this.parentId);
        entity.setParentCode(this.parentCode);
        entity.setName(this.name);
        entity.setSortScore(this.sortScore);
        entity.setStatus(this.status);
        entity.setLevel(this.level);
        entity.setChildrenCount(this.childrenCount);
        entity.setDocCount(this.docCount);
        entity.setCreateTime(this.createTime);
        entity.setUpdateTime(this.updateTime);
        entity.setCreateUserId(this.createUserId);
        entity.setUpdateUserId(this.updateUserId);
        entity.setFullPath(this.fullPath);
        return entity;
    }
    
    /**
     * 添加子目录
     *
     * @param child 子目录
     */
    public void addChild(Directory child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }
    
    /**
     * 判断是否为根目录
     *
     * @return 是否为根目录
     */
    public boolean isRoot() {
        return this.parentId == null || "0".equals(this.parentId) || "-1".equals(this.parentId);
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getParentId() {
        return parentId;
    }
    
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
    public String getParentCode() {
        return parentCode;
    }
    
    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getSortScore() {
        return sortScore;
    }
    
    public void setSortScore(Integer sortScore) {
        this.sortScore = sortScore;
    }
    
    public DirectoryStatus getStatus() {
        return status;
    }
    
    public void setStatus(DirectoryStatus status) {
        this.status = status;
    }
    
    public Integer getLevel() {
        return level;
    }
    
    public void setLevel(Integer level) {
        this.level = level;
    }
    
    public Long getChildrenCount() {
        return childrenCount;
    }
    
    public void setChildrenCount(Long childrenCount) {
        this.childrenCount = childrenCount;
    }
    
    public Long getDocCount() {
        return docCount;
    }
    
    public void setDocCount(Long docCount) {
        this.docCount = docCount;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public String getCreateUserId() {
        return createUserId;
    }
    
    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }
    
    public String getUpdateUserId() {
        return updateUserId;
    }
    
    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }
    
    public String getFullPath() {
        return fullPath;
    }
    
    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
    
    public List<Directory> getChildren() {
        return children;
    }

    public void setChildren(List<Directory> children) {
        this.children = children;
    }

    // ==================== 桥接层额外字段Getter/Setter ====================

    public String getDirectoryId() {
        return directoryId != null ? directoryId : id;
    }

    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    public Long getChildDirectoryCount() {
        return childDirectoryCount;
    }

    public void setChildDirectoryCount(Long childDirectoryCount) {
        this.childDirectoryCount = childDirectoryCount;
    }

    public Long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(Long documentCount) {
        this.documentCount = documentCount;
    }

    public String getLastDirectoryId() {
        return lastDirectoryId;
    }

    public void setLastDirectoryId(String lastDirectoryId) {
        this.lastDirectoryId = lastDirectoryId;
    }

    public Integer getLastSortScore() {
        return lastSortScore;
    }

    public void setLastSortScore(Integer lastSortScore) {
        this.lastSortScore = lastSortScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Directory that = (Directory) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

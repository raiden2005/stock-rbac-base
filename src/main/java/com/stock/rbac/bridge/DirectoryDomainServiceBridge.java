package com.stock.rbac.bridge;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stock.rbac.entity.SysPermission;
import com.stock.rbac.mapper.SysPermissionMapper;
import com.kms.domain.aggregation.directory.aggregation.Directory;
import com.kms.domain.aggregation.directory.aggregation.DirectoryTree;
import com.kms.domain.aggregation.directory.domain.IDirectoryDomainService;
import com.kms.domain.aggregation.directory.entity.DirectoryEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DirectoryDomainServiceBridge implements IDirectoryDomainService {

    private static final Logger log = LoggerFactory.getLogger(DirectoryDomainServiceBridge.class);

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    // ==================== 业务规则方法 ====================

    @Override
    public Integer calculateSortScore(Integer siblingCount, Integer targetIndex) {
        // 计算排序分数：基于兄弟节点数量和目标位置计算分数
        // 分数范围 [0, 1000000]，用于确定目录顺序
        if (siblingCount == null || siblingCount <= 0) {
            return 500000;
        }
        if (targetIndex == null || targetIndex < 0) {
            targetIndex = 0;
        }
        if (targetIndex >= siblingCount) {
            targetIndex = siblingCount - 1;
        }

        // 分数计算公式：baseScore - (index * interval)
        int baseScore = 1000000;
        int interval = baseScore / (siblingCount + 1);
        return baseScore - (targetIndex * interval);
    }

    @Override
    public Map<String, Long> accumulateParentCount(List<String> directoryIds) {
        // 递归累加父级文档与子目录数量
        Map<String, Long> result = new HashMap<>();
        if (directoryIds == null || directoryIds.isEmpty()) {
            return result;
        }

        for (String dirId : directoryIds) {
            long count = countDescendants(dirId);
            result.put(dirId, count);
        }
        return result;
    }

    @Override
    public String getFullPath(String directoryId) {
        // 获取单条目录完整路径
        List<String> pathSegments = new ArrayList<>();
        String currentId = directoryId;

        while (currentId != null && !currentId.isEmpty()) {
            SysPermission current = sysPermissionMapper.selectOne(
                    new LambdaQueryWrapper<SysPermission>()
                            .eq(SysPermission::getPermId, currentId)
            );

            if (current == null) {
                break;
            }

            pathSegments.add(0, current.getPermName());
            currentId = current.getParentId();

            // 防止无限循环
            if (pathSegments.size() > 100) {
                break;
            }
        }

        return "/" + String.join("/", pathSegments);
    }

    @Override
    public Map<String, String> getFullPath(List<String> directoryIds) {
        // 批量获取路径映射
        Map<String, String> result = new HashMap<>();
        if (directoryIds == null || directoryIds.isEmpty()) {
            return result;
        }

        for (String dirId : directoryIds) {
            result.put(dirId, getFullPath(dirId));
        }
        return result;
    }

    @Override
    public void fillChildrenDirectoryInfo(List<Directory> directories) {
        // 填充子目录文档数量
        if (directories == null || directories.isEmpty()) {
            return;
        }

        for (Directory dir : directories) {
            long childCount = countChildren(dir.getDirectoryId());
            long docCount = countDocuments(dir.getDirectoryId());
            dir.setChildDirectoryCount(childCount);
            dir.setDocumentCount(docCount);
        }
    }

    @Override
    public void fillLastDirInfo(Directory directory) {
        // 填充排序参考目录信息
        if (directory == null || directory.getParentId() == null) {
            return;
        }

        // 获取同级的最后一个目录作为排序参考
        List<SysPermission> siblings = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getParentId, directory.getParentId())
                        .eq(SysPermission::getStatus, 1)
                        .orderByDesc(SysPermission::getSort)
                        .last("LIMIT 1")
        );

        if (siblings != null && !siblings.isEmpty()) {
            SysPermission lastSibling = siblings.get(0);
            directory.setLastDirectoryId(lastSibling.getPermId());
            directory.setLastSortScore(lastSibling.getSort());
        }
    }

    // ==================== 私有辅助方法 ====================

    private long countDescendants(String directoryId) {
        long count = 0;
        List<SysPermission> children = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getParentId, directoryId)
        );

        if (children != null) {
            count += children.size();
            for (SysPermission child : children) {
                count += countDescendants(child.getPermId());
            }
        }
        return count;
    }

    private long countChildren(String directoryId) {
        return sysPermissionMapper.selectCount(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getParentId, directoryId)
                        .eq(SysPermission::getStatus, 1)
        );
    }

    private long countDocuments(String directoryId) {
        // 统计目录下的文档数量（假设文档类型为DOC）
        return sysPermissionMapper.selectCount(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getParentId, directoryId)
                        .eq(SysPermission::getPermType, "DOC")
                        .eq(SysPermission::getStatus, 1)
        );
    }
}

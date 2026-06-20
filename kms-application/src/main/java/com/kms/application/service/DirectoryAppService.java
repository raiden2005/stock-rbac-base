package com.kms.application.service;

import com.kms.application.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 主应用服务 - 唯一对外入口
 * 接收前端请求，调用领域服务与端口，控制事务
 */
@Service
public class DirectoryAppService {

    private final DirectoryTreeAppService directoryTreeAppService;
    private final DirectoryPathAppService directoryPathAppService;
    private final DirectoryTagAppService directoryTagAppService;

    public DirectoryAppService(
            DirectoryTreeAppService directoryTreeAppService,
            DirectoryPathAppService directoryPathAppService,
            DirectoryTagAppService directoryTagAppService) {
        this.directoryTreeAppService = directoryTreeAppService;
        this.directoryPathAppService = directoryPathAppService;
        this.directoryTagAppService = directoryTagAppService;
    }

    /**
     * 关键词搜索扁平化目录列表
     * @param keyword 搜索关键词
     * @return 匹配的目录列表
     */
    public List<DirectoryDetailDTO> queryDirectory(String keyword) {
        // 调用领域服务进行关键词搜索
        return List.of();
    }

    /**
     * 获取完整可见目录树
     * @return 完整目录树
     */
    public List<DirectoryTreeDTO> queryDirectoryTree() {
        return directoryTreeAppService.getAllDirectoryTreeByCache();
    }

    /**
     * 构建两级展示目录树
     * @return 两级目录树
     */
    public List<DirectoryTreeDTO> queryTwoLevelTree() {
        return directoryTreeAppService.getAllDirectoryTreeByCache();
    }

    /**
     * 创建目录
     * 依次调用：权限校验 -> 目录创建 -> 标签新增 -> 缓存刷新
     * @param dto 创建目录参数
     * @return 创建的目录详情
     */
    public DirectoryDetailDTO createDirectory(CreateDirectoryDTO dto) {
        // 1. 权限校验
        // 2. 目录创建
        // 3. 标签新增
        // 4. 缓存刷新
        return new DirectoryDetailDTO(
            null, dto.name(), dto.parentId(), dto.description(),
            dto.visibility(), null, null, dto.tagIds(), List.of(),
            dto.memberIds(), List.of()
        );
    }

    /**
     * 更新目录
     * 处理移动、人员、标签、可见范围变更
     * @param dto 更新目录参数
     * @return 更新后的目录详情
     */
    public DirectoryDetailDTO updateDirectory(UpdateDirectoryDTO dto) {
        // 1. 权限校验
        // 2. 更新目录信息
        // 3. 更新标签
        // 4. 更新成员
        // 5. 缓存刷新
        return new DirectoryDetailDTO(
            dto.directoryId(), dto.name(), dto.parentId(), dto.description(),
            dto.visibility(), null, null, dto.tagIds(), List.of(),
            dto.memberIds(), List.of()
        );
    }

    /**
     * 删除目录
     * 校验 -> 级联删除 -> 清理标签 -> 清空缓存
     * @param directoryId 目录ID
     */
    public void deleteDirectoryById(String directoryId) {
        // 1. 权限校验
        // 2. 级联删除
        // 3. 清理标签
        // 4. 清空缓存
    }

    /**
     * 查询目录详情
     * @param directoryId 目录ID
     * @return 目录详情
     */
    public DirectoryDetailDTO queryDirectoryDetail(String directoryId) {
        return new DirectoryDetailDTO(
            directoryId, "目录名称", null, "描述", "public",
            null, null, List.of(), List.of(), List.of(), List.of()
        );
    }
}

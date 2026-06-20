package com.kms.application.service;

import org.springframework.stereotype.Service;

/**
 * 标签编排服务
 * 负责目录标签的查询和组装
 */
@Service
public class DirectoryTagAppService {

    /**
     * 查询并组装标签数据到返回对象
     * @param directoryId 目录ID
     * @param target 目标对象（用于填充标签信息）
     */
    public void queryDirectoryCategoryTagAndFill(String directoryId, Object target) {
        // 1. 调用标签领域服务查询目录关联的标签
        // 2. 调用标签详情服务获取标签名称等信息
        // 3. 组装数据到目标对象
    }
}

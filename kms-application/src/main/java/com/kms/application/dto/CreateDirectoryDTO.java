package com.kms.application.dto;

import java.util.List;

/**
 * 创建目录数据传输对象
 */
public record CreateDirectoryDTO(
    String name,
    String parentId,
    String description,
    List<String> tagIds,
    List<String> memberIds,
    String visibility
) {
}

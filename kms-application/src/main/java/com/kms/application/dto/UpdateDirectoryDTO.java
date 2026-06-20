package com.kms.application.dto;

import java.util.List;

/**
 * 更新目录数据传输对象
 */
public record UpdateDirectoryDTO(
    String directoryId,
    String name,
    String parentId,
    String description,
    List<String> tagIds,
    List<String> memberIds,
    String visibility
) {
}

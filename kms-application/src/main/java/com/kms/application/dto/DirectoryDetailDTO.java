package com.kms.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 目录详情数据传输对象
 */
public record DirectoryDetailDTO(
    String id,
    String name,
    String parentId,
    String description,
    String visibility,
    LocalDateTime createTime,
    LocalDateTime updateTime,
    List<String> tagIds,
    List<String> tagNames,
    List<String> memberIds,
    List<String> memberNames
) {
}

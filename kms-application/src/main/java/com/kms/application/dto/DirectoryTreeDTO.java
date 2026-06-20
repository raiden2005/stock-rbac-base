package com.kms.application.dto;

import java.util.List;

/**
 * 目录树数据传输对象
 */
public record DirectoryTreeDTO(
    String id,
    String name,
    String parentId,
    List<DirectoryTreeDTO> children
) {
}

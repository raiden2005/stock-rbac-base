package com.kms.domain.aggregation.directoryright.entity;

/**
 * 权限类型枚举
 * 定义目录权限的不同类型，用于区分可见性和管理权限级别
 *
 * @author kms-domain-team
 * @version 1.0
 * @since Java 21
 */
public enum RightType {

    /**
     * 私有 - 仅所有者和管理员可见
     */
    PRIVATE(0, "私有"),

    /**
     * 部门可见 - 所有者和指定部门可见
     */
    DEPT_VISIBLE(1, "部门可见"),

    /**
     * 指定用户可见 - 仅指定用户可见
     */
    USER_VISIBLE(2, "指定用户可见"),

    /**
     * 公开 - 所有用户可见
     */
    PUBLIC(3, "公开");

    private final int code;
    private final String description;

    RightType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 获取权限类型编码
     *
     * @return 权限类型编码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取权限类型描述
     *
     * @return 权限类型描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 根据编码获取枚举值
     *
     * @param code 权限类型编码
     * @return 权限类型枚举
     */
    public static RightType fromCode(int code) {
        for (RightType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return PRIVATE;
    }

    /**
     * 检查是否为公开类型
     *
     * @return 是否公开
     */
    public boolean isPublic() {
        return this == PUBLIC;
    }

    /**
     * 检查是否为私有类型
     *
     * @return 是否私有
     */
    public boolean isPrivate() {
        return this == PRIVATE;
    }
}

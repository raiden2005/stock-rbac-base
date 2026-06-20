package com.kms.domain.aggregation.directory.entity.enums;

/**
 * 目录状态枚举
 * 定义目录的可用状态
 * 
 * @author kms
 */
public enum DirectoryStatus {
    
    /** 正常状态 */
    NORMAL(0),
    
    /** 已删除 */
    DELETED(1),
    
    /** 禁用状态 */
    DISABLED(2);
    
    private final Integer code;
    
    DirectoryStatus(Integer code) {
        this.code = code;
    }
    
    public Integer getCode() {
        return code;
    }
    
    /**
     * 根据code获取枚举值
     *
     * @param code 状态码
     * @return 目录状态枚举
     */
    public static DirectoryStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DirectoryStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}

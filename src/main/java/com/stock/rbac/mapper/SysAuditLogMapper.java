package com.stock.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.rbac.entity.SysAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysAuditLogMapper extends BaseMapper<SysAuditLog> {
}

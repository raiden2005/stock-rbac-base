package com.stock.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.rbac.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenant> {
}

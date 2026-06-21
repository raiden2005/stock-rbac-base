package com.stock.rbac.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.rbac.entity.StockKnowledge;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库主表 Mapper
 */
@Mapper
public interface StockKnowledgeMapper extends BaseMapper<StockKnowledge> {
}

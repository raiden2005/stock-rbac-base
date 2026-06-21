package com.stock.rbac.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.dto.KnowledgeAddDTO;
import com.stock.rbac.dto.KnowledgeQueryDTO;
import com.stock.rbac.entity.StockKnowledge;
import com.stock.rbac.entity.StockKnowledgeSlice;
import com.stock.rbac.mapper.StockKnowledgeMapper;
import com.stock.rbac.mapper.StockKnowledgeSliceMapper;
import com.stock.rbac.util.UserContext;
import com.stock.rbac.vo.KnowledgeListVO;
import com.stock.rbac.vo.KnowledgeStatsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库运营管理服务
 * CRUD、上下架、文件上传处理、统计
 */
@Service
public class KnowledgeAdminService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeAdminService.class);

    @Autowired
    private StockKnowledgeMapper knowledgeMapper;

    @Autowired
    private StockKnowledgeSliceMapper sliceMapper;

    @Autowired
    private KnowledgeSliceService sliceService;

    /**
     * 分页查询知识列表
     */
    public Page<KnowledgeListVO> listKnowledge(KnowledgeQueryDTO queryDTO) {
        LambdaQueryWrapper<StockKnowledge> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getCategory() != null && !queryDTO.getCategory().isEmpty()) {
            wrapper.eq(StockKnowledge::getCategory, queryDTO.getCategory());
        }
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().isEmpty()) {
            wrapper.like(StockKnowledge::getTitle, queryDTO.getKeyword());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(StockKnowledge::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(StockKnowledge::getCreateTime);

        Page<StockKnowledge> page = knowledgeMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        Page<KnowledgeListVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<KnowledgeListVO> voList = new ArrayList<>();
        for (StockKnowledge k : page.getRecords()) {
            voList.add(convertToVO(k));
        }
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 新增知识(含切片)
     */
    @Transactional(rollbackFor = Exception.class)
    public StockKnowledge addKnowledge(KnowledgeAddDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new RuntimeException("知识标题不能为空");
        }

        StockKnowledge knowledge = new StockKnowledge();
        knowledge.setTitle(dto.getTitle().trim());
        knowledge.setCategory(dto.getCategory());
        knowledge.setSourceType(dto.getSourceType() != null ? dto.getSourceType() : "text");
        knowledge.setOriginalFileUrl(dto.getFileUrl());
        knowledge.setWeight(dto.getWeight() != null ? dto.getWeight() : BigDecimal.ONE);
        knowledge.setStatus(1); // 默认上架
        knowledge.setCreateUser(UserContext.getUserAccount());
        knowledge.setTotalSliceNum(0);
        knowledge.setHitCount(0);

        knowledgeMapper.insert(knowledge);

        // 如果有文本内容，进行切片
        if (dto.getContent() != null && !dto.getContent().trim().isEmpty()) {
            processSlices(knowledge.getId(), dto.getContent());
        }

        log.info("新增知识: id={}, title={}", knowledge.getId(), knowledge.getTitle());
        return knowledge;
    }

    /**
     * 编辑知识
     */
    @Transactional(rollbackFor = Exception.class)
    public StockKnowledge updateKnowledge(String id, KnowledgeAddDTO dto) {
        StockKnowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new RuntimeException("知识不存在");
        }

        if (dto.getTitle() != null) {
            knowledge.setTitle(dto.getTitle().trim());
        }
        if (dto.getCategory() != null) {
            knowledge.setCategory(dto.getCategory());
        }
        if (dto.getWeight() != null) {
            knowledge.setWeight(dto.getWeight());
        }

        knowledgeMapper.updateById(knowledge);

        // 如果更新了文本内容，重新切片
        if (dto.getContent() != null && !dto.getContent().trim().isEmpty()) {
            // 删除旧切片
            sliceMapper.delete(new LambdaQueryWrapper<StockKnowledgeSlice>()
                    .eq(StockKnowledgeSlice::getKnowledgeId, id));
            processSlices(id, dto.getContent());
        }

        log.info("编辑知识: id={}", id);
        return knowledge;
    }

    /**
     * 上下架切换
     */
    public void toggleStatus(String id) {
        StockKnowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new RuntimeException("知识不存在");
        }
        int newStatus = knowledge.getStatus() == 1 ? 0 : 1;
        knowledge.setStatus(newStatus);
        knowledgeMapper.updateById(knowledge);
        log.info("知识状态切换: id={}, status={}", id, newStatus);
    }

    /**
     * 删除知识(逻辑删除，同时删除切片)
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeKnowledge(String id) {
        StockKnowledge knowledge = knowledgeMapper.selectById(id);
        if (knowledge == null) {
            throw new RuntimeException("知识不存在");
        }
        knowledgeMapper.deleteById(id);
        // 同时删除切片
        sliceMapper.delete(new LambdaQueryWrapper<StockKnowledgeSlice>()
                .eq(StockKnowledgeSlice::getKnowledgeId, id));
        log.info("删除知识: id={}", id);
    }

    /**
     * 统计看板数据
     */
    public KnowledgeStatsVO getStats() {
        KnowledgeStatsVO stats = new KnowledgeStatsVO();
        stats.setTotalKnowledge(knowledgeMapper.selectCount(null));
        stats.setOnlineKnowledge(knowledgeMapper.selectCount(
                new LambdaQueryWrapper<StockKnowledge>().eq(StockKnowledge::getStatus, 1)));
        stats.setTotalSlices(sliceMapper.selectCount(null));

        // 总命中次数: sum查询
        List<StockKnowledge> all = knowledgeMapper.selectList(null);
        long totalHits = all.stream()
                .mapToLong(k -> k.getHitCount() != null ? k.getHitCount() : 0)
                .sum();
        stats.setTotalHits(totalHits);

        return stats;
    }

    /**
     * 命中明细(某知识下的切片列表)
     */
    public Page<StockKnowledgeSlice> getHitRecord(String knowledgeId, int pageNum, int pageSize) {
        return sliceMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<StockKnowledgeSlice>()
                        .eq(StockKnowledgeSlice::getKnowledgeId, knowledgeId)
                        .orderByDesc(StockKnowledgeSlice::getHitCount));
    }

    /**
     * 处理文本切片并入库
     */
    private void processSlices(String knowledgeId, String rawText) {
        String cleaned = sliceService.cleanText(rawText);
        List<String> slices = sliceService.sliceText(cleaned);

        for (String sliceContent : slices) {
            StockKnowledgeSlice slice = new StockKnowledgeSlice();
            slice.setKnowledgeId(knowledgeId);
            slice.setSegmentContent(sliceContent);
            slice.setHitCount(0);
            sliceMapper.insert(slice);
        }

        // 更新知识切片总数
        StockKnowledge knowledge = knowledgeMapper.selectById(knowledgeId);
        if (knowledge != null) {
            knowledge.setTotalSliceNum(slices.size());
            knowledgeMapper.updateById(knowledge);
        }

        log.info("知识切片完成: knowledgeId={}, sliceCount={}", knowledgeId, slices.size());
    }

    /**
     * 实体转VO
     */
    private KnowledgeListVO convertToVO(StockKnowledge k) {
        KnowledgeListVO vo = new KnowledgeListVO();
        vo.setId(k.getId());
        vo.setTitle(k.getTitle());
        vo.setCategory(k.getCategory());
        vo.setSourceType(k.getSourceType());
        vo.setOriginalFileUrl(k.getOriginalFileUrl());
        vo.setTotalSliceNum(k.getTotalSliceNum());
        vo.setHitCount(k.getHitCount());
        vo.setWeight(k.getWeight());
        vo.setStatus(k.getStatus());
        vo.setCreateUser(k.getCreateUser());
        vo.setCreateTime(k.getCreateTime());
        vo.setUpdateTime(k.getUpdateTime());
        return vo;
    }
}

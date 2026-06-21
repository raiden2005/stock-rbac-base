package com.stock.rbac.bridge;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kms.domain.aggregation.knowledge.domain.IKnowledgeDomainService;
import com.kms.domain.aggregation.knowledge.entity.KnowledgeEntity;
import com.kms.domain.aggregation.knowledge.entity.KnowledgeSliceEntity;
import com.kms.domain.aggregation.knowledge.entity.VectorSearchResult;
import com.kms.domain.aggregation.knowledge.port.IVectorSearchPort;
import com.stock.rbac.entity.StockKnowledge;
import com.stock.rbac.entity.StockKnowledgeSlice;
import com.stock.rbac.mapper.StockKnowledgeMapper;
import com.stock.rbac.mapper.StockKnowledgeSliceMapper;
import com.stock.rbac.service.KnowledgeSliceService;
import com.stock.rbac.service.PromptBuildService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库领域服务Bridge
 * 实现IKnowledgeDomainService接口
 * 连接DDD领域层与基础设施层
 */
@Service
public class KnowledgeDomainServiceBridge implements IKnowledgeDomainService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeDomainServiceBridge.class);

    @Autowired
    private StockKnowledgeMapper knowledgeMapper;

    @Autowired
    private StockKnowledgeSliceMapper sliceMapper;

    @Autowired
    private KnowledgeSliceService sliceService;

    @Autowired
    private PromptBuildService promptBuildService;

    @Autowired
    private IVectorSearchPort vectorSearchPort;

    @Override
    public List<KnowledgeSliceEntity> createKnowledgeWithSlices(KnowledgeEntity entity, String rawText, int sliceLen, int overlap) {
        // 1. 保存知识实体
        StockKnowledge knowledge = convertToDO(entity);
        knowledgeMapper.insert(knowledge);
        entity.setId(knowledge.getId());

        // 2. 文本清洗和切片
        String cleaned = cleanText(rawText);
        List<String> sliceTexts = sliceText(cleaned, sliceLen, overlap);

        // 3. 保存切片
        List<KnowledgeSliceEntity> sliceEntities = new ArrayList<>();
        for (String content : sliceTexts) {
            StockKnowledgeSlice slice = new StockKnowledgeSlice();
            slice.setKnowledgeId(knowledge.getId());
            slice.setSegmentContent(content);
            slice.setHitCount(0);
            sliceMapper.insert(slice);

            KnowledgeSliceEntity sliceEntity = new KnowledgeSliceEntity();
            sliceEntity.setId(slice.getId());
            sliceEntity.setKnowledgeId(knowledge.getId());
            sliceEntity.setSegmentContent(content);
            sliceEntity.setCreateTime(slice.getCreateTime());
            sliceEntities.add(sliceEntity);
        }

        // 4. 更新切片总数
        knowledge.setTotalSliceNum(sliceTexts.size());
        knowledgeMapper.updateById(knowledge);

        log.info("创建知识及切片: knowledgeId={}, sliceCount={}", knowledge.getId(), sliceTexts.size());
        return sliceEntities;
    }

    @Override
    public List<String> sliceText(String text, int sliceLen, int overlap) {
        return sliceService.sliceText(text, sliceLen, overlap);
    }

    @Override
    public String cleanText(String text) {
        return sliceService.cleanText(text);
    }

    @Override
    public List<VectorSearchResult> searchAndRank(String queryText, int topN, double threshold) {
        List<VectorSearchResult> results = vectorSearchPort.search(queryText, topN, threshold);

        if (results == null || results.isEmpty()) {
            return new ArrayList<>();
        }

        // 计算加权得分并排序
        for (VectorSearchResult r : results) {
            r.calculateWeightedScore();
        }

        return results.stream()
                .sorted((a, b) -> b.getWeightedScore().compareTo(a.getWeightedScore()))
                .collect(Collectors.toList());
    }

    @Override
    public String buildRagPrompt(String systemPrompt, List<VectorSearchResult> searchResults, String userQuestion) {
        return promptBuildService.buildRagPrompt(systemPrompt, searchResults, userQuestion);
    }

    @Override
    public void updateKnowledgeStatus(String knowledgeId, int status) {
        StockKnowledge knowledge = knowledgeMapper.selectById(knowledgeId);
        if (knowledge != null) {
            knowledge.setStatus(status);
            knowledgeMapper.updateById(knowledge);
        }
    }

    @Override
    public void deleteKnowledgeAndSlices(String knowledgeId) {
        knowledgeMapper.deleteById(knowledgeId);
        sliceMapper.delete(new LambdaQueryWrapper<StockKnowledgeSlice>()
                .eq(StockKnowledgeSlice::getKnowledgeId, knowledgeId));
    }

    // ==================== 私有转换方法 ====================

    private StockKnowledge convertToDO(KnowledgeEntity entity) {
        StockKnowledge knowledge = new StockKnowledge();
        knowledge.setId(entity.getId());
        knowledge.setTitle(entity.getTitle());
        knowledge.setCategory(entity.getCategory());
        knowledge.setSourceType(entity.getSourceType());
        knowledge.setOriginalFileUrl(entity.getOriginalFileUrl());
        knowledge.setTotalSliceNum(entity.getTotalSliceNum());
        knowledge.setHitCount(entity.getHitCount());
        knowledge.setWeight(entity.getWeight());
        knowledge.setStatus(entity.getStatus());
        knowledge.setCreateUser(entity.getCreateUser());
        return knowledge;
    }
}

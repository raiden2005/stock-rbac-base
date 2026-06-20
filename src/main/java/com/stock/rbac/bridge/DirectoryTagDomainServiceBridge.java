package com.stock.rbac.bridge;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stock.rbac.entity.BizVisibleRule;
import com.stock.rbac.mapper.BizVisibleRuleMapper;
import com.kms.domain.aggregation.directorytag.domain.IDirectoryTagDomainService;
import com.kms.domain.aggregation.directorytag.entity.CategoryTag;
import com.kms.domain.aggregation.directorytag.entity.DirectoryTag;
import com.kms.domain.aggregation.directorytag.vo.CategoryTagVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 目录标签领域服务桥接实现
 * <p>
 * 将DDD KMS的IDirectoryTagDomainService接口与现有的Rbac系统对接
 * 实现标签相关的业务规则
 */
@Service
public class DirectoryTagDomainServiceBridge implements IDirectoryTagDomainService {

    private static final Logger log = LoggerFactory.getLogger(DirectoryTagDomainServiceBridge.class);

    @Autowired
    private BizVisibleRuleMapper bizVisibleRuleMapper;

    @Override
    public List<CategoryTagVO> queryDirectoryCategoryTag(String directoryId) {
        // 组装维度与标签信息
        List<CategoryTagVO> result = new ArrayList<>();
        try {
            // 查询目录关联的标签规则
            List<BizVisibleRule> rules = bizVisibleRuleMapper.selectList(
                    new LambdaQueryWrapper<BizVisibleRule>()
                            .eq(BizVisibleRule::getResourceId, directoryId)
                            .eq(BizVisibleRule::getResourceType, "TAG")
                            .eq(BizVisibleRule::getStatus, 1)
            );

            // 按category分组
            Map<String, List<BizVisibleRule>> groupedRules = rules.stream()
                    .collect(Collectors.groupingBy(r -> r.getCategoryId() != null ? r.getCategoryId() : "DEFAULT"));

            for (Map.Entry<String, List<BizVisibleRule>> entry : groupedRules.entrySet()) {
                CategoryTagVO vo = new CategoryTagVO();
                vo.setCategoryId(entry.getKey());
                vo.setCategoryName(getCategoryName(entry.getKey()));

                List<CategoryTagVO.TagInfo> tags = entry.getValue().stream()
                        .map(rule -> {
                            CategoryTagVO.TagInfo tagInfo = new CategoryTagVO.TagInfo();
                            tagInfo.setTagId(rule.getRuleId());
                            tagInfo.setTagName(rule.getRuleName());
                            return tagInfo;
                        })
                        .collect(Collectors.toList());

                vo.setTags(tags);
                result.add(vo);
            }
        } catch (Exception e) {
            log.error("组装维度与标签信息失败 directoryId={}", directoryId, e);
        }
        return result;
    }

    @Override
    public void insertCategoryTag(String directoryId, List<String> tagIds) {
        // 批量新增目录标签关联
        if (directoryId == null || tagIds == null || tagIds.isEmpty()) {
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            for (String tagId : tagIds) {
                BizVisibleRule rule = new BizVisibleRule();
                rule.setRuleId(UUID.randomUUID().toString().replace("-", ""));
                rule.setResourceId(directoryId);
                rule.setResourceType("TAG");
                rule.setRuleType("TAG_BINDING");
                rule.setRuleName(tagId); // 标签名称暂存
                rule.setCategoryId(getCategoryIdByTag(tagId));
                rule.setStatus(1);
                rule.setCreateTime(now);
                bizVisibleRuleMapper.insert(rule);
            }
            log.info("批量新增目录标签关联完成 directoryId={}, tagCount={}", directoryId, tagIds.size());
        } catch (Exception e) {
            log.error("批量新增目录标签关联失败 directoryId={}", directoryId, e);
        }
    }

    @Override
    public void deleteCategoryTagByDirectoryId(String directoryId) {
        // 删除目录绑定标签
        try {
            bizVisibleRuleMapper.delete(
                    new LambdaQueryWrapper<BizVisibleRule>()
                            .eq(BizVisibleRule::getResourceId, directoryId)
                            .eq(BizVisibleRule::getResourceType, "TAG")
            );
            log.info("删除目录绑定标签完成 directoryId={}", directoryId);
        } catch (Exception e) {
            log.error("删除目录绑定标签失败 directoryId={}", directoryId, e);
        }
    }

    @Override
    public Map<String, String> queryDirectoryCategoryTagId(List<String> tagIds) {
        // 查询标签ID-名称映射
        Map<String, String> result = new HashMap<>();
        if (tagIds == null || tagIds.isEmpty()) {
            return result;
        }

        try {
            List<BizVisibleRule> rules = bizVisibleRuleMapper.selectList(
                    new LambdaQueryWrapper<BizVisibleRule>()
                            .in(BizVisibleRule::getRuleId, tagIds)
                            .eq(BizVisibleRule::getResourceType, "TAG")
            );

            for (BizVisibleRule rule : rules) {
                result.put(rule.getRuleId(), rule.getRuleName());
            }
        } catch (Exception e) {
            log.error("查询标签ID-名称映射失败", e);
        }
        return result;
    }

    @Override
    public boolean checkIsAlreadyFollow(String directoryId, String userId) {
        // 校验是否已关注
        try {
            BizVisibleRule rule = bizVisibleRuleMapper.selectOne(
                    new LambdaQueryWrapper<BizVisibleRule>()
                            .eq(BizVisibleRule::getResourceId, directoryId)
                            .eq(BizVisibleRule::getUserGuid, userId)
                            .eq(BizVisibleRule::getRuleType, "FOLLOW")
                            .eq(BizVisibleRule::getStatus, 1)
            );
            return rule != null;
        } catch (Exception e) {
            log.error("校验是否已关注失败 directoryId={}, userId={}", directoryId, userId, e);
            return false;
        }
    }

    // ==================== 私有辅助方法 ====================

    private String getCategoryName(String categoryId) {
        // 根据categoryId获取分类名称
        // 实际实现应该查询分类表
        return "默认分类";
    }

    private String getCategoryIdByTag(String tagId) {
        // 根据标签ID获取分类ID
        // 实际实现应该查询标签表
        return "DEFAULT";
    }
}

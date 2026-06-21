package com.stock.rbac.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.annotation.AuditLog;
import com.stock.rbac.annotation.QuestionQuota;
import com.stock.rbac.entity.SysQuestion;
import com.stock.rbac.mapper.SysQuestionMapper;
import com.stock.rbac.service.RagQuestionService;
import com.stock.rbac.util.UserContext;
import com.stock.rbac.vo.RagAnswerVO;
import com.stock.rbac.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户提问相关接口：提交问题、历史问题列表、问题详情
 * 已集成RAG增强逻辑
 */
@RestController
@RequestMapping("/api/question")
public class QuestionController {

    private static final Logger log = LoggerFactory.getLogger(QuestionController.class);

    @Autowired
    private SysQuestionMapper questionMapper;

    @Autowired
    private RagQuestionService ragQuestionService;

    /**
     * 提交问题（走额度扣减：免费优先 -> 付费存量）
     * 集成RAG增强逻辑：向量检索 -> Prompt组装 -> LLM调用
     * 降级策略：向量检索异常自动降级旧版固定Prompt
     */
    @QuestionQuota(check = true)
    @AuditLog(module = "用户提问", operation = "提交问题")
    @PostMapping("/submit")
    public Result<SysQuestion> submit(@RequestBody SysQuestion req) {
        String title = req.getTitle();
        String content = req.getQuestionContent();
        if (content == null || content.trim().length() < 2) {
            return Result.error(400, "问题内容不能为空或过短");
        }
        if (title == null || title.trim().isEmpty()) {
            // 自动截取前30个字符作为标题
            title = content.length() <= 30 ? content : content.substring(0, 30);
        }
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            tenantId = "TENANT_DEFAULT";
        }
        String userGuid = UserContext.getUserGuid();
        String userAccount = UserContext.getUserAccount();

        // RAG增强问答
        String replyContent;
        String referenceKnowledge = null;
        String fullRagPrompt = null;
        try {
            RagAnswerVO ragAnswer = ragQuestionService.ragAnswer(content.trim());
            replyContent = ragAnswer.getAnswer();
            if (ragAnswer.getSources() != null && !ragAnswer.getSources().isEmpty()) {
                referenceKnowledge = buildReferenceKnowledgeJson(ragAnswer);
            }
            fullRagPrompt = ragAnswer.getFullRagPrompt();
            log.info("RAG增强回答成功: ragEnabled={}, sourceCount={}",
                    ragAnswer.isRagEnabled(),
                    ragAnswer.getSources() != null ? ragAnswer.getSources().size() : 0);
        } catch (Exception e) {
            log.error("RAG增强异常，降级为演示回复: {}", e.getMessage());
            replyContent = generateDemoReply(title.trim(), content.trim());
        }

        SysQuestion q = new SysQuestion();
        q.setTenantId(tenantId);
        q.setUserGuid(userGuid);
        q.setUserAccount(userAccount);
        q.setTitle(title.trim());
        q.setQuestionContent(content.trim());
        q.setStatus(1); // 已回复
        q.setPayType(0); // 0-免费 1-付费
        q.setReplyContent(replyContent);
        q.setReferenceKnowledge(referenceKnowledge);
        q.setFullRagPrompt(fullRagPrompt);
        questionMapper.insert(q);
        return Result.success(q);
    }

    /**
     * 当前登录用户的提问历史（分页）
     */
    @GetMapping("/my")
    public Result<?> myQuestions(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize) {
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            tenantId = "TENANT_DEFAULT";
        }
        LambdaQueryWrapper<SysQuestion> query = new LambdaQueryWrapper<>();
        query.eq(SysQuestion::getTenantId, tenantId);
        query.orderByDesc(SysQuestion::getCreateTime);
        Page<SysQuestion> page = questionMapper.selectPage(new Page<>(pageNum, pageSize), query);
        return Result.success(page);
    }

    /**
     * 问题详情
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable String id) {
        if (id == null || id.isEmpty()) {
            return Result.error(400, "问题ID不能为空");
        }
        SysQuestion q = questionMapper.selectById(id);
        if (q == null) {
            return Result.error(404, "问题不存在");
        }
        // 简单的租户隔离校验：只能看自己租户的问题
        String tenantId = UserContext.getTenantId();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            tenantId = "TENANT_DEFAULT";
        }
        if (!tenantId.equals(q.getTenantId()) && !isAdmin()) {
            return Result.error(403, "无权查看其他租户的问题");
        }
        return Result.success(q);
    }

    private boolean isAdmin() {
        String userType = UserContext.getUserType();
        if ("admin".equalsIgnoreCase(userType)) return true;
        // 角色判断
        java.util.List<String> roles = UserContext.getRoles();
        if (roles != null) {
            for (String r : roles) {
                if (r != null && r.toUpperCase().contains("ADMIN")) return true;
            }
        }
        return false;
    }

    /**
     * 构建知识溯源JSON
     */
    private String buildReferenceKnowledgeJson(RagAnswerVO ragAnswer) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ragAnswer.getSources().size(); i++) {
            RagAnswerVO.KnowledgeSourceVO source = ragAnswer.getSources().get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"knowledgeId\":\"").append(escapeJson(source.getKnowledgeId())).append("\",");
            sb.append("\"title\":\"").append(escapeJson(source.getTitle())).append("\",");
            sb.append("\"category\":\"").append(escapeJson(source.getCategory())).append("\",");
            sb.append("\"score\":").append(source.getScore()).append(",");
            sb.append("\"weight\":").append(source.getWeight());
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /**
     * 占位回复生成器（降级使用）
     */
    private String generateDemoReply(String title, String content) {
        StringBuilder reply = new StringBuilder();
        reply.append("[演示回复] 已收到您的问题：\u201C" + title + "\u201D。\n");
        reply.append("当前回答基于演示模板，若需真实 AI 分析请联系管理员。\n");
        reply.append("提交时间：").append(LocalDateTime.now().toString());
        return reply.toString();
    }
}

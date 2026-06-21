package com.stock.rbac.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.annotation.AuditLog;
import com.stock.rbac.annotation.QuestionQuota;
import com.stock.rbac.entity.SysQuestion;
import com.stock.rbac.exception.BusinessException;
import com.stock.rbac.mapper.SysQuestionMapper;
import com.stock.rbac.util.UserContext;
import com.stock.rbac.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户提问相关接口：提交问题、历史问题列表、问题详情
 */
@RestController
@RequestMapping("/api/question")
public class QuestionController {

    @Autowired
    private SysQuestionMapper questionMapper;

    /**
     * 提交问题（走额度扣减：免费优先 → 付费存量）
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

        SysQuestion q = new SysQuestion();
        q.setTenantId(tenantId);
        q.setUserGuid(userGuid);
        q.setUserAccount(userAccount);
        q.setTitle(title.trim());
        q.setQuestionContent(content.trim());
        q.setStatus(1); // 本系统默认已回复（演示用）
        q.setPayType(0); // 0-免费 1-付费，由调用方根据额度情况判断（此处简化为0）
        q.setReplyContent(generateDemoReply(title.trim(), content.trim()));
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
     * 占位回复生成器（真实系统应由 AI/人工回答）
     */
    private String generateDemoReply(String title, String content) {
        StringBuilder reply = new StringBuilder();
        reply.append("[演示回复] 已收到您的问题：“").append(title).append("”。\n");
        reply.append("当前回答基于演示模板，若需真实 AI 分析请联系管理员。\n");
        reply.append("提交时间：").append(LocalDateTime.now().toString());
        return reply.toString();
    }
}

package com.stock.rbac.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.rbac.annotation.AuditLog;
import com.stock.rbac.dto.KnowledgeAddDTO;
import com.stock.rbac.dto.KnowledgeQueryDTO;
import com.stock.rbac.entity.StockKnowledgeSlice;
import com.stock.rbac.service.KnowledgeAdminService;
import com.stock.rbac.vo.KnowledgeListVO;
import com.stock.rbac.vo.KnowledgeStatsVO;
import com.stock.rbac.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库运营后台接口
 */
@RestController
@RequestMapping("/api/system/knowledge")
public class KnowledgeAdminController {

    @Autowired
    private KnowledgeAdminService knowledgeAdminService;

    /**
     * 分页列表
     */
    @AuditLog(module = "知识库管理", operation = "查询列表")
    @GetMapping("/list")
    public Result<Page<KnowledgeListVO>> list(KnowledgeQueryDTO queryDTO) {
        Page<KnowledgeListVO> page = knowledgeAdminService.listKnowledge(queryDTO);
        return Result.success(page);
    }

    /**
     * 新增知识
     */
    @AuditLog(module = "知识库管理", operation = "新增知识")
    @PostMapping("/add")
    public Result<?> add(@RequestBody KnowledgeAddDTO dto) {
        knowledgeAdminService.addKnowledge(dto);
        return Result.success();
    }

    /**
     * 编辑知识
     */
    @AuditLog(module = "知识库管理", operation = "编辑知识")
    @PutMapping("/update")
    public Result<?> update(@RequestParam String id, @RequestBody KnowledgeAddDTO dto) {
        knowledgeAdminService.updateKnowledge(id, dto);
        return Result.success();
    }

    /**
     * 上下架切换
     */
    @AuditLog(module = "知识库管理", operation = "上下架切换")
    @PutMapping("/status")
    public Result<?> toggleStatus(@RequestParam String id) {
        knowledgeAdminService.toggleStatus(id);
        return Result.success();
    }

    /**
     * 删除知识
     */
    @AuditLog(module = "知识库管理", operation = "删除知识")
    @DeleteMapping("/remove/{id}")
    public Result<?> remove(@PathVariable String id) {
        knowledgeAdminService.removeKnowledge(id);
        return Result.success();
    }

    /**
     * 统计看板数据
     */
    @AuditLog(module = "知识库管理", operation = "查看统计")
    @GetMapping("/stats")
    public Result<KnowledgeStatsVO> stats() {
        KnowledgeStatsVO stats = knowledgeAdminService.getStats();
        return Result.success(stats);
    }

    /**
     * 命中明细
     */
    @GetMapping("/hitRecord/{knowledgeId}")
    public Result<?> hitRecord(@PathVariable String knowledgeId,
                               @RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<StockKnowledgeSlice> page = knowledgeAdminService.getHitRecord(knowledgeId, pageNum, pageSize);
        return Result.success(page);
    }
}

package com.stock.rbac.controller;

import com.stock.rbac.annotation.AuditLog;
import com.stock.rbac.annotation.QuestionQuota;
import com.stock.rbac.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/question")
public class QuestionDemoController {

    @QuestionQuota(check = true)
    @AuditLog(module = "AI提问", operation = "演示提问")
    @GetMapping("/demo")
    public Result<?> demo() {
        return Result.success("提问成功，已扣减额度");
    }
}

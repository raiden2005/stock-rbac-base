package com.stock.rbac.controller;

import com.stock.rbac.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class HealthController {

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OK");
    }
}

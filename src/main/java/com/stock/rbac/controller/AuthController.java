package com.stock.rbac.controller;

import com.stock.rbac.annotation.AuditLog;
import com.stock.rbac.auth.service.UserService;
import com.stock.rbac.vo.LoginRequestVO;
import com.stock.rbac.vo.LoginResponseVO;
import com.stock.rbac.vo.Result;
import com.stock.rbac.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @AuditLog(module = "认证", operation = "登录")
    public Result<LoginResponseVO> login(@RequestBody LoginRequestVO vo, HttpServletRequest request) {
        if (vo == null || vo.getUserAccount() == null || vo.getUserAccount().trim().isEmpty()
                || vo.getUserPwd() == null || vo.getUserPwd().isEmpty()) {
            return Result.error(400, "账号或密码不能为空");
        }
        LoginResponseVO response = userService.login(vo, request);
        return Result.success(response);
    }

    @GetMapping("/login")
    public Result<?> loginHint() {
        return Result.error(405,
                "请使用 POST 方式登录，请求体需包含 JSON: {\"userAccount\":\"账号\",\"userPwd\":\"密码\"}");
    }

    @RequestMapping(value = {"/logout"}, method = {RequestMethod.GET, RequestMethod.POST})
    @AuditLog(module = "认证", operation = "退出登录")
    public Result<?> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.logout(request, response);
        return Result.success();
    }

    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        UserInfoVO userInfo = userService.getCurrentUserInfo();
        return Result.success(userInfo);
    }
}

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

    @RequestMapping(value = {"/login"}, method = {RequestMethod.GET, RequestMethod.POST})
    @AuditLog(module = "认证", operation = "登录")
    public Result<LoginResponseVO> login(@RequestBody(required = false) LoginRequestVO vo,
                                         @RequestParam(required = false) String userAccount,
                                         @RequestParam(required = false) String userPwd,
                                         HttpServletRequest request) {
        if (vo == null || vo.getUserAccount() == null) {
            vo = new LoginRequestVO();
            vo.setUserAccount(userAccount);
            vo.setUserPwd(userPwd);
        }
        LoginResponseVO response = userService.login(vo, request);
        return Result.success(response);
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

package com.stock.rbac.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.rbac.config.LocalCacheConfig;
import com.stock.rbac.constant.RedisConstant;
import com.stock.rbac.constant.SsoConstants;
import com.stock.rbac.util.RbacConfigUtil;
import com.stock.rbac.util.UserContext;
import com.stock.rbac.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class AuthHandler {

    private final LocalCacheConfig.RedisTemplateFallback redisTemplate;
    private final RbacConfigUtil rbacConfigUtil;

    public AuthHandler(LocalCacheConfig.RedisTemplateFallback redisTemplate, RbacConfigUtil rbacConfigUtil) {
        this.redisTemplate = redisTemplate;
        this.rbacConfigUtil = rbacConfigUtil;
    }

    public void loginAndRedirect2GivenUrl(HttpServletRequest req, HttpServletResponse resp) {
        String referer = req.getHeader(SsoConstants.HEADER_REFERER_CUSTOM);
        String redirect;
        if (referer != null && !referer.isEmpty()) {
            redirect = referer;
        } else {
            StringBuffer url = req.getRequestURL();
            String queryString = req.getQueryString();
            if (queryString != null) {
                url.append("?").append(queryString);
            }
            redirect = url.toString();
        }

        String encodedRedirect = URLEncoder.encode(redirect, StandardCharsets.UTF_8);
        String loginPage = rbacConfigUtil.getLoginPage();
        String location = loginPage + "?redirect=" + encodedRedirect;

        resp.setStatus(SsoConstants.HTTP_401);
        resp.setHeader("Location", location);
        resp.setContentType("application/json;charset=UTF-8");

        try {
            PrintWriter writer = resp.getWriter();
            Result<?> result = Result.error401("请先登录");
            ObjectMapper mapper = new ObjectMapper();
            writer.write(mapper.writeValueAsString(result));
            writer.flush();
        } catch (IOException ignored) {
        }
    }

    public void logoutAndRedirect2GivenUrl(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            try {
                session.invalidate();
            } catch (Exception ignored) {
            }
        }

        String authHeader = req.getHeader(SsoConstants.HEADER_AUTH);
        if (authHeader != null && authHeader.startsWith(SsoConstants.TOKEN_PREFIX)) {
            String token = authHeader.substring(SsoConstants.TOKEN_PREFIX.length());
            String redisKey = RedisConstant.AUTH_JWT_PREFIX + token;
            try {
                redisTemplate.delete(redisKey);
            } catch (Exception ignored) {
            }
        }

        String userGuid = UserContext.getUserGuid();
        if (userGuid != null) {
            String roleKey = RedisConstant.RBAC_USER_ROLE_PREFIX + userGuid;
            try {
                redisTemplate.delete(roleKey);
            } catch (Exception ignored) {
            }
        }

        UserContext.clear();

        resp.setStatus(SsoConstants.HTTP_200);
        resp.setContentType("application/json;charset=UTF-8");
        try {
            PrintWriter writer = resp.getWriter();
            Result<?> result = Result.success("退出登录成功");
            ObjectMapper mapper = new ObjectMapper();
            writer.write(mapper.writeValueAsString(result));
            writer.flush();
        } catch (IOException ignored) {
        }
    }
}

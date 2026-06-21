package com.stock.rbac.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.rbac.auth.handler.AuthHandler;
import com.stock.rbac.auth.service.UserService;
import com.stock.rbac.config.LocalCacheConfig;
import com.stock.rbac.constant.RedisConstant;
import com.stock.rbac.constant.SsoConstants;
import com.stock.rbac.util.JwtUtil;
import com.stock.rbac.util.SpringContextHolder;
import com.stock.rbac.util.UserContext;
import com.stock.rbac.util.RbacConfigUtil;
import com.stock.rbac.vo.Result;
import com.stock.rbac.vo.UserInfoBean;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;

@Component
@org.springframework.core.annotation.Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class LoginFilter implements Filter {

    private final RbacConfigUtil rbacConfigUtil;
    private final JwtUtil jwtUtil;
    private final LocalCacheConfig.RedisTemplateFallback redisTemplate;
    private final AuthHandler authHandler;

    @Autowired
    public LoginFilter(RbacConfigUtil rbacConfigUtil, JwtUtil jwtUtil,
                      LocalCacheConfig.RedisTemplateFallback redisTemplate, AuthHandler authHandler) {
        this.rbacConfigUtil = rbacConfigUtil;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.authHandler = authHandler;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String uri = req.getRequestURI();
        String method = req.getMethod();

        if (rbacConfigUtil.isWhitelist(uri) || "OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(req, resp);
            return;
        }

        String authHeader = req.getHeader(SsoConstants.HEADER_AUTH);
        if (authHeader != null && authHeader.startsWith(SsoConstants.TOKEN_PREFIX)) {
            String token = authHeader.substring(SsoConstants.TOKEN_PREFIX.length());
            try {
                String redisKey = RedisConstant.AUTH_JWT_PREFIX + token;
                // Check if token exists in cache
                if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
                    writeJsonResponse(resp, SsoConstants.HTTP_401, "Token已过期或无效");
                    return;
                }

                String userGuid = jwtUtil.getUserGuidFromToken(token);
                if (userGuid == null) {
                    writeJsonResponse(resp, SsoConstants.HTTP_401, "Token解析失败");
                    return;
                }

                UserInfoBean userInfo = loadUserInfo(userGuid);
                if (userInfo == null) {
                    writeJsonResponse(resp, SsoConstants.HTTP_401, "用户信息不存在");
                    return;
                }
                if (userInfo.getUserStatus() != null && userInfo.getUserStatus() == 0) {
                    writeJsonResponse(resp, SsoConstants.HTTP_403, "账号已禁用");
                    return;
                }

                bindUserContext(userInfo);
                chain.doFilter(req, resp);
                UserContext.clear();
                return;
            } catch (Exception e) {
                writeJsonResponse(resp, SsoConstants.HTTP_401, "Token校验失败: " + e.getMessage());
                UserContext.clear();
                return;
            }
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(SsoConstants.SESSION_USER_KEY) == null) {
            authHandler.loginAndRedirect2GivenUrl(req, resp);
            return;
        }

        UserInfoBean userInfo = (UserInfoBean) session.getAttribute(SsoConstants.SESSION_USER_KEY);
        if (userInfo.getUserStatus() != null && userInfo.getUserStatus() == 0) {
            writeJsonResponse(resp, SsoConstants.HTTP_403, "账号已禁用");
            return;
        }

        bindUserContext(userInfo);
        chain.doFilter(req, resp);
        UserContext.clear();
    }

    private UserInfoBean loadUserInfo(String userGuid) {
        UserService userService = SpringContextHolder.getBean(UserService.class);
        return userService.getUserInfoByGuid(userGuid);
    }

    private void bindUserContext(UserInfoBean user) {
        UserContext.setUserGuid(user.getUserGuid());
        UserContext.setUserAccount(user.getUserAccount());
        UserContext.setUserName(user.getUserName());
        UserContext.setUserType(user.getUserType());
        UserContext.setRoles(user.getRoles());
        UserContext.setPermCodes(user.getPermCodes());
        UserContext.setTenantId("TENANT_DEFAULT");
    }

    private void writeJsonResponse(HttpServletResponse resp, int code, String msg) throws IOException {
        resp.setStatus(code);
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = resp.getWriter();
        Result<?> result = Result.error(code, msg);
        ObjectMapper mapper = new ObjectMapper();
        writer.write(mapper.writeValueAsString(result));
        writer.flush();
    }
}

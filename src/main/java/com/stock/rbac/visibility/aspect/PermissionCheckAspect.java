package com.stock.rbac.visibility.aspect;

import com.stock.rbac.annotation.RequirePermission;
import com.stock.rbac.annotation.RequireRole;
import com.stock.rbac.exception.ForbiddenException;
import com.stock.rbac.util.UserContext;
import com.stock.rbac.visibility.service.VisibilityPermissionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionCheckAspect implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(PermissionCheckAspect.class);

    private final VisibilityPermissionService visibilityPermissionService;

    public PermissionCheckAspect(VisibilityPermissionService visibilityPermissionService) {
        this.visibilityPermissionService = visibilityPermissionService;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        String userGuid = UserContext.getUserGuid();
        if (userGuid == null) {
            throw new ForbiddenException("未登录或已过期，请重新登录");
        }
        String permCode = requirePermission.value();
        if (!visibilityPermissionService.checkUserFunctionPerm(userGuid, permCode)) {
            log.warn("权限校验失败: userGuid={}, permCode={}, method={}",
                    userGuid, permCode, joinPoint.getSignature().toShortString());
            throw new ForbiddenException("您没有该操作的权限: " + permCode);
        }
        return joinPoint.proceed();
    }

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        String userGuid = UserContext.getUserGuid();
        if (userGuid == null) {
            throw new ForbiddenException("未登录或已过期，请重新登录");
        }
        String[] roles = requireRole.value();
        boolean hasRole = false;
        if (roles != null) {
            for (String role : roles) {
                if (visibilityPermissionService.checkUserRole(userGuid, role)) {
                    hasRole = true;
                    break;
                }
            }
        }
        if (!hasRole) {
            log.warn("角色校验失败: userGuid={}, requireRoles={}, method={}",
                    userGuid,
                    roles == null ? "[]" : String.join(",", roles),
                    joinPoint.getSignature().toShortString());
            throw new ForbiddenException("您没有该操作所需的角色");
        }
        return joinPoint.proceed();
    }
}

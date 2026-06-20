package com.stock.rbac.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.rbac.annotation.AuditLog;
import com.stock.rbac.entity.SysAuditLog;
import com.stock.rbac.mapper.SysAuditLogMapper;
import com.stock.rbac.util.IpUtil;
import com.stock.rbac.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    private final SysAuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    public AuditLogAspect(SysAuditLogMapper auditLogMapper, ObjectMapper objectMapper) {
        this.auditLogMapper = auditLogMapper;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        SysAuditLog logEntry = new SysAuditLog();
        logEntry.setLogId(UUID.randomUUID().toString().replace("-", ""));
        logEntry.setOperModule(auditLog.module());
        logEntry.setOperType(auditLog.operation());
        logEntry.setUserGuid(UserContext.getUserGuid());
        logEntry.setUserAccount(UserContext.getUserAccount());
        logEntry.setUserName(UserContext.getUserName());

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            logEntry.setRequestUri(request.getRequestURI());
            logEntry.setRequestMethod(request.getMethod());
            logEntry.setRequestIp(IpUtil.getClientIp(request));
        }

        try {
            Object[] args = pjp.getArgs();
            String paramStr = objectMapper.writeValueAsString(args);
            if (paramStr.length() > 2000) {
                paramStr = paramStr.substring(0, 2000) + "...";
            }
            logEntry.setRequestParam(paramStr);
        } catch (Exception ignored) {
        }

        Object result;
        try {
            result = pjp.proceed();
            logEntry.setResponseCode(200);
        } catch (Throwable t) {
            logEntry.setResponseCode(500);
            logEntry.setOperDesc("操作失败: " + t.getMessage());
            try {
                auditLogMapper.insert(logEntry);
            } catch (Exception e) {
                log.warn("审计日志写入失败", e);
            }
            throw t;
        }

        logEntry.setExecTime(System.currentTimeMillis() - start);
        logEntry.setOperDesc("操作成功");
        try {
            auditLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.warn("审计日志写入失败", e);
        }
        return result;
    }
}

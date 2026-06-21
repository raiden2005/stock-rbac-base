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

        String userGuid = UserContext.getUserGuid();
        String userAccount = UserContext.getUserAccount();
        String userName = UserContext.getUserName();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            logEntry.setRequestUri(request.getRequestURI());
            logEntry.setRequestMethod(request.getMethod());
            logEntry.setRequestIp(IpUtil.getClientIp(request));
        }

        // 安全脱敏：日志参数中模糊处理密码字段
        try {
            Object[] args = pjp.getArgs();
            String paramStr = objectMapper.writeValueAsString(args);
            paramStr = paramStr.replaceAll("\"userPwd\":\"[^\"]*\"", "\"userPwd\":\"***\"")
                    .replaceAll("\"password\":\"[^\"]*\"", "\"password\":\"***\"")
                    .replaceAll("\"oldPwd\":\"[^\"]*\"", "\"oldPwd\":\"***\"")
                    .replaceAll("\"newPwd\":\"[^\"]*\"", "\"newPwd\":\"***\"");
            if (paramStr.length() > 2000) {
                paramStr = paramStr.substring(0, 2000) + "...";
            }
            logEntry.setRequestParam(paramStr);
        } catch (Exception ignored) {
        }

        Object result;
        try {
            result = pjp.proceed();
            // 优先使用方法内绑定的用户信息（如 login 方法），否则使用进入时已有的
            String newGuid = UserContext.getUserGuid();
            logEntry.setUserGuid(newGuid != null ? newGuid : userGuid);
            String newAccount = UserContext.getUserAccount();
            logEntry.setUserAccount(newAccount != null ? newAccount : userAccount);
            String newName = UserContext.getUserName();
            logEntry.setUserName(newName != null ? newName : userName);
            logEntry.setResponseCode(200);
            logEntry.setOperDesc("操作成功");
        } catch (Throwable t) {
            // 失败场景：优先使用进入时的用户信息
            logEntry.setUserGuid(userGuid);
            logEntry.setUserAccount(userAccount);
            logEntry.setUserName(userName);
            logEntry.setResponseCode(500);
            String msg = t.getMessage();
            if (msg != null && msg.length() > 500) {
                msg = msg.substring(0, 500) + "...";
            }
            logEntry.setOperDesc("操作失败: " + msg);
            writeLogSafe(logEntry, start);
            throw t;
        }

        writeLogSafe(logEntry, start);
        return result;
    }

    private void writeLogSafe(SysAuditLog logEntry, long start) {
        try {
            logEntry.setExecTime(System.currentTimeMillis() - start);
            auditLogMapper.insert(logEntry);
        } catch (Exception e) {
            // 审计日志必须不能阻断主业务流程，只记录 warn
            log.warn("审计日志写入失败（非阻塞）: {}", e.getMessage());
        }
    }
}

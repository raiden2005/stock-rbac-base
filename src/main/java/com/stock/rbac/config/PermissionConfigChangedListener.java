package com.stock.rbac.config;

import com.stock.rbac.event.PermissionConfigChangedEvent;
import com.stock.rbac.permission.service.PermissionManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class PermissionConfigChangedListener implements ApplicationListener<PermissionConfigChangedEvent> {

    private static final Logger log = LoggerFactory.getLogger(PermissionConfigChangedListener.class);

    private final PermissionManagementService permissionManagementService;

    public PermissionConfigChangedListener(PermissionManagementService permissionManagementService) {
        this.permissionManagementService = permissionManagementService;
    }

    @Override
    public void onApplicationEvent(PermissionConfigChangedEvent event) {
        log.info("收到权限变更事件, changeType={}, operator={}", event.getChangeType(), event.getOperator());
        try {
            permissionManagementService.refreshGlobalPermissionCache();
        } catch (Exception e) {
            log.error("刷新全局权限缓存失败", e);
        }
    }
}

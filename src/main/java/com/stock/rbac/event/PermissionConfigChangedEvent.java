package com.stock.rbac.event;

import org.springframework.context.ApplicationEvent;

public class PermissionConfigChangedEvent extends ApplicationEvent {

    private final String changeType;

    private final String operator;

    public PermissionConfigChangedEvent(Object source, String changeType, String operator) {
        super(source);
        this.changeType = changeType;
        this.operator = operator;
    }

    public String getChangeType() {
        return changeType;
    }

    public String getOperator() {
        return operator;
    }
}

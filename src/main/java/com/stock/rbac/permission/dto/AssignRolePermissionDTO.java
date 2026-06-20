package com.stock.rbac.permission.dto;

import java.util.List;

public class AssignRolePermissionDTO {

    private String roleId;

    private List<String> permIds;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public List<String> getPermIds() {
        return permIds;
    }

    public void setPermIds(List<String> permIds) {
        this.permIds = permIds;
    }
}

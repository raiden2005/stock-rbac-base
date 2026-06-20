package com.stock.rbac.permission.dto;

import java.util.List;

public class AssignUserRoleDTO {

    private String userGuid;

    private List<String> roleIds;

    public String getUserGuid() {
        return userGuid;
    }

    public void setUserGuid(String userGuid) {
        this.userGuid = userGuid;
    }

    public List<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }
}

package com.stock.rbac.vo;

import java.util.List;

public class UserInfoVO {

    private String userGuid;

    private String userAccount;

    private String userName;

    private Integer userStatus;

    private String userType;

    private List<String> roles;

    private List<String> permCodes;

    public String getUserGuid() {
        return userGuid;
    }

    public void setUserGuid(String userGuid) {
        this.userGuid = userGuid;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermCodes() {
        return permCodes;
    }

    public void setPermCodes(List<String> permCodes) {
        this.permCodes = permCodes;
    }
}

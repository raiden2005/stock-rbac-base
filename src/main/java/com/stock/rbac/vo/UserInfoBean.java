package com.stock.rbac.vo;

import java.util.List;

public class UserInfoBean {

    private String userGuid;

    private String userAccount;

    private String userName;

    private Integer userStatus;

    private String userType;

    private List<String> roles;

    private List<String> permCodes;

    private Long loginTime;

    public UserInfoBean() {
    }

    public UserInfoBean(String userGuid, String userAccount, String userName, Integer userStatus, 
                       String userType, List<String> roles, List<String> permCodes, Long loginTime) {
        this.userGuid = userGuid;
        this.userAccount = userAccount;
        this.userName = userName;
        this.userStatus = userStatus;
        this.userType = userType;
        this.roles = roles;
        this.permCodes = permCodes;
        this.loginTime = loginTime;
    }

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

    public Long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userGuid;
        private String userAccount;
        private String userName;
        private Integer userStatus;
        private String userType;
        private List<String> roles;
        private List<String> permCodes;
        private Long loginTime;

        public Builder userGuid(String userGuid) {
            this.userGuid = userGuid;
            return this;
        }

        public Builder userAccount(String userAccount) {
            this.userAccount = userAccount;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder userStatus(Integer userStatus) {
            this.userStatus = userStatus;
            return this;
        }

        public Builder userType(String userType) {
            this.userType = userType;
            return this;
        }

        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder permCodes(List<String> permCodes) {
            this.permCodes = permCodes;
            return this;
        }

        public Builder loginTime(Long loginTime) {
            this.loginTime = loginTime;
            return this;
        }

        public UserInfoBean build() {
            return new UserInfoBean(userGuid, userAccount, userName, userStatus, 
                                   userType, roles, permCodes, loginTime);
        }
    }
}

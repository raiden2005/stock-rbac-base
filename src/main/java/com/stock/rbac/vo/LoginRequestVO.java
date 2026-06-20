package com.stock.rbac.vo;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestVO {

    @NotBlank(message = "账号不能为空")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    private String userPwd;

    private String loginClient = "pc";

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getLoginClient() {
        return loginClient;
    }

    public void setLoginClient(String loginClient) {
        this.loginClient = loginClient;
    }
}

package com.stock.rbac.auth.service;

import com.stock.rbac.vo.LoginRequestVO;
import com.stock.rbac.vo.LoginResponseVO;
import com.stock.rbac.vo.UserInfoBean;
import com.stock.rbac.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

    UserInfoBean getUserInfoByGuid(String userGuid);

    LoginResponseVO login(LoginRequestVO vo, HttpServletRequest request);

    void logout(HttpServletRequest request, HttpServletResponse response);

    UserInfoVO getCurrentUserInfo();
}

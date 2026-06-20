package com.stock.rbac.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserContext {

    private static final ThreadLocal<Map<String, Object>> CONTEXT_HOLDER = new ThreadLocal<>();

    private static final String KEY_USER_GUID = "userGuid";
    private static final String KEY_USER_ACCOUNT = "userAccount";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_TYPE = "userType";
    private static final String KEY_ROLES = "roles";
    private static final String KEY_PERM_CODES = "permCodes";

    private UserContext() {
    }

    private static Map<String, Object> getContext() {
        Map<String, Object> context = CONTEXT_HOLDER.get();
        if (context == null) {
            context = new HashMap<>();
            CONTEXT_HOLDER.set(context);
        }
        return context;
    }

    public static void setUserGuid(String userGuid) {
        getContext().put(KEY_USER_GUID, userGuid);
    }

    public static String getUserGuid() {
        Object value = getContext().get(KEY_USER_GUID);
        return value == null ? null : String.valueOf(value);
    }

    public static void setUserAccount(String userAccount) {
        getContext().put(KEY_USER_ACCOUNT, userAccount);
    }

    public static String getUserAccount() {
        Object value = getContext().get(KEY_USER_ACCOUNT);
        return value == null ? null : String.valueOf(value);
    }

    public static void setUserName(String userName) {
        getContext().put(KEY_USER_NAME, userName);
    }

    public static String getUserName() {
        Object value = getContext().get(KEY_USER_NAME);
        return value == null ? null : String.valueOf(value);
    }

    public static void setUserType(String userType) {
        getContext().put(KEY_USER_TYPE, userType);
    }

    public static String getUserType() {
        Object value = getContext().get(KEY_USER_TYPE);
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    public static void setRoles(List<String> roles) {
        getContext().put(KEY_ROLES, roles);
    }

    @SuppressWarnings("unchecked")
    public static List<String> getRoles() {
        Object value = getContext().get(KEY_ROLES);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static void setPermCodes(List<String> permCodes) {
        getContext().put(KEY_PERM_CODES, permCodes);
    }

    @SuppressWarnings("unchecked")
    public static List<String> getPermCodes() {
        Object value = getContext().get(KEY_PERM_CODES);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return new ArrayList<>();
    }

    public static boolean isGuest() {
        return getUserGuid() == null;
    }

    public static boolean isLogin() {
        return getUserGuid() != null;
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}

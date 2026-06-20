package com.stock.rbac.constant;

public class RedisConstant {

    public static final String RBAC_GLOBAL_PERMISSION = "rbac:global:permission";

    public static final String RBAC_USER_ROLE_PREFIX = "rbac:user:role:";

    public static final String VISIBILITY_PREFIX = "visibility:";

    public static final String AUTH_JWT_PREFIX = "auth:jwt:";

    public static final String AUTH_PC_SESSION_PREFIX = "auth:pc:session:";

    public static final String PERM_VERSION_KEY = "rbac:perm:version";

    public static final long RBAC_CACHE_EXPIRE = 3600L;

    public static final long VISIBILITY_CACHE_EXPIRE = 7200L;

    public static final long JWT_CACHE_EXPIRE = 604800L;
}

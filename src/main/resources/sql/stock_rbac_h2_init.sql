-- 创建用户表
CREATE TABLE IF NOT EXISTS sys_user (
    user_guid VARCHAR(64) PRIMARY KEY,
    user_account VARCHAR(100) NOT NULL UNIQUE,
    user_name VARCHAR(100),
    user_pwd_bcrypt VARCHAR(255) NOT NULL,
    dept_id VARCHAR(64),
    user_status INT DEFAULT 1,
    user_type VARCHAR(50),
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建角色表
CREATE TABLE IF NOT EXISTS sys_role (
    role_id VARCHAR(64) PRIMARY KEY,
    role_code VARCHAR(100) NOT NULL UNIQUE,
    role_name VARCHAR(100),
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id VARCHAR(64) PRIMARY KEY,
    user_guid VARCHAR(64) NOT NULL,
    role_id VARCHAR(64) NOT NULL,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    perm_id VARCHAR(64) PRIMARY KEY,
    perm_code VARCHAR(100) NOT NULL UNIQUE,
    perm_name VARCHAR(100),
    perm_type VARCHAR(50),
    status INT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id VARCHAR(64) PRIMARY KEY,
    role_id VARCHAR(64) NOT NULL,
    perm_id VARCHAR(64) NOT NULL,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建业务可见规则表
CREATE TABLE IF NOT EXISTS biz_visible_rule (
    rule_id VARCHAR(64) PRIMARY KEY,
    user_guid VARCHAR(64),
    role_id VARCHAR(64),
    resource_type VARCHAR(100),
    resource_id VARCHAR(255),
    status INT DEFAULT 1,
    valid_start TIMESTAMP,
    valid_end TIMESTAMP,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建权限版本表
CREATE TABLE IF NOT EXISTS sys_permission_version (
    id VARCHAR(64) PRIMARY KEY,
    perm_type VARCHAR(50),
    version_no BIGINT DEFAULT 1,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建审计日志表
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id VARCHAR(64) PRIMARY KEY,
    user_guid VARCHAR(64),
    user_account VARCHAR(100),
    user_name VARCHAR(100),
    module VARCHAR(100),
    operation VARCHAR(100),
    request_method VARCHAR(20),
    request_url VARCHAR(500),
    request_params CLOB,
    response_code VARCHAR(20),
    response_msg VARCHAR(500),
    client_ip VARCHAR(50),
    user_agent VARCHAR(500),
    duration_ms BIGINT,
    error_msg CLOB,
    deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入测试用户 (DataInitializer会在启动时创建这些用户，使用正确的BCrypt哈希)

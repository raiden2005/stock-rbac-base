-- ============================================================
-- 通用商用RBAC认证授权基座 V1.0 数据库建表脚本
-- 适用数据库：H2 内存数据库（开发环境）
-- ============================================================

-- ------------------------------------------------------------
-- 表1：sys_user 用户主表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    user_guid      VARCHAR(64)  NOT NULL           COMMENT '用户全局唯一ID(主键)',
    user_account   VARCHAR(64)  NOT NULL           COMMENT '登录账号',
    user_name      VARCHAR(128) NOT NULL           COMMENT '用户显示名称',
    user_pwd_bcrypt VARCHAR(255) NOT NULL          COMMENT 'BCrypt加密后的密码',
    dept_id        VARCHAR(64)      NULL           COMMENT '所属部门ID',
    user_status    TINYINT   NOT NULL DEFAULT 1 COMMENT '账号状态:1启用0禁用',
    user_type      VARCHAR(16)  NOT NULL DEFAULT 'normal' COMMENT '用户类型:admin/pc/mobile/guest',
    create_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted        TINYINT   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (user_guid),
    UNIQUE (user_account)
);

-- ------------------------------------------------------------
-- 表2：sys_user_role 用户角色关联表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
    user_guid    VARCHAR(64)  NOT NULL           COMMENT '用户GUID',
    role_id      VARCHAR(64)  NOT NULL           COMMENT '角色ID',
    create_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE (user_guid, role_id)
);

-- ------------------------------------------------------------
-- 表3：sys_role 角色表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    role_id      VARCHAR(64)  NOT NULL           COMMENT '角色ID(主键)',
    role_name    VARCHAR(128) NOT NULL           COMMENT '角色名称',
    role_code    VARCHAR(64)  NOT NULL           COMMENT '角色编码(英文唯一)',
    role_desc    VARCHAR(255)     NULL           COMMENT '角色描述',
    sort         INT      NOT NULL DEFAULT 0 COMMENT '排序号,数字越小越靠前',
    status       TINYINT   NOT NULL DEFAULT 1 COMMENT '角色状态:1启用0禁用',
    create_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (role_id),
    UNIQUE (role_code)
);

-- ------------------------------------------------------------
-- 表4：sys_permission 功能权限表(菜单/按钮/接口)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    perm_id      VARCHAR(64)  NOT NULL           COMMENT '权限ID(主键)',
    parent_id    VARCHAR(64)  NOT NULL DEFAULT '0' COMMENT '父级权限ID,顶级为0',
    perm_name    VARCHAR(128) NOT NULL           COMMENT '权限名称',
    perm_code    VARCHAR(128) NOT NULL           COMMENT '权限标识符(英文唯一,如user:add)',
    perm_type    VARCHAR(16)  NOT NULL DEFAULT 'menu' COMMENT '权限类型:menu菜单/button按钮/api接口',
    perm_url     VARCHAR(255)     NULL           COMMENT '权限对应URL(菜单路由或接口路径)',
    sort         INT      NOT NULL DEFAULT 0 COMMENT '排序号',
    icon         VARCHAR(64)      NULL           COMMENT '图标标识',
    status       TINYINT   NOT NULL DEFAULT 1 COMMENT '权限状态:1启用0禁用',
    create_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT   NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
    PRIMARY KEY (perm_id),
    UNIQUE (perm_code)
);

-- ------------------------------------------------------------
-- 表5：sys_role_permission 角色-权限关联表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
    role_id      VARCHAR(64)  NOT NULL           COMMENT '角色ID',
    perm_id      VARCHAR(64)  NOT NULL           COMMENT '权限ID',
    create_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE (role_id, perm_id)
);

-- ------------------------------------------------------------
-- 表6：biz_visible_rule 用户私有资源可见权限表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS biz_visible_rule;
CREATE TABLE biz_visible_rule (
    rule_id      VARCHAR(64)  NOT NULL           COMMENT '规则ID(主键)',
    user_guid    VARCHAR(64)  NOT NULL           COMMENT '用户GUID',
    resource_type VARCHAR(64) NOT NULL           COMMENT '资源类型:stock/file/report等',
    resource_id  VARCHAR(128) NOT NULL           COMMENT '资源ID(如股票代码、文件ID)',
    visible_type VARCHAR(16)  NOT NULL DEFAULT 'read' COMMENT '可见类型:read只读/edit可编辑',
    valid_start  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效开始时间',
    valid_end    TIMESTAMP     NOT NULL DEFAULT '2099-12-31 23:59:59' COMMENT '失效时间',
    status       TINYINT   NOT NULL DEFAULT 1 COMMENT '规则状态:1启用0禁用',
    create_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (rule_id),
    UNIQUE (user_guid, resource_type, resource_id)
);

-- ------------------------------------------------------------
-- 表7：sys_permission_version 权限版本管控表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_permission_version;
CREATE TABLE sys_permission_version (
    id           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
    perm_type    VARCHAR(32)  NOT NULL           COMMENT '权限类型:function功能权限/visible数据权限',
    version_no   BIGINT   NOT NULL DEFAULT 1 COMMENT '当前版本号,自增',
    update_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE (perm_type)
);

-- ------------------------------------------------------------
-- 表8：sys_audit_log 操作审计日志表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_audit_log;
CREATE TABLE sys_audit_log (
    log_id       VARCHAR(64)  NOT NULL           COMMENT '日志ID(主键)',
    user_guid    VARCHAR(64)      NULL           COMMENT '操作人用户GUID',
    user_account VARCHAR(64)      NULL           COMMENT '操作人账号',
    user_name    VARCHAR(128)     NULL           COMMENT '操作人姓名',
    oper_module  VARCHAR(128) NOT NULL           COMMENT '操作模块',
    oper_type    VARCHAR(64)  NOT NULL           COMMENT '操作类型:login/logout/add/update/delete/assign',
    oper_desc    VARCHAR(512)     NULL           COMMENT '操作描述',
    request_uri  VARCHAR(255)     NULL           COMMENT '请求URI',
    request_method VARCHAR(16)    NULL           COMMENT '请求方法:GET/POST/PUT/DELETE',
    request_ip   VARCHAR(64)      NULL           COMMENT '请求IP',
    request_param TEXT             NULL           COMMENT '请求参数(JSON)',
    response_code INT         NULL           COMMENT '响应状态码',
    exec_time    BIGINT       NULL           COMMENT '执行耗时(ms)',
    create_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (log_id)
);

-- ============================================================
-- 初始化数据
-- ============================================================

-- 初始化权限版本号
INSERT INTO sys_permission_version (perm_type, version_no) VALUES ('function', 1);
INSERT INTO sys_permission_version (perm_type, version_no) VALUES ('visible', 1);

-- 初始化默认超级管理员角色
INSERT INTO sys_role (role_id, role_name, role_code, role_desc, sort, status)
VALUES ('ROLE_SUPER_ADMIN', '超级管理员', 'SUPER_ADMIN', '系统最高权限角色,拥有所有权限', 1, 1);

INSERT INTO sys_role (role_id, role_name, role_code, role_desc, sort, status)
VALUES ('ROLE_NORMAL_USER', '普通用户', 'NORMAL_USER', '普通业务用户角色', 10, 1);

-- 初始化默认超级管理员账号(密码:admin123,已BCrypt加密)
-- 密码明文为 admin123,实际部署请立即修改
INSERT INTO sys_user (user_guid, user_account, user_name, user_pwd_bcrypt, dept_id, user_status, user_type)
VALUES ('USER_ADMIN_001', 'admin', '系统管理员',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'DEPT_ROOT', 1, 'admin');

-- 绑定管理员角色
INSERT INTO sys_user_role (user_guid, role_id) VALUES ('USER_ADMIN_001', 'ROLE_SUPER_ADMIN');

-- 初始化核心菜单/权限数据
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_ROOT', '0', '系统管理', 'system', 'menu', '/system', 1, 1);

INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_USER_MGR', 'PERM_ROOT', '用户管理', 'system:user', 'menu', '/system/user', 1, 1);

INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_USER_ADD', 'PERM_USER_MGR', '用户新增', 'system:user:add', 'button', '', 1, 1);

INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_USER_EDIT', 'PERM_USER_MGR', '用户编辑', 'system:user:edit', 'button', '', 2, 1);

INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_USER_DEL', 'PERM_USER_MGR', '用户删除', 'system:user:delete', 'button', '', 3, 1);

INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_ROLE_MGR', 'PERM_ROOT', '角色管理', 'system:role', 'menu', '/system/role', 2, 1);

INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_ROLE_ADD', 'PERM_ROLE_MGR', '角色新增', 'system:role:add', 'button', '', 1, 1);

INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_ROLE_ASSIGN', 'PERM_ROLE_MGR', '分配权限', 'system:role:assign', 'button', '', 2, 1);

INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_PERM_MGR', 'PERM_ROOT', '权限管理', 'system:perm', 'menu', '/system/permission', 3, 1);

INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_VISIBLE_MGR', 'PERM_ROOT', '数据权限', 'system:visible', 'menu', '/system/visible', 4, 1);

-- 给超级管理员分配所有顶层权限
INSERT INTO sys_role_permission (role_id, perm_id) VALUES
('ROLE_SUPER_ADMIN', 'PERM_ROOT'),
('ROLE_SUPER_ADMIN', 'PERM_USER_MGR'),
('ROLE_SUPER_ADMIN', 'PERM_USER_ADD'),
('ROLE_SUPER_ADMIN', 'PERM_USER_EDIT'),
('ROLE_SUPER_ADMIN', 'PERM_USER_DEL'),
('ROLE_SUPER_ADMIN', 'PERM_ROLE_MGR'),
('ROLE_SUPER_ADMIN', 'PERM_ROLE_ADD'),
('ROLE_SUPER_ADMIN', 'PERM_ROLE_ASSIGN'),
('ROLE_SUPER_ADMIN', 'PERM_PERM_MGR'),
('ROLE_SUPER_ADMIN', 'PERM_VISIBLE_MGR');

-- ============================================================
-- 建表脚本执行完成
-- ============================================================

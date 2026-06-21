-- ============================================================
-- Stock RBAC 认证授权基座 - H2 内存数据库建表脚本
-- 字段命名遵循 MyBatis-Plus map-underscore-to-camel-case
-- ============================================================

-- 清理旧数据
DROP TABLE IF EXISTS sys_user_role CASCADE;
DROP TABLE IF EXISTS sys_role_permission CASCADE;
DROP TABLE IF EXISTS sys_user CASCADE;
DROP TABLE IF EXISTS sys_role CASCADE;
DROP TABLE IF EXISTS sys_permission CASCADE;
DROP TABLE IF EXISTS biz_visible_rule CASCADE;
DROP TABLE IF EXISTS sys_permission_version CASCADE;
DROP TABLE IF EXISTS sys_audit_log CASCADE;

-- ------------------------------------------------------------
-- 表1: sys_user 用户主表
-- ------------------------------------------------------------
CREATE TABLE sys_user (
    user_guid       VARCHAR(64)  NOT NULL,
    user_account    VARCHAR(64)  NOT NULL,
    user_name       VARCHAR(128) NOT NULL,
    user_pwd_bcrypt VARCHAR(255) NOT NULL,
    dept_id         VARCHAR(64)      NULL,
    user_status     INT          NOT NULL DEFAULT 1,
    user_type       VARCHAR(16)  NOT NULL DEFAULT 'normal',
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (user_guid),
    UNIQUE (user_account)
);

-- ------------------------------------------------------------
-- 表2: sys_role 角色表
-- ------------------------------------------------------------
CREATE TABLE sys_role (
    role_id      VARCHAR(64)  NOT NULL,
    role_name    VARCHAR(128) NOT NULL,
    role_code    VARCHAR(64)  NOT NULL,
    role_desc    VARCHAR(255)     NULL,
    sort         INT          NOT NULL DEFAULT 0,
    status       INT          NOT NULL DEFAULT 1,
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted      INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (role_id),
    UNIQUE (role_code)
);

-- ------------------------------------------------------------
-- 表3: sys_user_role 用户-角色关联表
-- ------------------------------------------------------------
CREATE TABLE sys_user_role (
    id           VARCHAR(64)  NOT NULL,
    user_guid    VARCHAR(64)  NOT NULL,
    role_id      VARCHAR(64)  NOT NULL,
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted      INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (user_guid, role_id)
);

-- ------------------------------------------------------------
-- 表4: sys_permission 功能权限表
-- ------------------------------------------------------------
CREATE TABLE sys_permission (
    perm_id      VARCHAR(64)  NOT NULL,
    parent_id    VARCHAR(64)  NOT NULL DEFAULT '0',
    perm_name    VARCHAR(128) NOT NULL,
    perm_code    VARCHAR(128) NOT NULL,
    perm_type    VARCHAR(16)  NOT NULL DEFAULT 'menu',
    perm_url     VARCHAR(255)     NULL,
    sort         INT          NOT NULL DEFAULT 0,
    icon         VARCHAR(64)      NULL,
    status       INT          NOT NULL DEFAULT 1,
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted      INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (perm_id),
    UNIQUE (perm_code)
);

-- ------------------------------------------------------------
-- 表5: sys_role_permission 角色-权限关联表
-- ------------------------------------------------------------
CREATE TABLE sys_role_permission (
    id           VARCHAR(64)  NOT NULL,
    role_id      VARCHAR(64)  NOT NULL,
    perm_id      VARCHAR(64)  NOT NULL,
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted      INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (role_id, perm_id)
);

-- ------------------------------------------------------------
-- 表6: biz_visible_rule 业务可见权限规则表
-- ------------------------------------------------------------
CREATE TABLE biz_visible_rule (
    rule_id      VARCHAR(64)  NOT NULL,
    user_guid    VARCHAR(64)      NULL,
    role_id      VARCHAR(64)      NULL,
    rule_type    VARCHAR(32)      NULL,
    category_id  VARCHAR(64)      NULL,
    rule_name    VARCHAR(128)     NULL,
    dept_id      VARCHAR(64)      NULL,
    resource_type VARCHAR(64) NOT NULL,
    resource_id  VARCHAR(128) NOT NULL,
    visible_type VARCHAR(16)  NOT NULL DEFAULT 'read',
    valid_start  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_end    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status       INT          NOT NULL DEFAULT 1,
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted      INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (rule_id)
);

-- ------------------------------------------------------------
-- 表7: sys_permission_version 权限版本管控表
-- ------------------------------------------------------------
CREATE TABLE sys_permission_version (
    id           VARCHAR(64)  NOT NULL,
    perm_type    VARCHAR(32)  NOT NULL,
    version_no   BIGINT       NOT NULL DEFAULT 1,
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted      INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (perm_type)
);

-- ------------------------------------------------------------
-- 表8: sys_audit_log 操作审计日志表
-- ------------------------------------------------------------
CREATE TABLE sys_audit_log (
    log_id        VARCHAR(64)  NOT NULL,
    user_guid     VARCHAR(64)      NULL,
    user_account  VARCHAR(64)      NULL,
    user_name     VARCHAR(128)     NULL,
    oper_module   VARCHAR(128) NOT NULL,
    oper_type     VARCHAR(64)  NOT NULL,
    oper_desc     VARCHAR(512)     NULL,
    request_uri   VARCHAR(255)     NULL,
    request_method VARCHAR(16)     NULL,
    request_ip    VARCHAR(64)      NULL,
    request_param CLOB             NULL,
    response_code INT              NULL,
    exec_time     BIGINT           NULL,
    create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (log_id)
);

-- ============================================================
-- 初始化数据
-- ============================================================

INSERT INTO sys_permission_version (id, perm_type, version_no) VALUES ('PV001', 'function', 1);
INSERT INTO sys_permission_version (id, perm_type, version_no) VALUES ('PV002', 'visible', 1);

INSERT INTO sys_role (role_id, role_name, role_code, role_desc, sort, status)
VALUES ('ROLE_SUPER_ADMIN', '超级管理员', 'SUPER_ADMIN', '系统最高权限角色', 1, 1);
INSERT INTO sys_role (role_id, role_name, role_code, role_desc, sort, status)
VALUES ('ROLE_NORMAL_USER', '普通用户', 'NORMAL_USER', '普通业务用户角色', 10, 1);

-- 注意：admin 用户由 DataInitializer 自动创建（使用实际的 BCrypt 哈希）
-- DataInitializer 同时会把 admin 绑定到 ROLE_SUPER_ADMIN

-- 权限数据
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
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_AUDIT_MGR', 'PERM_ROOT', '审计日志', 'system:audit', 'menu', '/system/audit', 5, 1);

INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP001', 'ROLE_SUPER_ADMIN', 'PERM_ROOT');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP002', 'ROLE_SUPER_ADMIN', 'PERM_USER_MGR');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP003', 'ROLE_SUPER_ADMIN', 'PERM_USER_ADD');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP004', 'ROLE_SUPER_ADMIN', 'PERM_USER_EDIT');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP005', 'ROLE_SUPER_ADMIN', 'PERM_USER_DEL');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP006', 'ROLE_SUPER_ADMIN', 'PERM_ROLE_MGR');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP007', 'ROLE_SUPER_ADMIN', 'PERM_ROLE_ADD');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP008', 'ROLE_SUPER_ADMIN', 'PERM_ROLE_ASSIGN');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP009', 'ROLE_SUPER_ADMIN', 'PERM_PERM_MGR');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP010', 'ROLE_SUPER_ADMIN', 'PERM_VISIBLE_MGR');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP011', 'ROLE_SUPER_ADMIN', 'PERM_AUDIT_MGR');

-- ============================================================
-- 新增表结构 - 扩展模块
-- ============================================================

DROP TABLE IF EXISTS sys_tenant CASCADE;
DROP TABLE IF EXISTS sys_plan_config CASCADE;
DROP TABLE IF EXISTS sys_tenant_question_stat CASCADE;
DROP TABLE IF EXISTS sys_question_order CASCADE;
DROP TABLE IF EXISTS sys_sub_order CASCADE;
DROP TABLE IF EXISTS sys_sub_bill CASCADE;
DROP TABLE IF EXISTS sys_tenant_log CASCADE;

CREATE TABLE sys_tenant (
    tenant_id       VARCHAR(64)  NOT NULL,
    tenant_name     VARCHAR(128) NOT NULL,
    tenant_code     VARCHAR(64)  NOT NULL,
    contact_person  VARCHAR(64)      NULL,
    contact_phone   VARCHAR(32)      NULL,
    contact_email   VARCHAR(128)     NULL,
    plan_type       VARCHAR(32)  NOT NULL DEFAULT 'STANDARD',
    max_account_num INT          NOT NULL DEFAULT 50,
    valid_start     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valid_end       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenant_status   INT          NOT NULL DEFAULT 1,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (tenant_id),
    UNIQUE (tenant_code)
);

CREATE TABLE sys_plan_config (
    id                      VARCHAR(64)  NOT NULL,
    plan_type               VARCHAR(32)  NOT NULL DEFAULT 'STANDARD',
    year_sub_price          DECIMAL(10,2) NOT NULL DEFAULT 500.00,
    monthly_free_question_num INT        NOT NULL DEFAULT 3,
    over_question_unit_price DECIMAL(10,2) NOT NULL DEFAULT 20.00,
    create_time             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                 INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE sys_tenant_question_stat (
    id                  VARCHAR(64)  NOT NULL,
    tenant_id           VARCHAR(64)  NOT NULL,
    stat_month          VARCHAR(10)  NOT NULL,
    free_use_num        INT          NOT NULL DEFAULT 0,
    pay_use_num         INT          NOT NULL DEFAULT 0,
    surplus_pay_question INT         NOT NULL DEFAULT 0,
    create_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted             INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (tenant_id, stat_month)
);

-- 用户提交的提问记录（用户页面历史问题列表）
CREATE TABLE sys_question (
    id               VARCHAR(64)  NOT NULL,
    tenant_id        VARCHAR(64)  NOT NULL,
    user_guid        VARCHAR(64)      NULL,
    user_account     VARCHAR(64)      NULL,
    title            VARCHAR(200) NOT NULL,
    question_content TEXT         NOT NULL,
    reply_content    TEXT             NULL,
    status           INT          NOT NULL DEFAULT 0,
    pay_type         INT          NOT NULL DEFAULT 0,
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted          INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE sys_question_order (
    id              VARCHAR(64)  NOT NULL,
    order_no        VARCHAR(64)  NOT NULL,
    tenant_id       VARCHAR(64)  NOT NULL,
    buy_question_num INT          NOT NULL,
    unit_price      DECIMAL(10,2) NOT NULL,
    total_amount    DECIMAL(10,2) NOT NULL,
    pay_type        VARCHAR(32)      NULL,
    order_status    INT          NOT NULL DEFAULT 0,
    pay_time        TIMESTAMP        NULL,
    out_trade_no    VARCHAR(128)     NULL,
    remark          VARCHAR(255)     NULL,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (order_no)
);

CREATE TABLE sys_sub_order (
    id              VARCHAR(64)  NOT NULL,
    order_no        VARCHAR(64)  NOT NULL,
    tenant_id       VARCHAR(64)  NOT NULL,
    plan_type       VARCHAR(32)  NOT NULL,
    subscribe_year  INT          NOT NULL,
    unit_price      DECIMAL(10,2) NOT NULL,
    total_amount    DECIMAL(10,2) NOT NULL,
    pay_type        VARCHAR(32)      NULL,
    order_status    INT          NOT NULL DEFAULT 0,
    pay_time        TIMESTAMP        NULL,
    out_trade_no    VARCHAR(128)     NULL,
    order_category  INT          NOT NULL DEFAULT 1,
    remark          VARCHAR(255)     NULL,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (order_no)
);

CREATE TABLE sys_sub_bill (
    id              VARCHAR(64)  NOT NULL,
    bill_no         VARCHAR(64)  NOT NULL,
    tenant_id       VARCHAR(64)  NOT NULL,
    order_id        VARCHAR(64)  NOT NULL,
    bill_category   INT          NOT NULL DEFAULT 1,
    bill_amount     DECIMAL(10,2) NOT NULL,
    bill_status     INT          NOT NULL DEFAULT 1,
    bill_period     VARCHAR(20)      NULL,
    bill_content    VARCHAR(512)     NULL,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (bill_no)
);

CREATE TABLE sys_tenant_log (
    id              VARCHAR(64)  NOT NULL,
    tenant_id       VARCHAR(64)  NOT NULL,
    operator        VARCHAR(64)      NULL,
    operate_type    VARCHAR(32)  NOT NULL,
    operate_content VARCHAR(512)     NULL,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- ============================================================
-- 扩展模块初始化数据
-- ============================================================

INSERT INTO sys_tenant (tenant_id, tenant_code, tenant_name, plan_type, max_account_num, tenant_status)
VALUES ('TENANT_DEFAULT', 'DEFAULT', '默认租户', 'STANDARD', 50, 1);

INSERT INTO sys_plan_config (id, plan_type, year_sub_price, monthly_free_question_num, over_question_unit_price)
VALUES ('PC001', 'STANDARD', 500.00, 3, 20.00);

INSERT INTO sys_tenant_question_stat (id, tenant_id, stat_month, free_use_num, pay_use_num, surplus_pay_question)
VALUES ('QS001', 'TENANT_DEFAULT', '2026-06', 0, 0, 0);

INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_PLAN_MGR', 'PERM_ROOT', '套餐管理', 'sys:plan', 'menu', '/system/plan', 6, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_PLAN_CONFIG', 'PERM_PLAN_MGR', '套餐配置', 'sys:config:plan', 'button', '', 1, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_TENANT_MGR', 'PERM_ROOT', '租户管理', 'sys:tenant', 'menu', '/system/tenant', 7, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_TENANT_LIST', 'PERM_TENANT_MGR', '租户列表', 'sys:tenant:list', 'button', '', 1, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_TENANT_ADD', 'PERM_TENANT_MGR', '租户新增', 'sys:tenant:add', 'button', '', 2, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_TENANT_EDIT', 'PERM_TENANT_MGR', '租户编辑', 'sys:tenant:edit', 'button', '', 3, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_ORDER_MGR', 'PERM_ROOT', '订单管理', 'sys:order', 'menu', '/system/order', 8, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_ORDER_LIST', 'PERM_ORDER_MGR', '订单列表', 'sys:order:list', 'button', '', 1, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_BILL_MGR', 'PERM_ROOT', '账单管理', 'sys:bill', 'menu', '/system/bill', 9, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_BILL_LIST', 'PERM_BILL_MGR', '账单列表', 'sys:bill:list', 'button', '', 1, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_QUESTION_STAT', 'PERM_ROOT', '提问统计', 'sys:question:stat', 'menu', '/system/questionStat', 10, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_QUESTION_ORDER', 'PERM_ORDER_MGR', '增值订单', 'sys:order:question', 'button', '', 2, 1);

INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP012', 'ROLE_SUPER_ADMIN', 'PERM_PLAN_MGR');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP013', 'ROLE_SUPER_ADMIN', 'PERM_PLAN_CONFIG');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP014', 'ROLE_SUPER_ADMIN', 'PERM_TENANT_MGR');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP015', 'ROLE_SUPER_ADMIN', 'PERM_TENANT_LIST');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP016', 'ROLE_SUPER_ADMIN', 'PERM_TENANT_ADD');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP017', 'ROLE_SUPER_ADMIN', 'PERM_TENANT_EDIT');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP018', 'ROLE_SUPER_ADMIN', 'PERM_ORDER_MGR');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP019', 'ROLE_SUPER_ADMIN', 'PERM_ORDER_LIST');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP020', 'ROLE_SUPER_ADMIN', 'PERM_BILL_MGR');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP021', 'ROLE_SUPER_ADMIN', 'PERM_BILL_LIST');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP022', 'ROLE_SUPER_ADMIN', 'PERM_QUESTION_STAT');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP023', 'ROLE_SUPER_ADMIN', 'PERM_QUESTION_ORDER');

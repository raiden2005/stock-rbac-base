-- ============================================================
-- Stock RBAC 认证授权基座 数据库建表脚本 (MySQL 8.0+)
-- 字段命名遵循 MyBatis-Plus map-underscore-to-camel-case
-- ============================================================

-- ------------------------------------------------------------
-- 表1: sys_user 用户主表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    user_guid       VARCHAR(64)  NOT NULL               COMMENT '用户全局唯一ID(主键)',
    user_account    VARCHAR(64)  NOT NULL               COMMENT '登录账号',
    user_name       VARCHAR(128) NOT NULL               COMMENT '用户显示名称',
    user_pwd_bcrypt VARCHAR(255) NOT NULL               COMMENT 'BCrypt加密后的密码',
    dept_id         VARCHAR(64)      NULL               COMMENT '所属部门ID',
    user_status     TINYINT      NOT NULL DEFAULT 1     COMMENT '账号状态:1启用0禁用',
    user_type       VARCHAR(16)  NOT NULL DEFAULT 'normal' COMMENT '用户类型:admin/pc/mobile/guest',
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (user_guid),
    UNIQUE KEY uk_user_account (user_account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ------------------------------------------------------------
-- 表2: sys_role 角色表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    role_id      VARCHAR(64)  NOT NULL               COMMENT '角色ID(主键)',
    role_name    VARCHAR(128) NOT NULL               COMMENT '角色名称',
    role_code    VARCHAR(64)  NOT NULL               COMMENT '角色编码(英文唯一)',
    role_desc    VARCHAR(255)     NULL               COMMENT '角色描述',
    sort         INT          NOT NULL DEFAULT 0     COMMENT '排序号,数字越小越靠前',
    status       TINYINT      NOT NULL DEFAULT 1     COMMENT '角色状态:1启用0禁用',
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (role_id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ------------------------------------------------------------
-- 表3: sys_user_role 用户-角色关联表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id           VARCHAR(64)  NOT NULL               COMMENT '主键ID',
    user_guid    VARCHAR(64)  NOT NULL               COMMENT '用户GUID',
    role_id      VARCHAR(64)  NOT NULL               COMMENT '角色ID',
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_guid, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ------------------------------------------------------------
-- 表4: sys_permission 功能权限表(菜单/按钮/接口)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    perm_id      VARCHAR(64)  NOT NULL               COMMENT '权限ID(主键)',
    parent_id    VARCHAR(64)  NOT NULL DEFAULT '0'   COMMENT '父级权限ID,顶级为0',
    perm_name    VARCHAR(128) NOT NULL               COMMENT '权限名称',
    perm_code    VARCHAR(128) NOT NULL               COMMENT '权限标识符(英文唯一,如system:user:add)',
    perm_type    VARCHAR(16)  NOT NULL DEFAULT 'menu' COMMENT '权限类型:menu菜单/button按钮/api接口',
    perm_url     VARCHAR(255)     NULL               COMMENT '权限对应URL(菜单路由或接口路径)',
    sort         INT          NOT NULL DEFAULT 0     COMMENT '排序号',
    icon         VARCHAR(64)      NULL               COMMENT '图标标识',
    status       TINYINT      NOT NULL DEFAULT 1     COMMENT '权限状态:1启用0禁用',
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (perm_id),
    UNIQUE KEY uk_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ------------------------------------------------------------
-- 表5: sys_role_permission 角色-权限关联表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id           VARCHAR(64)  NOT NULL               COMMENT '主键ID',
    role_id      VARCHAR(64)  NOT NULL               COMMENT '角色ID',
    perm_id      VARCHAR(64)  NOT NULL               COMMENT '权限ID',
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_perm (role_id, perm_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ------------------------------------------------------------
-- 表6: biz_visible_rule 用户私有资源可见权限表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS biz_visible_rule;
CREATE TABLE biz_visible_rule (
    rule_id      VARCHAR(64)  NOT NULL               COMMENT '规则ID(主键)',
    user_guid    VARCHAR(64)      NULL               COMMENT '用户GUID(用户级规则填此项)',
    role_id      VARCHAR(64)      NULL               COMMENT '角色ID(角色级规则填此项)',
    rule_type    VARCHAR(32)      NULL               COMMENT '规则类型',
    category_id  VARCHAR(64)      NULL               COMMENT '分类ID',
    rule_name    VARCHAR(128)     NULL               COMMENT '规则名称',
    dept_id      VARCHAR(64)      NULL               COMMENT '部门ID',
    resource_type VARCHAR(64) NOT NULL               COMMENT '资源类型:stock/file/report等',
    resource_id  VARCHAR(128) NOT NULL               COMMENT '资源ID(如股票代码、文件ID)',
    visible_type VARCHAR(16)  NOT NULL DEFAULT 'read' COMMENT '可见类型:read只读/edit可编辑',
    valid_start  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生效开始时间',
    valid_end    TIMESTAMP    NOT NULL DEFAULT '2099-12-31 23:59:59' COMMENT '失效时间',
    status       TINYINT      NOT NULL DEFAULT 1     COMMENT '规则状态:1启用0禁用',
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (rule_id),
    KEY idx_user_guid (user_guid),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务可见权限规则表';

-- ------------------------------------------------------------
-- 表7: sys_permission_version 权限版本管控表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_permission_version;
CREATE TABLE sys_permission_version (
    id           VARCHAR(64)  NOT NULL               COMMENT '主键ID',
    perm_type    VARCHAR(32)  NOT NULL               COMMENT '权限类型:function功能权限/visible数据权限',
    version_no   BIGINT       NOT NULL DEFAULT 1     COMMENT '当前版本号,自增',
    create_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted      TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_type (perm_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限版本表';

-- ------------------------------------------------------------
-- 表8: sys_audit_log 操作审计日志表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_audit_log;
CREATE TABLE sys_audit_log (
    log_id        VARCHAR(64)  NOT NULL               COMMENT '日志ID(主键)',
    user_guid     VARCHAR(64)      NULL               COMMENT '操作人用户GUID',
    user_account  VARCHAR(64)      NULL               COMMENT '操作人账号',
    user_name     VARCHAR(128)     NULL               COMMENT '操作人姓名',
    oper_module   VARCHAR(128) NOT NULL               COMMENT '操作模块',
    oper_type     VARCHAR(64)  NOT NULL               COMMENT '操作类型:login/logout/add/update/delete/assign',
    oper_desc     VARCHAR(512)     NULL               COMMENT '操作描述',
    request_uri   VARCHAR(255)     NULL               COMMENT '请求URI',
    request_method VARCHAR(16)     NULL               COMMENT '请求方法:GET/POST/PUT/DELETE',
    request_ip    VARCHAR(64)      NULL               COMMENT '请求IP',
    request_param TEXT             NULL               COMMENT '请求参数(JSON)',
    response_code INT              NULL               COMMENT '响应状态码',
    exec_time     BIGINT           NULL               COMMENT '执行耗时(ms)',
    create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (log_id),
    KEY idx_user_guid (user_guid),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 初始化权限版本号
INSERT INTO sys_permission_version (id, perm_type, version_no) VALUES ('PV001', 'function', 1);
INSERT INTO sys_permission_version (id, perm_type, version_no) VALUES ('PV002', 'visible', 1);

-- 初始化默认超级管理员角色
INSERT INTO sys_role (role_id, role_name, role_code, role_desc, sort, status)
VALUES ('ROLE_SUPER_ADMIN', '超级管理员', 'SUPER_ADMIN', '系统最高权限角色,拥有所有权限', 1, 1);

INSERT INTO sys_role (role_id, role_name, role_code, role_desc, sort, status)
VALUES ('ROLE_NORMAL_USER', '普通用户', 'NORMAL_USER', '普通业务用户角色', 10, 1);

-- 注意：admin 用户由 DataInitializer 自动创建（使用实际的 BCrypt 哈希）
-- DataInitializer 同时会把 admin 绑定到 ROLE_SUPER_ADMIN

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

INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_AUDIT_MGR', 'PERM_ROOT', '审计日志', 'system:audit', 'menu', '/system/audit', 5, 1);

-- 给超级管理员分配所有顶层权限
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

-- ------------------------------------------------------------
-- 表A: sys_tenant 租户表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_tenant;
CREATE TABLE sys_tenant (
    tenant_id       VARCHAR(64)  NOT NULL               COMMENT '租户ID(主键)',
    tenant_name     VARCHAR(128) NOT NULL               COMMENT '租户名称',
    tenant_code     VARCHAR(64)  NOT NULL               COMMENT '租户编码(唯一)',
    contact_person  VARCHAR(64)      NULL               COMMENT '联系人',
    contact_phone   VARCHAR(32)      NULL               COMMENT '联系电话',
    contact_email   VARCHAR(128)     NULL               COMMENT '联系邮箱',
    plan_type       VARCHAR(32)  NOT NULL DEFAULT 'STANDARD' COMMENT '套餐类型',
    max_account_num INT          NOT NULL DEFAULT 50    COMMENT '最大账号数',
    valid_start     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '有效期开始',
    valid_end       TIMESTAMP    NOT NULL DEFAULT '2099-12-31 23:59:59' COMMENT '有效期结束',
    tenant_status   TINYINT      NOT NULL DEFAULT 1     COMMENT '状态:1启用0冻结',
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (tenant_id),
    UNIQUE KEY uk_tenant_code (tenant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- ------------------------------------------------------------
-- 表B: sys_plan_config 全局套餐配置表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_plan_config;
CREATE TABLE sys_plan_config (
    id                      VARCHAR(64)  NOT NULL               COMMENT '主键ID',
    plan_type               VARCHAR(32)  NOT NULL DEFAULT 'STANDARD' COMMENT '套餐编码',
    year_sub_price          DECIMAL(10,2) NOT NULL DEFAULT 500.00 COMMENT '年度订阅价格',
    monthly_free_question_num INT        NOT NULL DEFAULT 3     COMMENT '每月免费提问次数',
    over_question_unit_price DECIMAL(10,2) NOT NULL DEFAULT 20.00 COMMENT '超额单题单价',
    create_time             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                 TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局套餐配置表';

-- ------------------------------------------------------------
-- 表C: sys_tenant_question_stat 租户提问统计表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_tenant_question_stat;
CREATE TABLE sys_tenant_question_stat (
    id                  VARCHAR(64)  NOT NULL               COMMENT '主键ID',
    tenant_id           VARCHAR(64)  NOT NULL               COMMENT '租户ID',
    stat_month          VARCHAR(10)  NOT NULL               COMMENT '统计月份 yyyy-MM',
    free_use_num        INT          NOT NULL DEFAULT 0     COMMENT '本月免费已用',
    pay_use_num         INT          NOT NULL DEFAULT 0     COMMENT '本月付费已用',
    surplus_pay_question INT         NOT NULL DEFAULT 0     COMMENT '剩余永久付费存量',
    create_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted             TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_month (tenant_id, stat_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户提问统计表';

-- ------------------------------------------------------------
-- 表D1: sys_question 用户提问记录表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_question;
CREATE TABLE sys_question (
    id               VARCHAR(64)  NOT NULL               COMMENT '主键ID',
    tenant_id        VARCHAR(64)  NOT NULL               COMMENT '租户ID',
    user_guid        VARCHAR(64)      NULL               COMMENT '提交用户GUID',
    user_account     VARCHAR(64)      NULL               COMMENT '提交用户账号',
    title            VARCHAR(200) NOT NULL               COMMENT '问题标题',
    question_content TEXT         NOT NULL               COMMENT '问题正文',
    reply_content    TEXT             NULL               COMMENT '回复内容',
    status           TINYINT      NOT NULL DEFAULT 0     COMMENT '状态:0待回复1已回复',
    pay_type         TINYINT      NOT NULL DEFAULT 0     COMMENT '0免费1付费',
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    KEY idx_tenant_time (tenant_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户提问记录表';

-- ------------------------------------------------------------
-- 表D: sys_question_order 增值提问订单表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_question_order;
CREATE TABLE sys_question_order (
    id              VARCHAR(64)  NOT NULL               COMMENT '主键ID',
    order_no        VARCHAR(64)  NOT NULL               COMMENT '订单号',
    tenant_id       VARCHAR(64)  NOT NULL               COMMENT '租户ID',
    buy_question_num INT          NOT NULL               COMMENT '购买题目数',
    unit_price      DECIMAL(10,2) NOT NULL               COMMENT '单价快照',
    total_amount    DECIMAL(10,2) NOT NULL               COMMENT '订单总额',
    pay_type        VARCHAR(32)      NULL               COMMENT '支付方式',
    order_status    TINYINT      NOT NULL DEFAULT 0     COMMENT '状态:0待支付1已支付2取消3过期',
    pay_time        TIMESTAMP        NULL               COMMENT '支付时间',
    out_trade_no    VARCHAR(128)     NULL               COMMENT '第三方流水号',
    remark          VARCHAR(255)     NULL               COMMENT '备注',
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='增值提问订单表';

-- ------------------------------------------------------------
-- 表E: sys_sub_order 订阅订单表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_sub_order;
CREATE TABLE sys_sub_order (
    id              VARCHAR(64)  NOT NULL               COMMENT '主键ID',
    order_no        VARCHAR(64)  NOT NULL               COMMENT '订单号',
    tenant_id       VARCHAR(64)  NOT NULL               COMMENT '租户ID',
    plan_type       VARCHAR(32)  NOT NULL               COMMENT '套餐类型',
    subscribe_year  INT          NOT NULL               COMMENT '订阅年数',
    unit_price      DECIMAL(10,2) NOT NULL               COMMENT '单价快照',
    total_amount    DECIMAL(10,2) NOT NULL               COMMENT '订单总额',
    pay_type        VARCHAR(32)      NULL               COMMENT '支付方式',
    order_status    TINYINT      NOT NULL DEFAULT 0     COMMENT '状态:0待支付1已支付2取消3过期',
    pay_time        TIMESTAMP        NULL               COMMENT '支付时间',
    out_trade_no    VARCHAR(128)     NULL               COMMENT '第三方流水号',
    order_category  INT          NOT NULL DEFAULT 1     COMMENT '订单类别:1年费订阅',
    remark          VARCHAR(255)     NULL               COMMENT '备注',
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订阅订单表';

-- ------------------------------------------------------------
-- 表F: sys_sub_bill 账单表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_sub_bill;
CREATE TABLE sys_sub_bill (
    id              VARCHAR(64)  NOT NULL               COMMENT '主键ID',
    bill_no         VARCHAR(64)  NOT NULL               COMMENT '账单号',
    tenant_id       VARCHAR(64)  NOT NULL               COMMENT '租户ID',
    order_id        VARCHAR(64)  NOT NULL               COMMENT '关联订单ID',
    bill_category   INT          NOT NULL DEFAULT 1     COMMENT '账单类别:1年费订阅2增值提问',
    bill_amount     DECIMAL(10,2) NOT NULL               COMMENT '账单金额',
    bill_status     TINYINT      NOT NULL DEFAULT 1     COMMENT '状态:1正常',
    bill_period     VARCHAR(20)      NULL               COMMENT '账期',
    bill_content    VARCHAR(512)     NULL               COMMENT '账单内容',
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    UNIQUE KEY uk_bill_no (bill_no),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单表';

-- ------------------------------------------------------------
-- 表G: sys_tenant_log 租户操作日志表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS sys_tenant_log;
CREATE TABLE sys_tenant_log (
    id              VARCHAR(64)  NOT NULL               COMMENT '主键ID',
    tenant_id       VARCHAR(64)  NOT NULL               COMMENT '租户ID',
    operator        VARCHAR(64)      NULL               COMMENT '操作人',
    operate_type    VARCHAR(32)  NOT NULL               COMMENT '操作类型',
    operate_content VARCHAR(512)     NULL               COMMENT '操作内容',
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户操作日志表';

-- ============================================================
-- 扩展模块初始化数据
-- ============================================================

INSERT INTO sys_tenant (tenant_id, tenant_code, tenant_name, plan_type, max_account_num, valid_end, tenant_status)
VALUES ('TENANT_DEFAULT', 'DEFAULT', '默认租户', 'STANDARD', 50, '2099-12-31 23:59:59', 1);

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

-- ============================================================
-- 建表脚本执行完成
-- ============================================================

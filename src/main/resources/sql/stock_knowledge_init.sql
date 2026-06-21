-- ============================================================
-- Stock RBAC 知识库模块 数据库建表脚本 (MySQL 8.0+)
-- Stock-RBAC向量知识库增强LLM问答模块
-- ============================================================

-- ------------------------------------------------------------
-- 表1: stock_knowledge 知识库主表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS stock_knowledge;
CREATE TABLE stock_knowledge (
    id               VARCHAR(64)  NOT NULL               COMMENT '知识ID(主键)',
    title            VARCHAR(200) NOT NULL               COMMENT '知识标题',
    category         VARCHAR(64)      NULL               COMMENT '知识分类',
    source_type      VARCHAR(32)  NOT NULL DEFAULT 'text' COMMENT '来源类型:text文本/file文件',
    original_file_url VARCHAR(512)    NULL               COMMENT '原始文件URL(file类型时有值)',
    total_slice_num  INT          NOT NULL DEFAULT 0     COMMENT '切片总数',
    hit_count        INT          NOT NULL DEFAULT 0     COMMENT '命中次数',
    weight           DECIMAL(5,2) NOT NULL DEFAULT 1.00  COMMENT '权重(0.01~10.00)',
    status           TINYINT      NOT NULL DEFAULT 1     COMMENT '状态:1上架0下架',
    create_user      VARCHAR(64)      NULL               COMMENT '创建人',
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted          TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    KEY idx_category (category),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库主表';

-- ------------------------------------------------------------
-- 表2: stock_knowledge_slice 知识切片明细表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS stock_knowledge_slice;
CREATE TABLE stock_knowledge_slice (
    id               VARCHAR(64)  NOT NULL               COMMENT '切片ID(主键)',
    knowledge_id     VARCHAR(64)  NOT NULL               COMMENT '关联知识ID',
    segment_content  TEXT         NOT NULL               COMMENT '切片文本内容',
    milvus_vector_id VARCHAR(64)      NULL               COMMENT 'Milvus向量ID(已入库时填写)',
    hit_count        INT          NOT NULL DEFAULT 0     COMMENT '命中次数',
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted          TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除标记',
    PRIMARY KEY (id),
    KEY idx_knowledge_id (knowledge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识切片明细表';

-- ------------------------------------------------------------
-- 表3: sys_question 表增加RAG相关字段
-- ------------------------------------------------------------
ALTER TABLE sys_question ADD COLUMN reference_knowledge TEXT NULL COMMENT 'RAG引用的知识溯源(JSON格式)' AFTER reply_content;
ALTER TABLE sys_question ADD COLUMN full_rag_prompt TEXT NULL COMMENT 'RAG完整增强Prompt(调试用)' AFTER reference_knowledge;

-- ============================================================
-- 初始化知识库相关权限数据
-- ============================================================

-- 知识库管理菜单（挂在系统管理下）
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_KNOWLEDGE_MGR', 'PERM_ROOT', '知识库管理', 'stock:knowledge:manage', 'menu', '/system/knowledge', 11, 1);

-- 知识库管理按钮权限
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_KNOWLEDGE_ADD', 'PERM_KNOWLEDGE_MGR', '知识新增', 'stock:knowledge:add', 'button', '', 1, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_KNOWLEDGE_EDIT', 'PERM_KNOWLEDGE_MGR', '知识编辑', 'stock:knowledge:edit', 'button', '', 2, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_KNOWLEDGE_DELETE', 'PERM_KNOWLEDGE_MGR', '知识删除', 'stock:knowledge:delete', 'button', '', 3, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_KNOWLEDGE_STATUS', 'PERM_KNOWLEDGE_MGR', '知识上下架', 'stock:knowledge:status', 'button', '', 4, 1);
INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_KNOWLEDGE_ADMIN', 'PERM_KNOWLEDGE_MGR', '知识库管理', 'stock:knowledge:admin', 'button', '', 5, 1);

-- 给超级管理员分配知识库权限
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP024', 'ROLE_SUPER_ADMIN', 'PERM_KNOWLEDGE_MGR');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP025', 'ROLE_SUPER_ADMIN', 'PERM_KNOWLEDGE_ADD');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP026', 'ROLE_SUPER_ADMIN', 'PERM_KNOWLEDGE_EDIT');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP027', 'ROLE_SUPER_ADMIN', 'PERM_KNOWLEDGE_DELETE');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP028', 'ROLE_SUPER_ADMIN', 'PERM_KNOWLEDGE_STATUS');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP029', 'ROLE_SUPER_ADMIN', 'PERM_KNOWLEDGE_ADMIN');

-- ============================================================
-- 建表脚本执行完成
-- ============================================================

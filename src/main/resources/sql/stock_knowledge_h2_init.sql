-- ============================================================
-- Stock RBAC 知识库模块 - H2 内存数据库建表脚本
-- Stock-RBAC向量知识库增强LLM问答模块
-- ============================================================

-- 清理旧知识库表
DROP TABLE IF EXISTS stock_knowledge_slice CASCADE;
DROP TABLE IF EXISTS stock_knowledge CASCADE;

-- ------------------------------------------------------------
-- 表1: stock_knowledge 知识库主表
-- ------------------------------------------------------------
CREATE TABLE stock_knowledge (
    id               VARCHAR(64)  NOT NULL,
    title            VARCHAR(200) NOT NULL,
    category         VARCHAR(64)      NULL,
    source_type      VARCHAR(32)  NOT NULL DEFAULT 'text',
    original_file_url VARCHAR(512)    NULL,
    total_slice_num  INT          NOT NULL DEFAULT 0,
    hit_count        INT          NOT NULL DEFAULT 0,
    weight           DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    status           INT          NOT NULL DEFAULT 1,
    create_user      VARCHAR(64)      NULL,
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted          INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

-- ------------------------------------------------------------
-- 表2: stock_knowledge_slice 知识切片明细表
-- ------------------------------------------------------------
CREATE TABLE stock_knowledge_slice (
    id               VARCHAR(64)  NOT NULL,
    knowledge_id     VARCHAR(64)  NOT NULL,
    segment_content  CLOB         NOT NULL,
    milvus_vector_id VARCHAR(64)      NULL,
    hit_count        INT          NOT NULL DEFAULT 0,
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted          INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

-- ------------------------------------------------------------
-- 表3: sys_question 表增加RAG相关字段（H2兼容写法）
-- ------------------------------------------------------------
ALTER TABLE sys_question ADD COLUMN IF NOT EXISTS reference_knowledge CLOB NULL;
ALTER TABLE sys_question ADD COLUMN IF NOT EXISTS full_rag_prompt CLOB NULL;

-- ============================================================
-- 初始化知识库相关权限数据
-- ============================================================

INSERT INTO sys_permission (perm_id, parent_id, perm_name, perm_code, perm_type, perm_url, sort, status)
VALUES ('PERM_KNOWLEDGE_MGR', 'PERM_ROOT', '知识库管理', 'stock:knowledge:manage', 'menu', '/system/knowledge', 11, 1);

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

INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP024', 'ROLE_SUPER_ADMIN', 'PERM_KNOWLEDGE_MGR');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP025', 'ROLE_SUPER_ADMIN', 'PERM_KNOWLEDGE_ADD');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP026', 'ROLE_SUPER_ADMIN', 'PERM_KNOWLEDGE_EDIT');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP027', 'ROLE_SUPER_ADMIN', 'PERM_KNOWLEDGE_DELETE');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP028', 'ROLE_SUPER_ADMIN', 'PERM_KNOWLEDGE_STATUS');
INSERT INTO sys_role_permission (id, role_id, perm_id) VALUES ('RP029', 'ROLE_SUPER_ADMIN', 'PERM_KNOWLEDGE_ADMIN');

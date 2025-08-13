-- 创建智能业务规则配置表
CREATE TABLE IF NOT EXISTS business_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_description VARCHAR(500) COMMENT '规则描述',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型：KEYWORD_TRIGGER, MESSAGE_FORWARD, AUTO_REPLY',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级（数字越小优先级越高）',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    start_time TIME COMMENT '生效开始时间',
    end_time TIME COMMENT '生效结束时间',
    effective_days VARCHAR(20) COMMENT '生效日期：1,2,3,4,5,6,7 (周一到周日)',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator_id BIGINT COMMENT '创建者ID',
    updater_id BIGINT COMMENT '更新者ID',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_rule_type (rule_type),
    INDEX idx_enabled (enabled),
    INDEX idx_priority (priority)
) COMMENT='智能业务规则配置表';

-- 创建规则触发条件表
CREATE TABLE IF NOT EXISTS rule_condition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    rule_id BIGINT NOT NULL COMMENT '关联规则ID',
    condition_type VARCHAR(50) NOT NULL COMMENT '条件类型：KEYWORD_MATCH, PHRASE_MATCH, REGEX_MATCH',
    condition_value TEXT NOT NULL COMMENT '条件值（关键词、短语或正则表达式）',
    match_mode VARCHAR(20) DEFAULT 'CONTAINS' COMMENT '匹配模式：CONTAINS, EQUALS, STARTS_WITH, ENDS_WITH',
    case_sensitive BOOLEAN DEFAULT FALSE COMMENT '是否区分大小写',
    weight DECIMAL(3,2) DEFAULT 1.0 COMMENT '条件权重',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (rule_id) REFERENCES business_rule(id) ON DELETE CASCADE,
    INDEX idx_rule_id (rule_id),
    INDEX idx_condition_type (condition_type)
) COMMENT='规则触发条件表';

-- 创建规则动作配置表
CREATE TABLE IF NOT EXISTS rule_action (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    rule_id BIGINT NOT NULL COMMENT '关联规则ID',
    action_type VARCHAR(50) NOT NULL COMMENT '动作类型：MESSAGE_FORWARD, AUTO_REPLY, CREATE_TICKET, NOTIFICATION',
    action_config TEXT NOT NULL COMMENT '动作配置（JSON格式）',
    execution_order INT DEFAULT 1 COMMENT '执行顺序',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (rule_id) REFERENCES business_rule(id) ON DELETE CASCADE,
    INDEX idx_rule_id (rule_id),
    INDEX idx_action_type (action_type)
) COMMENT='规则动作配置表';

-- 创建规则执行日志表
CREATE TABLE IF NOT EXISTS rule_execution_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    rule_id BIGINT NOT NULL COMMENT '关联规则ID',
    message_id BIGINT COMMENT '关联消息ID',
    trigger_content TEXT COMMENT '触发内容',
    matched_conditions TEXT COMMENT '匹配的条件（JSON格式）',
    executed_actions TEXT COMMENT '执行的动作（JSON格式）',
    execution_result VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '执行结果：SUCCESS, FAILED, PARTIAL',
    error_message TEXT COMMENT '错误信息',
    execution_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    processing_duration INT COMMENT '处理耗时（毫秒）',
    FOREIGN KEY (rule_id) REFERENCES business_rule(id),
    INDEX idx_rule_id (rule_id),
    INDEX idx_execution_time (execution_time),
    INDEX idx_execution_result (execution_result)
) COMMENT='规则执行日志表';

-- 创建规则链配置表
CREATE TABLE IF NOT EXISTS rule_chain (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    chain_name VARCHAR(100) NOT NULL COMMENT '规则链名称',
    chain_description VARCHAR(500) COMMENT '规则链描述',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator_id BIGINT COMMENT '创建者ID',
    updater_id BIGINT COMMENT '更新者ID',
    INDEX idx_enabled (enabled)
) COMMENT='规则链配置表';

-- 创建规则链关联表
CREATE TABLE IF NOT EXISTS rule_chain_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    chain_id BIGINT NOT NULL COMMENT '规则链ID',
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    execution_order INT NOT NULL COMMENT '执行顺序',
    condition_logic VARCHAR(10) DEFAULT 'AND' COMMENT '条件逻辑：AND, OR',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (chain_id) REFERENCES rule_chain(id) ON DELETE CASCADE,
    FOREIGN KEY (rule_id) REFERENCES business_rule(id) ON DELETE CASCADE,
    UNIQUE KEY uk_chain_rule (chain_id, rule_id),
    INDEX idx_chain_id (chain_id),
    INDEX idx_execution_order (execution_order)
) COMMENT='规则链关联表';

-- 插入默认的业务规则示例
INSERT INTO business_rule (rule_name, rule_description, rule_type, priority, enabled, effective_days) VALUES
('停电关键词自动转发', '检测到停电相关关键词时自动转发给运维人员', 'MESSAGE_FORWARD', 1, TRUE, '1,2,3,4,5,6,7'),
('故障报修自动回复', '用户报修故障时自动回复处理流程', 'AUTO_REPLY', 2, TRUE, '1,2,3,4,5,6,7'),
('电费查询引导', '用户查询电费时提供查询指引', 'AUTO_REPLY', 3, TRUE, '1,2,3,4,5,6,7');

-- 插入默认的触发条件
INSERT INTO rule_condition (rule_id, condition_type, condition_value, match_mode) VALUES
(1, 'KEYWORD_MATCH', '停电,断电,没电,跳闸', 'CONTAINS'),
(2, 'KEYWORD_MATCH', '故障,报修,维修,坏了', 'CONTAINS'),
(3, 'KEYWORD_MATCH', '电费,缴费,账单,用电量', 'CONTAINS');

-- 插入默认的动作配置
INSERT INTO rule_action (rule_id, action_type, action_config, execution_order) VALUES
(1, 'MESSAGE_FORWARD', '{"targetUsers":["admin"],"message":"检测到停电相关消息，请及时处理","includeOriginal":true}', 1),
(2, 'AUTO_REPLY', '{"replyMessage":"您好，我们已收到您的故障报修信息。请提供详细的故障描述和联系方式，我们将尽快安排工作人员处理。","delay":0}', 1),
(3, 'AUTO_REPLY', '{"replyMessage":"您可以通过以下方式查询电费：\n1. 关注我们的微信公众号\n2. 登录官方网站\n3. 拨打客服热线95598\n如需帮助请随时联系我们。","delay":0}', 1);

-- 创建默认规则链
INSERT INTO rule_chain (chain_name, chain_description, enabled) VALUES
('客户服务规则链', '处理客户服务相关消息的规则链', TRUE),
('故障处理规则链', '处理故障报修相关消息的规则链', TRUE);

-- 配置规则链关联
INSERT INTO rule_chain_relation (chain_id, rule_id, execution_order, condition_logic) VALUES
(1, 2, 1, 'OR'),
(1, 3, 2, 'OR'),
(2, 1, 1, 'AND');
package com.kms.domain.aggregation.knowledge.port;

import com.kms.domain.common.port.Port;

/**
 * LLM调用端口接口
 * 领域层定义，基础设施层实现
 * 负责调用大语言模型API获取回答
 */
public interface IllmClientPort extends Port {

    /**
     * 调用LLM获取回答
     *
     * @param prompt 完整的Prompt(包含系统指令、知识、用户问题)
     * @return LLM的回答文本
     */
    String chat(String prompt);

    /**
     * 调用LLM获取回答(带系统指令)
     *
     * @param systemPrompt 系统指令
     * @param userMessage   用户消息
     * @return LLM的回答文本
     */
    String chat(String systemPrompt, String userMessage);

    /**
     * 检查LLM服务是否可用
     *
     * @return 是否可用
     */
    boolean isAvailable();
}

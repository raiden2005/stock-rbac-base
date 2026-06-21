package com.kms.domain.aggregation.knowledge.port;

import com.kms.domain.common.port.Port;

/**
 * 文件解析端口接口
 * 领域层定义，基础设施层实现
 * 负责解析不同格式的文件，提取纯文本内容
 */
public interface IFileParsePort extends Port {

    /**
     * 解析文件为纯文本
     *
     * @param filePath 文件路径(本地路径或URL)
     * @param fileName 文件名(用于判断文件类型)
     * @return 解析后的纯文本内容
     */
    String parse(String filePath, String fileName);

    /**
     * 判断文件类型是否支持
     *
     * @param fileName 文件名
     * @return 是否支持
     */
    boolean isSupported(String fileName);

    /**
     * 获取支持的文件类型列表
     *
     * @return 文件扩展名列表(如: txt, docx, pdf)
     */
    String[] getSupportedTypes();
}

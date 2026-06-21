package com.stock.rbac.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本清洗与智能切片服务
 * 按字数切片，默认500字/片，重叠50字
 */
@Service
public class KnowledgeSliceService {

    /** 默认切片长度 */
    public static final int DEFAULT_SLICE_LENGTH = 500;

    /** 默认重叠长度 */
    public static final int DEFAULT_OVERLAP = 50;

    /**
     * 智能文本切片
     * 按字数切片，保留重叠区域，确保语义连续性
     *
     * @param text     原始文本
     * @param sliceLen 切片长度
     * @param overlap  重叠长度
     * @return 切片文本列表
     */
    public List<String> sliceText(String text, int sliceLen, int overlap) {
        List<String> slices = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return slices;
        }

        String cleaned = cleanText(text);
        if (cleaned.isEmpty()) {
            return slices;
        }

        int textLen = cleaned.length();
        if (textLen <= sliceLen) {
            slices.add(cleaned);
            return slices;
        }

        int step = sliceLen - overlap;
        if (step <= 0) {
            step = sliceLen;
        }

        int start = 0;
        while (start < textLen) {
            int end = Math.min(start + sliceLen, textLen);

            // 尝试在句号、换行等位置断句
            if (end < textLen) {
                int breakPos = findBreakPosition(cleaned, end, 50);
                if (breakPos > start) {
                    end = breakPos;
                }
            }

            String slice = cleaned.substring(start, end).trim();
            if (!slice.isEmpty()) {
                slices.add(slice);
            }

            start = end;
            // 避免无限循环
            if (end >= textLen) {
                break;
            }
        }

        return slices;
    }

    /**
     * 使用默认参数切片
     */
    public List<String> sliceText(String text) {
        return sliceText(text, DEFAULT_SLICE_LENGTH, DEFAULT_OVERLAP);
    }

    /**
     * 文本清洗
     * 去除多余空白、特殊字符、不可见字符等
     *
     * @param text 原始文本
     * @return 清洗后文本
     */
    public String cleanText(String text) {
        if (text == null) {
            return "";
        }

        // 去除BOM标记
        String cleaned = text.replace("\uFEFF", "");

        // 统一换行符
        cleaned = cleaned.replace("\r\n", "\n").replace("\r", "\n");

        // 去除连续多个空行为单个空行
        cleaned = cleaned.replaceAll("\n{3,}", "\n\n");

        // 去除行首行尾空白
        String[] lines = cleaned.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                sb.append(trimmed).append("\n");
            }
        }

        String result = sb.toString().trim();

        // 去除不可见控制字符(保留换行、制表符)
        result = result.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");

        return result;
    }

    /**
     * 寻找断句位置
     * 在指定位置附近寻找句号、问号、感叹号、换行等自然断句位置
     *
     * @param text    文本
     * @param pos     起始查找位置
     * @param maxLook 最大回溯距离
     * @return 断句位置
     */
    private int findBreakPosition(String text, int pos, int maxLook) {
        int searchStart = Math.max(0, pos - maxLook);
        String subText = text.substring(searchStart, Math.min(pos + 1, text.length()));

        // 优先找句号、问号、感叹号
        int lastPeriod = -1;
        int lastQuestion = -1;
        int lastExclaim = -1;
        int lastNewline = -1;
        int lastComma = -1;

        for (int i = subText.length() - 1; i >= 0; i--) {
            char c = subText.charAt(i);
            if (c == '。' || c == '.' || c == '！' || c == '!') {
                if (lastPeriod < 0) lastPeriod = i;
            }
            if (c == '？' || c == '?') {
                if (lastQuestion < 0) lastQuestion = i;
            }
            if (c == '\n') {
                if (lastNewline < 0) lastNewline = i;
            }
            if (c == '，' || c == ',' || c == '；' || c == ';') {
                if (lastComma < 0) lastComma = i;
            }
        }

        // 按优先级返回
        if (lastPeriod >= 0) return searchStart + lastPeriod + 1;
        if (lastQuestion >= 0) return searchStart + lastQuestion + 1;
        if (lastExclaim >= 0) return searchStart + lastExclaim + 1;
        if (lastNewline >= 0) return searchStart + lastNewline + 1;
        if (lastComma >= 0) return searchStart + lastComma + 1;

        return pos;
    }
}

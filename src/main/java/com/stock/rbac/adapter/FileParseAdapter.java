package com.stock.rbac.adapter;

import com.kms.domain.aggregation.knowledge.port.IFileParsePort;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * 文件解析适配器
 * 实现IFileParsePort，支持TXT/Word/PDF文件解析
 */
@Component
public class FileParseAdapter implements IFileParsePort {

    private static final Logger log = LoggerFactory.getLogger(FileParseAdapter.class);

    private static final String[] SUPPORTED_TYPES = {"txt", "docx", "pdf"};

    /** 文件大小限制: 20MB */
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    @Override
    public String parse(String filePath, String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }

        String extension = getFileExtension(fileName).toLowerCase();
        File file = new File(filePath);

        // 校验文件大小
        if (file.exists() && file.length() > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小超过限制(最大20MB)");
        }

        return switch (extension) {
            case "txt" -> parseTxt(filePath);
            case "docx" -> parseDocx(filePath);
            case "pdf" -> parsePdf(filePath);
            default -> throw new RuntimeException("不支持的文件类型: " + extension);
        };
    }

    @Override
    public boolean isSupported(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        String ext = getFileExtension(fileName).toLowerCase();
        return Arrays.asList(SUPPORTED_TYPES).contains(ext);
    }

    @Override
    public String[] getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    /**
     * 解析TXT文件
     */
    private String parseTxt(String filePath) {
        try {
            return Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("TXT文件解析失败: {}", e.getMessage());
            throw new RuntimeException("TXT文件解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析Word(docx)文件
     */
    private String parseDocx(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            StringBuilder sb = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph para : paragraphs) {
                String text = para.getText();
                if (text != null && !text.trim().isEmpty()) {
                    sb.append(text.trim()).append("\n");
                }
            }
            return sb.toString();
        } catch (IOException e) {
            log.error("Word文件解析失败: {}", e.getMessage());
            throw new RuntimeException("Word文件解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析PDF文件
     */
    private String parsePdf(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath);
             PDDocument document = org.apache.pdfbox.Loader.loadPDF(new java.io.File(filePath));) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("PDF文件解析失败: {}", e.getMessage());
            throw new RuntimeException("PDF文件解析失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }
}

package com.yl.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniqueIdExtractor {
    public static void main(String[] args) {
        String inputFilePath = "C:\\Users\\Administrator\\Desktop\\工作簿3.xlsx";
        String outputFilePath = "C:\\Users\\Administrator\\Desktop\\提取结果_去重4.xlsx";
        
        // 通用正则表达式，匹配多种ID格式
        Pattern pattern = Pattern.compile(
            "[(`]?\\s*`id`\\s*=\\s*'([a-f0-9-]{18,36})'\\s*[)]?", 
            Pattern.CASE_INSENSITIVE);
        
        // 使用LinkedHashSet保持插入顺序并自动去重
        Set<String> uniqueIds = new LinkedHashSet<>();
        
        try (Workbook inputWorkbook = new XSSFWorkbook(new FileInputStream(inputFilePath))) {
            Sheet inputSheet = inputWorkbook.getSheetAt(0);
            
            // 第一遍：提取所有ID并去重
            for (Row row : inputSheet) {
                if (row == null) continue;
                
                Cell cell = row.getCell(0);
                if (cell == null) continue;
                
                String cellValue = getCellValueAsString(cell);
                Matcher matcher = pattern.matcher(cellValue);
                
                while (matcher.find()) {
                    String id = matcher.group(1);
                    uniqueIds.add(id); // 自动去重
                }
            }
            
            System.out.println("共找到 " + uniqueIds.size() + " 个唯一ID");
            
            // 第二遍：将去重后的ID写入新文件
            try (Workbook outputWorkbook = new SXSSFWorkbook(100)) {
                Sheet outputSheet = outputWorkbook.createSheet("唯一ID");
                
                // 添加表头
                Row headerRow = outputSheet.createRow(0);
                headerRow.createCell(0).setCellValue("序号");
                headerRow.createCell(1).setCellValue("唯一ID");
                headerRow.createCell(2).setCellValue("ID类型");
                
                int rowNum = 1;
                for (String id : uniqueIds) {
                    Row row = outputSheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rowNum - 1);
                    row.createCell(1).setCellValue(id);
                    
                    // 判断ID类型
                    String idType = id.matches("\\d{19}") ? "数字ID" : 
                                   id.matches("[a-f0-9]{32}") ? "十六进制ID" : "其他格式";
                    row.createCell(2).setCellValue(idType);
                }

                // 写入输出文件
                try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                    outputWorkbook.write(fos);
                    System.out.println("去重后的ID已保存到: " + outputFilePath);
                }
            }
            
        } catch (Exception e) {
            System.err.println("处理文件时出错:");
            e.printStackTrace();
        }
    }
    
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.format("%.0f", cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
package com.yl.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AllRowsIdExtractor {
    public static void main(String[] args) {
        String inputFilePath = "C:\\Users\\Administrator\\Desktop\\工作簿3.xlsx";
        String outputFilePath = "C:\\Users\\Administrator\\Desktop\\提取结果3.xlsx";
        
        // 增强版正则表达式，匹配多种可能的ID格式
        Pattern pattern = Pattern.compile(
                "[(`]?\\s*`id`\\s*=\\s*'([a-f0-9-]{18,36})'\\s*[)]?",
                Pattern.CASE_INSENSITIVE);
        try (Workbook inputWorkbook = new XSSFWorkbook(new FileInputStream(inputFilePath));
             Workbook outputWorkbook = new SXSSFWorkbook(100)) { // 使用SXSSFWorkbook处理大文件
            
            Sheet inputSheet = inputWorkbook.getSheetAt(0);
            Sheet outputSheet = outputWorkbook.createSheet("提取结果");
            
            // 添加表头
            Row headerRow = outputSheet.createRow(0);
            headerRow.createCell(0).setCellValue("原始行号");
            headerRow.createCell(1).setCellValue("提取的ID");
            headerRow.createCell(2).setCellValue("状态");
            
            int outputRowNum = 1;
            int totalExtracted = 0;
            
            // 遍历所有行
            for (int rowIndex = 0; rowIndex <= inputSheet.getLastRowNum(); rowIndex++) {
                Row row = inputSheet.getRow(rowIndex);
                if (row == null) continue;
                
                Cell cell = row.getCell(0); // 假设ID在第一列
                if (cell == null) {
                    createStatusRow(outputSheet, outputRowNum++, rowIndex+1, "空单元格");
                    continue;
                }
                
                String cellValue = getCellValueAsString(cell);
                Matcher matcher = pattern.matcher(cellValue);
                
                if (matcher.find()) {
                    String id = matcher.group(1);
                    Row outputRow = outputSheet.createRow(outputRowNum++);
                    outputRow.createCell(0).setCellValue(rowIndex + 1); // 原始行号(1-based)
                    outputRow.createCell(1).setCellValue(id);
                    outputRow.createCell(2).setCellValue("成功提取");
                    totalExtracted++;
                    System.out.printf("第 %d 行提取到ID: %s%n", rowIndex+1, id);
                } else {
                    createStatusRow(outputSheet, outputRowNum++, rowIndex+1, "未找到匹配ID");
                    System.out.printf("第 %d 行未找到匹配ID%n", rowIndex+1);
                }
            }
            
            // 自动调整列宽
//            for (int i = 0; i < 3; i++) {
//                outputSheet.autoSizeColumn(i);
//            }
            
            // 写入输出文件
            try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                outputWorkbook.write(fos);
                System.out.printf("处理完成！共处理 %d 行，成功提取 %d 个ID%n", 
                                 inputSheet.getLastRowNum()+1, totalExtracted);
                System.out.println("结果已保存到: " + outputFilePath);
            }
            
        } catch (Exception e) {
            System.err.println("处理文件时出错:");
            e.printStackTrace();
        }
    }
    
    private static String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    private static void createStatusRow(Sheet sheet, int rowNum, int originalRow, String status) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(originalRow);
        row.createCell(2).setCellValue(status);
    }
}
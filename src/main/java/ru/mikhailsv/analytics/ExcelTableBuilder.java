package ru.mikhailsv.analytics;

import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;
import ru.mikhailsv.support.Route;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@NoArgsConstructor
public final class ExcelTableBuilder {
    private XSSFWorkbook currentBook;
    private XSSFSheet currentSheet;
    private XSSFCellStyle currentStyle;

    private void setupTable(String label) {
        currentBook = new XSSFWorkbook();
        currentSheet = currentBook.createSheet(label);
        currentStyle = currentBook.createCellStyle();

        currentStyle.setAlignment(HorizontalAlignment.CENTER);
        currentStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
        currentStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        currentStyle.setLocked(true);
    }

    private void tearDownTable(String filename) throws IOException {
        currentSheet.protectSheet("protect");
        currentBook.write(new FileOutputStream(filename));
        currentBook.close();
    }

    public <K, V> void createTwoColumnTable(Map<K, V> kvMap, String label, String filename,
                String keyColumnName, String valueColumnName) throws IOException {
        setupTable(label);

        XSSFRow row = currentSheet.createRow(0);
        XSSFCell key = row.createCell(0);
        XSSFCell value = row.createCell(1);

        key.setCellValue(keyColumnName);
        value.setCellValue(valueColumnName);

        key.setCellStyle(currentStyle);
        value.setCellStyle(currentStyle);

        int i = 0;
        for (Map.Entry<K, V> entry : kvMap.entrySet()) {
            row = currentSheet.createRow(i + 1);
            row.createCell(0).setCellValue(String.valueOf(entry.getKey()));
            row.createCell(1).setCellValue(String.valueOf(entry.getValue()));
            ++i;
        }
        tearDownTable(filename);
    }

    public <V> void createTableForRoute(Map<Route, V> routeVMap, String label, String filename,
                                         String valueColumnName) throws IOException {
        setupTable(label);

        XSSFRow row = currentSheet.createRow(0);
        XSSFCell from = row.createCell(0);
        XSSFCell to = row.createCell(1);
        XSSFCell value = row.createCell(2);

        from.setCellValue("From");
        to.setCellValue("To");
        value.setCellValue(valueColumnName);

        from.setCellStyle(currentStyle);
        to.setCellStyle(currentStyle);
        value.setCellStyle(currentStyle);

        int i = 0;
        for (Map.Entry<Route, V> entry : routeVMap.entrySet()) {
            row = currentSheet.createRow(i + 1);
            row.createCell(0).setCellValue(String.valueOf(entry.getKey().getDeparture()));
            row.createCell(1).setCellValue(String.valueOf(entry.getKey().getArrival()));
            row.createCell(2).setCellValue(String.valueOf(entry.getValue()));
            ++i;
        }
        tearDownTable(filename);
    }

}

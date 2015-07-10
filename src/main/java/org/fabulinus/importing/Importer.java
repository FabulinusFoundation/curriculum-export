package org.fabulinus.importing;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.fabulinus.model.Content;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Importer extends Thread{
    private final File file;
    private final ImportListener listener;

    public Importer(File file, ImportListener listener) {
        this.file = file;
        this.listener = listener;
    }

    @Override
    public void run() {
        listener.onProgress(-1);
        FileInputStream fileInputStream = null;
        Workbook workbook = null;
        try {
            fileInputStream = new FileInputStream(file);
            workbook = WorkbookFactory.create(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Content content = new Content("Content", null);
            double size = sheet.getPhysicalNumberOfRows();
            for (int i = 1; i < size; i++) {
                processRow(sheet.getRow(i), content);
                listener.onProgress(i/size);
            }
            listener.onImportDone(content);
        } catch (IOException e){
            listener.onError(e.getMessage());
            listener.onProgress(0);
        } catch (IllegalArgumentException | InvalidFormatException e){
            listener.onError("Invalid format");
            listener.onProgress(0);
        } finally {
            performFinalTasks(fileInputStream, workbook);
        }
    }

    private void processRow(Row row, Content root){
        try {
            String topic = row.getCell(0).getStringCellValue();
            String subtopicA = row.getCell(1).getStringCellValue();
            String subtopicB = row.getCell(2).getStringCellValue();
            String subtopicC = row.getCell(3).getStringCellValue();
            root.findOrCreate(topic, subtopicA, subtopicB, subtopicC);
        } catch (Exception e){
            listener.onError(e.getMessage());
        }
    }

    private void performFinalTasks(FileInputStream fileInputStream, Workbook workbook){
        try {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (workbook != null){
                workbook.close();
            }
        } catch (IOException e){
            listener.onError(e.getMessage());
        }
    }
}
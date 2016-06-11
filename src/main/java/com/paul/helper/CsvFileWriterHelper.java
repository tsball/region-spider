package com.paul.helper;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import com.google.common.collect.Lists;

public class CsvFileWriterHelper {
     
    //Delimiter used in CSV file
    private static final String NEW_LINE_SEPARATOR = "\n";
    
    public static <T> void writeCsvFile(String filePath, Object[] columnNames, String[] columnFields, List<T> list) {
        FileWriter fileWriter = null;
        CSVPrinter csvFilePrinter = null;
        //Create the CSVFormat object with "\n" as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
                 
        try {
            //initialize FileWriter object
            fileWriter = new FileWriter(filePath);
            //initialize CSVPrinter object
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
            
            //Create CSV file header
            csvFilePrinter.printRecord(columnNames);

            //Write the rows to csv file
            for (T rowObj : list) {
                List<Object> csvRowColumnsList = Lists.newArrayList();

                // Columns of each row
                for (String columnField : columnFields) {
                	Field field = rowObj.getClass().getDeclaredField(columnField);
                	csvRowColumnsList.add(getFieldValue(rowObj, field));
                }
                
                csvFilePrinter.printRecord(csvRowColumnsList);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static Object getFieldValue(Object object, Field field) {
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}


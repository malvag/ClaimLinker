package csd.thesis.tools;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CSV_Parser {
    private String delimiter;
    private boolean hasfieldNames;
    private boolean hasRowNumbers;

    public CSV_Parser(boolean hasfieldNames, boolean hasRowNumbers, String delimiter) {
        this.delimiter = delimiter;
        this.hasfieldNames = hasfieldNames;
        this.hasRowNumbers = hasRowNumbers;
    }

    public void parseCSV(String pathToCsv) {
        String row = "";
        int count = -1;
        ArrayList<String> fields = new ArrayList<>();
        List<Map<String, String>> list = new ArrayList<>();
        BufferedReader csvReader = null;
        File csvFile = new File(pathToCsv);
        if (csvFile.isFile()) {
            try {

                csvReader = new BufferedReader(new FileReader(pathToCsv));
                while (true) {
                    if ((row = csvReader.readLine()) == null) break;
                    count++;
//                    System.out.println("====" + + "====");
                    String[] row_arr = row.split(this.delimiter);

                    Map<String, String> row_JSON = new LinkedHashMap<>();
                    // if there are field names
                    if (count == 0 && this.hasfieldNames) {
                        //fill the field names
                        for (int i = 0; i < row_arr.length; i++) {
                            fields.add(row_arr[i]);
                        }

                    } else {
                        //get data from each column of the current row := String[] row_arr
                        if (count == 5) {
                            csvReader.close();
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.enable(SerializationFeature.INDENT_OUTPUT);
                            mapper.writeValue(System.out, list);
                            System.exit(1);
                        }
                        if (this.hasfieldNames) {
                            for (int j = 0; j < fields.size(); j++) {
                                //for
                                try {
                                    row_JSON.put(fields.get(j), row_arr[j]);
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    row_JSON.put(fields.get(j), null);
                                }
                            }
                            list.add(row_JSON);
                        }
//                        if (this.hasRowNumbers) {
//                            // the first column will always be a number without field
//                        } else {
//
//                        }
                        // do something with the data
                    }
                }
                csvReader.close();
//                ObjectMapper mapper = new ObjectMapper();
//                mapper.enable(SerializationFeature.INDENT_OUTPUT);
//                mapper.writeValue(System.out, list);
//                System.exit(0);
                System.out.println(list.size());


            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("File path is incorrect\n");

        }
    }


    public void parse2(String pathToCsv) {
        try {
            String row = "";
            ArrayList<String> fields = new ArrayList<>();
            List<Map<String, String>> list = new ArrayList<>();

            File csvFile = new File(pathToCsv);
            Reader in = new FileReader(pathToCsv);
            BufferedReader csvReader = new BufferedReader(in);

            if ((row = csvReader.readLine()) == null) throw new IOException("Cant read file!");
            String[] row_arr = row.split(this.delimiter);

            // if there are field names
            if ( this.hasfieldNames) {
                //fill the field names
                for (int i = 0; i < row_arr.length; i++) {
                    fields.add(row_arr[i]);
                }

            }
            Iterable<CSVRecord> records = CSVFormat.EXCEL
                    .withHeader(fields.toArray(new String[fields.size()]))// due to JVM optimizations,
            .withIgnoreEmptyLines(true).parse(in);

            for (CSVRecord record : records) {
                Map<String, String> row_JSON = new LinkedHashMap<>();
                fields.forEach(header -> {
                    row_JSON.put(header, record.get(header));
                    System.out.println(header + " : " +record.get(header));
                });
                list.add(row_JSON);
            }

            csvReader.close();

//            ObjectMapper mapper = new ObjectMapper();
//            mapper.enable(SerializationFeature.INDENT_OUTPUT);
//            mapper.writeValue(System.out, list);
//            System.exit(1);

        } catch (IOException e) {

        }
    }
}

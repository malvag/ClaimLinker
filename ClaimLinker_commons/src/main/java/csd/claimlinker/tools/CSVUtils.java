package csd.claimlinker.tools;


import java.io.*;
import java.util.*;

public class CSVUtils {

    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';

    private static final int MAX_ENTRIES = 2;

    public ArrayList<Map<String, Object>> parse(String csvFile) throws Exception {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        int count = 0;
        String row;
        ArrayList<String> fields = new ArrayList<>();
        Reader in = new FileReader(csvFile);
        BufferedReader csvReader = new BufferedReader(in);
        Scanner scanner = new Scanner(new File(csvFile));

        if ((row = csvReader.readLine()) == null)
            throw new IOException("Cant read file!");

        String[] row_arr = row.split(",");

        //fill the field names
        Collections.addAll(fields, row_arr);

        while (scanner.hasNext()) {
            Map<String, Object> obj = new LinkedHashMap<>();
            List<String> line = parseLine(scanner.nextLine());
            if (count == 0) {
                count++;
                continue;
            }
            count++;
            if (count > MAX_ENTRIES)
                break;

            if (line.size() > 23)
                continue;

            for (int i = 0; i < line.size(); i++) {
                try {
                    obj.put(fields.get(i), line.get(i));
                } catch (IndexOutOfBoundsException e) {
                }
            }
            list.add(obj);
        }
        scanner.close();
        list.trimToSize();

        System.out.println("Imported " + list.size() + " entries from " + csvFile);
        return list;
    }

    public static List<String> parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators) {
        return parseLine(cvsLine, separators, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators, char customQuote) {

        List<String> result = new ArrayList<>();

        //if empty, return!
        if (Objects.requireNonNull(cvsLine).isEmpty()) {
            return result;
        }

        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = cvsLine.toCharArray();

        for (char ch : chars) {

            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }

                }
            } else {
                if (ch == customQuote) {

                    inQuotes = true;

                    //Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '\"') {
                        curVal.append('"');
                    }

                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }

                } else if (ch == separators) {

                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    //ignore LF characters
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }

        }

        result.add(curVal.toString());

        return result;
    }

}

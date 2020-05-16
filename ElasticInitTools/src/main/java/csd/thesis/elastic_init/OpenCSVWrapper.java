package csd.thesis.elastic_init;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class OpenCSVWrapper {

    private static final boolean debug = false;
    private static final int MAX = 7;
    private String path;
    public OpenCSVWrapper(String path){
        this.path = path;
    }

    public ArrayList<Map<String, Object>> parse() throws Exception {
        Reader reader = new BufferedReader(new FileReader(this.path));
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        ArrayList<String> fields = new ArrayList<>();
        CSVReader csvReader = new CSVReader(reader);
        int counter = 0;
        String[] line = new String[0];
        while (line != null) {
            try {
                line = csvReader.readNext();
            }catch(IOException e){
                break;
            }
            if(OpenCSVWrapper.debug)
                if(counter>=OpenCSVWrapper.MAX)
                    break;

            Map<String,Object> claim = new LinkedHashMap<>();
            if(counter==0){
                // If we are in the first row we include the headers first
                fields.addAll(Arrays.asList(line));
            }else{
                //then we construct a map with < "field-name" : "value" >
                for (int i = 0; i < fields.size(); i++) {
                    if(fields.get(i).equals("extra_entities_body"))
                        line[i] = line[i].replaceAll("\",\"\"",",\"");
                    if(fields.get(i).equals("extra_entities_claimReview_claimReviewed"))
                        line[i] = line[i].replaceAll("\",\"\"",",\"");
                    claim.put(fields.get(i),line[i]);
                }
                // followed by the addition to the list
                list.add(claim);
            }
            counter++;
        }
        reader.close();
        csvReader.close();
        return list;
    }
}

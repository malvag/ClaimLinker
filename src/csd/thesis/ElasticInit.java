package csd.thesis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import csd.thesis.tools.CSVUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElasticInit {
    public static void main(String[] args) throws Exception {
        ArrayList<Map<String, Object>> master;

        CSVUtils csvUtils = new CSVUtils();
        master = csvUtils.parse("data/claim_extraction_18_10_2019_annotated.csv");


        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        for (Map<String, Object> elem : master) {
            //                System.out.println(mapper.writeValueAsString(elem.get("extra_entities_body")));
//            System.out.println(elem);

//            mapper.writeValue(System.out,elem);
            final String regex = "(?<![\\{: \\[\\\\,]) *(?!\\\" ?[,\\]\\}:] *)\\\"";
//            String in = elem.get("")
//            in.replace(regex, "");
//            in.replace("(-?\\d+(?:[\\.,]\\d+)?)\"", "");
//            elem.put("extra_entities_body", in);

//            in =  in.replace("\"", "");
//            in = in.replaceAll( "(?<=\\{|, ?)([a-zA-Z]+?): ?(?![ {\\[])(.+?)(?=,|})", "\"$1\": \"$2\"");
//            in.replaceAll("({|,)?\\s*'?([A-Za-z_$\\.][A-Za-z0-9_ \\-\\.$]*)'?\\s*:\\s*","\"$1\": \"$2\"");
//            List list = Arrays.asList(mapper.readValue(in, Map[].class));

//                ll.put("extra_entities_body",elem.get("extra_entities_body"));
//                System.out.println(ll.get("extra_entities_body"));
//            mapper.writeValue(System.out, list);
        }

    }
}

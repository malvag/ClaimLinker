package csd.thesis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import csd.thesis.model.ViewPoint;
import csd.thesis.model.WebArticle;
import csd.thesis.tools.CSVUtils;
import csd.thesis.tools.NLPlib;
import csd.thesis.tools.URL_Parser;
import csd.thesis.tools.elastic.ElasticWrapper;
import edu.stanford.nlp.util.Pair;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    static boolean default_operation;
    static private String command;
    static private int iter;
    static private final int TOP_ENTRIES_VIEW_MAX = 15;
    static public ViewPoint masterVP;
    static NLPlib nlp_instance;

    public static void main(String[] args) throws Exception {
        System.out.println("Initiating ...");
        ArrayList<Map<String, Object>> master;
//        nlp_instance = new NLPlib(NLPlib.mode.NLP);

        ElasticWrapper elasticWrapper = new ElasticWrapper();


        CSVUtils csvUtils = new CSVUtils();
        master = csvUtils.parse("data/claim_extraction_18_10_2019_annotated.csv");


        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        master.forEach(elem->{
            try {
                System.out.println(mapper.writeValueAsString(elem));
                System.out.println("+++++++++++++++");
                mapper.writeValue(System.out,(List<Map<String, Object>>)elem.get("extra_entities_body"));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });



//        client.close();
        System.out.println("Exiting ...");
    }

    static private void phaceC() {
        masterVP = new ViewPoint();
        Main.getViewPoint("data/data_links_pro.txt");
        Main.getViewPoint("data/data_links_against.txt");
    }

    static public void getViewPoint(String file_path) {
        Main.masterVP.clear();
        ArrayList<WebArticle> parsed_content = null;
        int counter = 0;
        URL_Parser master = null;
        try {
            master = new URL_Parser(null, null, true);
        } catch (Exception e) {
            System.err.println("[URL_Parser] Error on URL parsing");
            e.printStackTrace();
        } finally {
            parsed_content = ((master != null) ? (ArrayList<WebArticle>) master.getContentByComposition(file_path) : null);
        }
        System.out.println("======== Finished Parsing Content ========");
        for (WebArticle a : parsed_content) {
            a.annotate(Main.nlp_instance);

            System.out.println(a.getUrl());
            NLPlib.getAnnotationSentences(a.getDoc(), Main.masterVP);
            System.out.println("======== Finished Article #" + (counter++) + " Annotation ========");
        }
        counter = 0;
        for (Map.Entry<Pair<String, String>, Integer> entry : Main.masterVP.getPairsSortedByValue().entrySet()) {
            Pair<String, String> elem = entry.getKey();
            Integer occ = entry.getValue();
            System.out.println(elem.first + " " + elem.second + " : " + occ);
            if (counter++ > TOP_ENTRIES_VIEW_MAX)
                break;
        }
    }

    /**
     * Parse a page through URL and write it to file Boilerpiped
     *
     * @return parsed_output cleaned(boilerpiped)
     */
    static private String defaultMode(String url) {
        final String filename = "parsed.txt";
        URL_Parser master = null;
        try {
            master = new URL_Parser(url, filename, false);
        } catch (Exception e) {
            System.err.println("[URL_Parser] Error on URL parsing");
            e.printStackTrace();
        } finally {
            return (master != null) ? master.getClean() : "empty";
        }
    }

}
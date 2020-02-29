package csd.thesis;

import csd.thesis.model.ViewPoint;
import csd.thesis.model.WebArticle;
import csd.thesis.tools.CSV_Parser;
import csd.thesis.tools.NLPlib;
import csd.thesis.tools.URL_Parser;
import edu.stanford.nlp.util.Pair;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;

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

    public static void main(String[] args) throws IOException {
        System.out.println("Initiating ...");

//        nlp_instance = new NLPlib(NLPlib.mode.NLP);
//
//        RestHighLevelClient client = new RestHighLevelClient(
//                RestClient.builder(
//                        new HttpHost("localhost", 9200, "http"),
//                        new HttpHost("localhost", 9201, "http")));


        CSV_Parser CSV = new CSV_Parser(true,true,",");
//        CSV.parseCSV("data/claim_extraction_18_10_2019_annotated.csv");
        CSV.parse2("data/claim_front.csv");


//        try (InputStream in = new FileInputStream(csvFile);) {
//            CSV csv = new CSV(true, ',', in );
//            List< String > fieldNames = null;
//            if (csv.hasNext()) fieldNames = new ArrayList < > (csv.next());
//            List < Map < String, String >> list = new ArrayList < > ();
//            while (csv.hasNext()) {
//                List < String > x = csv.next();
//                Map < String, String > obj = new LinkedHashMap < > ();
//                for (int i = 0; i < fieldNames.size(); i++) {
//                    obj.put(fieldNames.get(i), x.get(i));
//                }
//                list.add(obj);
//            }
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.enable(SerializationFeature.INDENT_OUTPUT);
//            mapper.writeValue(System.out, list);
//        }



//        client.close();
        System.out.println("Exiting ...");
    }

    static private void phaceC() {
        masterVP = new ViewPoint();
        Main.getViewPoint("data/data_links_pro.txt");
        Main.getViewPoint("data/data_links_against.txt");
    }

    static public void getViewPoint(String file_path){
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
            if(counter++ > TOP_ENTRIES_VIEW_MAX)
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
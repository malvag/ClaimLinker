package csd.thesis;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import csd.thesis.model.Claim;
import csd.thesis.model.ViewPoint;
import csd.thesis.model.WebArticle;
import csd.thesis.tools.CSVUtils;
import csd.thesis.tools.NLPlib;
import csd.thesis.tools.OpenCSVWrapper;
import csd.thesis.tools.URL_Parser;
import csd.thesis.tools.elastic.ElasticWrapper;
import edu.stanford.nlp.util.Pair;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Synset;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import com.fasterxml.jackson.core.type.TypeReference;
import org.tartarus.snowball.ext.PorterStemmer;

import javax.json.Json;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Main {
    static boolean default_operation;
    static private String command;
    static private int iter;
    static private final int TOP_ENTRIES_VIEW_MAX = 15;
    static public ViewPoint masterVP;

    public static void main(String[] args) throws Exception {
        System.out.println("Initiating ...");
        JWNL.initialize(new FileInputStream("Properties.xml"));
        System.out.println("_______________________________________________");

//        ElasticWrapper elasticWrapper = new ElasticWrapper();
        {
            ArrayList<Map<String, Object>> csv_to_map = new OpenCSVWrapper("data/claim_extraction_18_10_2019_annotated.csv").parse();
            ClaimLinker claimLinker = new ClaimLinker();
//            claimLinker.addClaimsFromCSV(csv_to_map);
            claimLinker.pipeline();
        }
        System.out.println("_______________________________________________");
        System.out.println("Exiting ...");

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
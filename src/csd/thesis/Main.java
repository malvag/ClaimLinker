package csd.thesis;

//import csd.thesis.elastic.OpenCSVWrapper;
import csd.thesis.model.ViewPoint;

import java.util.*;

public class Main {
    static boolean default_operation;
    static private String command;
    static private int iter;
    static private final int TOP_ENTRIES_VIEW_MAX = 15;
    static public ViewPoint masterVP;

    public static void main(String[] args) throws Exception {
        System.out.println("Initiating ...");
        System.out.println("_______________________________________________");

//        ElasticWrapper elasticWrapper = new ElasticWrapper();
        {
//            ArrayList<Map<String, Object>> csv_to_map = new OpenCSVWrapper("data/claim_extraction_18_10_2019_annotated.csv").parse();
            ClaimLinker claimLinker = new ClaimLinker("Properties.xml","data/stopwords.txt");
//            claimLinker_Servlet.addClaimsFromCSV(csv_to_map);
            claimLinker.pipeline();
        }
        System.out.println("_______________________________________________");
        System.out.println("Exiting ...");

    }

}
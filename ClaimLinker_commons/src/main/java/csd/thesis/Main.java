package csd.thesis;

//import csd.thesis.elastic.OpenCSVWrapper;


public class Main {
    static boolean default_operation;
    static private String command;
    static private int iter;
    static private final int TOP_ENTRIES_VIEW_MAX = 15;

    public static void main(String[] args) throws Exception {
        System.out.println("[Demo]Initiating ...");
        System.out.println("_______________________________________________");

        {
            ClaimLinker claimLinker = new ClaimLinker("Properties.xml", "data/stopwords.txt", "data/english-20200420.hash","data/claim_extraction_18_10_2019_annotated.csv");
            claimLinker.demo_pipeline();
        }
        System.out.println("_______________________________________________");
        System.out.println("[Demo]Exiting ...");

    }

}
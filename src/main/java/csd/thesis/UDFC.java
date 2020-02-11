package csd.thesis;

import csd.thesis.model.ViewPoint;
import csd.thesis.model.WebArticle;
import csd.thesis.tools.NLPlib;
import csd.thesis.tools.Parser;
import edu.stanford.nlp.util.Pair;

import java.util.ArrayList;
import java.util.Map;

public class UDFC {
    static boolean default_operation;
    static private String command;
    static private int iter;
    static private final int TOP_ENTRIES_VIEW_MAX = 15;
    static public ViewPoint masterVP;
    static NLPlib nlp_instance;

    public static void main(String[] args) {
        masterVP = new ViewPoint();
        nlp_instance = new NLPlib(NLPlib.mode.NLP);
        console_op();
    }

    static private void console_op() {
        UDFC.getViewPoint("data/data_links_pro.txt");
        UDFC.getViewPoint("data/data_links_against.txt");

//            Scanner in = new Scanner(System.in);
//            String input = in.nextLine();
//            Pattern pattern = Pattern.compile("(^\\w+)[\\s]?(.*)?");
//            Matcher matcher = pattern.matcher(input);
//
//            if (!matcher.find()) continue;
//
//            command = matcher.group(1);
//            if (command.equals("quit") || command.equals("q")) {
//                System.err.println("[Main] exiting...");
//                System.exit(0);
//
//            } else if (command.equals("parse") || command.equals("p")) {
//                System.err.println("\n[Main] parsing command...");
//                if (matcher.group(2).isBlank()) {
//                    System.err.println("[Error] No input for parsing whatsoever!");
//                    continue;
//                }
//                System.out.println(matcher.group(2));
//                parsed_content = defaultMode(matcher.group(2));
//                if(parsed_content.equals("empty"))
//                    continue;
//
//                CoreDocument doc = new CoreDocument(parsed_content); // change doc's content
//
//                nlp.annotate(doc);
//            }
//
//        }

    }

    static public void getViewPoint(String file_path){
        UDFC.masterVP.clear();
        ArrayList<WebArticle> parsed_content = null;
        int counter = 0;
        Parser master = null;
        try {
            master = new Parser(null, null, true);
        } catch (Exception e) {
            System.err.println("[Parser] Error on URL parsing");
            e.printStackTrace();
        } finally {
            parsed_content = ((master != null) ? (ArrayList<WebArticle>) master.getContentByComposition(file_path) : null);
        }
        System.out.println("======== Finished Parsing Content ========");
        for (WebArticle a : parsed_content) {
            a.annotate(UDFC.nlp_instance);

            System.out.println(a.getUrl());
            NLPlib.output_annotation(a.getDoc());
            System.out.println("======== Finished Article #" + (counter++) + " Annotation ========");
        }
        counter = 0;
        for (Map.Entry<Pair<String, String>, Integer> entry : UDFC.masterVP.getPairsSortedByValue().entrySet()) {
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
        Parser master = null;
        try {
            master = new Parser(url, filename, false);
        } catch (Exception e) {
            System.err.println("[Parser] Error on URL parsing");
            e.printStackTrace();
        } finally {
            return (master != null) ? master.getClean() : "empty";
        }
    }

}
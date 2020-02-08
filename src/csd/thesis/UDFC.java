package csd.thesis;

import csd.thesis.model.WebArticle;
import csd.thesis.tools.NLPlib;
import csd.thesis.tools.Parser;

import java.util.ArrayList;

public class UDFC {
    static boolean default_operation;
    static private String command;
    static private int iter;

    public static void main(String[] args) {

        NLPlib nlp = null;//new NLPlib(NLPlib.mode.NLP);
        console_op(nlp);
    }

    static private void console_op(NLPlib nlp) {
        ArrayList<WebArticle> parsed_content = null;
//        while (true) {
//
//            parsed_content = "";
//            iter = 0;
            System.out.print("[Main] UDFC_thesis$ ");
        {
            Parser master = null;
            try {
                master = new Parser(null, null,true);
            } catch (Exception e) {
                System.err.println("[Parser] Error on URL parsing");
                e.printStackTrace();
            } finally {
                parsed_content = ((master != null) ? (ArrayList<WebArticle>) master.getContentByComposition("data/data_links_pro.txt") :null);
            }

            parsed_content.forEach(elem ->{
                System.out.println(elem.getUrl());
//                System.out.println(elem.getRawText());
            });
        }

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


    /**
     * Parse a page through URL and write it to file Boilerpiped
     *
     * @return parsed_output cleaned(boilerpiped)
     */
    static private String defaultMode(String url) {
        final String filename = "parsed.txt";
        Parser master = null;
        try {
            master = new Parser(url, filename,false);
        } catch (Exception e) {
            System.err.println("[Parser] Error on URL parsing");
            e.printStackTrace();
        } finally {
            return (master != null) ? master.getClean() : "empty";
        }
    }

}
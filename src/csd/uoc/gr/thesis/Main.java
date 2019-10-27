package csd.uoc.gr.thesis;

import csd.uoc.gr.thesis.lib.NLPlib;
import csd.uoc.gr.thesis.lib.Parser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.TokenizerAnnotator;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static boolean default_operation;

    public static void main(String[] args){
        default_operation = check_operation_mode(args);

        /**
         * if it's not default operation, take the content from terminal args
         */
        String parsed_content = (default_operation) ? "empty" : args[1] ;
        CoreDocument doc = new CoreDocument(parsed_content);

        if (default_operation) {

            parsed_content = defaultMode();

            doc = new CoreDocument(parsed_content); // change doc's content
        }

        NLPlib nlp = new NLPlib(doc);

        nlp.annotate();

    }


    /**
     * Parse a page through URL and write it to file Boilerpiped
     * @return parsed_output cleaned(boilerpiped)
     */
    static private String defaultMode(){
        final String filename = "parsed.txt";
        Parser master = null;
        try {
            System.out.print("[Main] Give me a URL to process: ");
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();
            master = new Parser(input, filename);
        }catch(Exception e){
            System.err.println("[Parser] Error on URL parsing");
            e.printStackTrace();
        }finally {
            return (master != null) ? master.getClean() : "empty";
        }
    }


    /**
     *
     * @param args given from main
     * @return The binary operation mode (true = default && false = only_NLP)
     */
    static private boolean check_operation_mode(String[] args){
        boolean def_op = true;
        if (args.length > 1) {
            if (args[0].equals("nlp") && !args[1].isEmpty()) {
                def_op = false;
            }
        } else {
            System.out.println("Default Operation");
        }
        return def_op;
    }
}
package csd.thesis;

import csd.thesis.lib.NLPlib;
import csd.thesis.lib.Parser;
import edu.stanford.nlp.pipeline.CoreDocument;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    static boolean default_operation;
    static private String command;
    static private int iter;

    public static void main(String[] args) {
        /** Usage tips:
         * if you want to use the console version:
         *        <executable> --console
         */
        default_operation = check_operation_mode(args);


        String parsed_content = "empty";
        CoreDocument doc = new CoreDocument(parsed_content);
        NLPlib nlp = new NLPlib();

        if (default_operation) {

            parsed_content = defaultMode();

            doc = new CoreDocument(parsed_content); // change doc's content

            nlp.annotate(doc);
        }else{
            console_op(nlp,doc);
        }



    }

    static private void console_op(NLPlib nlp, CoreDocument doc) {
        while (true) {
            System.out.print("[Main] UDFC_thesis$ ");
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();
            iter = 0;
            Pattern pattern = Pattern.compile("(^\\w+)(.*)?");
            Matcher matcher = pattern.matcher(input);
            if (!matcher.find()) continue;
            command = matcher.group(1);
            if (command.equals("quit") || command.equals("q")) {
                System.err.println("[Main] exiting...");
                System.exit(0);
            } else if (command.equals("parse") || command.equals("p")) {
                System.err.println("[Main] parsing command...");
                if (matcher.group(2).isBlank()) {
                    System.err.println("[Error] No input for parsing whatsoever!");
                    continue;
                }
                doc = new CoreDocument(matcher.group(2)); // change doc's content
                nlp.annotate(doc);
            }

        }
    }


    /**
     * Parse a page through URL and write it to file Boilerpiped
     *
     * @return parsed_output cleaned(boilerpiped)
     */
    static private String defaultMode() {
        final String filename = "parsed.txt";
        Parser master = null;
        try {
            System.out.print("[Main] Give me a URL to process: ");
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();
            master = new Parser(input, filename);
        } catch (Exception e) {
            System.err.println("[Parser] Error on URL parsing");
            e.printStackTrace();
        } finally {
            return (master != null) ? master.getClean() : "empty";
        }
    }


    /**
     * @param args given from main
     * @return The binary operation mode (true = default && false = only_NLP)
     */
    static private boolean check_operation_mode(String[] args) {
        boolean def_op = true;
        if (args.length > 0) {
            if ((args[0].equals("--console") || args[0].equals("-C")) ) {
                def_op = false;
            }
        } else {
            System.out.println("Default Operation");
        }
        return def_op;
    }
}
package csd.thesis;

import csd.thesis.lib.NLPlib;
import csd.thesis.lib.Parser;
import edu.stanford.nlp.pipeline.CoreDocument;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UDFC {
    static boolean default_operation;
    static private String command;
    static private int iter;

    public static void main(String[] args) {

        NLPlib nlp = new NLPlib(NLPlib.mode.NLP);
        console_op(nlp);
    }

    static private void console_op(NLPlib nlp) {
        String parsed_content;
        while (true) {
            parsed_content = "";
            iter = 0;
            System.out.print("[Main] UDFC_thesis$ ");
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();
            Pattern pattern = Pattern.compile("(^\\w+)[\\s]?(.*)?");
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
                System.out.println(matcher.group(2));
                parsed_content = defaultMode(matcher.group(2));
                if(parsed_content.equals("empty"))
                    continue;

                CoreDocument doc = new CoreDocument(parsed_content); // change doc's content

                nlp.annotate(doc);
            }

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
            master = new Parser(url, filename);
        } catch (Exception e) {
            System.err.println("[Parser] Error on URL parsing");
            e.printStackTrace();
        } finally {
            return (master != null) ? master.getClean() : "empty";
        }
    }

}
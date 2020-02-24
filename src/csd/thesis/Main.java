package csd.thesis;

import csd.thesis.model.ViewPoint;
import csd.thesis.model.WebArticle;
import csd.thesis.tools.NLPlib;
import csd.thesis.tools.Parser;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.Pair;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    static boolean default_operation;
    static private String command;
    static private int iter;
    static private final int TOP_ENTRIES_VIEW_MAX = 15;
    static public ViewPoint masterVP;
    static NLPlib nlp_instance;

    public static void main(String[] args) throws IOException {
        nlp_instance = new NLPlib(NLPlib.mode.NLP);

        String a = "Kostas found Vaggelis in the woods while playing with his volley balls .";
        String b = "Kostas found George in the woods while playing precisely with his basketball";
        AtomicReference<String> cleaned = new AtomicReference<>("");
        JaccardSimilarity JS = new JaccardSimilarity();
        System.out.println("---------------jaccard similarity");
        System.out.println(JS.apply(a,b));
        System.out.println("---------------stopwords");
        PorterStemmer ps = new PorterStemmer();
        nlp_instance.NLPlib_annotate(new CoreDocument(a));
        nlp_instance.removeStopWords().forEach(elem ->{
            System.out.println(elem);
            cleaned.updateAndGet(v -> v + elem.lemma() + " ");
        });

        System.out.println("---------------stemmed");
        ps.setCurrent(cleaned.get());
        ps.stem();
        System.out.println( ps.getCurrent());

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
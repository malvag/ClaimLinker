package csd.thesis.tools;

import csd.thesis.model.ViewPoint;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.jlt.Configuration;
//import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
//import it.uniroma1.lcl.babelfy.core.Babelfy;
//import it.uniroma1.lcl.jlt.util.Language;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class NLPlib {
    private StanfordCoreNLP master_pipeline;
    private CoreDocument doc;
    private Babelfy bfy;
    private mode current_mode;
    List<String> stopwords;
    private final static boolean debug = false;

    public enum mode {
        NLP, NLP_BFY
    }

    public NLPlib(mode init_mode) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner"); // not enough memory
        this.current_mode = init_mode;
        this.initStopword("data/stopwords.txt");
        master_pipeline = new StanfordCoreNLP(props);

        if (init_mode == mode.NLP_BFY) {
            Configuration cc = Configuration.getInstance();
            bfy = new Babelfy();
        }
    }

    public CoreDocument getDoc() {
        return doc;
    }

    public void NLPlib_annotate(CoreDocument doc) {
        this.doc = doc;
        master_pipeline.annotate(doc);
    }

    public static List getAnnotationSentences(CoreDocument doc, ViewPoint vp) {
        if (debug) System.out.println("= = =");
        if (debug) System.out.println("[NLPlib] Entities found");
        if (debug) System.out.println("= = =");
        boolean inEntity = false;
        int counter = 0;
        String currentEntity = "";
        String currentEntityType = "";
        ArrayList<String> sentenceNEs = new ArrayList<>();
        Annotation document = new Annotation(doc.annotation());
        List sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (Object sentence : sentences) {
            sentenceNEs.clear();
            if (debug) System.out.println("[NLPlib] Sentence #" + counter++);
            for (CoreLabel token : ((CoreMap) sentence).get(CoreAnnotations.TokensAnnotation.class)) {

                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                if (debug) System.out.println("[OT: " + token.originalText() + " ]");
                if (!inEntity) {
                    if (!"O".equals(ne)) {
                        inEntity = true;
                        currentEntity = "";
                        currentEntityType = ne;
                    }
                }
                if (inEntity) {
                    if ("O".equals(ne)) {
                        inEntity = false;
                        sentenceNEs.add(currentEntity);
                        if (debug) System.out.println("Extracted " + currentEntityType + " " + currentEntity.trim());

                    } else {
                        currentEntity += " " + token.originalText();
                    }

                }

            }
            vp.addTokensfromSentence(sentenceNEs);
        }

        return sentences;
    }


    public List<CoreLabel> removeStopWords(){
        int counter = 0;
        ArrayList<String> sentenceNEs = new ArrayList<>();
        Annotation document = new Annotation(doc.annotation());
        List sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<CoreLabel> without_stopwords = new ArrayList<>();
        for (Object sentence : sentences) {
            sentenceNEs.clear();
            if (debug) System.out.println("[NLPlib] Sentence #" + counter++);
            for (CoreLabel token : ((CoreMap) sentence).get(CoreAnnotations.TokensAnnotation.class)) {
                System.out.printf("[NLPlib] Token : %15s", token);
                if(this.stopwords.contains(token.originalText()))
                    System.out.print(" **");
                else
                    without_stopwords.add(token);
                System.out.print("\n");
            }
        }
        if (debug) System.out.println("[NLPlib] --------  ");
        return without_stopwords;
    }

    private void initStopword(String file_path){
        this.stopwords = new ArrayList<String>();
        try (BufferedReader in = new BufferedReader(new FileReader(file_path))) {
            String input;
            while((input= in.readLine()) != null){
                this.stopwords.add(input);
            }
        }catch (IOException e){
            System.err.println("Stopwords file error!");
            System.exit(1);
        }
    }

}

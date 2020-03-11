package csd.thesis.tools;

import csd.thesis.misc.ConsoleColor;
import csd.thesis.model.ViewPoint;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.jlt.Configuration;
import org.apache.logging.log4j.core.Core;
import org.tartarus.snowball.ext.PorterStemmer;
//import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
//import it.uniroma1.lcl.babelfy.core.Babelfy;
//import it.uniroma1.lcl.jlt.util.Language;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    public Babelfy getBfy() {
        return bfy;
    }

    public List<String> getLemmas() {
        return this.doc.tokens().stream()
                .map(cl -> cl.lemma())
                .collect(Collectors.toList());
    }
    public String getLemmas_toString(CoreDocument doc) {
        AtomicReference<String> cleaned = new AtomicReference<>("");
        if (doc != null) this.setDoc(doc);
        this.getLemmas().forEach(elem -> {
//            System.out.println(elem);
            cleaned.updateAndGet(v -> v + elem + " ");
        });
        System.out.println(ConsoleColor.ANSI_CYAN + "[NLPlib] INFO got lemmas" + ConsoleColor.ANSI_RESET);
        return cleaned.get();
    }

    public String getStemmed(CoreDocument a) {
        PorterStemmer ps = new PorterStemmer();
        String stemmed_str = "";
        ArrayList<String> stemmed = new ArrayList<String>();
        a.tokens().forEach(token -> {
            ps.setCurrent(token.originalText());
            ps.stem();
            synchronized (stemmed) {
                stemmed.add(ps.getCurrent());
            }
        });

        stemmed_str = String.join(" ", stemmed);
        System.out.println(ConsoleColor.ANSI_CYAN + "[NLPlib] INFO stemmed" + ConsoleColor.ANSI_RESET);
        return stemmed_str;
    }
    public String getWithoutStopwords(CoreDocument doc) {
        AtomicReference<String> cleaned = new AtomicReference<>("");
        if (doc != null) this.setDoc(doc);
        this.removeStopWords().forEach(elem -> {
//            System.out.println(elem);
            cleaned.updateAndGet(v -> v + elem.originalText() + " ");
        });
        System.out.println(ConsoleColor.ANSI_CYAN + "[NLPlib] INFO stopwords removed" + ConsoleColor.ANSI_RESET);
        return cleaned.get();
    }


    public CoreDocument getDoc() {
        return doc;
    }

    public void NLPlib_annotate(CoreDocument doc) {
        this.setDoc(doc);
        this.master_pipeline.annotate(doc);
    }

    public void setDoc(CoreDocument doc) {
        this.doc = doc;
    }

    public ArrayList<String> getAnnotationSentences(CoreDocument doc) {
        if (debug) System.out.println("= = =");
        if (debug) System.out.println("[NLPlib] Entities found");
        if (debug) System.out.println("= = =");
        boolean inEntity = false;
        int counter = 0;
        String currentEntity = "";
        String currentEntityType = "";
        ArrayList<String> sentenceNEs = new ArrayList<>();
        ArrayList<String> tokens = new ArrayList<>();
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
                        tokens.add(currentEntity);
                        if (true) System.out.println("Extracted " + currentEntityType + " " + currentEntity.trim());

                    } else {
                        currentEntity += " " + token.originalText();
                    }

                }

            }
//            vp.addTokensfromSentence(sentenceNEs);
        }

        return tokens;
    }


    public List<CoreLabel> removeStopWords() {
        int counter = 0;
        Annotation document = new Annotation(doc.annotation());
        List sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<CoreLabel> without_stopwords = new ArrayList<>();
        for (Object sentence : sentences) {
            if (debug) System.out.println("[NLPlib] Sentence #" + counter++);
            for (CoreLabel token : ((CoreMap) sentence).get(CoreAnnotations.TokensAnnotation.class)) {
                if (debug) {
                    System.out.printf("[NLPlib] Token : %15s - %15s", token, token.originalText());
                }
                if (!this.stopwords.contains(token.originalText()))
                    without_stopwords.add(token);
                else
                    if (debug)
                        System.out.printf(" <-");
                if (debug) System.out.print("\n");
            }
        }
        if (debug) System.out.println("[NLPlib] --------  ");
        return without_stopwords;
    }

    private void initStopword(String file_path) {
        this.stopwords = new ArrayList<String>();
        try (BufferedReader in = new BufferedReader(new FileReader(file_path))) {
            String input;
            while ((input = in.readLine()) != null) {
                this.stopwords.add(input);
            }
        } catch (IOException e) {
            System.err.println("Stopwords file error!");
            System.exit(1);
        }
    }

}

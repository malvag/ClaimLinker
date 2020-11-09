package csd.claimlinker.nlp;

import com.yahoo.semsearch.fastlinking.hash.QuasiSuccinctEntityHash;
import csd.claimlinker.es.misc.ConsoleColor;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import it.unimi.dsi.fastutil.io.BinIO;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class NLPlib {
    private final StanfordCoreNLP master_pipeline;
    private CoreDocument doc;
    List<String> stopwords;
    private final static boolean debug = false;
    protected QuasiSuccinctEntityHash quasiSuccinctEntityHash;

    public NLPlib( String stopwords_path, String Hash_Path) throws IOException, ClassNotFoundException {
            System.out.println("========================================");
            System.out.println("NLPlib initializing ...");
            this.quasiSuccinctEntityHash = (QuasiSuccinctEntityHash) BinIO.loadObject(Hash_Path);
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner"); // not enough memory
            this.initStopword(stopwords_path);
            master_pipeline = new StanfordCoreNLP(props);
            System.out.println("========================================");
            System.out.println("NLPlib initialization finished ...");
            if(master_pipeline == null){
                System.err.println("NLP error");
                System.exit(100);
            }
    }

    public List<String> getLemmas(CoreDocument a) {
        if (a == null) return null;
        this.NLPlib_annotate(a);
        return this.doc.tokens().stream()
                .map(CoreLabel::lemma)
                .collect(Collectors.toList());
    }

    public String getLemmas_toString(CoreDocument a) {
        AtomicReference<String> cleaned = new AtomicReference<>("");
        if (a == null) return "null";
        this.getLemmas(a).forEach(elem -> {
//            System.out.println(elem);
            cleaned.updateAndGet(v -> v + elem + " ");
        });
        System.out.println(ConsoleColor.ANSI_CYAN + "[NLPlib] INFO got lemmas" + ConsoleColor.ANSI_RESET);
        return cleaned.get();
    }

    public String getStemmed(CoreDocument a) {
        PorterStemmer ps = new PorterStemmer();
        if (a == null) return "null";
        String stemmed_str;
        ArrayList<String> stemmed = new ArrayList<>();
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

    public String getWithoutStopwords(CoreDocument a) {
        AtomicReference<String> cleaned = new AtomicReference<>("");
        if (a != null) this.setDoc(a);
        this.removeStopWords().forEach(elem -> {
//            System.out.println(elem);
            cleaned.updateAndGet(v -> v + elem.originalText() + " ");
        });
//        System.out.println(ConsoleColor.ANSI_CYAN + "[NLPlib] stopwords removed" + ConsoleColor.ANSI_RESET);
        return cleaned.get();
    }
//
//    public void getWordnetExpansion(CoreDocument a) throws JWNLException {
//        final net.didion.jwnl.dictionary.Dictionary dictionary = net.didion.jwnl.dictionary.Dictionary.getInstance();
//        for (CoreLabel elem : a.tokens()) {
//            try {
//                String ne = elem.get(CoreAnnotations.PartOfSpeechAnnotation.class);
////                    System.out.println(ne + " " + elem.originalText());
//                IndexWord indexWord = null;
//                if (ne.startsWith("NN")) {
//                    indexWord = dictionary.getIndexWord(POS.NOUN, a.text());
//                } else if (ne.startsWith("RB")) {
//                    indexWord = dictionary.getIndexWord(POS.ADVERB, a.text());
//                } else if (ne.startsWith("VB")) {
//                    indexWord = dictionary.getIndexWord(POS.VERB, a.text());
//                }
//                if (indexWord == null)
//                    continue;
//                Synset[] senses = indexWord.getSenses();
//                for (Synset set : senses) {
//                    System.out.println(indexWord + ": " + set.getGloss());
//                }
//            } catch (JWNLException e) {
//                e.printStackTrace();
//            }
//        }
//
//        System.out.println(ConsoleColor.ANSI_CYAN + "[NLPlib] INFO WordNet Expansion applied" + ConsoleColor.ANSI_RESET);
//
//    }

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
        ArrayList<String> tokens = new ArrayList<>();
        Annotation document = new Annotation(doc.annotation());
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (Object sentence : sentences) {
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
//                        System.out.println("Extracted " + currentEntityType + " " + currentEntity.trim());

                    } else {
                        currentEntity += " " + token.originalText();
                    }

                }

            }
        }

        return tokens;
    }


    public List<CoreLabel> removeStopWords() {
        int counter = 0;
        Annotation document = new Annotation(doc.annotation());
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<CoreLabel> without_stopwords = new ArrayList<>();
        for (Object sentence : sentences) {
            if (debug) System.out.println("[NLPlib] Sentence #" + counter++);
            for (CoreLabel token : ((CoreMap) sentence).get(CoreAnnotations.TokensAnnotation.class)) {
                if (debug) {
                    System.out.printf("[NLPlib] Token : %15s - %15s\n", token, token.originalText());
                }
                if (!this.stopwords.contains(token.originalText()))
                    without_stopwords.add(token);
                else if (debug)
                    System.out.println(" <-");
                if (debug) System.out.print("\n");
            }
        }
        if (debug) System.out.println("[NLPlib] --------  ");
        return without_stopwords;
    }

    private void initStopword(String file_path) {
        this.stopwords = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new FileReader(file_path))) {
            String input;
            while ((input = in.readLine()) != null) {
                this.stopwords.add(input);
            }
            System.out.println("Stopwords initialization finished!");
        } catch (IOException e) {
            System.err.println("Stopwords file error!");
            System.exit(1);
        }
    }

}

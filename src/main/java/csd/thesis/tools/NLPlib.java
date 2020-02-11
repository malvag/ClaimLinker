package csd.thesis.tools;

import csd.thesis.UDFC;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.jlt.util.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class NLPlib {
    private StanfordCoreNLP master_pipeline;
    private CoreDocument doc;
    private Babelfy bfy;
    private mode current_mode;
    private final static boolean debug = false;

    public enum mode {
        NLP, NLP_BFY
    }

    public NLPlib(mode init_mode) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner"); // not enough memory
        this.current_mode = init_mode;
        master_pipeline = new StanfordCoreNLP(props);

        if (init_mode == mode.NLP_BFY) {
            bfy = new Babelfy();
        }
    }

    public CoreDocument getDoc() {
        return doc;
    }

    public void NLPlib_annotate(CoreDocument doc, boolean doPrint, List<SemanticAnnotation> bfysa) {
        this.doc = doc;
        master_pipeline.annotate(doc);
        if (this.current_mode == mode.NLP_BFY) {
            bfysa = bfy.babelfy(doc.text(), Language.EN);
            if (doPrint) {
                output_annotation(doc, bfysa);
            }
        } else {
            if (doPrint) {
                output_annotation(doc, null);
            }
        }
    }

    public static void output_annotation(CoreDocument doc, List<SemanticAnnotation> bfyAnnotations) {
        if(debug)System.out.println("= = =");
        if(debug)System.out.println("[NLPlib] Entities found");
        if(debug)System.out.println("= = =");
        boolean inEntity = false;
        int counter = 0;
        String currentEntity = "";
        String currentEntityType = "";
        ArrayList<String> sentenceNEs = new ArrayList<>();
        Annotation document = new Annotation(doc.annotation());
        List sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (Object sentence : sentences) {
            sentenceNEs.clear();
            if(debug)System.out.println("[NLPlib] Sentence #"+counter++);
            for (CoreLabel token : ((CoreMap) sentence).get(CoreAnnotations.TokensAnnotation.class)) {

                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                if(debug)System.out.println("[OT: "+token.originalText()+" ]");
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
                        switch (currentEntityType) {
                            case "PERSON":
                                if(debug)System.out.println("Extracted Person - " + currentEntity.trim());
                                break;
                            case "ORGANIZATION":
                                if(debug)System.out.println("Extracted Organization - " + currentEntity.trim());
                                break;
                            case "LOCATION":
                                if(debug)System.out.println("Extracted Location - " + currentEntity.trim());
                                break;
                            case "DATE":
                                if(debug)System.out.println("Extracted Date " + currentEntity.trim());
                                break;
                            default:
                                if(debug)System.out.println("Extracted " + currentEntityType + " " + currentEntity.trim());
                                break;
                        }
                    } else {
                        currentEntity += " " + token.originalText();
                    }

                }
            }
            UDFC.masterVP.addTokensfromSentence(sentenceNEs);
        }

//        // BUG!
//        for (CoreEntityMention em : doc.entityMentions()) {
//            System.out.println("\tdetected entity: \t" + em.text() + "\t" + em.entityType());
//        }
//        System.out.println("= = =");
//        System.out.println("[NLPlib] Tokens and ner tags");
//        System.out.println("= = =");
//
//        String tokensAndNERTags = doc.tokens().stream().map(token -> "<" + token.word() + "," + token.ner() + ">").collect(
//                Collectors.joining(" "));
//        System.out.println(tokensAndNERTags);
//
//        if (bfyAnnotations != null) {
//            for (SemanticAnnotation annotation : bfyAnnotations) {
//                //splitting the input text using the CharOffsetFragment start and end anchors
//                String frag = doc.text().substring(annotation.getCharOffsetFragment().getStart(),
//                        annotation.getCharOffsetFragment().getEnd() + 1);
//                System.out.println(frag + "\t" + annotation.getBabelSynsetID());
//                System.out.println("\t" + annotation.getBabelNetURL());
//                System.out.println("\t" + annotation.getDBpediaURL());
//                System.out.println("\t" + annotation.getSource());
//            }
//            System.out.println("= = =");
//            System.out.println("[NLPlib] Named entity recognition and disambiguation was successful");
//            System.out.println("= = =");
//        }


    }

}

package csd.uoc.gr.thesis.lib;

import edu.stanford.nlp.pipeline.*;
import java.util.Properties;
import java.util.stream.Collectors;

public class NLPlib {
    private StanfordCoreNLP master_pipeline;
    private CoreDocument doc;

    public NLPlib() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner"); // not enough memory
        master_pipeline = new StanfordCoreNLP(props);
    }

    public void annotate(CoreDocument doc) {
        this.doc = doc;
        output_annotation(master_pipeline);
    }

    private void output_annotation(StanfordCoreNLP pipeline) {
//        PrintWriter out = new PrintWriter(System.out);
        pipeline.annotate(doc);
        System.out.println("= = =");
        System.out.println("[NLPlib] Entities found");
        for (CoreEntityMention em : doc.entityMentions()) {
            System.out.println("\tdetected entity: \t" + em.text() + "\t" + em.entityType());
        }
        System.out.println("= = =");
        System.out.println("[NLPlib] Tokens and ner tags");
        String tokensAndNERTags = doc.tokens().stream().map(token -> "<" + token.word() + "," + token.ner() + ">").collect(
                Collectors.joining(" "));
        System.out.println(tokensAndNERTags);
    }

}

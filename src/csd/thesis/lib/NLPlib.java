package csd.thesis.lib;

import edu.stanford.nlp.pipeline.*;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.jlt.util.Language;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class NLPlib {
    private StanfordCoreNLP master_pipeline;
    private CoreDocument doc;
    private Babelfy bfy;

    public NLPlib() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner"); // not enough memory
        master_pipeline = new StanfordCoreNLP(props);
        bfy = new Babelfy();
    }

    public void annotate(CoreDocument doc) {
        this.doc = doc;
        master_pipeline.annotate(doc);
        List<SemanticAnnotation> bfyAnnotations = bfy.babelfy(doc.text(), Language.EN);
        output_annotation(master_pipeline,bfyAnnotations);

    }

    private void output_annotation(StanfordCoreNLP pipeline,List<SemanticAnnotation> bfyAnnotations) {
        System.out.println("= = =");
        System.out.println("[NLPlib] Entities found");
        System.out.println("= = =");

        for (CoreEntityMention em : doc.entityMentions()) {
            System.out.println("\tdetected entity: \t" + em.text() + "\t" + em.entityType());
        }
        System.out.println("= = =");
        System.out.println("[NLPlib] Tokens and ner tags");
        System.out.println("= = =");

        String tokensAndNERTags = doc.tokens().stream().map(token -> "<" + token.word() + "," + token.ner() + ">").collect(
                Collectors.joining(" "));
        System.out.println(tokensAndNERTags);


        for (SemanticAnnotation annotation : bfyAnnotations)
        {
            //splitting the input text using the CharOffsetFragment start and end anchors
            String frag = doc.text().substring(annotation.getCharOffsetFragment().getStart(),
                    annotation.getCharOffsetFragment().getEnd() + 1);
            System.out.println(frag + "\t" + annotation.getBabelSynsetID());
            System.out.println("\t" + annotation.getBabelNetURL());
            System.out.println("\t" + annotation.getDBpediaURL());
            System.out.println("\t" + annotation.getSource());
        }
        System.out.println("= = =");
        System.out.println("[NLPlib] Named entity recognition and disambiguation was successful");
        System.out.println("= = =");


    }

}

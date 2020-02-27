package csd.thesis;

import csd.thesis.model.WebArticle;
import csd.thesis.tools.URL_Parser;
import edu.stanford.nlp.pipeline.CoreDocument;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.tartarus.snowball.ext.PorterStemmer;

import java.util.concurrent.atomic.AtomicReference;

import static csd.thesis.Main.nlp_instance;

public class ClaimLinker {
    private WebArticle article;

    public ClaimLinker(String s_url) {
        URL_Parser p = new URL_Parser(s_url, null, true);
        this.article = new WebArticle(p.getClean());
    }

    //temporary
    private void pipeline() {
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
}



package csd.thesis;

import csd.thesis.misc.ConsoleColor;
import csd.thesis.model.Claim;
import csd.thesis.model.WebArticle;
import csd.thesis.tools.AnalyzerDispatcher;
import csd.thesis.tools.NLPlib;
import csd.thesis.tools.URL_Parser;
import edu.cmu.lti.ws4j.WS4J;
import edu.cmu.lti.ws4j.demo.SimilarityCalculationDemo;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.util.logging.Color;
import edu.stanford.nlp.util.logging.OutputHandler;
import it.uniroma1.lcl.jlt.util.Language;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.logging.log4j.core.Core;
import org.tartarus.snowball.ext.PorterStemmer;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ClaimLinker {
    private WebArticle article;
    private ArrayList<Claim> claims;
    private NLPlib nlp_instance;

    private final static boolean debug = false;



    public ClaimLinker(WebArticle wa) {
        super();
        if (wa == null)
            throw new IllegalStateException("WebArticle cannot be null");
        if (!wa.getUrl().isEmpty()) {
            URL_Parser p = new URL_Parser(wa.getUrl(), null, true);
        }
        this.article = wa;
    }

    public ClaimLinker() {
        nlp_instance = new NLPlib(NLPlib.mode.NLP_BFY);
        this.claims = new ArrayList<>();
    }

    public void addClaimsFromCSV(ArrayList<Map<String, Object>> master) {
        if (master == null)
            throw new IllegalStateException("[addClaimsFromCSV] ArrayList of Map cannot be null");
        for (int i = 0; i < master.size(); i++) {
//            mapper.writeValue(System.out, master.get(i));
            this.addClaim(new Claim(master.get(i)));
        }
    }

    public void addClaim(Claim claim) {
        this.claims.add(claim);
    }

    public ArrayList<Claim> getClaims() {
        return claims;
    }

    public void viewClaims() {
        int counter = 0;
        for (Claim claim : this.getClaims()) {
            System.out.printf("[%5d] %5s %150s %15s\n", counter++, claim.getObjectMap().get(""), claim.getClaimReviewedBody().substring(0, Math.min(claim.getClaimReviewedBody().length(), 150)), claim.getClaimRatingName());
        }
    }

    public void viewClaims(int index) {
        Claim claim = this.getClaims().get(index);
        System.out.printf("[%5d] %5s %150s %15s\n", index, claim.getObjectMap().get(""), claim.getClaimReviewedBody().substring(0, Math.min(claim.getClaimReviewedBody().length(), 150)), claim.getClaimRatingName());
    }

    public void viewClaims(int start, int end) {
        assert (start > 0 && end < this.getClaims().size());
        for (int i = start; i < end; i++) {
            Claim claim = this.getClaims().get(i);
            System.out.printf("[%5d] %5s %150s %15s\n", i, claim.getObjectMap().get(""), claim.getClaimReviewedBody().substring(0, Math.min(claim.getClaimReviewedBody().length(), 150)), claim.getClaimRatingName());
        }
    }

    public CoreDocument NLP_annotate(String a) {
        CoreDocument document = new CoreDocument(a);
        nlp_instance.NLPlib_annotate(document);
        return document;
    }





    //temporary
    public void pipeline() {
//        String a = "Kostas found Donald Trump in the woods while playing with Hilary Clinton .";
//        String b = "Kostas found Hilary Clinton in the woods while playing precisely with his basket ball";
        String a = "Eventually, a huge cyclone hit the entrance of my house.";
        String b = "Finally, a massive hurricane attacked my home.";
        CoreDocument CD_a = NLP_annotate(a);
        CoreDocument CD_b = NLP_annotate(b);
        AnalyzerDispatcher analyzerDispatcher = new AnalyzerDispatcher(this.nlp_instance);
        AnalyzerDispatcher.Analyzer[] analyzers = {
                AnalyzerDispatcher.Analyzer.jcrd_comm_words,
                AnalyzerDispatcher.Analyzer.jcrd_comm_ne,
                AnalyzerDispatcher.Analyzer.jcrd_comm_lemm_words,
                AnalyzerDispatcher.Analyzer.jcrd_comm_dissambig_ents,
                AnalyzerDispatcher.Analyzer.jcrd_comm_pos_words
        };
        analyzerDispatcher.addAnalyzer(analyzers);
        analyzerDispatcher.analyze(CD_a,CD_b);

        //This measure calculates relatedness
        // by considering the depths of the two synsets in the WordNet taxonomies,
        // along with the depth of the LCS
//        WS4JConfiguration ws4JConfiguration = WS4JConfiguration.getInstance();
//        ws4JConfiguration.
//        WS4J ws4J = new WS4J();
//        System.out.println("WS4J.WUP\n- result: " + WS4J.runLESK(CD_a.text(), CD_b.text()));

//        WS4J.runJCN()
        System.out.println("_______________________________________________");


    }

//    Common (jaccard) words of specific POS, e.g., common verbs
//    ()
//    Here you can think which POS tags make sense
//    Num of common n-grams (e.g., 2-grams, 3-grams, 4-grams, 5-grams)
//    Think which n-grams make sense
//    Num of common n-chargrams (e.g., 2-chargrams, 3-chargrams, 4-chargrams, … …)
//    Think which n-grams make sense









}



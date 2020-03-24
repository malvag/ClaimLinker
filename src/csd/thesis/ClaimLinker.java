package csd.thesis;

import csd.thesis.model.Claim;
import csd.thesis.model.WebArticle;
import csd.thesis.tools.AnalyzerDispatcher;
import csd.thesis.tools.NLPlib;
import csd.thesis.tools.URL_Parser;
import edu.stanford.nlp.pipeline.CoreDocument;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        String a = "He later tweeted that he himself opened a major Apple Manufacturing plant in Texas that will bring high paying jobs back to America.";
//        String b = "Finally, a massive hurricane attacked my home.";
        String url = "https://www.washingtonpost.com/business/2019/11/21/trump-took-credit-opening-mac-factory-its-been-open-since/";

        WebArticle b = new WebArticle(new URL_Parser(url,null,false).getClean(),url);
        CoreDocument CD_a = NLP_annotate(a);
        CoreDocument CD_b = NLP_annotate(b.getCleaned());
//        try {
//            this.nlp_instance.getWordnetExpansion(CD_a);
//        } catch (JWNLException e) {
//            e.printStackTrace();
//        }

        System.out.println(a);
        System.out.println(b.getCleaned());
        AnalyzerDispatcher analyzerDispatcher = new AnalyzerDispatcher(this.nlp_instance);
        AnalyzerDispatcher.SimilarityMeasure[] similarityMeasures = {
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_words,
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_lemm_words,
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ne,
//                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_dissambig_ents,
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_pos_words,
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ngram,
                AnalyzerDispatcher.SimilarityMeasure.vec_cosine_sim
        };
        analyzerDispatcher.addSimMeasure(similarityMeasures);
        analyzerDispatcher.analyze(CD_a,CD_b);


        System.out.println("_______________________________________________");


    }


//    Num of common n-chargrams (e.g., 2-chargrams, 3-chargrams, 4-chargrams, … …)
//    Think which n-grams make sense

    //Kulczynski2
    //







}



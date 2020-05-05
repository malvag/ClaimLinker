package csd.thesis;

import csd.thesis.model.Claim;
import csd.thesis.model.WebArticle;
import csd.thesis.tools.AnalyzerDispatcher;
import csd.thesis.tools.NLPlib;
import csd.thesis.tools.URL_Parser;
import edu.stanford.nlp.pipeline.CoreDocument;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClaimLinker {
    private WebArticle article;
    private ArrayList<Claim> claims;
    private NLPlib nlp_instance;

    private final static boolean debug = false;

    public ClaimLinker(String JWNLProperties_path, String stopwords_path) {
        System.out.println("========================================");
        System.out.println("ClaimLinker initializing ... ");

        nlp_instance = new NLPlib(NLPlib.mode.NLP_BFY,JWNLProperties_path,stopwords_path);
        this.claims = new ArrayList<>();
        System.out.println("========================================");
        System.out.println("ClaimLinker's initialization finished...");
        System.out.println("========================================");
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
//        String a = "He later tweeted that he himself opened a major Apple Manufacturing plant in Texas that will bring high paying jobs back to America.";
//        String b = "Finally, a massive hurricane attacked my home.";
//        String url = "https://www.washingtonpost.com/business/2019/11/21/trump-took-credit-opening-mac-factory-its-been-open-since/";


        String t1 = "He later tweeted that he himself opened a major Apple Manufacturing plant in Texas that will bring high paying jobs back to America.";
        String t2 = "Today I opened a major Apple Manufacturing plant in Texas that will bring high paying jobs back to America.";

//        WebArticle b = new WebArticle(url);
        CoreDocument CD_a = NLP_annotate(t1);
        CoreDocument CD_b = NLP_annotate(t2);
//        try {
//            this.nlp_instance.getWordnetExpansion(CD_a);
//        } catch (JWNLException e) {
//            e.printStackTrace();
//        }

//        System.out.println(a);
//        System.out.println(b.getCleaned());
        AnalyzerDispatcher analyzerDispatcher = new AnalyzerDispatcher(this.nlp_instance);
        analyzerDispatcher.addSimMeasure(new AnalyzerDispatcher.SimilarityMeasure[]{
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_words,           //Common (jaccard) words
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_lemm_words,      //Common (jaccard) lemmatized words
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ne,              //Common (jaccard) named entities
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_dissambig_ents,  //Common (jaccard) disambiguated entities BFY
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_pos_words,       //Common (jaccard) words of specific POS
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ngram,           //Common (jaccard) ngrams
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_nchargram,       //Common (jaccard) nchargrams
                AnalyzerDispatcher.SimilarityMeasure.vec_cosine_sim             //Cosine similarity
        });
        analyzerDispatcher.analyze(CD_a,CD_b);


        System.out.println("_______________________________________________");


    }

    //Kulczynski2 ?TBD
}



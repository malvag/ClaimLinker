package csd.thesis;

import csd.thesis.model.Claim;
import csd.thesis.model.WebArticle;
import csd.thesis.tools.AnalyzerDispatcher;
import csd.thesis.tools.NLPlib;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.util.Pair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class ClaimLinker {
    public ArrayList<Claim> claims;
    public NLPlib nlp_instance;
    public final static boolean debug = false;
    public AnalyzerDispatcher analyzerDispatcher;

    public ClaimLinker(String JWNLProperties_path, String stopwords_path, String Hash_Path, String claims_path) throws IOException, ClassNotFoundException {
        System.out.println("========================================");
        System.out.println("ClaimLinker initializing ... ");
        AtomicInteger counter = new AtomicInteger(0);
        nlp_instance = new NLPlib(JWNLProperties_path, stopwords_path, Hash_Path);
        this.claims = new ArrayList<>();
        ElasticInitializer.path = claims_path;
//        System.out.println("Opening CSV record of Claims");
//        if (ElasticInitializer.master_claim_record == null)
//            ElasticInitializer.openClaimsRecord();
//        System.out.println("Initializing master_claim_record of all available claims...");
//        ElasticInitializer.master_claim_record.forEach(elem -> {
//            Claim new_claim = new Claim(elem);
//            new_claim.setDoc(NLP_annotate(new_claim.getReviewedBody()));
//            this.claims.add(new_claim);
//            System.out.printf("Annotated: %d\r", counter.getAndIncrement());
//        });
//        System.out.printf("Annotated: %d\n", counter.getAndIncrement());
        this.analyzerDispatcher = new AnalyzerDispatcher(this.nlp_instance);
        this.analyzerDispatcher.addSimMeasure(new AnalyzerDispatcher.SimilarityMeasure[]{
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_words,           //Common (jaccard) words
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_lemm_words,      //Common (jaccard) lemmatized words
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ne,              //Common (jaccard) named entities
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_dissambig_ents,  //Common (jaccard) disambiguated entities BFY
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_pos_words,       //Common (jaccard) words of specific POS
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ngram,           //Common (jaccard) ngrams
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_nchargram,       //Common (jaccard) nchargrams
                AnalyzerDispatcher.SimilarityMeasure.vec_cosine_sim             //Cosine similarity
        });
        System.out.println("========================================");
        System.out.println("ClaimLinker's initialization finished...");
        System.out.println("========================================");
    }

    public ArrayList<Claim> getClaims() {
        return claims;
    }

    public CoreDocument NLP_annotate(String a) {
        CoreDocument document = new CoreDocument(a);
        nlp_instance.NLPlib_annotate(document);
        return document;
    }

    public void demo_pipeline() throws UnsupportedEncodingException {
        System.out.println("Demo pipeline started!");

        String url = "https://www.washingtonpost.com/business/2019/11/21/trump-took-credit-opening-mac-factory-its-been-open-since/";
//        String t1 = "He later tweeted that he himself opened a major Apple Manufacturing plant in Texas that will bring high paying jobs back to America.";

        String t2 //= "Today I opened a major Apple Manufacturing plant in Texas that will bring high paying jobs back to America.";
                = "C++ designer Bjarne Stroustrup admitted in an interview that he developed the language solely to create high-paying jobs for programmers.";

        CoreDocument CD_b = NLP_annotate(nlp_instance.getWithoutStopwords(NLP_annotate(t2)));
        Instant start = Instant.now();

        this.claims = ElasticWrapper.findCatalogItemWithoutApi("claimReview_claimReviewed", URLEncoder.encode(t2, StandardCharsets.UTF_8), 100);
        ArrayList<Pair<Double, Claim>> records = new ArrayList<>();
        int counter = 0;
        for (Claim claim : this.claims) {
            CoreDocument CD_c = NLP_annotate(claim.getReviewedBody());
            System.out.printf("%d\r", counter++);
            records.add(new Pair<Double, Claim>(this.analyzerDispatcher.analyze(CD_c, CD_b), claim));
        }

        records.sort(Collections.reverseOrder());
        records.forEach(elem -> {
            System.out.println("===");
            if (elem.first > 0)
                System.out.println(elem.first + " \n" + elem.second.getReviewedBody());
        });

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();

        System.out.println("_______________________________________________");
        System.out.println("Time passed: " + (double) timeElapsed / 1000 + "s");
        System.out.println("Demo pipeline shutting down ...");


    }

}



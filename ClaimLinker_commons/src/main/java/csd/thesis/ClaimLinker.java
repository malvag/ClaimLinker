package csd.thesis;

import csd.thesis.model.Assoc_t;
import csd.thesis.model.Claim;
import csd.thesis.tools.AnalyzerDispatcher;
import csd.thesis.tools.NLPlib;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.util.Pair;
import org.apache.logging.log4j.core.util.Assert;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

public class ClaimLinker {
    public ArrayList<Claim> claims;
    public NLPlib nlp_instance;
    public final static boolean debug = false;
    public AnalyzerDispatcher analyzerDispatcher;
    public ElasticWrapper elasticWrapper;

    public ClaimLinker(String JWNLProperties_path, String stopwords_path, String Hash_Path, String claims_path, String ES_host) throws IOException, ClassNotFoundException {
        System.out.println("========================================");
        System.out.println("ClaimLinker initializing ... ");
        nlp_instance = new NLPlib(JWNLProperties_path, stopwords_path, Hash_Path);
        this.claims = new ArrayList<>();
//        ElasticInitializer elasticInitializer = new ElasticInitializer(claims_path,ES_host,9200,9201,"http");
        this.elasticWrapper = new ElasticWrapper(ES_host, 9200, 9201, "http");
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
        String t2 //= "Today I opened a major Apple Manufacturing plant in Texas that will bring high paying jobs back to America.";
                = "C++ designer Bjarne Stroustrup ";
        CoreDocument CD_b = NLP_annotate(nlp_instance.getWithoutStopwords(NLP_annotate(t2)));
        Instant start = Instant.now();
        JsonArray results = this.claimLink(t2, Assoc_t.same_as);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        for (int i = 0; i < results.size(); i++) {
            JsonObject j = results.getJsonObject(i);
            System.out.printf("%4d %150s %20f %20f \n", i, j.getString("claimReview_claimReviewed"), j.getJsonNumber("NLP_score").doubleValue(), j.getJsonNumber("ElasticScore").doubleValue());
        }

        System.out.println("_______________________________________________");
        System.out.println("Time passed: " + (double) timeElapsed / 1000 + "s");
        System.out.println("Demo pipeline shutting down ...");


    }

    public JsonArray claimLink(String selection, Assoc_t assoc_t) {
        this.claims = null;

        if (assoc_t == Assoc_t.all) {
            this.claims = this.elasticWrapper.findCatalogItemWithoutApi("claimReview_claimReviewed", URLEncoder.encode(selection, StandardCharsets.UTF_8), 100);
            // needs optimization // too slow
            CoreDocument CD_selection = this.NLP_annotate(
                    this.nlp_instance.getWithoutStopwords(
                            this.NLP_annotate(selection)));
            //
            ArrayList<Pair<Double, Claim>> records = new ArrayList<>();
            int counter = 0;
            for (Claim claim : this.claims) {
                CoreDocument CD_c = this.NLP_annotate(claim.getReviewedBody());
                System.out.printf("%d\r", counter++);
                records.add(new Pair<>(this.analyzerDispatcher.analyze(CD_c, CD_selection), claim));
            }

            records.sort(Collections.reverseOrder());
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Pair<Double, Claim> elem : records) {
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("claimReview_claimReviewed", elem.second.getReviewedBody())
                        .add("rating_alternateName", elem.second.getRatingName())
                        .add("extra_title", elem.second.getExtraTitle())
                        .add("NLP_score", elem.first)
                        .add("ElasticScore", elem.second.getElasticScore())

                );
//                System.out.println(elem.first + " \n" + elem.second.getReviewedBody());
            }
            return arrayBuilder.build();
        } else if (assoc_t == Assoc_t.author_of) {

            // find the NN* Persons from selection
            // match the persons with the claims_author in ES
            // generate candidates and rank them with Sim Measures

        } else if (assoc_t == Assoc_t.topic_of) {

            // use a POS tagger (e.g., of Stanford NLP) and find all tags of type NN (nouns) and NNP (proper nouns).
            // find the NN* from selection
            // Given those nouns, we can submit a keyword query to an Elasticsearch index and get a ranked list of candidate claims
            // generate candidates and rank them with Sim Measures

        } else if (assoc_t == Assoc_t.same_as) {

            // Consider all sentences (filter out those with small Elasticsearch retrieval score–we need to find a threshold)
            // match those nouns as keywords in ES
            // generate candidates and rank them with Sim Measures
            // Given a sentence, we can submit a keyword query to an Elasticsearch index and get a ranked list of candidate claims


        }


        System.out.println("OOPS");
        return null;
    }


}



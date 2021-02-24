package csd.claimlinker.web.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import csd.claimlinker.ClaimLinker;
import csd.claimlinker.model.Association_type;
import csd.claimlinker.model.CLAnnotation;
import csd.claimlinker.model.Claim;
import csd.claimlinker.model.WebArticle;
import csd.claimlinker.nlp.AnalyzerDispatcher;
import org.json.JSONObject;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@WebServlet(name = "claimLinker_Servlet")
public class claimLinker_Servlet extends HttpServlet {

    protected static ClaimLinker claimLinker;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            AnalyzerDispatcher.SimilarityMeasure[] similarityMeasures = new AnalyzerDispatcher.SimilarityMeasure[]{
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_words, //Common (jaccard) words
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_lemm_words, //Common (jaccard) lemmatized words
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ne, //Common (jaccard) named entities
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_dissambig_ents, //Common (jaccard) disambiguated entities BFY
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_pos_words, //Common (jaccard) words of specific POS
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ngram, //Common (jaccard) ngrams
                AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_nchargram, //Common (jaccard) nchargrams
                AnalyzerDispatcher.SimilarityMeasure.vec_cosine_sim //Cosine similarity
            };

            claimLinker = new ClaimLinker(5, similarityMeasures,
                    getServletContext().getResource("/WEB-INF/data/stopwords.txt").getPath(),
                    getServletContext().getResource("/WEB-INF/data/puncs.txt").getPath(),
                    getServletContext().getResource("/WEB-INF/data/english-20200420.hash").getPath(),
                    "localhost");
            System.out.println(getServletName() + " initialization finished! ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        claimLinker = null;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("In: doGet() of claimLinker_Servlet");
        JsonObjectBuilder factory = Json.createObjectBuilder();
        JsonObjectBuilder flags = Json.createObjectBuilder();
        factory.add("message", "API ClaimLinker").add("flags", flags);
        JsonObject respose_json = factory.build();
        if (request.getParameter("app") != null) {
            if (request.getParameter("app").equals("demo")) {
                respose_json = claimLinker_Servlet.ClaimLinkHandler(request, Association_type.same_as);
            }
            if (request.getParameter("app").equals("service")) {
                respose_json = claimLinker_Servlet.ClaimLinkHandler(request, Association_type.same_as);
            }
        }
        String assoc_type = request.getParameter("assoc_t");
//        System.out.println("==>URL: " + request.getParameter("url"));
//        System.out.println("==>assoc_type: " + assoc_type);
        if (request.getParameter("url") != null) {
            if (assoc_type != null) {
                switch (assoc_type) {
                    case "all":
                        respose_json = claimLinker_Servlet.ClaimLinkfromURLHandler(request, Association_type.all);
                        break;
                    case "author_of":
                        respose_json = claimLinker_Servlet.ClaimLinkfromURLHandler(request, Association_type.author_of);
                        break;
                    case "topic_of":
                        respose_json = claimLinker_Servlet.ClaimLinkfromURLHandler(request, Association_type.topic_of);
                        break;
                    case "same_as":
                        respose_json = claimLinker_Servlet.ClaimLinkfromURLHandler(request, Association_type.same_as);
                        break;
                    default:
                        response.setStatus(400);
                }
            }
        }
        if (assoc_type != null) {
            switch (assoc_type) {
                case "all":
                    respose_json = claimLinker_Servlet.ClaimLinkHandler(request, Association_type.all);
                    break;
                case "author_of":
                    respose_json = claimLinker_Servlet.ClaimLinkHandler(request, Association_type.author_of);
                    break;
                case "topic_of":
                    respose_json = claimLinker_Servlet.ClaimLinkHandler(request, Association_type.topic_of);
                    break;
                case "same_as":
                    respose_json = claimLinker_Servlet.ClaimLinkHandler(request, Association_type.same_as);
                    break;
                default:
                    response.setStatus(400);
            }
        }

        PrintWriter out = response.getWriter();
        response.setContentType("text/json");
        out.println(respose_json);
        response.setStatus(200);

    }

    public static JsonObject ClaimLinkHandler(HttpServletRequest request, Association_type associationtype) {
        Instant start = Instant.now();
        String text = request.getParameter("text");
        JsonObjectBuilder factory = Json.createObjectBuilder();
        ArrayList<CLAnnotation> clAnnotations = new ArrayList<CLAnnotation>(claimLinker.claimLink(text, 5, associationtype));
        clAnnotations.trimToSize();

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (CLAnnotation clAnnotation : clAnnotations) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            JsonObject obj = builder.build();
            Map<String, Object> objectMap = clAnnotation.toObject();
            builder.add("text", (String) objectMap.get("text"));
            builder.add("tokenBeginPosition", (Integer) objectMap.get("tokenBeginPosition"));
            builder.add("tokenEndPosition", (Integer) objectMap.get("tokenEndPosition"));
            builder.add("sentencePosition", (Integer) objectMap.get("sentencePosition"));
            builder.add("association_type", objectMap.get("association_type").toString());
            JsonArrayBuilder arrayB = Json.createArrayBuilder();
            for (Object claim : new ArrayList<>((Collection<?>) objectMap.get("linkedClaims"))) {
                Claim linkedClaim = (Claim) claim;
                //System.out.println("o-->" + linkedClaim.getExtraTitle());
                JsonObject linkedClaim_obj = Json.createObjectBuilder()
                        .add("claimReview_claimReviewed", linkedClaim.getReviewedBody())
                        .add("_score", (Double) linkedClaim.getElasticScore())
                        .add("extra_title", (String) linkedClaim.getExtraTitle())
                        .add("rating_alternateName", (String) linkedClaim.getRatingName())
                        .add("creativeWork_author_name", (String) linkedClaim.getAuthorName())
                        .add("claimReview_url", (String) linkedClaim.getclaimReviewedURL())
                        .add("claim_uri", (String) linkedClaim.getClaimURI())
                        .build();
                arrayB.add(linkedClaim_obj);
            }

            builder.add("linkedClaims", arrayB.build());
            arrayBuilder.add(builder);

        }
        factory.add("_results", arrayBuilder);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        factory.add("timeElapsed", timeElapsed);

        return factory.build();
    }

    public static JsonObject ClaimLinkfromURLHandler(HttpServletRequest request, Association_type associationtype) {
        Instant start = Instant.now();
        String param_url = request.getParameter("url");
        System.out.println("PARAM URL = " + param_url);
        JsonObjectBuilder factory = Json.createObjectBuilder();
        if (param_url == null) {
            return null;
        }
        WebArticle webArticle;

        webArticle = new WebArticle(param_url, null, WebArticle.WebArticleType.url);
        ArrayList<CLAnnotation> clAnnotations = new ArrayList<CLAnnotation>(claimLinker.claimLink(webArticle.getDoc().text(), 5, associationtype));

        factory.add("url", param_url).add("cleaned_text_from_url", webArticle.getDoc().text());
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (CLAnnotation clAnnotation : clAnnotations) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            JsonObject obj = builder.build();
            Map<String, Object> objectMap = clAnnotation.toObject();
            builder.add("text", (String) objectMap.get("text"));
            builder.add("tokenBeginPosition", (Integer) objectMap.get("tokenBeginPosition"));
            builder.add("tokenEndPosition", (Integer) objectMap.get("tokenEndPosition"));
            builder.add("sentencePosition", (Integer) objectMap.get("sentencePosition"));
            builder.add("association_type", objectMap.get("association_type").toString());
            JsonArrayBuilder arrayB = Json.createArrayBuilder();
            for (Object claim : new ArrayList<>((Collection<?>) objectMap.get("linkedClaims"))) {
                Claim linkedClaim = (Claim) claim;
                JsonObject linkedClaim_obj = Json.createObjectBuilder()
                        .add("claimReview_claimReviewed", linkedClaim.getReviewedBody())
                        .add("_score", (Double) linkedClaim.getElasticScore())
                        .add("extra_title", (String) linkedClaim.getExtraTitle())
                        .add("rating_alternateName", (String) linkedClaim.getRatingName())
                        .add("creativeWork_author_name", (String) linkedClaim.getAuthorName())
                        .add("claimReview_url", (String) linkedClaim.getclaimReviewedURL())
                        .add("claim_uri", (String) linkedClaim.getClaimURI())
                        .build();
                arrayB.add(linkedClaim_obj);
            }

            builder.add("linkedClaims", arrayB.build());
            arrayBuilder.add(builder);

        }
        factory.add("_results", arrayBuilder);
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        factory.add("timeElapsed", timeElapsed);

        return factory.build();
    }

}

package csd.thesis.servlet;

import csd.thesis.ClaimLinker;
import csd.thesis.ElasticWrapper;
import csd.thesis.model.Claim;
import csd.thesis.model.WebArticle;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.util.Pair;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;

@WebServlet(name = "claimLinker_Servlet")
public class claimLinker_Servlet extends HttpServlet {
    protected static ClaimLinker claimLinker;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            claimLinker = new ClaimLinker(
                    getServletContext().getResource("/WEB-INF/Properties.xml").getPath(),
                    getServletContext().getResource("/WEB-INF/data/stopwords.txt").getPath(),
                    getServletContext().getResource("/WEB-INF/data/english-20200420.hash").getPath(),
                    getServletContext().getResource("/WEB-INF/data/claim_extraction_18_10_2019_annotated.csv").getPath());
            System.out.println(getServletName() + " initialization finished! ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObjectBuilder factory = Json.createObjectBuilder();
        JsonObjectBuilder flags = Json.createObjectBuilder();
//        .add("claimOwner_of",request.getParameter("claimOwner_of")) // if exists
//                .add("topic_of",request.getParameter("topic_of")) // if exists
//                .add("same_as",request.getParameter("same_as")) // if exists
        factory.add("message", "API ClaimLinker").add("flags", flags);
        JsonObject respose_json = factory.build();
//        if (request.getParameter("claimOwner_of").equals("true")) {
//
//        }
//        if (request.getParameter("topic_of").equals("true")) {
//
//        }
        if (request.getParameter("assoc_t").equals("same_as")) {
            respose_json = claimLinker_Servlet.ClaimLinkHandler(request, Assoc_t.same_as);
        }
        PrintWriter out = response.getWriter();
        response.setContentType("text/json");
        out.println(respose_json);
        response.setStatus(200);

    }


    public static JsonObject ClaimLinkHandler(HttpServletRequest request, Assoc_t assoc_t) {
        Instant start = Instant.now();
        String param_url = request.getParameter("url");
        String param_selection = request.getParameter("selection");
        JsonObjectBuilder factory = Json.createObjectBuilder();
        factory.add("message", "API ClaimLinker");
        if (param_url == null) {
            return null;
        }
        WebArticle webArticle;
        if (param_selection != null) {
            webArticle = new WebArticle(param_url, param_selection, WebArticle.WebArticleType.selection);
            factory.add("selection", webArticle.getCleaned());
        } else {
            webArticle = new WebArticle(param_url, null, WebArticle.WebArticleType.url);
        }
        factory.add("url", param_url).add("cleaned_text_from_url", webArticle.getDoc().text());
        factory.add("results", ClaimLinkPipeline(param_selection, assoc_t));
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        factory.add("timeElapsed", timeElapsed);

        return factory.build();
    }

    public static JsonArray ClaimLinkPipeline(String selection, Assoc_t assoc_t) {
        claimLinker.claims = null;
        if (assoc_t == Assoc_t.all) {
            claimLinker.claims = ElasticWrapper.findCatalogItemWithoutApi("claimReview_claimReviewed", URLEncoder.encode(selection, StandardCharsets.UTF_8), 100);
            // needs optimization
            CoreDocument CD_selection = claimLinker.NLP_annotate(
                    claimLinker.nlp_instance.getWithoutStopwords(
                            claimLinker_Servlet.claimLinker.NLP_annotate(selection)));
            //
            ArrayList<Pair<Double, Claim>> records = new ArrayList<>();
            int counter = 0;
            for (Claim claim : claimLinker.claims) {
                CoreDocument CD_c = claimLinker.NLP_annotate(claim.getReviewedBody());
                System.out.printf("%d\r", counter++);
                records.add(new Pair<>(claimLinker.analyzerDispatcher.analyze(CD_c, CD_selection), claim));
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
                System.out.println(elem.first + " \n" + elem.second.getReviewedBody());
            }
            return arrayBuilder.build();
        } else if (assoc_t == Assoc_t.author_of) {

        } else if (assoc_t == Assoc_t.topic_of) {

        } else if (assoc_t == Assoc_t.same_as) {

        }
        return null;
    }
}

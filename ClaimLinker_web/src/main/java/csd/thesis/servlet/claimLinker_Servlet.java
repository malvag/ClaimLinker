package csd.thesis.servlet;

import csd.thesis.ClaimLinker;
import csd.thesis.model.Association_type;
import csd.thesis.model.WebArticle;

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
                    getServletContext().getResource("/WEB-INF/data/claim_extraction_18_10_2019_annotated.csv").getPath(),
                    "192.168.2.112");
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
//        .add("claimOwner_of",request.getParameter("author_of")) // if exists
//                .add("topic_of",request.getParameter("topic_of")) // if exists
//                .add("same_as",request.getParameter("same_as")) // if exists
        factory.add("message", "API ClaimLinker").add("flags", flags);
        JsonObject respose_json = factory.build();
        if (request.getParameter("all").equals("true")) {
            respose_json = claimLinker_Servlet.ClaimLinkHandler(request, Association_type.all);
        }else {
            if (request.getParameter("author_of").equals("true")) {
                respose_json = claimLinker_Servlet.ClaimLinkHandler(request, Association_type.author_of);
            } else if (request.getParameter("topic_of").equals("true")) {
                respose_json = claimLinker_Servlet.ClaimLinkHandler(request, Association_type.topic_of);
            } else if (request.getParameter("same_as").equals("true")) {
                respose_json = claimLinker_Servlet.ClaimLinkHandler(request, Association_type.same_as);
            }
        }
        PrintWriter out = response.getWriter();
        response.setContentType("text/json");
        out.println(respose_json);
        response.setStatus(200);

    }


    public static JsonObject ClaimLinkHandler(HttpServletRequest request, Association_type associationtype) {
        Instant start = Instant.now();
        String param_url = request.getParameter("url");
        String context = request.getParameter("context");
        JsonObjectBuilder factory = Json.createObjectBuilder();
        factory.add("message", "API ClaimLinker");
        if (param_url == null) {
            return null;
        }
        WebArticle webArticle;
        if (context != null) {
            webArticle = new WebArticle(param_url, context, WebArticle.WebArticleType.selection);
            factory.add("selection", webArticle.getSelection());
        } else {
            webArticle = new WebArticle(param_url, null, WebArticle.WebArticleType.url);
        }
        factory.add("url", param_url).add("cleaned_text_from_url", webArticle.getDoc().text());
        factory.add("results", (JsonArrayBuilder) claimLinker.claimLink(webArticle.getDoc().text(), context, 5, 0.4, associationtype));
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        factory.add("timeElapsed", timeElapsed);

        return factory.build();
    }

}

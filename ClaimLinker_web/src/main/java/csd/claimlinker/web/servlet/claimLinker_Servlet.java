package csd.claimlinker.web.servlet;

import csd.claimlinker.ClaimLinker;
import csd.claimlinker.model.Association_type;
import csd.claimlinker.model.CLAnnotation;
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
import java.util.ArrayList;

@WebServlet(name = "claimLinker_Servlet")
public class claimLinker_Servlet extends HttpServlet {
	protected static ClaimLinker claimLinker;

	@Override
	public void init() throws ServletException {
		super.init();
		try {
			AnalyzerDispatcher.SimilarityMeasure[] similarityMeasures = new AnalyzerDispatcher.SimilarityMeasure[]{
					AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_words,           //Common (jaccard) words
					AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_lemm_words,      //Common (jaccard) lemmatized words
					AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ne,              //Common (jaccard) named entities
					AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_dissambig_ents,  //Common (jaccard) disambiguated entities BFY
					AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_pos_words,       //Common (jaccard) words of specific POS
					AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ngram,           //Common (jaccard) ngrams
					AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_nchargram,       //Common (jaccard) nchargrams
					AnalyzerDispatcher.SimilarityMeasure.vec_cosine_sim             //Cosine similarity
			};

			claimLinker = new ClaimLinker(20, similarityMeasures,
					getServletContext().getResource("/WEB-INF/data/stopwords.txt").getPath(),
					getServletContext().getResource("/WEB-INF/data/english-20200420.hash").getPath(),
					"192.168.2.112");
			System.out.println(getServletName() + " initialization finished! ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		claimLinker.claims=null;
		claimLinker = null;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JsonObjectBuilder factory = Json.createObjectBuilder();
		JsonObjectBuilder flags = Json.createObjectBuilder();
		factory.add("message", "API ClaimLinker").add("flags", flags);
		JsonObject respose_json = factory.build();
		if (request.getParameter("app").equals("demo")) {
			respose_json = claimLinker_Servlet.ClaimLinkHandler(request, Association_type.same_as);
		}

		if (request.getParameter("all") != null && request.getParameter("all").equals("true")) {
			respose_json = claimLinker_Servlet.ClaimLinkfromURLHandler(request, Association_type.all);
		}
		if (request.getParameter("author_of") != null && request.getParameter("author_of").equals("true")) {
			respose_json = claimLinker_Servlet.ClaimLinkfromURLHandler(request, Association_type.author_of);
		}
		if (request.getParameter("topic_of") != null && request.getParameter("topic_of").equals("true")) {
			respose_json = claimLinker_Servlet.ClaimLinkfromURLHandler(request, Association_type.topic_of);
		}
		if (request.getParameter("same_as") != null && request.getParameter("same_as").equals("true")) {
			respose_json = claimLinker_Servlet.ClaimLinkfromURLHandler(request, Association_type.same_as);
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
		JSONObject JSON = new JSONObject();
		ArrayList<CLAnnotation> clAnnotations = new ArrayList(claimLinker.claimLink(text, "", 5, 0.4, associationtype));
		clAnnotations.trimToSize();
		JSON.put("clresults",clAnnotations);

		factory.add("_results", JSON.toString());
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();
		factory.add("timeElapsed", timeElapsed);

		return factory.build();
	}


	public static JsonObject ClaimLinkfromURLHandler(HttpServletRequest request, Association_type associationtype) {
		Instant start = Instant.now();
		String param_url = request.getParameter("url");
		String context = request.getParameter("context");
		JsonObjectBuilder factory = Json.createObjectBuilder();
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
		factory.add("clresults", (JsonObjectBuilder) claimLinker.claimLink(webArticle.getDoc().text(), context, 5, 0.2, associationtype));
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();
		factory.add("timeElapsed", timeElapsed);

		return factory.build();
	}

}

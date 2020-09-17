package csd.thesis;

//import csd.thesis.elastic.OpenCSVWrapper;


import csd.thesis.model.Assoc_t;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.vmplugin.v5.JUnit4Utils;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class ClaimLinkerTest {
	public static void main(String[] args) throws Exception {
		testClaimLink();
	}

	public static void testClaimLink() throws Exception {
		System.out.println("[Demo]Initiating ...");
		System.out.println("_______________________________________________");

		ClaimLinker claimLinker = new ClaimLinker("Properties.xml", "data/stopwords.txt", "data/english-20200420.hash", "data/claim_extraction_18_10_2019_annotated.csv", "localhost");
		JsonArray jsonArray = demo_pipeline(claimLinker);
		assert Objects.requireNonNull(jsonArray).size() == 1;
		System.out.println("_______________________________________________");
		System.out.println("[Demo]Exited without errors ...");

	}

	public static JsonArray demo_pipeline(ClaimLinker CLInstance) {
		System.out.println("Demo pipeline started!");
		String t2 = "C++ designer Bjarne Stroustrup";
		String text = "Bjarne Stroustrup is a Danish computer scientist, most notable for the creation and development of the C++ programming language.[4] He is a visiting professor at Columbia University, and works at Morgan Stanley as a Managing Director in New York.";
		Instant start = Instant.now();
		JsonArray results = CLInstance.claimLink(text, t2, Assoc_t.author_of);
		Instant finish = Instant.now();
		long timeElapsed = Duration.between(start, finish).toMillis();
		if (results == null)
			return null;
		for (int i = 0; i < results.size(); i++) {
			JsonObject j = results.getJsonObject(i);
			System.out.printf("%4d %150s %20f %20f \n", i, j.getString("claimReview_claimReviewed"), j.getJsonNumber("NLP_score").doubleValue(), j.getJsonNumber("ElasticScore").doubleValue());
		}

		System.out.println("_______________________________________________");
		System.out.println("Time passed: " + (double) timeElapsed / 1000 + "s");
		System.out.println("Demo pipeline shutting down ...");
		return results;

	}

}
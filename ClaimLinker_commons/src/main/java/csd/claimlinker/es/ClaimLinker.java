package csd.claimlinker.es;

import csd.claimlinker.es.nlp.NLPlib;
import csd.claimlinker.es.misc.ConsoleColor;
import csd.claimlinker.es.model.Association_type;
import csd.claimlinker.es.model.CLAnnotation;
import csd.claimlinker.es.model.Claim;
import csd.claimlinker.es.nlp.AnalyzerDispatcher;
import edu.stanford.nlp.pipeline.CoreDocument;

import java.io.IOException;
import java.util.*;

import static java.util.Comparator.comparing;

public class ClaimLinker {
	public ArrayList<Claim> claims;
	public NLPlib nlp_instance;
	public final static boolean debug = true;
	public AnalyzerDispatcher analyzerDispatcher;
	public ElasticWrapper elasticWrapper;

	public ClaimLinker(double ES_threshold, AnalyzerDispatcher.SimilarityMeasure[] similarityMeasures, String stopwords_path, String Hash_Path, String ES_host) throws IOException, ClassNotFoundException {
		System.out.println("========================================");
		System.out.println("ClaimLinker initializing ... ");
		this.nlp_instance = new NLPlib(stopwords_path, Hash_Path);
		this.claims = new ArrayList<>();
		this.elasticWrapper = new ElasticWrapper(ES_threshold, ES_host, 9200, 9201);
		this.analyzerDispatcher = new AnalyzerDispatcher(this.nlp_instance);
		this.analyzerDispatcher.addSimMeasure(similarityMeasures);
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

	public Set<CLAnnotation> claimLink(String text, String context, int num_of_returned_claims, double similarity_threshold, Association_type associationtype) {
		this.claims = null;
		System.out.println(ConsoleColor.ANSI_YELLOW + "Attempting to claimlink " + ConsoleColor.ANSI_RESET);
		return associationtype.annotate(this, text, context, num_of_returned_claims, similarity_threshold);
	}


}



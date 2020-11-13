package csd.claimlinker;

import csd.claimlinker.es.ElasticWrapper;
import csd.claimlinker.nlp.NLPlib;
import csd.claimlinker.es.misc.ConsoleColor;
import csd.claimlinker.model.Association_type;
import csd.claimlinker.model.CLAnnotation;
import csd.claimlinker.model.Claim;
import csd.claimlinker.nlp.AnalyzerDispatcher;
import edu.stanford.nlp.pipeline.CoreDocument;

import java.io.IOException;
import java.util.*;

import static java.util.Comparator.comparing;

public class ClaimLinker {

	public NLPlib nlp_instance;
	public final static boolean debug = false;
	public AnalyzerDispatcher analyzerDispatcher;
	public ElasticWrapper elasticWrapper;

	public ClaimLinker(double ES_threshold, AnalyzerDispatcher.SimilarityMeasure[] similarityMeasures, String stopwords_path, String Hash_Path, String ES_host) throws IOException, ClassNotFoundException {
		System.out.println("========================================");
		System.out.println("ClaimLinker initializing ... ");

		this.nlp_instance = new NLPlib(stopwords_path, Hash_Path);
		this.elasticWrapper = new ElasticWrapper(ES_threshold, ES_host, 9200, 9201);
		this.analyzerDispatcher = new AnalyzerDispatcher(this.nlp_instance,similarityMeasures);

		System.out.println("ClaimLinker's initialization finished...");
		System.out.println("========================================");
	}


	public Set<CLAnnotation> claimLink(String text, int num_of_returned_claims, Association_type associationtype) {
		System.out.println(ConsoleColor.ANSI_YELLOW + "Attempting to claimlink " + ConsoleColor.ANSI_RESET);
		return associationtype.annotate(this, text, num_of_returned_claims);
	}


}



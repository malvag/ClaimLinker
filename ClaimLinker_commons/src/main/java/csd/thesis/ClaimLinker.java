package csd.thesis;

import com.google.common.collect.Lists;
import csd.thesis.misc.ConsoleColor;
import csd.thesis.model.Association_type;
import csd.thesis.model.CLAnnotation;
import csd.thesis.model.Claim;
import csd.thesis.nlp.AnalyzerDispatcher;
import csd.thesis.nlp.NLPlib;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

public class ClaimLinker {
	public ArrayList<Claim> claims;
	public NLPlib nlp_instance;
	public final static boolean debug = false;
	public AnalyzerDispatcher analyzerDispatcher;
	public ElasticWrapper elasticWrapper;

	public ClaimLinker(double ES_threshold, AnalyzerDispatcher.SimilarityMeasure[] similarityMeasures, String stopwords_path, String Hash_Path, String claims_path, String ES_host) throws IOException, ClassNotFoundException {
		System.out.println("========================================");
		System.out.println("ClaimLinker initializing ... ");
		this.nlp_instance = new NLPlib(stopwords_path, Hash_Path);
		this.claims = new ArrayList<>();
		this.elasticWrapper = new ElasticWrapper(ES_threshold, ES_host, 9200, 9201);
		this.analyzerDispatcher = new AnalyzerDispatcher(this.nlp_instance);
		this.analyzerDispatcher.addSimMeasure(
				similarityMeasures
//				AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_words,           //Common (jaccard) words
//				AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_lemm_words,      //Common (jaccard) lemmatized words
//				AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ne,              //Common (jaccard) named entities
//				AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_dissambig_ents,  //Common (jaccard) disambiguated entities BFY
//				AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_pos_words,       //Common (jaccard) words of specific POS
//				AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ngram,           //Common (jaccard) ngrams
//				AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_nchargram,       //Common (jaccard) nchargrams
//				AnalyzerDispatcher.SimilarityMeasure.vec_cosine_sim             //Cosine similarity
		);
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



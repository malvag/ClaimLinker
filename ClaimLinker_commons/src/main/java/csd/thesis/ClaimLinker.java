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

	public ClaimLinker(String JWNLProperties_path, String stopwords_path, String Hash_Path, String claims_path, String ES_host) throws IOException, ClassNotFoundException {
		System.out.println("========================================");
		System.out.println("ClaimLinker initializing ... ");
		nlp_instance = new NLPlib(JWNLProperties_path, stopwords_path, Hash_Path);
		this.claims = new ArrayList<>();
		this.elasticWrapper = new ElasticWrapper(ES_host, 9200, 9201);
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

	public Set<CLAnnotation> claimLink(String text, String context, int num_of_returned_claims, double similarity_threshold, Association_type associationtype) {
		this.claims = null;
		System.out.println(ConsoleColor.ANSI_YELLOW + "Attempting to claimlink " + ConsoleColor.ANSI_RESET);
		return associationtype.annotate(this, text, context, num_of_returned_claims, similarity_threshold);
	}

	public JsonArray annotate_author_of(String text, String selection) {
		CoreDocument CD_selection = this.NLP_annotate(
				this.nlp_instance.getWithoutStopwords(
						this.NLP_annotate(selection)));
		Annotation document = new Annotation(CD_selection.annotation());
		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		Set<CoreMap> entities = new HashSet<>();

		for (Object sentence : sentences) {
			for (CoreMap mention : ((CoreMap) sentence).get(CoreAnnotations.MentionsAnnotation.class)) {
				for (CoreLabel token : mention.get(CoreAnnotations.TokensAnnotation.class)) {
					if (token.ner().equals("PERSON")) {
						entities.add(mention);
					}
				}
			}
		}
		entities.forEach(elem -> System.out.printf("Person entities : %15s  \n", elem));

		Set<Claim> unique_claims = new HashSet<>();
		entities.forEach(entity -> unique_claims.addAll(this.elasticWrapper.findCatalogItemWithoutApi("creativeWork_author_name", URLEncoder.encode(entity.toString(), StandardCharsets.UTF_8), 5)));
		this.claims = Lists.newArrayList(unique_claims);
		CoreDocument CD_text = this.NLP_annotate(
				this.nlp_instance.getWithoutStopwords(
						this.NLP_annotate(text)));
		ArrayList<Pair<Double, Claim>> records = new ArrayList<>();

		System.out.println("Processing candidate claims");
		for (Claim claim : this.claims) {
			CoreDocument CD_c = this.NLP_annotate(claim.getReviewedBody());
			records.add(new Pair<>(this.analyzerDispatcher.analyze(CD_c, CD_text), claim));
		}

		records.sort(Collections.reverseOrder());
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (Pair<Double, Claim> elem : records) {
			arrayBuilder.add(Json.createObjectBuilder()
							.add("claimReview_claimReviewed", elem.second.getReviewedBody())
							.add("rating_alternateName", elem.second.getRatingName())
							.add("extra_title", elem.second.getExtraTitle())
//                        .add("author_name", elem.second.getAuthorName())
							.add("NLP_score", elem.first)
							.add("ElasticScore", elem.second.getElasticScore())

			);
		}
		return arrayBuilder.build();
	}

	public JsonArray annotate_topic_of(String text, String selection) {
		CoreDocument CD_selection = this.NLP_annotate(
				this.nlp_instance.getWithoutStopwords(
						this.NLP_annotate(selection)));
		Annotation document = new Annotation(CD_selection.annotation());
		List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		Set<CoreMap> NNouns = new HashSet<>();


		for (CoreMap sentence : sentences) {
			for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				String ne = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				if (ne.startsWith("NN"))
					NNouns.add(token);
			}
		}
		NNouns.forEach(elem -> System.out.printf("Nouns : %15s  \n", elem));
		// match the persons with the claims_author in ES
		Set<Claim> unique_claims = new HashSet<>();
		NNouns.forEach(noun -> unique_claims.addAll(this.elasticWrapper.findCatalogItemWithoutApi("claimReview_claimReviewed", URLEncoder.encode(noun.toString(), StandardCharsets.UTF_8), 50)));
		List<Claim> unique = unique_claims.stream()
				.collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(Claim::getReviewedBody))),
						ArrayList::new));

		this.claims = Lists.newArrayList(unique);
		// generate candidates and rank them with Sim Measures
		CoreDocument CD_text = this.NLP_annotate(
				this.nlp_instance.getWithoutStopwords(
						this.NLP_annotate(text)));
		ArrayList<Pair<Double, Claim>> records = new ArrayList<>();

		System.out.println("Processing candidate claims");
		for (Claim claim : this.claims) {
			CoreDocument CD_c = this.NLP_annotate(claim.getReviewedBody());
			records.add(new Pair<>(this.analyzerDispatcher.analyze(CD_c, CD_text), claim));
		}

		records.sort(Collections.reverseOrder());
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (Pair<Double, Claim> elem : records) {
			arrayBuilder.add(Json.createObjectBuilder()
							.add("claimReview_claimReviewed", elem.second.getReviewedBody())
							.add("rating_alternateName", elem.second.getRatingName())
							.add("extra_title", elem.second.getExtraTitle())
//                        .add("author_name", elem.second.getAuthorName())
							.add("NLP_score", elem.first)
							.add("ElasticScore", elem.second.getElasticScore())

			);
		}
		return arrayBuilder.build();
	}


}



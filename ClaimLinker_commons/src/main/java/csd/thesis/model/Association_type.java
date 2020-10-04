package csd.thesis.model;

import com.google.common.collect.Lists;
import csd.thesis.ClaimLinker;
import csd.thesis.misc.ConsoleColor;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

public enum Association_type {

	author_of {
		// find the Persons from selection
		// match the persons with the claims_author in ES
		// generate candidates and rank them with Sim Measures based on the whole text's similarities per sentence
		@Override
		public Set<CLAnnotation> annotate(ClaimLinker claimLinker, String text, String context, int num_of_result, double similarity_threshold) {
			final int hits = 100;
			System.out.println(ConsoleColor.ANSI_YELLOW + "[Author_of] Attempting to claimlink with association_type " + this + ConsoleColor.ANSI_RESET);
			Instant start = Instant.now();
			CoreDocument CD_selection = claimLinker.NLP_annotate(
					claimLinker.nlp_instance.getWithoutStopwords(
							claimLinker.NLP_annotate(text)));
			Annotation document = new Annotation(CD_selection.annotation());
			List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
			Map<CLAnnotation, CoreMap> entities = new HashMap<>();
			int sentencePosition = -1;
			for (Object sentence : sentences) {
				sentencePosition++;
				for (CoreMap mention : ((CoreMap) sentence).get(CoreAnnotations.MentionsAnnotation.class)) {
					AtomicBoolean skip = new AtomicBoolean(false);
					int tokenPosition = -1;
					for (CoreLabel token : mention.get(CoreAnnotations.TokensAnnotation.class)) {
						tokenPosition++;
						if (token.ner().equals("PERSON")) {

							System.out.print("[Author_of] token: " + token.originalText());
							token.nerConfidence().forEach((str, dbl) -> {
								System.out.println(" " + str + " " + dbl);
								if (dbl < 0.5) {
									skip.set(true);
									System.out.println("[Author_of] ^skipped");
								}
							});
							if (skip.get())
								continue;
							CLAnnotation annotation = new CLAnnotation(mention.toString(), token.beginPosition(), token.endPosition(), sentencePosition, this);
							this.annotationSet.add(annotation);
							entities.put(annotation, mention);
						}
					}
				}
			}

			entities.forEach((annotation, mention) -> {
				System.out.printf("[Author_of] Person entities : %15s  \n", mention.toString());
				CLAnnotation tmp = annotationSet.stream().filter(item -> item.equals(annotation)).findFirst().get();
				tmp.getLinkedClaims().addAll(claimLinker.elasticWrapper.findCatalogItemWithoutApi("creativeWork_author_name", URLEncoder.encode(mention.toString(), StandardCharsets.UTF_8), hits));
			});

			CoreDocument CD_text = claimLinker.NLP_annotate(
					claimLinker.nlp_instance.getWithoutStopwords(
							claimLinker.NLP_annotate(text)));

			System.out.println("[Author_of] Processing candidate claims");
			this.annotationSet.forEach(annotation -> {
				PriorityQueue<Claim> records = new PriorityQueue<>();
				for (Claim claim : annotation.getLinkedClaims()) {
					CoreDocument CD_c = claimLinker.NLP_annotate(claim.getReviewedBody());
					claim.setNLPScore(claimLinker.analyzerDispatcher.analyze(CD_c, CD_text));
					records.add(claim);
				}
				while (records.size() > num_of_result) {
					records.remove(); // get only as many results as we needed
				}
				annotation.setLinkedClaims(new ArrayList<>(records));
			});
			Instant finish = Instant.now();
			long timeElapsed = Duration.between(start, finish).toMillis();
			System.out.println("[Author_of] Time passed: " + (double) timeElapsed / 1000 + "s");

			return this.annotationSet;
		}
	}, topic_of {
		// Consider all sentences (filter out those with small Elasticsearch retrieval score–we need to find a threshold)
		// match those nouns as keywords in ES
		// generate candidates and rank them with Sim Measures
		// Given a sentence, we can submit a keyword query to an Elasticsearch index and get a ranked list of candidate claims
		@Override
		public Set<CLAnnotation> annotate(ClaimLinker claimLinker, String text, String context, int num_of_result, double similarity_threshold) {
			System.out.println(ConsoleColor.ANSI_YELLOW + "Attempting to claimlink with association_type " + this + ConsoleColor.ANSI_RESET);
			Instant start = Instant.now();
			final int hits = 30;
			CoreDocument CD_text = claimLinker.NLP_annotate(
					claimLinker.nlp_instance.getWithoutStopwords(
							claimLinker.NLP_annotate(text)));
			Annotation document = new Annotation(CD_text.annotation());
			List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
//			Set<String> NNouns = new HashSet<>();
			Map<CLAnnotation, CoreLabel> NNouns_map = new HashMap<>();

			int sentencePosition = 0;
			for (CoreMap sentence : sentences) {
				for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
					String ne = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
					if (ne.startsWith("NN")) {
//						NNouns.add(token.lemma());
						CLAnnotation annotation = new CLAnnotation(token.lemma(), token.beginPosition(), token.endPosition(), sentencePosition, this);
						this.annotationSet.add(annotation);
						NNouns_map.put(annotation, token);
					}
				}
				sentencePosition++;
			}
			// match the NNouns with the claims_text in ES
			NNouns_map.forEach((annotation, noun) -> {
				System.out.printf("[Topic_of]  : %15s  \n", noun.toString());
				CLAnnotation tmp = annotationSet.stream().filter(item -> item.equals(annotation)).findFirst().get();
				tmp.getLinkedClaims().addAll(claimLinker.elasticWrapper.findCatalogItemWithoutApi("claimReview_claimReviewed", URLEncoder.encode(noun.lemma(), StandardCharsets.UTF_8), hits));
			});

			// generate candidates and rank them with Sim Measures

			System.out.println("[Topic_of] Processing candidate claims");
			this.annotationSet.forEach(annotation -> {
				PriorityQueue<Claim> records = new PriorityQueue<>();
				for (Claim claim : annotation.getLinkedClaims()) {
					CoreDocument CD_c = claimLinker.NLP_annotate(claim.getReviewedBody());
					claim.setNLPScore(claimLinker.analyzerDispatcher.analyze(CD_c, CD_text));
					records.add(claim);
				}
				while (records.size() > num_of_result) {
					records.remove(); // get only as many results as we needed
				}
				annotation.setLinkedClaims(new ArrayList<>(records));
			});
			Instant finish = Instant.now();
			long timeElapsed = Duration.between(start, finish).toMillis();
			System.out.println("[Topic_of] Time passed: " + (double) timeElapsed / 1000 + "s");
			return this.annotationSet;
		}
	}, same_as {
		// Consider all sentences (filter out those with small Elasticsearch retrieval score–we need to find a threshold)
		// match those nouns as keywords in ES
		// generate candidates and rank them with Sim Measures
		// Given a sentence, we can submit a keyword query to an Elasticsearch index and get a ranked list of candidate claims
		@Override
		public Set<CLAnnotation> annotate(ClaimLinker claimLinker, String text, String context, int num_of_result, double similarity_threshold) {
			System.out.println(ConsoleColor.ANSI_YELLOW + "Attempting to claimlink with association_type " + this + ConsoleColor.ANSI_RESET);
			Instant start = Instant.now();
			final int hits = 30;
			CoreDocument CD_text = claimLinker.NLP_annotate(text);
			Annotation document = new Annotation(CD_text.annotation());
			List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
			int sentencePosition = 0;
			Map<CLAnnotation, CoreMap> claims_map = new HashMap<>();

			for (CoreMap sentence : sentences) {
				CLAnnotation annotation = new CLAnnotation(sentence.toString(), -1, -1, sentencePosition, this);
				this.annotationSet.add(annotation);
				claims_map.put(annotation, sentence);
				sentencePosition++;
			}
			claims_map.forEach((annotation, sentence) -> {
				synchronized (this) {
					System.out.printf("[Same_as]  : %15s  \n", sentence.toString());
					CLAnnotation tmp = annotationSet.stream().filter(item -> item.equals(annotation)).findFirst().get();
					CoreDocument doc = claimLinker.nlp_instance.NLPlib_annotate(new CoreDocument(sentence.toString()));
					tmp.getLinkedClaims().addAll(claimLinker.elasticWrapper.findCatalogItemWithoutApi("claimReview_claimReviewed", URLEncoder.encode(
							claimLinker.nlp_instance.getWithoutStopwords(doc), StandardCharsets.UTF_8), hits));
				}
			});
			System.out.println("[Same_as] Processing candidate claims");
			this.annotationSet.forEach(annotation -> {
				PriorityQueue<Claim> records = new PriorityQueue<>();
				for (Claim claim : annotation.getLinkedClaims()) {
					CoreDocument CD_c = claimLinker.NLP_annotate(claim.getReviewedBody());
					claim.setNLPScore(claimLinker.analyzerDispatcher.analyze(CD_c, CD_text));
					records.add(claim);
				}
				while (records.size() > num_of_result) {
					records.remove(); // get only as many results as we needed
				}
				annotation.setLinkedClaims(new ArrayList<>(records));
			});
			Instant finish = Instant.now();
			long timeElapsed = Duration.between(start, finish).toMillis();
			System.out.println("[Same_as] Time passed: " + (double) timeElapsed / 1000 + "s");
			this.annotationSet.forEach(System.out::println);
			return this.annotationSet;
		}
	}, all {
		@Override
		public Set<CLAnnotation> annotate(ClaimLinker claimLinker, String text, String context, int num_of_result, double similarity_threshold) {
			Instant start = Instant.now();
			this.annotationSet.addAll(Association_type.author_of.annotate(claimLinker, text, context, num_of_result, similarity_threshold));
			this.annotationSet.addAll(Association_type.topic_of.annotate(claimLinker, text, context, num_of_result, similarity_threshold));
			this.annotationSet.addAll(Association_type.same_as.annotate(claimLinker, text, context, num_of_result, similarity_threshold));
			System.out.println("----");
			Instant finish = Instant.now();
			long timeElapsed = Duration.between(start, finish).toMillis();
			System.out.println("[All] Time passed: " + (double) timeElapsed / 1000 + "s");
//			this.annotationSet.forEach(System.out::println);

			return this.annotationSet;
		}
	};

	Association_type() {
		this.annotationSet = new HashSet<>();
	}

	public Set<CLAnnotation> annotationSet;

	abstract public Set<CLAnnotation> annotate(ClaimLinker claimLinker, String text, String context, int num_of_result, double similarity_threshold);
}
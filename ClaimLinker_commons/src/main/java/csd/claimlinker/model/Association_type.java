package csd.claimlinker.model;

import csd.claimlinker.ClaimLinker;
import csd.claimlinker.es.misc.ConsoleColor;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.util.CoreMap;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public enum Association_type {

    author_of {
        // find the Persons from selection
        // match the persons with the claims_author in ES
        // generate candidates and rank them with Sim Measures based on the whole text's similarities per sentence
        @Override
        public Set<CLAnnotation> annotate(ClaimLinker claimLinker, String text, int num_of_result) {

            final int hits = 100;
            System.out.println(ConsoleColor.ANSI_YELLOW + "[Author_of] Attempting to claimlink with association_type " + this + ConsoleColor.ANSI_RESET);
            Instant start = Instant.now();
            CoreDocument CD_selection = claimLinker.nlp_instance.NLPlib_annotate(
                    claimLinker.nlp_instance.getWithoutStopwords(
                            claimLinker.nlp_instance.NLPlib_annotate(text)));
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
                            if (skip.get()) {
                                continue;
                            }
                            CLAnnotation annotation = new CLAnnotation(mention.toString(), token.beginPosition(), token.endPosition(), sentencePosition, this);
                            this.annotationSet.removeIf(it -> it.getAssoc_t() == this && it.getText().equals(mention.toString()));
                            this.annotationSet.add(annotation);
                            entities.put(annotation, mention);
                        }
                    }
                }
            }

            entities.forEach((annotation, mention) -> {
                System.out.printf("[Author_of] Person entities : %15s  \n", mention.toString());
                CLAnnotation tmp = annotationSet.stream().filter(item -> item.equals(annotation)).findFirst().get();
                ArrayList<Claim> results = claimLinker.elasticWrapper.findCatalogItemWithoutApi(true, new String[]{"creativeWork_author_name"}, URLEncoder.encode(mention.toString(), StandardCharsets.UTF_8), hits);
                if (results != null) {
                    tmp.getLinkedClaims().addAll(results);
                }
            });

            CoreDocument CD_text = claimLinker.nlp_instance.NLPlib_annotate(
                    claimLinker.nlp_instance.getWithoutStopwords(
                            claimLinker.nlp_instance.NLPlib_annotate(text)));

            System.out.println("[Author_of] Processing candidate claims");
            this.annotationSet.forEach(annotation -> {
                PriorityQueue<Claim> records = new PriorityQueue<>();
                for (Claim claim : annotation.getLinkedClaims()) {
                    CoreDocument CD_c = claimLinker.nlp_instance.NLPlib_annotate(claim.getReviewedBody());
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

        @Override
        public String toString() {
            return "author_of";
        }
    }, topic_of {
        // Consider all sentences (filter out those with small Elasticsearch retrieval score–we need to find a threshold)
        // match those nouns as keywords in ES
        // generate candidates and rank them with Sim Measures
        // Given a sentence, we can submit a keyword query to an Elasticsearch index and get a ranked list of candidate claims
        @Override
        public Set<CLAnnotation> annotate(ClaimLinker claimLinker, String text, int num_of_result) {
            System.out.println(ConsoleColor.ANSI_YELLOW + "Attempting to claimlink with association_type " + this + ConsoleColor.ANSI_RESET);
            Instant start = Instant.now();
            final int hits = 30;
            CoreDocument CD_text = claimLinker.nlp_instance.NLPlib_annotate(
                    claimLinker.nlp_instance.getWithoutStopwords(
                            claimLinker.nlp_instance.NLPlib_annotate(text)));
            Annotation document = new Annotation(CD_text.annotation());
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
            Map<CLAnnotation, CoreLabel> NNouns_map = new HashMap<>();

            int sentencePosition = 0;
            for (CoreMap sentence : sentences) {
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    String ne = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    if (ne.startsWith("NN")) {
                        CLAnnotation annotation = new CLAnnotation(token.lemma(), token.beginPosition(), token.endPosition(), sentencePosition, this);
                        this.annotationSet.removeIf(it -> it.getAssoc_t() == this && it.getText().equals(token.lemma()));
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
                ArrayList<Claim> results = claimLinker.elasticWrapper.findCatalogItemWithoutApi(true, new String[]{"claimReview_claimReviewed", "extra_title"}, URLEncoder.encode(noun.lemma(), StandardCharsets.UTF_8), hits);
                if (results != null) {
                    tmp.getLinkedClaims().addAll(results);
                }
            });

            // generate candidates and rank them with Sim Measures
            System.out.println("[Topic_of] Processing candidate claims");
            this.annotationSet.forEach(annotation -> {
                PriorityQueue<Claim> records = new PriorityQueue<>();
                for (Claim claim : annotation.getLinkedClaims()) {
                    CoreDocument CD_c = claimLinker.nlp_instance.NLPlib_annotate(claim.getReviewedBody());
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

        @Override
        public String toString() {
            return "topic_of";
        }
    }, same_as {
        // Consider all sentences (filter out those with small Elasticsearch retrieval score–we need to find a threshold)
        // match those nouns as keywords in ES
        // generate candidates and rank them with Sim Measures
        // Given a sentence, we can submit a keyword query to an Elasticsearch index and get a ranked list of candidate claims
        @Override
        public Set<CLAnnotation> annotate(ClaimLinker claimLinker, String text, int num_of_result) {
            System.out.println(ConsoleColor.ANSI_YELLOW + "Attempting to claimlink with association_type " + this + ConsoleColor.ANSI_RESET);
            Instant start = Instant.now();
            final int hits = 30;
            CoreDocument CD_text = claimLinker.nlp_instance.NLPlib_annotate(text);
            Annotation document = new Annotation(CD_text.annotation());
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

            int sentencePosition = 0;
            Map<CLAnnotation, CoreMap> claims_map = new HashMap<>();
            //Create a new CLAnnotation for every sentence

            this.annotationSet.clear(); // BY PAVLOS

            for (CoreMap sentence : sentences) {
                CLAnnotation annotation = new CLAnnotation(sentence.toString(), -1, -1, sentencePosition, this);
                this.annotationSet.removeIf(it -> it.getAssoc_t() == this && it.getText().equals(sentence.toString()));
                this.annotationSet.add(annotation);
                claims_map.put(annotation, sentence);
                sentencePosition++;
            }
            claims_map.forEach((annotation, sentence) -> {
                synchronized (this) {
                    System.out.printf("[Same_as]  : %15s  \n", sentence.toString());
                    CLAnnotation tmp = annotationSet.stream().filter(item -> item.equals(annotation)).findFirst().get();
                    CoreDocument doc = claimLinker.nlp_instance.NLPlib_annotate(sentence.toString());
                    ArrayList<Claim> results = claimLinker.elasticWrapper.findCatalogItemWithoutApi(false, new String[]{"claimReview_claimReviewed", "extra_title"}, URLEncoder.encode(
                            claimLinker.nlp_instance.getWithoutStopwords(doc), StandardCharsets.UTF_8), hits);
                    if (results != null) {
                        tmp.getLinkedClaims().addAll(results);
                    }
                }
            });
            System.out.println("[Same_as] Processing candidate claims");
            this.annotationSet.removeIf((CLAnnotation a) -> a.getLinkedClaims().size() == 0);
            this.annotationSet.forEach(annotation -> {

                PriorityQueue<Claim> tmp = new PriorityQueue<>();
                String sentence_punc_cleaned = claimLinker.nlp_instance.getWithoutPunctuations(claimLinker.nlp_instance.NLPlib_annotate(new CoreDocument(annotation.getText())));
                //System.out.println("     sentence_punc_cleaned = " + sentence_punc_cleaned);
                CoreDocument CD_sentence = claimLinker.nlp_instance.NLPlib_annotate(sentence_punc_cleaned);
                for (Claim claim : annotation.getLinkedClaims()) {
                    String claim_punc_cleaned = claimLinker.nlp_instance.getWithoutPunctuations(claimLinker.nlp_instance.NLPlib_annotate(new CoreDocument(claim.getReviewedBody())));
                    CoreDocument CD_c = claimLinker.nlp_instance.NLPlib_annotate(claim_punc_cleaned);
                    claim.setNLPScore(claimLinker.analyzerDispatcher.analyze(CD_c, CD_sentence));// for the specific sentence
                    tmp.add(claim);
                }
                ArrayList<Claim> records = new ArrayList<>();
                while (!tmp.isEmpty()) {
                    records.add(0, tmp.poll());
                }

                if (records.size() > num_of_result) {
                    // get only as many results as we needed
                    records.subList(num_of_result, records.size()).clear();
                }
                annotation.setLinkedClaims(new ArrayList<>(records));

            });
            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();
            System.out.println("[Same_as] Time passed: " + (double) timeElapsed / 1000 + "s");
            this.annotationSet.forEach(System.out::println);
            return this.annotationSet;
        }

        @Override
        public String toString() {
            return "same_as";
        }
    }, all {
        @Override
        public Set<CLAnnotation> annotate(ClaimLinker claimLinker, String text, int num_of_result) {
            Instant start = Instant.now();
            this.annotationSet.addAll(Association_type.author_of.annotate(claimLinker, text, num_of_result));
            this.annotationSet.addAll(Association_type.topic_of.annotate(claimLinker, text, num_of_result));
            this.annotationSet.addAll(Association_type.same_as.annotate(claimLinker, text, num_of_result));
            System.out.println("----");
            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();
            System.out.println("[All] Time passed: " + (double) timeElapsed / 1000 + "s");
            return this.annotationSet;
        }

        @Override
        public String toString() {
            return "all";
        }
    };

    Association_type() {
        this.annotationSet = new HashSet<>();
    }

    public Set<CLAnnotation> annotationSet;

    abstract public Set<CLAnnotation> annotate(ClaimLinker claimLinker, String text, int num_of_result);
}

package csd.thesis.tools;

import csd.thesis.misc.ConsoleColor;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.util.Pair;
import it.uniroma1.lcl.jlt.util.Language;
import org.apache.commons.text.similarity.CosineSimilarity;
import org.apache.commons.text.similarity.JaccardSimilarity;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class AnalyzerDispatcher {
    private static NLPlib nlp_instance;

    public AnalyzerDispatcher(NLPlib nlp_instance) {
        AnalyzerDispatcher.nlp_instance = nlp_instance;
        this.similarityMeasures = new ArrayList<>();
    }

    public void addSimMeasure(SimilarityMeasure[] similarityMeasure_array) {
        this.similarityMeasures.addAll(Arrays.asList(similarityMeasure_array));
    }

    public void analyze(CoreDocument claim, CoreDocument text) {
        ArrayList<Double> arr = new ArrayList<>();
        ExecutorService es = Executors.newCachedThreadPool();

        text.sentences().forEach(sentence -> {
            AtomicReference<Double> sum = new AtomicReference<>(0d);
            this.similarityMeasures.forEach(elem -> {
                es.execute(() -> {
                    double result = elem.analyze(claim, sentence.document());
                    sum.getAndUpdate(v -> v + result < 0 ? 0 : result);
                });
            });
            arr.add(sum.getAcquire());
        });
        es.shutdown();


        while (true) {
            try {
                if (es.awaitTermination(1, TimeUnit.MINUTES))
                    break;
            } catch (InterruptedException e) {
                System.err.println("Concurrent measures crashed");
                break;
            }

        }

//        Collections.sort(arr);
//        arr.forEach(elem -> {
//            System.out.println("Sentence " + elem / this.similarityMeasures.size());
//        });

    }

    private ArrayList<SimilarityMeasure> similarityMeasures;

    public enum SimilarityMeasure {
        jcrd_comm_words {
            //    Common (jaccard) words
            @Override
            double analyze(CoreDocument claim, CoreDocument text) {
                double result = similarity(claim.tokens().stream()
                        .map(CoreLabel::originalText)
                        .collect(Collectors.toList()), text.tokens().stream()
                        .map(CoreLabel::originalText)
                        .collect(Collectors.toList()));
                synchronized (this) {
                    String out = ConsoleColor.ANSI_GREEN + "INFO Common (jaccard) words similarity applied" + ConsoleColor.ANSI_RESET;
                    if (debug) System.out.printf("[ClaimLinker] %-100s [%20s]\n", out, result);
                }
                return result;
            }
        },
        jcrd_comm_lemm_words {
            //    Common (jaccard) lemmatized words
            @Override
            double analyze(CoreDocument claim, CoreDocument text) {
                double result = similarity(super.nlp_instance.getLemmas(claim), super.nlp_instance.getLemmas(text));

                synchronized (this) {
                    String out = ConsoleColor.ANSI_GREEN + "INFO Common (jaccard) lemmatized words similarity applied" + ConsoleColor.ANSI_RESET;
                    if (debug) System.out.printf("[ClaimLinker] %-100s [%20s]\n", out, result);
                }
                return result;
            }
        },
        jcrd_comm_ne {
            //    Common (jaccard) named entities (result of StanfrodNLP)
            @Override
            double analyze(CoreDocument claim, CoreDocument text) {
                ArrayList<String> listA = super.nlp_instance.getAnnotationSentences(claim);
                ArrayList<String> listB = super.nlp_instance.getAnnotationSentences(text);

                if (listA.size() == 0 || listB.size() == 0) {
                    System.out.println(-1);
                    return -1;
                }
                double result = similarity(listA, listB);
                synchronized (this) {
                    String out = ConsoleColor.ANSI_GREEN + "INFO Common (jaccard) named entities StanfordNLP similarity applied" + ConsoleColor.ANSI_RESET;
                    if (debug) System.out.printf("[ClaimLinker] %-100s [%20s]\n", out, result);
                }
                return result;
            }
        },
        jcrd_comm_dissambig_ents {
            //    Common (jaccard) disambiguated entities (result of Babelfy)
            @Override
            double analyze(CoreDocument claim, CoreDocument text) {
                ArrayList<String> listA = super.nlp_instance.getAnnotationSentences(claim);
                ArrayList<String> listB = super.nlp_instance.getAnnotationSentences(text);
                ArrayList<String> bfyA = new ArrayList<>();
                ArrayList<String> bfyB = new ArrayList<>();
                JaccardSimilarity JS = new JaccardSimilarity();
                if (verbose) {
                    System.out.println("- Document A:\"" + claim.text() + "\"");
                    System.out.println("- Document B:\"" + text.text() + "\"");
                }
                babelfy(listA, bfyA);
                babelfy(listB, bfyB);
                if (verbose) {
                    bfyA.forEach(System.out::println);
                    bfyB.forEach(System.out::println);
                }
                bfyA.trimToSize();
                bfyB.trimToSize();
                if (bfyA.size() == 0 || bfyB.size() == 0) {
                    System.out.println(-1);
                    return -1;
                }
                double result = similarity(bfyA, bfyB);
                synchronized (this) {
                    String out = ConsoleColor.ANSI_GREEN + "INFO Common (jaccard) disambiguated entities BFY similarity applied" + ConsoleColor.ANSI_RESET;
                    if (debug) System.out.printf("[ClaimLinker] %-100s [%20s]\n", out, result);
                }
                return result;
            }

            private void babelfy(ArrayList<String> list, ArrayList<String> bfy) {
                try {
                    super.nlp_instance.getBfy().babelfy(String.join(" ", list), Language.EN).forEach(elem -> {
                        String token = String.join(" ", list).substring(elem.getCharOffsetFragment().getStart(),
                                elem.getCharOffsetFragment().getEnd() + 1);
                        if (verbose) {
                            System.out.println("Coherence " + elem.getCoherenceScore());
                            System.out.println("Global " + elem.getGlobalScore());
                            System.out.println("Score " + elem.getScore());
                            System.out.println(token);
                            System.out.println("------");
                        }
                        if (elem.getCoherenceScore() >= 0.2)
                            if (elem.getGlobalScore() != 0)
                                bfy.add(token);
                    });
                } catch (Exception e) {
                    System.err.println("Bfy exceeded usage limit");
                }
            }
        },
        jcrd_comm_pos_words {
            //Common (jaccard) words of specific POS
            @Override
            double analyze(CoreDocument claim, CoreDocument text) {
                JaccardSimilarity JS = new JaccardSimilarity();
                ArrayList<Pair<String, String>> A_Nouns = new ArrayList<>();
                ArrayList<Pair<String, String>> A_Verbs = new ArrayList<>();
                ArrayList<Pair<String, String>> A_Adverbs = new ArrayList<>();
                ArrayList<Pair<String, String>> A_Wh_pronoun = new ArrayList<>();
                ArrayList<Pair<String, String>> B_Nouns = new ArrayList<>();
                ArrayList<Pair<String, String>> B_Verbs = new ArrayList<>();
                ArrayList<Pair<String, String>> B_Adverbs = new ArrayList<>();
                ArrayList<Pair<String, String>> B_Wh_pronoun = new ArrayList<>();

                claim.tokens().forEach(elem -> {
                    String ne = elem.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//                    System.out.println(ne + " " + elem.originalText());
                    if (ne.startsWith("NN")) {
                        A_Nouns.add(new Pair<String, String>(ne, elem.originalText()));
                    } else if (ne.startsWith("RB")) {
                        A_Adverbs.add(new Pair<String, String>(ne, elem.originalText()));
                    } else if (ne.startsWith("VB")) {
                        A_Verbs.add(new Pair<String, String>(ne, elem.originalText()));
                    } else if (ne.equals("WP")) {
                        A_Wh_pronoun.add(new Pair<String, String>(ne, elem.originalText()));
                    }
                });
                text.tokens().forEach(elem -> {
                    String ne = elem.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//                    System.out.println(ne + " " + elem.originalText());
                    if (ne.startsWith("NN")) {
                        B_Nouns.add(new Pair<String, String>(ne, elem.originalText()));
                    } else if (ne.startsWith("RB")) {
                        B_Adverbs.add(new Pair<String, String>(ne, elem.originalText()));
                    } else if (ne.startsWith("VB")) {
                        B_Verbs.add(new Pair<String, String>(ne, elem.originalText()));
                    } else if (ne.equals("WP")) {
                        B_Wh_pronoun.add(new Pair<String, String>(ne, elem.originalText()));
                    }
                });
                if (verbose) {
                    A_Nouns.forEach(System.out::println);
                    System.out.println("-");
                    B_Nouns.forEach(System.out::println);
                    System.out.println("- - ");
                    A_Verbs.forEach(System.out::println);
                    System.out.println("-");
                    B_Verbs.forEach(System.out::println);
                    System.out.println("- - ");
                    A_Adverbs.forEach(System.out::println);
                    System.out.println("-");
                    B_Adverbs.forEach(System.out::println);
                    System.out.println("- - ");
                    A_Wh_pronoun.forEach(System.out::println);
                    System.out.println("-");
                    B_Wh_pronoun.forEach(System.out::println);
                    System.out.println("\nVerbs:      " + this.similarity(A_Verbs, B_Verbs));
                    System.out.println("Nouns:      " + this.similarity(A_Nouns, B_Nouns));
                    System.out.println("Adverbs:    " + this.similarity(A_Adverbs, B_Adverbs));
                    System.out.println("Wh_pronoun: " + this.similarity(A_Wh_pronoun, B_Wh_pronoun));
                }
                double result = 0d, tmp = 0d;
                int elems = 0;
                if ((tmp = this.similarity(A_Verbs, B_Verbs)) >= 0) {
                    result += tmp;
                    elems++;
                }
                if ((tmp = this.similarity(A_Nouns, B_Nouns)) >= 0){
                    result += tmp;
                    elems++;
                }
                if ((tmp = this.similarity(A_Adverbs, B_Adverbs)) >= 0){
                    result += tmp;
                    elems++;
                }
                if ((tmp = this.similarity(A_Wh_pronoun, B_Wh_pronoun)) >= 0){
                    result += tmp;
                    elems++;
                }

                synchronized (this) {
                    String out = (ConsoleColor.ANSI_GREEN + "INFO Common (jaccard) words of specific POS similarity applied" + ConsoleColor.ANSI_RESET);
                    if (debug) System.out.printf("[ClaimLinker] %-100s [%20s]\n", out, (double) result / elems);
                }
                return (double) result / elems;
            }
        },
        jcrd_comm_ngram {
            //    Num of common n-grams (e.g., 2-grams, 3-grams, 4-grams, 5-grams)
            @Override
            double analyze(CoreDocument claim, CoreDocument text) {
                double Ngram2 = this.similarity(getNgrams(2, claim), getNgrams(2, text));
                double Ngram3 = this.similarity(getNgrams(3, claim), getNgrams(3, text));
                double Ngram4 = this.similarity(getNgrams(4, claim), getNgrams(4, text));
                double result = 0;
                synchronized (this) {
                    String out = ConsoleColor.ANSI_GREEN + "INFO Common (jaccard) ngrams similarity applied" + ConsoleColor.ANSI_RESET;
                    if(verbose) {
                        System.out.printf("2_grams:    %11fd %20s\n", Ngram2, " importance factor 20%");
                        System.out.printf("3_grams:    %11fd %20s\n", Ngram3, " importance factor 45%");
                        System.out.printf("4_grams:    %11fd %20s\n", Ngram4, " importance factor 35%");
                    }
                    result = Ngram2 * ((double) 20 / 100) + Ngram3 * ((double) 45 / 100) + Ngram4 * ((double) 35 / 100);
                    if (debug) System.out.printf("[ClaimLinker] %-100s [%20s]\n", out, result);

                }
                return result;
            }

            ArrayList<String> getNgrams(int n, CoreDocument a) {
                ArrayList<String> list = (ArrayList<String>) a.tokens().stream()
                        .map(CoreLabel::originalText)
                        .collect(Collectors.toList());
                ArrayList<String> ngrams = new ArrayList<>();
                for (int i = 0; i < list.size(); i ++) {
                    StringBuilder entry = new StringBuilder();
                    for (int j = i; j < i + n && i + n - 1 < list.size(); j++) {
                        entry.append(list.get(j)).append(" ");
                    }
                    ngrams.add(entry.toString());
//                    System.out.println(entry.toString());
                }
                return ngrams;
            }
        },
        jcrd_comm_nchargram {
            //    Num of common n-grams (e.g., 2-grams, 3-grams, 4-grams, 5-grams)
            @Override
            double analyze(CoreDocument claim, CoreDocument text) {
                double Ngram2 = this.similarity(getNchargrams(2, claim), getNchargrams(2, text));
                double Ngram3 = this.similarity(getNchargrams(3, claim), getNchargrams(3, text));
                double Ngram4 = this.similarity(getNchargrams(4, claim), getNchargrams(4, text));
                double result = 0;
                synchronized (this) {
                    String out = ConsoleColor.ANSI_GREEN + "INFO Common (jaccard) nchargrams similarity applied" + ConsoleColor.ANSI_RESET;
                    if(verbose) {
                        System.out.printf("2_chargrams:    %11fd %20s\n", Ngram2, " importance factor 20%");
                        System.out.printf("3_chargrams:    %11fd %20s\n", Ngram3, " importance factor 35%");
                        System.out.printf("4_chargrams:    %11fd %20s\n", Ngram4, " importance factor 45%");
                    }
                    result = Ngram2 * ((double) 20 / 100) + Ngram3 * ((double) 35 / 100) + Ngram4 * ((double) 45 / 100);
                    if (debug) System.out.printf("[ClaimLinker] %-100s [%20s]\n", out, result);

                }
                return result;
            }

            ArrayList<String> getNchargrams(int n, CoreDocument a) {
                ArrayList<String> list = (ArrayList<String>) a.tokens().stream()
                        .map(CoreLabel::originalText)
                        .collect(Collectors.toList());
                ArrayList<String> ngrams = new ArrayList<>();
                for (int i = 0; i < a.text().length(); i ++) {
                    StringBuilder entry = new StringBuilder();
                    for (int j = i; j < i + n && i + n - 1 < a.text().length(); j++) {
                        entry.append(a.text().charAt(j));
                    }
                    ngrams.add(entry.toString());
//                    System.out.println(entry.toString());
                }
                return ngrams;
            }
        },
        vec_cosine_sim {
            @Override
            double analyze(CoreDocument claim, CoreDocument text) {
                CosineSimilarity sim = new CosineSimilarity();
                HashMap<CharSequence, Integer> hash = new HashMap<>();
                HashMap<CharSequence, Integer> hash2 = new HashMap<>();

                claim.tokens().forEach(elem -> {
                    hash.put(elem.originalText(), 1);
                });
                text.tokens().forEach(elem -> {
                    hash2.put(elem.originalText(), 1);
                });
                double result = sim.cosineSimilarity(hash, hash2);
                synchronized (this) {
                    String out = ConsoleColor.ANSI_GREEN + "INFO Cosine similarity applied" + ConsoleColor.ANSI_RESET;
                    if (debug) System.out.printf("[ClaimLinker] %-100s [%20s]\n", out, result);
                }
                return result;
            }
        };

        private final NLPlib nlp_instance;
        private final static boolean debug = true;
        private final static boolean verbose = false;

        SimilarityMeasure() {
            this.nlp_instance = AnalyzerDispatcher.nlp_instance;
        }

        abstract double analyze(CoreDocument claim, CoreDocument text);

        /**
         * @author kui.liu
         */
        public <T> Double similarity(final List<T> l1, final List<T> l2) {

            if (l1 == null || l2 == null) return Double.NaN;
            if (l1.isEmpty() || l2.isEmpty()) return -1d;
            if (l1.containsAll(l2) && l2.containsAll(l1)) return 1d;

            List<T> intersectionList = l1.stream().filter(l2::contains).distinct().collect(Collectors.toList());
            List<T> unionList = new ArrayList<T>();
            unionList.addAll(l1);
            unionList.addAll(l2);
            unionList = unionList.stream().distinct().collect(Collectors.toList());

            int intersectionNum = intersectionList.size();
            int unionNum = unionList.size();

            return (double) intersectionNum / unionNum;
        }

    }


}

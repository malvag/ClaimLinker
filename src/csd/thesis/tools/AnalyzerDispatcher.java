package csd.thesis.tools;

import csd.thesis.misc.ConsoleColor;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.util.Pair;
import it.uniroma1.lcl.jlt.util.Language;
import org.apache.commons.text.similarity.JaccardSimilarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyzerDispatcher {
    private static NLPlib nlp_instance;

    public AnalyzerDispatcher(NLPlib nlp_instance) {
        AnalyzerDispatcher.nlp_instance = nlp_instance;
        this.analyzers = new ArrayList<>();
    }

    public void addAnalyzer(Analyzer[] analyzer_array) {
        this.analyzers.addAll(Arrays.asList(analyzer_array));
    }

    public void analyze(CoreDocument a, CoreDocument b) {
        this.analyzers.forEach(elem -> {
            elem.analyze(a, b);
        });
    }

    ArrayList<Analyzer> analyzers;

    public enum Analyzer {
        jcrd_comm_words {
            //    Common (jaccard) words
            @Override
            double analyze(CoreDocument a, CoreDocument b) {
                System.out.println(ConsoleColor.ANSI_GREEN + "[ClaimLinker] INFO Common (jaccard) words similarity applied" + ConsoleColor.ANSI_RESET);
                JaccardSimilarity JS = new JaccardSimilarity();
                if (debug) {
                    System.out.println("- Document A:\"" + a.text() + "\"");
                    System.out.println("- Document B:\"" + b.text() + "\"");
                }
                System.out.print("- result: ");
                double result = JS.apply(a.text(), b.text());
                System.out.println(result);
                return result;
            }
        },
        jcrd_comm_lemm_words {
            //    Common (jaccard) lemmatized words
            @Override
            double analyze(CoreDocument a, CoreDocument b) {
                System.out.println(ConsoleColor.ANSI_GREEN + "[ClaimLinker] INFO Common (jaccard) lemmatized words similarity applied" + ConsoleColor.ANSI_RESET);
                String stra = super.nlp_instance.getLemmas_toString(a);
                String strb = super.nlp_instance.getLemmas_toString(b);

                JaccardSimilarity JS = new JaccardSimilarity();
                if (debug) {
                    System.out.println("- Document A:\"" + stra + "\"");
                    System.out.println("- Document B:\"" + strb + "\"");
                }
                System.out.print("- result: ");

                double result = JS.apply(stra, strb);
                System.out.println(result);
                return result;
            }
        },
        jcrd_comm_ne {
            //    Common (jaccard) named entities (result of StanfrodNLP)
            @Override
            double analyze(CoreDocument a, CoreDocument b) {
                System.out.println(ConsoleColor.ANSI_GREEN + "[ClaimLinker] INFO Common (jaccard) named entities SNLP similarity applied" + ConsoleColor.ANSI_RESET);
                ArrayList<String> listA = super.nlp_instance.getAnnotationSentences(a);
                ArrayList<String> listB = super.nlp_instance.getAnnotationSentences(b);
                JaccardSimilarity JS = new JaccardSimilarity();
                if (debug) {
                    System.out.println("- Document A:\"" + a.text() + "\"");
                    System.out.println("- Document B:\"" + b.text() + "\"");
                    listA.forEach(System.out::println);
                    listB.forEach(System.out::println);
                }
                System.out.print("- result: ");
                if (listA.size() == 0 || listB.size() == 0) {
                    System.out.println(-1);
                    return -1;
                }
                double result = JS.apply(String.join(" ", listA), String.join(" ", listB));
                System.out.println(result);
                return result;
            }
        },
        jcrd_comm_dissambig_ents {
            //    Common (jaccard) disambiguated entities (result of Babelfy)
            @Override
            double analyze(CoreDocument a, CoreDocument b) {
                System.out.println(ConsoleColor.ANSI_GREEN + "[ClaimLinker] INFO Common (jaccard) disambiguated entities BFY similarity applied" + ConsoleColor.ANSI_RESET);
                ArrayList<String> listA = super.nlp_instance.getAnnotationSentences(a);
                ArrayList<String> listB = super.nlp_instance.getAnnotationSentences(b);
                ArrayList<String> bfyA = new ArrayList<>();
                ArrayList<String> bfyB = new ArrayList<>();
                JaccardSimilarity JS = new JaccardSimilarity();
                if (debug) {
                    System.out.println("- Document A:\"" + a.text() + "\"");
                    System.out.println("- Document B:\"" + b.text() + "\"");
                }
                babelfy(listA, bfyA);
                babelfy(listB, bfyB);
                if (debug) {
                    bfyA.forEach(System.out::println);
                    bfyB.forEach(System.out::println);
                }
                bfyA.trimToSize();
                bfyB.trimToSize();
                System.out.print("- result: ");
                if (bfyA.size() == 0 || bfyB.size() == 0) {
                    System.out.println(-1);
                    return -1;
                }
                double result = JS.apply(String.join(" ", bfyA), String.join(" ", bfyB));
                System.out.println(result);
                return result;
            }

            private void babelfy(ArrayList<String> list, ArrayList<String> bfy) {
                super.nlp_instance.getBfy().babelfy(String.join(" ", list), Language.EN).forEach(elem -> {
                    String token = String.join(" ", list).substring(elem.getCharOffsetFragment().getStart(),
                            elem.getCharOffsetFragment().getEnd() + 1);
                    if (debug) {
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
            }
        }, jcrd_comm_pos_words {
            //Common (jaccard) words of specific POS
            @Override
            double analyze(CoreDocument a, CoreDocument b) {
                JaccardSimilarity JS = new JaccardSimilarity();
                ArrayList<Pair<String, String>> A_Nouns = new ArrayList<>();
                ArrayList<Pair<String, String>> A_Verbs = new ArrayList<>();
                ArrayList<Pair<String, String>> A_Adverbs = new ArrayList<>();
                ArrayList<Pair<String, String>> A_Wh_pronoun = new ArrayList<>();
                ArrayList<Pair<String, String>> B_Nouns = new ArrayList<>();
                ArrayList<Pair<String, String>> B_Verbs = new ArrayList<>();
                ArrayList<Pair<String, String>> B_Adverbs = new ArrayList<>();
                ArrayList<Pair<String, String>> B_Wh_pronoun = new ArrayList<>();

                System.out.println(ConsoleColor.ANSI_GREEN + "[ClaimLinker] INFO Common (jaccard) words of specific POS similarity applied" + ConsoleColor.ANSI_RESET);
                a.tokens().forEach(elem -> {
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
                System.out.println("===");
                b.tokens().forEach(elem -> {
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
//                System.out.print("- result: ");
                A_Nouns.forEach(System.out::println);
                System.out.println("-");
                B_Nouns.forEach(System.out::println);
                System.out.println("- - ");
                A_Verbs.forEach(System.out::println);
                System.out.println("-");
                B_Verbs.forEach(System.out::println) ;
                System.out.println("- - ");
                A_Adverbs.forEach(System.out::println);
                System.out.println("-");
                B_Adverbs.forEach(System.out::println);
                System.out.println("- - ");
                A_Wh_pronoun.forEach(System.out::println);
                System.out.println("-");
                B_Wh_pronoun.forEach(System.out::println);

                System.out.println(this.similarity(A_Verbs,B_Verbs));
                System.out.println(this.similarity(A_Nouns,B_Nouns));
                System.out.println(this.similarity(A_Adverbs,B_Adverbs));
                System.out.println(this.similarity(A_Wh_pronoun,B_Wh_pronoun));

                return 0;
            }
        };
        private NLPlib nlp_instance;
        private final static boolean debug = false;

        Analyzer() {
            this.nlp_instance = AnalyzerDispatcher.nlp_instance;
        }

        abstract double analyze(CoreDocument a, CoreDocument b);

        /**
         * @author kui.liu
         */
        public <T> Double similarity(final List<T> l1, final List<T> l2) {

            if (l1 == null || l2 == null) return Double.NaN;
            if (l1.containsAll(l2) && l2.containsAll(l1)) return 1d;
            /*
             *  FIXME: if there are several same objects in one list, what should we do?
             *  If so, it is preferred to use Kulczynski-2 algorithm.
             */
            List<T> intersectionList = l1.stream().filter(t -> l2.contains(t)).distinct().collect(Collectors.toList());
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

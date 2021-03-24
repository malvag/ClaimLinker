package csd.claimlinker;

import csd.claimlinker.model.Association_type;
import csd.claimlinker.model.CLAnnotation;
import csd.claimlinker.model.Claim;
import csd.claimlinker.nlp.AnalyzerDispatcher;
import java.io.BufferedReader;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClaimLinkerEval {

    private static HashMap<String, String> tweetID2text = new HashMap<>();
    private static HashMap<String, String> tweetID2claimID = new HashMap<>();
    private static HashMap<String, String> claimID2claimText = new HashMap<>();
    private static HashMap<String, String> claimID2claimTitle = new HashMap<>();

    public static void readGTdata() {
        String filepath = "data/tweets.queries.tsv";
        try (FileInputStream fis = new FileInputStream(filepath);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr)) {

            String str;
            int n = 0;
            while ((str = reader.readLine()) != null) {
                n++;
                if (n == 1) {
                    continue;
                }
                String data[] = str.split("\t");
                String tweetId = data[0];
                String tweetContent = data[1];
                tweetID2text.put(tweetId, tweetContent);

                System.out.println(tweetId);
                System.out.println(tweetContent);
                System.out.println("-------");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Tweets Size: " + tweetID2text.size());

        System.out.println("================");

        String filepath2 = "data/tweet-vclaim-pairs.qrels";
        try (FileInputStream fis = new FileInputStream(filepath2);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr)) {

            String str;
            while ((str = reader.readLine()) != null) {
                String data[] = str.split("\t");
                String tweetId = data[0];
                String claimId = data[2];

                if (tweetID2claimID.containsKey(tweetId)) {
                    System.out.println("SOS SOS " + tweetId);
                }
                tweetID2claimID.put(tweetId, claimId);

                System.out.println(tweetId);
                System.out.println(claimId);
                System.out.println("-------");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("GT size: " + tweetID2claimID.size());

        System.out.println("================");

        String filepath3 = "data/verified_claims.docs.tsv";
        try (FileInputStream fis = new FileInputStream(filepath3);
                InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(isr)) {

            String str;
            while ((str = reader.readLine()) != null) {
                String data[] = str.split("\t");
                String num = data[0];
                String claimText = data[1];
                String claimTitle = data[2];

                claimID2claimText.put(num, claimText);
                claimID2claimTitle.put(num, claimTitle);

                System.out.println(num);
                System.out.println(claimText);
                System.out.println(claimTitle);
                System.out.println("-------");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Claim num 2 text size: " + claimID2claimText.size());
        System.out.println("Claim num 2 title size: " + claimID2claimTitle.size());

    }

    public static void runExperiments() throws IOException, ClassNotFoundException {
        readGTdata();

        int tweetNumber = 0;
        int annotTotalNumber = 0;
        int correctTop1 = 0;
        int correctTop2 = 0;
        int correctTop3 = 0;
        int correctTop4 = 0;
        int correctTop5 = 0;
        double pAt3 = 0;
        double pAt5 = 0;
        for (String tweetID : tweetID2text.keySet()) {
            tweetNumber++;
            String tweetText = tweetID2text.get(tweetID);
            int pos1 = tweetText.toLowerCase().indexOf("http");
            if (pos1 != -1) {
                int pos2 = tweetText.indexOf(" ", pos1 + 1);
                String substr = tweetText.substring(pos1, pos2);
                tweetText = tweetText.replace(substr, " ");
            }

            int pos3 = tweetText.toLowerCase().indexOf("pic.twitter");
            if (pos3 != -1) {
                int pos4 = tweetText.indexOf(" ", pos3 + 1);
                String substr = tweetText.substring(pos3, pos4);
                tweetText = tweetText.replace(substr, " ");
            }

            int pos5 = tweetText.lastIndexOf("— ");
            if (pos5 != -1) {
                tweetText = tweetText.substring(0, pos5);
            }

            tweetText = tweetText.replace(".", " ").replace("!", " ").replace(";", " ").replace("?", " ");
            while (tweetText.contains("  ")) {
                tweetText = tweetText.replace("  ", " ");
            }
            try {
                Set<CLAnnotation> annots = demo_pipeline(tweetText);
                int annotNumber = 1;
//                if (tweetNumber == 5) {
//                    break;
//                }
                System.out.println("Tweet_" + tweetNumber + " = " + tweetID + " (" + tweetText + ")");
                if (!annots.isEmpty()) {
                    annotTotalNumber++;
                }
             
                int correctIn3 = 0;
                int correctIn5 = 0;
                for (CLAnnotation annot : annots) {

                    System.out.println("  Annot_" + (annotNumber++) + " (" + annot.getText() + ")");
                    List<Claim> linkedClaims = annot.getLinkedClaims();
                    int linkedClaimsNumber = 0;
                    Collections.sort(linkedClaims, Collections.reverseOrder());
                    
                    for (Claim linkedClaim : linkedClaims) {
                        linkedClaimsNumber++;

                        String claimID = linkedClaim.getclaimReviewedURL();
                        boolean rel = false;
                        String correctClaimID = tweetID2claimID.get(tweetID);
                        if (tweetID2claimID.get(tweetID).equals(claimID)) {
                            rel = true;
                        }

                        double nlpScore = (linkedClaim.getNLPScore() * 100.0);
                        double ESScore = linkedClaim.getElasticScore();
                        double sum = nlpScore + ESScore;
                        System.out.println("    LinkedClaim_" + linkedClaimsNumber + ": " + linkedClaim.getReviewedBody() + "[" + claimID + "] [" + sum + "] [" + rel + "]");

                        if (linkedClaimsNumber <=3 && rel) {
                            correctIn3++;
                        }
                        if (linkedClaimsNumber <=5 && rel) {
                            correctIn5++;
                        }
                        
                        if (linkedClaimsNumber == 1 && rel) {
                            correctTop1++;
                            correctTop2++;
                            correctTop3++;
                            correctTop4++;
                            correctTop5++;
                            break;
                        }
                        if (linkedClaimsNumber == 2 && rel) {
                            correctTop2++;
                            correctTop3++;
                            correctTop4++;
                            correctTop5++;
                            break;
                        }
                        if (linkedClaimsNumber == 3 && rel) {
                            correctTop3++;
                            correctTop4++;
                            correctTop5++;
                            break;
                        }
                        if (linkedClaimsNumber == 4 && rel) {
                            correctTop4++;
                            correctTop5++;
                            break;
                        }
                        if (linkedClaimsNumber == 5 && rel) {
                            correctTop5++;
                            break;
                        }

                    }
                }
                
                double corIn3 = (double)correctIn3 / 3.0;
                pAt3 += corIn3;
                double corIn5 = (double)correctIn5 / 5.0;
                pAt5 += corIn5;
                
                
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ClaimLinkerEval.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        double avgPat3 = pAt3 / (double) tweetNumber;
        double avgPat5 = pAt5 / (double) tweetNumber;
        System.out.println("TOTAL NUMBER OF TWEETS (TEST CASES) = " + tweetNumber);
        System.out.println("TOTAL NUMBER OF TWEET ANNOTATIONS = " + annotTotalNumber);
        System.out.println("TOTAL NUMBER OF CORRECT TOP-1 ANNOTATIONS = " + correctTop1);
        System.out.println("TOTAL NUMBER OF CORRECT TOP-2 ANNOTATIONS = " + correctTop2);
        System.out.println("TOTAL NUMBER OF CORRECT TOP-3 ANNOTATIONS = " + correctTop3);
        System.out.println("TOTAL NUMBER OF CORRECT TOP-4 ANNOTATIONS = " + correctTop4);
        System.out.println("TOTAL NUMBER OF CORRECT TOP-5 ANNOTATIONS = " + correctTop5);
        System.out.println("P@1 = " + (double) correctTop1 / (double) tweetNumber );
        System.out.println("P@3 = " + avgPat3);
        System.out.println("P@5 = " + avgPat5);
    }

    public static void main(String[] args) throws Exception {
        //testClaimLink();
        runExperiments();
    }

    static void testClaimLink() throws Exception {
        System.out.println("[Demo]Initiating ...");
        System.out.println("_______________________________________________");
        //demo_pipeline("Of course, we are 5 percent of the world's population; we have to trade with the other 95 percent.\n");
        demo_pipeline("Mitch Daniels says interest on debt will soon exceed security spending.\n");
        System.out.println("_______________________________________________");
        System.out.println("[Demo]Exited without errors ...");

    }

    public static Set<CLAnnotation> demo_pipeline(String text) throws IOException, ClassNotFoundException {
        AnalyzerDispatcher.SimilarityMeasure[] similarityMeasures = new AnalyzerDispatcher.SimilarityMeasure[]{
            AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_words, //Common (jaccard) words
            AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_lemm_words, //Common (jaccard) lemmatized words
            AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ne, //Common (jaccard) named entities
            AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_dissambig_ents, //Common (jaccard) disambiguated entities BFY
            AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_pos_words, //Common (jaccard) words of specific POS
            AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ngram, //Common (jaccard) ngrams
            AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_nchargram, //Common (jaccard) nchargrams
            AnalyzerDispatcher.SimilarityMeasure.vec_cosine_sim //Cosine similarity
        };
        ClaimLinker CLInstance = new ClaimLinker(5, similarityMeasures, "data/stopwords.txt", "data/puncs.txt", "data/english-20200420.hash", "localhost");
        System.out.println("Demo pipeline started!");
        Set<CLAnnotation> results = CLInstance.claimLink(text, 5, Association_type.same_as);
        return results;
    }

}

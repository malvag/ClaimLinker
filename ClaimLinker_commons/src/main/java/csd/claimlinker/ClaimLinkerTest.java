package csd.claimlinker;

import csd.claimlinker.model.Association_type;
import csd.claimlinker.model.CLAnnotation;
import csd.claimlinker.nlp.AnalyzerDispatcher;

import java.io.IOException;
import java.util.Set;

public class ClaimLinkerTest {
	public static void main(String[] args) throws Exception {
		testClaimLink();
	}

	static void testClaimLink() throws Exception {
		System.out.println("[Demo]Initiating ...");
		System.out.println("_______________________________________________");
        demo_pipeline("Of course, we are 5 percent of the world's population; we have to trade with the other 95 percent.\n");
		System.out.println("_______________________________________________");
		System.out.println("[Demo]Exited without errors ...");

	}

	static Set<CLAnnotation> demo_pipeline(String text) throws IOException, ClassNotFoundException {
		AnalyzerDispatcher.SimilarityMeasure[] similarityMeasures = new AnalyzerDispatcher.SimilarityMeasure[]{
				AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_words,           //Common (jaccard) words
				AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_lemm_words,      //Common (jaccard) lemmatized words
				AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ne,              //Common (jaccard) named entities
				AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_dissambig_ents,  //Common (jaccard) disambiguated entities BFY
				AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_pos_words,       //Common (jaccard) words of specific POS
				AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_ngram,           //Common (jaccard) ngrams
				AnalyzerDispatcher.SimilarityMeasure.jcrd_comm_nchargram,       //Common (jaccard) nchargrams
				AnalyzerDispatcher.SimilarityMeasure.vec_cosine_sim             //Cosine similarity
		};
		ClaimLinker CLInstance = new ClaimLinker(20, similarityMeasures, "data/stopwords.txt","data/puncs.txt", "data/english-20200420.hash", "localhost");
		System.out.println("Demo pipeline started!");
		Set<CLAnnotation> results = CLInstance.claimLink(text, 5, Association_type.all);
		//Set<CLAnnotation> results2 = CLInstance.claimLink("Trump was gifted a Mac Pro from Tim Cook", 5, Association_type.all, true);

		return null;
	}

}

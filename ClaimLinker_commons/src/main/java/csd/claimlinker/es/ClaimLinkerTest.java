package csd.claimlinker.es;

import csd.claimlinker.es.model.Association_type;
import csd.claimlinker.es.model.CLAnnotation;
import csd.claimlinker.es.nlp.AnalyzerDispatcher;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class ClaimLinkerTest {
	public static void main(String[] args) throws Exception {
		testClaimLink();
	}

	static void testClaimLink() throws Exception {
		System.out.println("[Demo]Initiating ...");
		System.out.println("_______________________________________________");

//        Set<CLAnnotation> results_sample1 = demo_pipeline("Well, I think that trade is an important issue.    Of course, we are 5 percent of the world's population; we have to trade with the other 95 percent.    And we need to have smart, fair trade deals.    We also, though, need to have a tax system that rewards work and not just financial transactions.    And the kind of plan that Donald has put forth would be trickle-down economics all over again.    In fact, it would be the most extreme version, the biggest tax cuts for the top percent of the people in this country than we've ever had.    I call it trumped-up trickle-down, because that's exactly what it would be.    That is not how we grow the economy.    We just have a different view about what's best for growing the economy, how we make investments that will actually produce jobs and rising incomes.    I think we come at it from somewhat different perspectives.    I understand that.    You know, Donald was very fortunate in his life, and that's all to his benefit.    He started his business with $14 million, borrowed from his father, and he really believes that the more you help wealthy people, the better off we'll be and that everything will work out from there.    I don't buy that.    I have a different experience.    My father was a small-businessman.    He worked really hard.    He printed drapery fabrics on long tables, where he pulled out those fabrics and he went down with a silkscreen and dumped the paint in and took the squeegee and kept going.    And so what I believe is the more we can do for the middle class, the more we can invest in you, your education, your skills, your future, the better we will be off and the better we'll grow.    That's the kind of economy I want us to see again.");
//        FileWriter myWriter = new FileWriter("results_sample1.json");
//        myWriter.write(String.valueOf(results_sample1));
//        myWriter.close();
//        Set<CLAnnotation> results_sample2 = demo_pipeline("Nine million people -- nine million people lost their jobs.    Five million people lost their homes.    And $13 trillion in family wealth was wiped out.    Now, we have come back from that abyss.    And it has not been easy.    So we're now on the precipice of having a potentially much better economy, but the last thing we need to do is to go back to the policies that failed us in the first place.    Independent experts have looked at what I've proposed and looked at what Donald's proposed, and basically they've said this, that if his tax plan, which would blow up the debt by over $5 trillion and would in some instances disadvantage middle-class families compared to the wealthy, were to go into effect, we would lose 3.5 million jobs and maybe have another recession.    They've looked at my plans and they've said, OK, if we can do this, and I intend to get it done, we will have 10 million more new jobs, because we will be making investments where we can grow the economy.    Take clean energy.    Some country is going to be the clean- energy superpower of the 21st century.    Donald thinks that climate change is a hoax perpetrated by the Chinese.   I think it's real.    ");
//        FileWriter myWriter2 = new FileWriter("results_sample2.json");
//        myWriter2.write(String.valueOf(results_sample2));
//        myWriter2.close();
//        Set<CLAnnotation> results_sample3 = demo_pipeline("The fact is, he's going to advocate for the largest tax cuts we've ever seen, three times more than the tax cuts under the Bush administration.    I have said repeatedly throughout this campaign: I will not raise taxes on anyone making $250,000 or less.    I also will not add a penny to the debt.    I have costed out what I'm going to do.    He will, through his massive tax cuts, add 20 trillion to the debt.    Well, he mentioned the debt.    We know how to get control of the debt.    When my husband was president, we went from a $300 billion deficit to a $200 billion surplus and we were actually on the path to eliminating the national debt.    When President Obama came into office, he inherited the worst economic disaster since the Great Depression.    He has cut the deficit by two-thirds.    So, yes, one of the ways you go after the debt, one of the ways you create jobs is by investing in people.    So I do have investments, investments in new jobs, investments in education, skill training, and the opportunities for people to get ahead and stay ahead. That's the kind of approach that will work.");
//        FileWriter myWriter3 = new FileWriter("results_sample3.json");
//        myWriter3.write(String.valueOf(results_sample3));
//        myWriter3.close();
//		Set<CLAnnotation> results_sample5 = demo_pipeline("Well, I think that trade is an important issue. Of course, we are 5 percent of the world's population; we have to trade with the other 95 percent. You konw, interest on debt will soon exceed security spending. I think we come at it from somewhat different perspectives. I understand that. You know, Donald was very fortunate in his life, and that's all to his benefit. Obama's administration spent more on Cash for the Clunkers than on our space program. Ted used Nazi terminology, something like 'subhuman mongrel' for describing President Obama.  My father was a small-businessman, he worked really hard.  He printed drapery fabrics on long tables, where he pulled out those fabrics and he went down with a silkscreen and dumped the paint in and took the squeegee and kept going. \n");
        Set<CLAnnotation> results_sample5 = demo_pipeline("Of course, we are 5 percent of the world's population; we have to trade with the other 95 percent. Trump \n");
        FileWriter myWriter4 = new FileWriter("results_sample5.json");
//        myWriter4.write(String.valueOf(results_sample5));
//        myWriter4.close();
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
		ClaimLinker CLInstance = new ClaimLinker(20, similarityMeasures, "data/stopwords.txt", "data/english-20200420.hash", "192.168.2.112");
		System.out.println("Demo pipeline started!");
//        String t2 = "C++ designer Bjarne Stroustrup";
		Set<CLAnnotation> results = CLInstance.claimLink(text, "", 5, 0.4, Association_type.all);
		System.out.println("_______________________________________________");
		System.out.println("Demo pipeline shutting down ...");
		return results;

	}

}

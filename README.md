
# ClaimLinker  
ClaimLinker is a Web service and API that links arbitrary text to fact-checked claims, offering a novel kind of semantic annotation of unstructured content.
The system is based on a scalable, fully unsupervised and modular approach that does not require training or tuning and which can serve high quality results at real time.

More information is available at the following paper: 
```
Maliaroudakis E., Boland K., Dietze S., Todorov K., Tzitzikas Y. and Fafalios P., 
"ClaimLinker: Linking Text to a Knowledge Graph of Fact-checked Claims", 
In Companion Proceedings of the Web Conference, 2021 (to appear).
```
A sub-module system has been implemented, divided respectively by the following modules: 
#### Claimlinker_commons (main library for claim linking)
 - ClaimLinker
 - Core NLP API
 - Association Type
 - Similarity Measures
 - Test class
#### Claimlinker_web (web services)
 - Web Servlet that returns annotations in JSON 
 - Web Servlet for Bookmarklet that allows a user to select a piece of text in a web page and check if there are fact-checked claims linked to the selected text.
 - JSP offering a form where the user can give sometext and check if there are fact-checked claims linked to that text.
#### ElasticSearch_Tools (indexing of claims and search service provision)
 - Initializer for an ElasticSearch server
 - Wrapper API for an ElasticSearch server
 - OpenCSV wrapper class

## Getting Started 

### Installation

1. We need to install the FEL library to the ClaimLinker_commons pom.

```bash
cd ClaimLinker_commons
mvn install:install-file -Dfile=./lib/FEL-0.1.0-fat.jar -DgroupId=com.yahoo.semsearch -DartifactId=FEL -Dversion=0.1.0 -Dpackaging=jar -DgeneratePom=true
```

2. Then compile the whole project from the project's root directory:
```bash
cd ..
mvn compile package #to compile and package into jar the claimlinker
```
 
3. Get the claim data from a csv, the hash file for **FEL** library the stopwords file and the punctuations file:
i.e.
	 - data/ 
		 - **claim_extraction_18_10_2019_annotated.csv** (a dataset of fact-checked claims)
		 - **english-20200420.hash** (used by FEL)
		 -  **stopwords.txt**
		 -  **puncs.txt**

		 
#### ElasticSearch setup

You can check [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html) how you can set up a single-node elasticsearch using docker.

## Usage

Initializing ElasticSearch:
```bash
java ElasticInitializer -f "data.csv" -h "elasticsearch_host"
```

Running the ClaimlinkerTest class:
```bash
java -cp .:ClaimLinker_commons/target/ClaimLinker_commons-1.0-jar-with-dependencies.jar:ClaimLinker_web/target/ClaimLinker_web-1.0.jar:ElasticSearch_Tools/target/ElasticSearch_Tools-1.0.jar: csd.claimlinker.ClaimLinkerTest
```
Using it as a library:
```java
ClaimLinker CLInstance = new ClaimLinker(elastic_search_threashold, similarityMeasures, stopwords_file, punctuations_file english_hash_FEL, ElasticSearch_host);

CLInstance.claimLink(text, num_of_returned_claims, associationtype, cleanPrevAnnotations)
```
i.e. (csd.claimlinker.ClaimLinkerTest)
```java
public static void main(String[] args) throws Exception{
        demo_pipeline("Of course, we are 5 percent of the world's population;\n");
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
	ClaimLinker CLInstance = new ClaimLinker(20, similarityMeasures, "data/stopwords.txt", "data/puncs.txt", "data/english-20200420.hash", "192.168.2.112");
	System.out.println("Demo pipeline started!");
	Set<CLAnnotation> results = CLInstance.claimLink(text, 5, Association_type.all, true);
	return results;
}
```
Using it as a Web Service returning results in JSON:
```
Example request for text "You know, interest on debt will soon exceed security spending.":

http://<claimlinker-url>/claimlinker?app=service&text=You%20know,%20interest%20on%20debt%20will%20soon%20exceed%20security%20spending.

Example of results in JSON: 

{"_results":[{
   "text":"You know, interest on debt will soon exceed security spending.",
   "sentencePosition":0,
   "association_type":"same_as",
   "linkedClaims":[
       { "claimReview_claimReviewed":"'Within a few years, we will be spending more on interest payments than on national security.'",
	 "_score":39.261948,
	 "extra_title":"Mitch Daniels says interest on debt will soon exceed security spending",
	 "rating_alternateName":"false",
	 "creativeWork_author_name":"Mitch Daniels",
	 "claimReview_url":"http://www.politifact.com/truth-o-meter/statements/2011/feb/17/mitch-daniels/mitch-daniels-says-interest-debt-will-soon-exceed-/",
	 "claim_uri":"http://data.gesis.org/claimskg/creative_work/16076cfe-34c2-542b-9ffa-41a4c8677ced"},
       { "claimReview_claimReviewed":"'In just 17 years, spending for Social Security, federal health care and interest on the debt will exceed ALL tax revenue!'",
         "_score":28.52819,
	 "extra_title":"Brat says entitlement and debt payments will consume all taxes in 2032",
	 "rating_alternateName":"mostly true",
	 "creativeWork_author_name":"Dave Brat",
	 "claimReview_url":"http://www.politifact.com/virginia/statements/2015/jun/16/dave-brat/brat-says-entitlement-and-debt-payments-will-consu/",
	 "claim_uri":"http://data.gesis.org/claimskg/creative_work/928f16c7-1ae8-53bb-a6d2-8aa6a59dbd45"},
       { "claimReview_claimReviewed":"'By 2022, just the interest payment on our debt will be greater than the defense of our country.'",
         "_score":28.45221,
         "extra_title":"Will interest on the debt exceed defense spending by 2022?",
         "rating_alternateName":"mostly true",
         "creativeWork_author_name":"Joe Manchin",
         "claimReview_url":"http://www.politifact.com/truth-o-meter/statements/2018/may/10/joe-manchin/will-interest-debt-exceed-defense-spending-2022/",
         "claim_uri":"http://data.gesis.org/claimskg/creative_work/3579711f-d846-57ba-998a-6c0983c2c2cb"},
       { "claimReview_claimReviewed":"'The debt will soon eclipse our entire economy.'",
         "_score":18.63715,
	 "extra_title":"Paul Ryan, in State of the Union response, says U.S. debt will soon eclipse GDP",
	 "rating_alternateName":"true",
	 "creativeWork_author_name":"Paul Ryan",
	 "claimReview_url":"http://www.politifact.com/truth-o-meter/statements/2011/jan/26/paul-ryan/paul-ryan-state-union-response-says-us-debt-will-s/",
	 "claim_uri":"http://data.gesis.org/claimskg/creative_work/7951dee8-b793-512c-bd13-074f648a052a"},
       { "claimReview_claimReviewed":"'Our spending has caught up with us, and our debt soon will eclipse the entire size of our national economy.'",
         "_score":17.90907,
	 "extra_title":"House Speaker John Boehner has the right count on the magnitude of the federal debt",
	 "rating_alternateName":"true",
	 "creativeWork_author_name":"John Boehner",
	 "claimReview_url":"http://www.politifact.com/ohio/statements/2011/jan/10/john-boehner/house-speaker-john-boehner-has-right-count-magnitu/",
	 "claim_uri":"http://data.gesis.org/claimskg/creative_work/4c906dfc-c6af-5d38-aec4-0808edb2dcb9"}]}],"timeElapsed":1117}
```

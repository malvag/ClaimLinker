
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

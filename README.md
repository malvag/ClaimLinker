
# ClaimLinker  
ClaimLinker implements a tool-set used to generate candidate claims using [ElasticSearch](https://github.com/elastic/elasticsearch), compare them using [StanfordNLP](https://github.com/stanfordnlp/CoreNLP) and link them based on the results.

A sub-module system has been implemented, divided respectively by
#### Claimlinker_commons
 - Claimlinker
 - Core NLP API
 - Association Type
 - Similarity Measures
 - Test class
#### Claimlinker_web
 - Web Servlet that returns CLAnnotations in JSON 
 - Web Servlet for Bookmarklet that returns CLAnnotations in simple HTML 
 - JSP for demonstration purposes
#### ElasticSearch_Tools
 - Initializer for an ElasticSearch server
 - Wrapper API for an ElasticSearch server
 - OpenCSV wrapper class

## The idea
We study the problem of claim linking, i.e., linking a piece of text to claims in a reference knowledge base of fact-checked claims. We define the problem, categorize the different types of associations between a piece of text and a claim, and provide a set of baseline claim linking methods that make use of a large set of similarity features. Our focus is on unsupervised methods that do not require training data and thus can be applied in different contexts and types of claims, e.g., politics-related, hoaxes, etc  
 
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
 
3. Get the claim data from a csv, the hash file for **FEL** library and the stopwords file:
i.e.
	 - data/ 
		 - **claim_extraction_18_10_2019_annotated.csv**
		 - **english-20200420.hash**
		 -  **stopwords.txt**
		 
#### ElasticSearch setup

You can check [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html) how you can set up a single-node elasticsearch using docker.

## Usage

Initializing ElasticSearch:
```bash
java ElasticInitializer -f "data.csv" -h "elasticsearch_host"
```

Running the ClaimlinkerTest class:
```bash
java -cp .:ClaimLinker_commons/target/ClaimLinker_commons-1.0-jar-with-dependencies.jar:ClaimLinker_web/target/ClaimLinker_web-1.0.jar:ElasticSearch_Tools/target/ElasticSearch_Tools-1.0.jar: csd.thesis.ClaimLinkerTest
```
Using it as a library:
```java
ClaimLinker CLInstance = new ClaimLinker(elastic_search_threashold, similarityMeasures, stopwords_file, english_hash_FEL, ElasticSearch_host);

CLInstance.claimLink(text, num_of_returned_claims, associationtype)
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
	ClaimLinker CLInstance = new ClaimLinker(20, similarityMeasures, "data/stopwords.txt", "data/english-20200420.hash", "192.168.2.112");
	System.out.println("Demo pipeline started!");
	Set<CLAnnotation> results = CLInstance.claimLink(text, 5, Association_type.all);
	return results;
}
```

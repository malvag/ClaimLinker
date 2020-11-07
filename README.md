# ClaimLinker
We study the problem of claim linking, i.e., linking a piece of text to claims in a reference knowledge base of fact-checked claims. We define the problem, categorise the different types of associations between a piece of text and a claim, and provide a set of baseline claim linking methods that make use of a large set of similarity features. Our focus is on unsupervised methods that do not require training data and thus can be applied in different contexts and types of claims, e.g., politics-related, hoaxes, etc

## Installation

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

## Usage
Initializing ElasticSearch: [DRAFT] 
```bash
java csd.claimlinker.es.ElasticInitializer -f "data.csv" -h "elasticsearch_host"
```

Running the ClaimlinkerTest class:
```bash
java -cp .:ClaimLinker_commons/target/ClaimLinker_commons-1.0-jar-with-dependencies.jar:ClaimLinker_web/target/ClaimLinker_web-1.0.jar:ElasticSearch_Tools/target/ElasticSearch_Tools-1.0.jar: csd.thesis.ClaimLinkerTest
```
Using it as a library:
```java
ClaimLinker CLInstance = new ClaimLinker(
									elastic_search_threashold,
									similarityMeasures,
									stopwords_file,
									english_hash_FEL, ElasticSearch_host);  
```
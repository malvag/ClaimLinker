package csd.claimlinker.es;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;

public class ElasticInitializer {

    //The config parameters for the connection
    private final String HOST;
    private final int PORT_ONE;
    private final int PORT_TWO;
    public String path;
    private int counter;

    private RestHighLevelClient restHighLevelClient;
    private BulkRequest bulkRequest;

    public static ArrayList<Map<String, Object>> master_claim_record;

    public ElasticInitializer(String HOST, int PORT_O, int PORT_T) {
        this.HOST = HOST;
        this.PORT_ONE = PORT_O;
        this.PORT_TWO = PORT_T;
    }

    public ElasticInitializer(String path, String HOST, int PORT_O, int PORT_T) {
        this.path = path;
        this.HOST = HOST;
        this.PORT_ONE = PORT_O;
        this.PORT_TWO = PORT_T;
    }

    public synchronized boolean exists(String index) throws IOException {
        this.makeConnection();
        GetIndexRequest request = new GetIndexRequest(index);
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        this.closeConnection();
        return exists;
    }

    public synchronized void makeConnection() {
        System.out.println("Trying to establish connection with elastic search...");
        counter = 0;
        if (restHighLevelClient == null) {
            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(HOST, PORT_ONE, "http"),
                            new HttpHost(HOST, PORT_TWO, "http")));
            System.out.println("Connection established with elastic search.");
        } else {
            System.out.println("Already connected with elastic search...");
        }

    }

    public synchronized void closeConnection() {
        try {
            restHighLevelClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        restHighLevelClient = null;
    }

    public void index(XContentBuilder o, boolean bulk) throws IOException {
        IndexRequest indexRequest = new IndexRequest("claim")
                .id(String.valueOf(counter++)).source(o);
        IndexResponse indexResponse = null;
        if (!bulk) {
            indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } else {
            bulkRequest.add(indexRequest);
        }
    }

    public void bulkIndex() {
        boolean flag = false;
        IOException err = null;
        if (bulkRequest == null) {
            return;
        }
        BulkResponse bulkResponse;
        BulkItemResponse.Failure failure = null;
        try {
            bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                if (bulkItemResponse.isFailed()) {
                    failure = bulkItemResponse.getFailure();
                    flag = true;
                }
            }
        } catch (IOException e) {
            flag = true;
            err = e;
        }
        if (!flag) {
            System.out.println("Bulk index finished succesfully!");
        } else {
            assert failure != null;
            System.err.println("Bulk index finished with errors: ");

        }

    }

    public void openClaimsRecord() {
        ElasticInitializer.master_claim_record = new ArrayList<>();
        OpenCSVWrapper openCSVWrapper = new OpenCSVWrapper(this.path);
        try {
            ElasticInitializer.master_claim_record = openCSVWrapper.parse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertClaims(boolean bulk, String path) {
        this.path = path;
        this.insertClaims(bulk);
    }

    public void insertClaims(boolean bulk) {
        System.out.println("Trying to read from file: " + this.path);
        bulkRequest = new BulkRequest();
        try {
            if (ElasticInitializer.master_claim_record == null) {
                this.openClaimsRecord();
            }
            ElasticInitializer.master_claim_record.forEach(elem -> {
                XContentBuilder builder;
                try {
                    builder = XContentFactory.jsonBuilder();

                    builder.startObject();
                    {
                        builder.field("claimReview_claimReviewed", elem.get("claimReview_claimReviewed"));
                        builder.field("claimReview_url", elem.get("claimReview_url"));
                        builder.field("creativeWork_author_name", elem.get("creativeWork_author_name"));
                        builder.field("extra_title", elem.get("extra_title"));
                        builder.field("rating_alternateName", elem.get("rating_alternateName"));

                    }
                    builder.endObject();
                    this.index(builder, bulk);
                    System.out.printf("%.2f\r", (double) counter / 33886);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        bulkIndex();
    }

    public void insertClaimsRDF(boolean bulk) {

        bulkRequest = new BulkRequest();

        System.out.println("Reading RDF model...");
        Model model = ModelFactory.createDefaultModel();
        model.read("data/claimskg_18_10_2019.ttl");

        System.out.println("Running query...");
        String queryStr = "SELECT DISTINCT ?claim ?claimText ?authorName ?url (LCASE(?ratingN) AS ?ratingName) ?extraTitle WHERE { ?claim a <http://schema.org/CreativeWork> ; "
                + " <http://schema.org/text> ?claimText . OPTIONAL { ?claim <http://schema.org/author> ?author . ?author <http://schema.org/name> ?authorName } "
                + " ?claimReview <http://schema.org/itemReviewed> ?claim ; <http://schema.org/url> ?url ; <http://schema.org/reviewRating> ?reviewRating . ?reviewRating <http://schema.org/author> ?ratingAuthor FILTER (?ratingAuthor != <http://data.gesis.org/claimskg/organization/claimskg>) . ?reviewRating <http://schema.org/alternateName> ?ratingN "
                + " OPTIONAL { ?claimReview <http://schema.org/headline> ?extraTitle }  }";
        Query query = QueryFactory.create(queryStr);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet results = qe.execSelect();

        HashSet<String> claimUris = new HashSet<>();
        XContentBuilder builder;

        System.out.println("Reading query results...");
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            RDFNode claimNode = qs.get("claim");
            String claim = claimNode.toString().replace("@en", "");

            if (claimUris.contains(claim)) {
                continue;
            }
            claimUris.add(claim);

            RDFNode claimTextNode = qs.get("claimText");
            String claimText = claimTextNode.toString().replace("@en", "");;

            RDFNode ratingNameNode = qs.get("ratingName");
            String ratingName = ratingNameNode.toString().replace("@en", "");

            RDFNode authorNameNode = qs.get("authorName");
            String authorName = "";
            if (authorNameNode != null) {
                authorName = authorNameNode.toString().replace("@en", "");;
            }

            RDFNode extraTitleNode = qs.get("extraTitle");
            String extraTitle = "";
            if (extraTitleNode != null) {
                extraTitle = extraTitleNode.toString().replace("@en", "");;
            }

            RDFNode urlNode = qs.get("url");
            String url = urlNode.toString();

//            System.out.println("->" + claim);
//            System.out.println("->" + claimText);
//            System.out.println("->" + authorName);
//            System.out.println("->" + extraTitle);
//            System.out.println("->" + url);
//            System.out.println("->" + ratingName);
//            System.out.println("---------------");
            try {
                builder = XContentFactory.jsonBuilder();
                builder.startObject();
                {
                    builder.field("claim_uri", claim);
                    builder.field("claimReview_claimReviewed", claimText);
                    builder.field("claimReview_url", url);
                    builder.field("creativeWork_author_name", authorName);
                    builder.field("extra_title", extraTitle);
                    builder.field("rating_alternateName", ratingName);

                }
                builder.endObject();
                this.index(builder, bulk);
                if (counter % 1000 == 0) {
                    System.out.println(counter);
                }
                //System.out.printf("%.2f\r", (double) counter / 33886);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        bulkIndex();
        System.out.println("COUNTER = " + counter);
        System.out.println("CLAIM URIS = " + claimUris.size());
        qe.close();
    }

    public void deleteClaims() {
        try {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("claim");
            AcknowledgedResponse deleteIndexResponse = this.restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException | ElasticsearchStatusException e) {
            System.err.println("Claim index doesn't exist in ES, proceeding ...");
        }

    }

    public void insertClaimsFullFactTransform(boolean bulk) {

        System.out.println("Trying to read from file: " + this.path);
        bulkRequest = new BulkRequest();
        try {
            if (ElasticInitializer.master_claim_record == null) {
                this.openClaimsRecord();
            }
            ElasticInitializer.master_claim_record.forEach(elem -> {
                XContentBuilder builder;
                try {
                    builder = XContentFactory.jsonBuilder();

                    builder.startObject();
                    {

                        String claimExtraTitle = elem.get("extra_title").toString().replace("â€œ", "'").replace("â€�", "'").replace("â€™", "'").replace("\n", " ").trim();
                        if (claimExtraTitle.startsWith("\"")) {
                            claimExtraTitle = claimExtraTitle.substring(1);
                        }
                        if (claimExtraTitle.endsWith("\"")) {
                            claimExtraTitle = claimExtraTitle.substring(0, claimExtraTitle.length() - 1);
                        }
                        claimExtraTitle = claimExtraTitle.replace("\"", "'");

                        String claimText = elem.get("claimReview_claimReviewed").toString().replace("â€œ", "'").replace("â€�", "'").replace("â€™", "'").replace("\n", " ").trim();
                        if (claimText.startsWith("\"")) {
                            claimText = claimText.substring(1);
                        }
                        if (claimText.endsWith("\"")) {
                            claimText = claimText.substring(0, claimText.length() - 1);
                        }
                        claimText = claimText.replace("\"", "'");

                        builder.field("claimReview_claimReviewed", claimText);
                        builder.field("claimReview_url", elem.get("claimReview_url"));
                        builder.field("creativeWork_author_name", elem.get("creativeWork_author_name"));
                        builder.field("extra_title", claimExtraTitle);
                        builder.field("rating_alternateName", elem.get("rating_alternateName"));

                        MessageDigest md = MessageDigest.getInstance("MD5");
                        String tohash = elem.get("claimReview_claimReviewed").toString() + elem.get("claimReview_url").toString();
                        md.update(tohash.getBytes());
                        byte[] digest = md.digest();
                        String claimUri = "http://data.gesis.org/claimskg/creative_work/" + DatatypeConverter.printHexBinary(digest);
                        System.out.println("claim uri ==>" + claimUri);
                        builder.field("claim_uri", claimUri);

                        String tohash2 = elem.get("claimReview_url").toString();
                        md.update(tohash2.getBytes());
                        byte[] digest2 = md.digest();
                        String articleUri = "http://data.gesis.org/claimskg/claim_review/" + DatatypeConverter.printHexBinary(digest2);
                        System.out.println("claim review uri  ==>" + articleUri);

                    }
                    builder.endObject();
                    this.index(builder, bulk);
                    System.out.printf("%.2f\r", (double) counter / 33886);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(ElasticInitializer.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        bulkIndex();

    }


	public static void main(String[] args) {

		if(args.length>1 && args[0].equals("-f")){
			//"data/claim_extraction_18_10_2019_annotated.csv"
			String ip = "localhost";
			if(args[2].equals("-h"))
				ip = args[3];
			ElasticInitializer demo = new ElasticInitializer(args[1], ip, 9200, 9201);
			demo.makeConnection();

			demo.deleteClaims();
			demo.insertClaims(true);

			demo.closeConnection();
		}

	}

}

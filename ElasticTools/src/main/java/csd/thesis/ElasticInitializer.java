package csd.thesis;

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
import org.elasticsearch.common.xcontent.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class ElasticInitializer {

    //The config parameters for the connection
    private static final String HOST = "localhost";
    private static final int PORT_ONE = 9200;
    private static final int PORT_TWO = 9201;
    private static final String SCHEME = "http";
    public static String path;
    private static int counter;

    private static RestHighLevelClient restHighLevelClient;
    private static BulkRequest bulkRequest;

    public static ArrayList<Map<String, Object>> master_claim_record;

    public static synchronized void makeConnection(String path) {
        System.out.println("Trying to establish connection with elastic search...");
        counter = 0;
        if (restHighLevelClient == null) {
            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(HOST, PORT_ONE, SCHEME),
                            new HttpHost(HOST, PORT_TWO, SCHEME)));
        }
        ElasticInitializer.path = path;
        System.out.println("Connection established with elastic search.");

    }

    public static synchronized void closeConnection() {
        try {
            restHighLevelClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        restHighLevelClient = null;
    }

    public static void index(XContentBuilder o, boolean bulk) throws IOException {
        IndexRequest indexRequest = new IndexRequest("claim")
                .id(String.valueOf(counter++)).source(o);
        IndexResponse indexResponse = null;
        if (!bulk)
            indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        else
            bulkRequest.add(indexRequest);
    }

    public static void bulkIndex() {
        boolean flag = false;
        if (bulkRequest == null)
            return;
        BulkResponse bulkResponse = null;
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
        }
        if (!flag)
            System.out.println("Bulk index finished succesfully!");
        else {
            assert failure != null;
            System.err.println("Bulk index finished with errors: ");
        }

    }

    public static ArrayList<Map<String, Object>> openClaimsRecord() {
        ElasticInitializer.master_claim_record = new ArrayList<>();
        OpenCSVWrapper openCSVWrapper = new OpenCSVWrapper(ElasticInitializer.path);
        try {
            ElasticInitializer.master_claim_record = openCSVWrapper.parse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ElasticInitializer.master_claim_record;
    }

    public static void insertClaims(boolean bulk) {
        System.out.println("Trying to read from file: " + ElasticInitializer.path);
        bulkRequest = new BulkRequest();
        try {
            if (ElasticInitializer.master_claim_record == null)
                ElasticInitializer.openClaimsRecord();
            ElasticInitializer.master_claim_record.forEach(elem -> {
                XContentBuilder builder = null;
                try {
                    builder = XContentFactory.jsonBuilder();

                    builder.startObject();
                    {
                        builder.field("claimReview_author_name", elem.get("claimReview_author_name"));
                        builder.field("claimReview_claimReviewed", elem.get("claimReview_claimReviewed"));
                        builder.field("extra_title", elem.get("extra_title"));
                        builder.field("rating_alternateName", elem.get("rating_alternateName"));

                    }
                    builder.endObject();
                    ElasticInitializer.index(builder, bulk);
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

    public static void deleteClaims() {
        try {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("claim");
            AcknowledgedResponse deleteIndexResponse = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException | ElasticsearchStatusException e) {
//            e.printStackTrace();
            System.err.println("Claim index doesn't exist in ES, proceeding ...");
        }

    }


    public static void main(String[] args) {
        makeConnection("data/claim_extraction_18_10_2019_annotated.csv");
//
        deleteClaims();
        insertClaims(true);

        closeConnection();
    }

}

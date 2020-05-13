package csd.thesis.elastic_wrap;

import com.google.gson.*;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.SimpleQueryStringBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ElasticWrapper {

    //The config parameters for the connection
    private static final String HOST = "localhost";
    private static final int PORT_ONE = 9200;
    private static final int PORT_TWO = 9201;
    private static final String SCHEME = "http";
    private static String path;
    private static int counter;

    private static RestHighLevelClient restHighLevelClient;
    private static BulkRequest bulkRequest;


    public static synchronized RestHighLevelClient makeConnection(String path) {
        System.out.println("Trying to establish connection with elastic search...");
        counter = 0;
        if (restHighLevelClient == null) {
            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(HOST, PORT_ONE, SCHEME),
                            new HttpHost(HOST, PORT_TWO, SCHEME)));
        }
        ElasticWrapper.path = path;
        System.out.println("Connection established with elastic search.");

        return restHighLevelClient;
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
            System.err.println("Bulk index finished with errors: " + failure.getMessage());
        }

    }

    public static void insertClaims(boolean bulk) {
        System.out.println("Trying to read from file: " + ElasticWrapper.path);
        bulkRequest = new BulkRequest();
        OpenCSVWrapper openCSVWrapper = new OpenCSVWrapper(ElasticWrapper.path);
        try {
            openCSVWrapper.parse().forEach(elem -> {
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
                    ElasticWrapper.index(builder, bulk);
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

    public static ArrayList<String> findCatalogItemWithoutApi(String field, String value, int num_of_hits) {
        String url = "http://localhost:9200/_search?q=" + field + ":" + value+"&size="+num_of_hits;
        URL obj = null;
        try {
            obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            BufferedReader br = new BufferedReader(new InputStreamReader(obj.openStream()));
            String strTemp, jsonstr = "";
            while ((strTemp = br.readLine()) != null) {
                jsonstr = strTemp;
            }

            JsonElement je = new JsonParser().parse(jsonstr);
            JsonObject jo = je.getAsJsonObject();
            AtomicInteger counter = new AtomicInteger();
            jo.getAsJsonObject("hits").getAsJsonArray("hits").forEach(claim->{
                JsonObject claim_obj = claim.getAsJsonObject();
                System.out.println((counter.getAndIncrement()) + " " + claim_obj.get("_score") + " "+ claim_obj.getAsJsonArray("claimReview_claimReviewed"));

            });


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static ArrayList<String> findCatalogItemWith(String field, String value, int num_of_hits) {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            SearchRequest request = new SearchRequest("claim");
            SearchSourceBuilder scb = new SearchSourceBuilder();
            SimpleQueryStringBuilder query = QueryBuilders.simpleQueryStringQuery(value)
                    .field(field)
                    // Values are indexed differently. Avoid errors when executing an IP search against a text field, for example.
                    .lenient(true);
            scb.query(query);
            request.source(scb);

            scb.size(num_of_hits);
            SearchResponse response =
                    restHighLevelClient.search(request, RequestOptions.DEFAULT);
            SearchHits hits = response.getHits();
            System.out.printf("Found : %10s %10s %20s %s\n", "ID", "Score", "Relative Score", "Claim");

            hits.forEach(hit -> {
                System.out.printf("Found : %10s %10.2f %20.2f %s\n", hit.getId(), hit.getScore(), (double) (hit.getScore() / hits.getMaxScore()), hit.getSourceAsMap().get(field));
                arrayList.add((String) hit.getSourceAsMap().get(field));
            });
//            return catalogItems;
        } catch (IOException ex) {
//            LOG.warn("Could not post {} to ES", text, ex);
        }
        return arrayList;
//        return Collections.emptyList();
    }

    public static void main(String[] args) {
//        makeConnection("data/claim_extraction_18_10_2019_annotated.csv");
//
//        deleteClaims();
//        insertClaims(true);

        findCatalogItemWithoutApi("claimReview_claimReviewed", "President", 100);

//        closeConnection();
    }

    static Map<String, Object> createMapFromJsonObject(JsonObject jo) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            map.put(key, getValueFromJsonElement(value));
        }
        return map;
    }

    static Object getValueFromJsonElement(JsonElement je) {
        if (je.isJsonObject()) {
            return createMapFromJsonObject(je.getAsJsonObject());
        } else if (je.isJsonArray()) {
            JsonArray array = je.getAsJsonArray();
            List<Object> list = new ArrayList<Object>(array.size());
            for (JsonElement element : array) {
                list.add(getValueFromJsonElement(element));
            }
            return list;
        } else if (je.isJsonNull()) {
            return null;
        } else // must be primitive
        {
            JsonPrimitive p = je.getAsJsonPrimitive();
            if (p.isBoolean()) return p.getAsBoolean();
            if (p.isString()) return p.getAsString();
            // else p is number, but don't know what kind
            String s = p.getAsString();
            try {
                return new BigInteger(s);
            } catch (NumberFormatException e) {
                // must be a decimal
                return new BigDecimal(s);
            }
        }
    }
}

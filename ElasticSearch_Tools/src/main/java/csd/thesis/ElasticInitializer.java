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
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ElasticInitializer {

	//The config parameters for the connection

	private final String HOST;
	private final int PORT_ONE;
	private final int PORT_TWO;
	private final String SCHEME;
	public final String path;
	private int counter;

	private RestHighLevelClient restHighLevelClient;
	private BulkRequest bulkRequest;

	public static ArrayList<Map<String, Object>> master_claim_record;

	public ElasticInitializer(String path, String HOST, int PORT_O, int PORT_T, String SCHEME) {
		this.path = path;
		this.HOST = HOST;
		this.PORT_ONE = PORT_O;
		this.PORT_TWO = PORT_T;
		this.SCHEME = SCHEME;
	}

	public synchronized void makeConnection() {
		System.out.println("Trying to establish connection with elastic search...");
		counter = 0;
		if (restHighLevelClient == null) {
			restHighLevelClient = new RestHighLevelClient(
					RestClient.builder(
							new HttpHost(HOST, PORT_ONE, SCHEME),
							new HttpHost(HOST, PORT_TWO, SCHEME)));
		}
		System.out.println("Connection established with elastic search.");

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
		if (!bulk)
			indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
		else
			bulkRequest.add(indexRequest);
	}

	public void bulkIndex() {
		boolean flag = false;
		if (bulkRequest == null)
			return;
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
		}
		if (!flag)
			System.out.println("Bulk index finished succesfully!");
		else {
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

	public void insertClaims(boolean bulk) {
		System.out.println("Trying to read from file: " + this.path);
		bulkRequest = new BulkRequest();
		try {
			if (ElasticInitializer.master_claim_record == null)
				this.openClaimsRecord();
			ElasticInitializer.master_claim_record.forEach(elem -> {
				XContentBuilder builder;
				try {
					builder = XContentFactory.jsonBuilder();

					builder.startObject();
					{
						builder.field("claimReview_claimReviewed", elem.get("claimReview_claimReviewed"));
//                        builder.field("claimReview_datePublished", elem.get("claimReview_datePublished"));
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

	public void deleteClaims() {
		try {
			DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("claim");
			AcknowledgedResponse deleteIndexResponse = this.restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
		} catch (IOException | ElasticsearchStatusException e) {
//            e.printStackTrace();
			System.err.println("Claim index doesn't exist in ES, proceeding ...");
		}

	}


	public static void main(String[] args) {
		ElasticInitializer demo = new ElasticInitializer("data/claim_extraction_18_10_2019_annotated.csv", "192.168.2.112", 9200, 9201, "http");
		demo.makeConnection();

		demo.deleteClaims();
		demo.insertClaims(true);

		demo.closeConnection();
	}

}

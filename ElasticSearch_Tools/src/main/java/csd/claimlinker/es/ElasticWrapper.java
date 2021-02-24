package csd.claimlinker.es;

import com.google.gson.*;
import com.roxstudio.utils.CUrl;
import csd.claimlinker.es.misc.ConsoleColor;
import csd.claimlinker.model.Claim;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ElasticWrapper is a wrapper class for some functions in ElasticSearch,
 * providing a simple API for finding claims inside our elasticsearch.
 */
public class ElasticWrapper {

    // The config parameters for the connection
    private final String HOST;
    private final int PORT_ONE;
    private final String SCHEME;
    private final ElasticInitializer elasticInitializer;
    private double threshold;

    public ElasticWrapper(double threshold, String HOST, int PORT_O, int PORT_T) {
        this.HOST = HOST;
        this.PORT_ONE = PORT_O;
        this.SCHEME = "http";
        this.threshold = threshold;
        this.elasticInitializer = new ElasticInitializer(HOST, PORT_O, PORT_T);
    }

    public ArrayList<Claim> findCatalogItemWithoutApi(boolean use_SoftThreshold, String[] field, String value, int num_of_hits) {

        //BUG when used as artifact for tomcat
//		try {
//			if(!this.elasticInitializer.exists("claim")){
//				System.err.println("Index doesn't exist, exiting");
//				System.exit(-1);
//			}
//		} catch (IOException e) {
//			System.err.println("Error connecting to ElasticSearch");
//			System.exit(-1);
//		}
        String url = SCHEME + "://" + HOST + ":" + PORT_ONE + "/_search";
        String data = String.format("'{\"query\": { \"multi_match\" : {  \"query\":    \"%s\", \n      \"fields\": [",
                value);
        StringBuilder ss = new StringBuilder();
        for (String s : field) {
            ss.append("\"").append(s).append("\", ");
        }
        ss.replace(ss.length() - 2, ss.length(), "");
        ss.append("]}}}'");
        data += ss.toString();
        ArrayList<Claim> claimArrayList = new ArrayList<>();
        ArrayList<Claim> pillow_claimArrayList = new ArrayList<>();
        CUrl xcurl = new CUrl(url).header("Content-Type : application/json").data(data);

        xcurl.exec();

        String jsonstr = xcurl.getStdout(CUrl.UTF8, null);
        double secondary_threshold = this.threshold - 10 >= 0 ? this.threshold - 10 : 0;
        JsonElement je;
        try {
            je = JsonParser.parseString(jsonstr).getAsJsonObject();
        } catch (Exception e) {
            System.err.println("[Error] Elastic Search error on reading response or could not reach server!");
            return null;
        }
        JsonObject jo = je.getAsJsonObject();
        AtomicInteger counter = new AtomicInteger();
        jo.getAsJsonObject("hits").getAsJsonArray("hits").forEach(claim -> {
            JsonObject claim_obj = claim.getAsJsonObject();
            if (claim_obj.get("_score").getAsFloat() >= this.threshold && claimArrayList.size() < num_of_hits) {
                claimArrayList.add(new Claim(claim_obj));
            } else {
                pillow_claimArrayList.add(new Claim(claim_obj));
            }

        });

        if (use_SoftThreshold) {
            for (Claim claim : pillow_claimArrayList) {
                if (claim.getElasticScore() > secondary_threshold && claimArrayList.size() < 10) {
                    claimArrayList.add(claim);
                }

            }
        }
        for (Claim claim : claimArrayList) {
            System.out.println(ConsoleColor.ANSI_GREEN + "[ES_API] " + (counter.getAndIncrement()) + " "
                    + claim.getElasticScore() + " " + claim.getReviewedBody() + ConsoleColor.ANSI_RESET);
        }

        return claimArrayList;
    }

    public static void main(String[] args) {
        new ElasticWrapper(20, "192.168.2.112", 9200, 9201).findCatalogItemWithoutApi(false,
                new String[]{"claimReview_claimReviewed", "extra_title"},
                URLEncoder.encode("Trump", StandardCharsets.UTF_8), 10);

    }

    static Map<String, Object> createMapFromJsonObject(JsonObject jo) {
        Map<String, Object> map = new HashMap<>();
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
            List<Object> list = new ArrayList<>(array.size());
            for (JsonElement element : array) {
                list.add(getValueFromJsonElement(element));
            }
            return list;
        } else if (je.isJsonNull()) {
            return null;
        } else // must be primitive
        {
            JsonPrimitive p = je.getAsJsonPrimitive();
            if (p.isBoolean()) {
                return p.getAsBoolean();
            }
            if (p.isString()) {
                return p.getAsString();
            }
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

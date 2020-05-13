package csd.thesis.elastic_wrapper;

import com.google.gson.*;
import csd.thesis.model.Claim;

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



    public static ArrayList<Claim> findCatalogItemWithoutApi(String field, String value, int num_of_hits) {
        String url = "http://localhost:9200/_search?q=" + field + ":" + value+"&size="+num_of_hits;
        URL obj = null;
        ArrayList<Claim> claimArrayList = new ArrayList<>();
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
                System.out.println((counter.getAndIncrement()) + " " + claim_obj.get("_score") + " "+ claim_obj.getAsJsonObject("_source").get("claimReview_claimReviewed"));
                claimArrayList.add(new Claim(claim_obj));
            });


        } catch (IOException e) {
            e.printStackTrace();
        }
        return claimArrayList;
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

package csd.thesis;

import com.google.gson.*;
import csd.thesis.model.Claim;
import io.github.cdimascio.dotenv.Dotenv;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ElasticWrapper {

    //The config parameters for the connection\
    static Dotenv dotenv = Dotenv.configure().directory("ElasticSearch_Tools").ignoreIfMalformed()
            .ignoreIfMissing().load();
    private static final String HOST = Objects.requireNonNull(Objects.requireNonNull(dotenv.get("HOST")).replaceAll("\"",""));
    private static final String PORT_ONE = Objects.requireNonNull(dotenv.get("PORT_ONE")).replaceAll("\"","");
    private static final String PORT_TWO = Objects.requireNonNull(dotenv.get("PORT_TWO")).replaceAll("\"","");
    private static final String SCHEME =  Objects.requireNonNull(dotenv.get("SCHEME")).replaceAll("\"","");


    public static ArrayList<Claim> findCatalogItemWithoutApi(String field, String value, int num_of_hits) {
        String url = SCHEME + "://" + HOST + ":" + PORT_ONE + "/_search?q=" + field + ":" + value + "&size=" + num_of_hits;
        URL obj = null;

//        System.out.println(url);
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

            JsonElement je = JsonParser.parseString(jsonstr).getAsJsonObject();
            JsonObject jo = je.getAsJsonObject();
            AtomicInteger counter = new AtomicInteger();
            jo.getAsJsonObject("hits").getAsJsonArray("hits").forEach(claim -> {
                JsonObject claim_obj = claim.getAsJsonObject();
                System.out.println((counter.getAndIncrement()) + " " + claim_obj.get("_score") + " " + claim_obj.getAsJsonObject("_source").get("claimReview_claimReviewed"));
                claimArrayList.add(new Claim(claim_obj));
            });


        } catch (IOException e) {
            System.err.println("ElasticSearch error");
//            e.printStackTrace();
        }
        return claimArrayList;
    }

    public static void main(String[] args) {
        JsonObjectBuilder factory = Json.createObjectBuilder();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        ElasticWrapper.findCatalogItemWithoutApi("claimReview_claimReviewed", "President", 10).forEach(claim -> {
            arrayBuilder.add(Json.createObjectBuilder().add("claimReview_claimReviewed", claim.getReviewedBody()));
        });
        factory.add("results",arrayBuilder);
        factory.add("search", Json.createArrayBuilder());
        System.out.println(factory.build());
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

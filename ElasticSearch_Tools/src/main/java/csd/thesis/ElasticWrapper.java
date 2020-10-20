package csd.thesis;

import com.google.gson.*;
import csd.thesis.misc.ConsoleColor;
import csd.thesis.model.Claim;


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

	//The config parameters for the connection
	private final String HOST;
	private final int PORT_ONE;
	private final String SCHEME;
	private final boolean use_SoftThreshold;

	public ElasticWrapper(double threshold, String HOST, int PORT_O, int PORT_T) {
		this.HOST = HOST;
		this.PORT_ONE = PORT_O;
		this.SCHEME = "http";
		this.use_SoftThreshold = false;

	}


	public ArrayList<Claim> findCatalogItemWithoutApi(boolean use_SoftThreshold, double threshold, String field, String value, int num_of_hits) {
		String url = SCHEME + "://" + HOST + ":" + PORT_ONE + "/_search?q=" + field + ":" + value + "&size=" + num_of_hits;
		URL obj;
		double secondary_threshold = threshold - 10 >= 0 ? threshold - 10 : 0;
		System.out.println(url);
		ArrayList<Claim> claimArrayList = new ArrayList<>();
		ArrayList<Claim> pillow_claimArrayList = new ArrayList<>();
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
				if (claim_obj.get("_score").getAsFloat() >= threshold) {
					claimArrayList.add(new Claim(claim_obj));
				} else
					pillow_claimArrayList.add(new Claim(claim_obj));

			});

			if (use_SoftThreshold) {
				for (Claim claim : pillow_claimArrayList) {
					if (claim.getElasticScore() > secondary_threshold &&
							claimArrayList.size() < 10)
						claimArrayList.add(claim);

				}
			}
			for (Claim claim : claimArrayList) {
				System.out.println(ConsoleColor.ANSI_GREEN + "[ES_API] " + (counter.getAndIncrement()) + " " + claim.getElasticScore() + " " + claim.getReviewedBody() + ConsoleColor.ANSI_RESET);
			}


		} catch (IOException e) {
			System.err.println("ElasticSearch error");
//            e.printStackTrace();
		}
		return claimArrayList;
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

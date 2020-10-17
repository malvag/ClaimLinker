package csd.thesis.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.JsonObject;
import edu.stanford.nlp.pipeline.CoreDocument;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Claim implements Comparable<Claim> {
	Map<String, Object> objectMap;
	private CoreDocument doc;

	Claim() {
	}

	public Claim(JsonObject claim_obj) {
		objectMap = new HashMap<>();
		this.objectMap.put("claimReview_claimReviewed", claim_obj.getAsJsonObject("_source").get("claimReview_claimReviewed").getAsString());
		this.objectMap.put("_score", claim_obj.get("_score").getAsDouble());
		this.objectMap.put("extra_title", claim_obj.getAsJsonObject("_source").get("extra_title").getAsString());
		this.objectMap.put("rating_alternateName", claim_obj.getAsJsonObject("_source").get("rating_alternateName").getAsString());
		this.objectMap.put("creativeWork_author_name", claim_obj.getAsJsonObject("_source").get("creativeWork_author_name").getAsString());
	}

	public Claim(Map<String, Object> in) {
		this.objectMap = in;
	}

	public void setDoc(CoreDocument doc) {
		this.doc = doc;
	}

	public void setNLPScore(double nlpScore) {
		this.objectMap.put("nlp_score", nlpScore);
	}

	public double getElasticScore() {
		return (double) this.objectMap.get("_score");
	}

	public String getReviewedBody() {
		return (String) this.objectMap.get("claimReview_claimReviewed");
	}

	public String getRatingName() {
		return (String) this.objectMap.get("rating_alternateName");
	}

	public Double getNLPScore() {
		return (Double) this.objectMap.get("nlp_score");
	}


	public String getExtraTitle() {
		return (String) this.objectMap.get("extra_title");
	}

	public String getAuthorName() {
		String auth_name = (String) this.objectMap.get("creativeWork_author_name");
		return auth_name == null || auth_name.isEmpty() ? "" : auth_name;
	}

	public String toJson() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		return mapper.writeValueAsString(this.objectMap);
	}

	@Override
	public String toString() {
		try {
			return this.toJson();
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	@Override
	public int compareTo(@NotNull Claim o) {
		if (this.getNLPScore() == null || o.getNLPScore() == null)
			return Double.compare(this.getElasticScore(), o.getElasticScore());
		return Double.compare(this.getNLPScore() + this.getElasticScore(), o.getNLPScore() + o.getElasticScore());
	}
}

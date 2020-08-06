package csd.thesis.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.JsonObject;
import edu.stanford.nlp.pipeline.CoreDocument;

import java.util.HashMap;
import java.util.Map;

public class Claim {
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
    }

    public Claim(Map<String, Object> in) {
        this.objectMap = in;
    }

    public CoreDocument getDoc() {
        return doc;
    }

    public void setDoc(CoreDocument doc) {
        this.doc = doc;
    }

    public Map<String, Object> getObjectMap() {
        return objectMap;
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

    public String getExtraTitle() {
        return (String) this.objectMap.get("extra_title");
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
}

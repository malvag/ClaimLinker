package csd.thesis.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class Claim {
    double score;
    Map<String,Object> objectMap;
    Claim(){

    }
    public Claim(Map<String,Object> obj){
        objectMap = obj;
    }

    public Claim(JsonObject claim_obj) {
        objectMap = new HashMap<>();
        this.objectMap.put("claimReview_claimReviewed" ,claim_obj.getAsJsonObject("_source").get("claimReview_claimReviewed").getAsString());
        this.objectMap.put("_score" ,claim_obj.get("_score"));
        this.objectMap.put("extra_title" ,claim_obj.getAsJsonObject("_source").get("extra_title").getAsString());
        this.objectMap.put("rating_alternateName" ,claim_obj.getAsJsonObject("_source").get("rating_alternateName").getAsString());
    }

    public Map<String, Object> getObjectMap() {
        return objectMap;
    }

    public String getReviewedBody(){
        return (String) this.objectMap.get("claimReview_claimReviewed");
    }
    public String getRatingName(){
        return (String) this.objectMap.get("rating_alternateName");
    }
    public String getExtraTitle(){
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

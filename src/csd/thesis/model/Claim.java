package csd.thesis.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Map;

public class Claim {
    double score;
    Map<String,Object> objectMap;
    Claim(){

    }
    public Claim(Map<String,Object> obj){
        objectMap = obj;
    }

    public Map<String, Object> getObjectMap() {
        return objectMap;
    }

    public String getClaimReviewedBody(){
        return (String) this.objectMap.get("claimReview_claimReviewed");
    }
    public String getClaimRatingName(){
        return (String) this.objectMap.get("rating_alternateName");
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

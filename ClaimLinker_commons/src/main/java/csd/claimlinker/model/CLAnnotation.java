package csd.claimlinker.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

public class CLAnnotation {
	private String text;
	private int tokenBeginPosition;
	private int tokenEndPosition;
	private int sentencePosition;
	private Association_type associationtype;
	private List<Claim> linkedClaims;

	public CLAnnotation(String text, int position, int tokenEndPosition, int sentencePosition, Association_type associationtype) {
		this.text = text;
		this.tokenBeginPosition = position;
		this.tokenEndPosition = tokenEndPosition;
		this.sentencePosition = sentencePosition;
		this.associationtype = associationtype;
		this.linkedClaims = new ArrayList<>();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CLAnnotation that = (CLAnnotation) o;
		return text.equals(that.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text);
	}

	public Map<String, Object> toObject(){
		Map<String,Object> objectMap = new HashMap<>();
		objectMap.put("text", this.text);
		objectMap.put("tokenBeginPosition",this.tokenBeginPosition );
		objectMap.put("tokenEndPosition",this.tokenEndPosition );
		objectMap.put("sentencePosition",this.sentencePosition );
		objectMap.put("association_type",this.associationtype);
		objectMap.put("linkedClaims", this.linkedClaims); //Check again
		return objectMap;
	}
	public String toJson() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		return mapper.writeValueAsString(this.toObject());
	}

	@Override
	public String toString() {
		try {
			return this.toJson();
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	public Association_type getAssoc_t() {
		return associationtype;
	}
	public int getPosition() {
		return tokenBeginPosition;
	}

	public Association_type getAssociationtype() {
		return associationtype;
	}

	public int getTokenBeginPosition() {
		return tokenBeginPosition;
	}

	public List<Claim> getLinkedClaims() {
		return linkedClaims;
	}
	public String getText() {
		return text;
	}
	public int getSentencePosition() {
		return sentencePosition;
	}

	public void setAssoc_t(Association_type associationtype) {
		this.associationtype = associationtype;
	}
	public void setLinkedClaims(List<Claim> linkedClaims) {
		this.linkedClaims = linkedClaims;
	}
	public void addLinkedClaim(Claim claim){
		this.linkedClaims.add(claim);
	}
	public void setPosition(int position) {
		this.tokenBeginPosition = position;
	}
	public void setText(String text) {
		this.text = text;
	}
	public void setSentencePosition(int sentencePosition) {
		this.sentencePosition = sentencePosition;
	}

	public int getTokenEndPosition() {
		return tokenEndPosition;
	}

	public void setTokenEndPosition(int tokenEndPosition) {
		this.tokenEndPosition = tokenEndPosition;
	}
}

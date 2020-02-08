package csd.thesis.model;

import csd.thesis.tools.NLPlib;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;

import java.util.List;

public class WebArticle {
    private String rawText;
    private String cleaned;
    private List<CoreEntityMention> entities;
    private List<SemanticAnnotation> bfyAnnotations;
    private CoreDocument doc;
    private String url;

    public void setUrl(String url) {
        this.url = url;
    }
    public void setCleaned(String cleaned) {
        this.cleaned = cleaned;
    }
    public void setDoc(CoreDocument doc) {
        this.doc = doc;
    }
    public void setEntities(List<CoreEntityMention> entities) {
        this.entities = entities;
    }
    public void setRawText(String rawText) {
        this.rawText = rawText;
    }
    public void setBfyAnnotations(List<SemanticAnnotation> bfyAnnotations) {
        this.bfyAnnotations = bfyAnnotations;
    }

    public String getUrl() {
        return url;
    }
    public CoreDocument getDoc() {
        return doc;
    }
    public List<SemanticAnnotation> getBfyAnnotations() {
        return bfyAnnotations;
    }
    public List<CoreEntityMention> getEntities() {
        return entities;
    }
    public String getCleaned() {
        return cleaned;
    }
    public String getRawText() {
        return rawText;
    }

    public WebArticle(String rawText,String url){
        this.rawText = rawText;
        this.url = url;
    }
    public WebArticle(String rawText){
        this.rawText = rawText;
    }

    public void annotate(NLPlib nlp_instance){
        nlp_instance.annotate(this.doc,false);
        this.doc = nlp_instance.getDoc();

    }

}

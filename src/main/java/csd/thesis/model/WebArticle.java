package csd.thesis.model;

import csd.thesis.tools.NLPlib;
import edu.stanford.nlp.ling.Document;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;

import java.util.List;

public class WebArticle {
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

    public WebArticle(String cleaned){
        this.cleaned = cleaned;
        this.doc = new CoreDocument(this.cleaned);
    }
    public WebArticle(String cleaned,String url){
        this(cleaned);
        this.url = url;
    }


    public void annotate(NLPlib nlp_instance){
        this.doc = new CoreDocument(this.cleaned);
        nlp_instance.NLPlib_annotate(this.doc,false,this.bfyAnnotations);
    }

}

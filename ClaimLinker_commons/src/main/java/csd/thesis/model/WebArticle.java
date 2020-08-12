package csd.thesis.model;

import csd.thesis.tools.URL_Parser;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;

import java.util.List;


public class WebArticle {
    private String cleaned;
    private String selection;
    private boolean hasSelection;
    private List<CoreEntityMention> entities;
    //    private List<SemanticAnnotation> bfyAnnotations;
    private CoreDocument doc;
    private String url;

    public enum WebArticleType {
        selection, url
    }

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
//    public void setBfyAnnotations(List<SemanticAnnotation> bfyAnnotations) {
//        this.bfyAnnotations = bfyAnnotations;
//    }

    public String getUrl() {
        return url;
    }

    public CoreDocument getDoc() {
        return doc;
    }

    //    public List<SemanticAnnotation> getBfyAnnotations() {
//        return bfyAnnotations;
//    }
    public List<CoreEntityMention> getEntities() {
        return entities;
    }


    public String getSelection() {
        return selection;
    }

    public String getCleaned() {
        return cleaned;
    }

    public WebArticle(String URL, String selection, WebArticleType type) {
        this.hasSelection = false;
        URL_Parser url_parser = new URL_Parser(URL);
        this.cleaned = url_parser.getCleaned();
        this.url = URL;
        this.doc = new CoreDocument(this.cleaned);
        if (type == WebArticleType.selection) {
            this.hasSelection = true;
            this.selection = selection;
        }
    }

//    public void annotate(NLPlib nlp_instance,WebArticleType type) {
//        if(type == WebArticleType.selection)
//            this.doc = new CoreDocument(this.selection);
//        this.doc = new CoreDocument(this.doc.text());
//        nlp_instance.NLPlib_annotate(this.doc);
//    }

    private void removeStopWords() {

    }

    private void stem() {

    }


}

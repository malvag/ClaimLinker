package csd.claimlinker.model;

import csd.claimlinker.tools.URL_Parser;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;

import java.util.List;


public class WebArticle {
    private String cleaned;
    private String selection;
    private boolean hasSelection;
    private List<CoreEntityMention> entities;
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

    public String getUrl() {
        return url;
    }

    public CoreDocument getDoc() {
        return doc;
    }

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
}

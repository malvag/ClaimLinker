package csd.thesis;

import csd.thesis.model.WebArticle;
import csd.thesis.tools.Parser;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.simple.Sentence;
import org.apache.logging.log4j.core.Core;

import java.util.ArrayList;
import java.util.List;

public class ClaimLinker {
    private WebArticle article;

    public ClaimLinker(String s_url) {
        Parser p = new Parser(s_url, null, true);
        this.article = new WebArticle(p.getClean());
    }

    private void pipeline() {



    }
}



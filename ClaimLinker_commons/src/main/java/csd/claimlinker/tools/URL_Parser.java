package csd.claimlinker.tools;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL_Parser
 *
 * @author Evangelos Maliaroudakis
 * @version 1.0
 */

public class URL_Parser {
    /**
     * URL_Parser's output
     */
    private String boilerpiped;

    final private static boolean debug = false;
    /**
     * Regex for {@code URL} format
     */
    final static private String URLregex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    /**
     * Default Constructor for initialization
     *
     * @param s_url {@code URL} given from the user in {@code String} form
     */
    public URL_Parser(String s_url) {

        //URL format check
        if (!this.isMatch(s_url)) {
            System.err.println("[URL_Parser] The URL doesn't have the right format!\nAborting...");
            boilerpiped = "empty";
        } else {

            //Call a method that uses Boilerpipe API URL
            try {
                getContent(s_url);
            } catch (BoilerpipeProcessingException | IOException e) {
                System.err.println("[URL_Parser] Unexpected Error !!\nExiting...");
                System.exit(1);
            }
        }
    }


    /**
     * Gets the output of the whole URL_Parser
     *
     * @return Returns the clean data from boilerpipe API
     */
    public String getCleaned() {
        return boilerpiped;
    }

    /**
     * Checks for  {@code URL} format
     *
     * @param input url given from the user in {@code String} form
     * @return Returns true if the URL has the right format and false otherwise
     */
    private boolean isMatch(String input) {
        try {
            Pattern patt = Pattern.compile(URLregex);
            Matcher matcher = patt.matcher(input);
            return matcher.matches();
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * Sends an API call to boilerpipe with our page's source(via {@code URL}) and then
     * the data we get as a response are the clean data(articles/text) we want.
     *
     * @param s_url url given from the user in String form
     * @throws IOException on wrong url
     */
    private void getContent(String s_url) throws IOException, BoilerpipeProcessingException {
        String API = "http://boilerpipe-web.appspot.com/extract?url=";

//        if (s_url.charAt(0) == '#') return;

        if (debug) System.out.println("[URL_Parser][API] Begin");
        String out = ArticleExtractor.INSTANCE.getText(new URL(s_url));
//        String out = Jsoup.connect(API + s_url)
//                .userAgent("Mozilla/5.0")
//                .referrer("http://www.google.com")
//                .get()
//                .select(".x-boilerpipe-mark1").html();

        if (debug) System.out.println("[URL_Parser][API] Done! ");
        this.boilerpiped = out;
    }
}

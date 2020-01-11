package csd.thesis.lib;

import org.jsoup.Jsoup;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TBD_Parser
 *
 * @author Evangelos Maliaroudakis
 * @version 1.0
 */

public class Parser {
    /**
     * Parser's output
     */
    private String boilerpiped;

    /**
     * Regex for {@code URL} format
     */
    final static private String URLregex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    /**
     * Default Constructor for initialization
     *
     * @param s_url    {@code URL} given from the user in {@code String} form
     * @param filename the name of the generated output file
     */
    public Parser(String s_url, String filename) {

        //URL format check
        if (!this.isMatch(s_url)) {
            System.err.println("[Parser] The URL doesn't have the right format!\nAborting...");
            boilerpiped = "empty";
        }else {

            //Call a method that uses Boilerpipe API URL
            try {
                getContent(s_url, filename);
            } catch (IOException e) {
                System.err.println("[Parser] Unexpected Error !!\nExiting...");
                System.exit(1);
            }
        }

    }

    /**
     * Gets the output of the whole Parser
     *
     * @return Returns the clean data from boilerpipe API
     */
    public String getClean() {
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
     * Gets a file and retrieves all articles
     * @param file_path String
     */
    public void getContentByComposition(String file_path){

    }


    /**
     * Sends an API call to boilerpipe with our page's source(via {@code URL}) and then
     * the data we get as a response are the clean data(articles/text) we want.
     *
     * @param s_url    url given from the user in String form
     * @param filename the name of the generated output file
     * @throws IOException on wrong url
     */
    private void getContent(String s_url, String filename) throws IOException {
        String API = "http://boilerpipe-web.appspot.com/extract?url=";

        System.out.println("[Parser] The API url call begins ...");
        String out = Jsoup.connect(API + s_url).get().select(".x-boilerpipe-mark1").html();

        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(out);
        writer.close();

        System.out.println("[Parser] The API url call was successful ! ");
        System.out.println("[Parser] A file named " + filename + " was created for output! ");
        boilerpiped = out;
    }
}

package csd.thesis.tools;

import csd.thesis.model.WebArticle;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
     * @param file
     */
    public Parser(String s_url, String filename, boolean file) {

        //URL format check
        if (!file) {
            if (!this.isMatch(s_url)) {
                System.err.println("[Parser] The URL doesn't have the right format!\nAborting...");
                boilerpiped = "empty";
            } else {

                //Call a method that uses Boilerpipe API URL
                try {
                    getContent(s_url, filename, true);
                } catch (IOException e) {
                    System.err.println("[Parser] Unexpected Error !!\nExiting...");
                    System.exit(1);
                }
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
     *
     * @param file_path String
     */
    public List getContentByComposition(String file_path) {
        List data = null;
        Scanner reader = null;
        data = new ArrayList<WebArticle>();
        File myObj = new File(file_path);
        try {
            reader = new Scanner(myObj);
        } catch (IOException e) {
            System.out.println("[Parser][Content] An error occurred opening file.");
            return null;
        }
        int counter = 0;
        while (reader.hasNextLine()) {
            String parsed_url = reader.nextLine();
            try {
                getContent(parsed_url, null, false);
            } catch (IOException e) {
                System.err.println("[Parser][Content] Failed to get boilerpiped article. \n");
                e.printStackTrace();
            }
            if (this.boilerpiped == null) {
                System.err.println("[Parser][Content] No input from bfy !");
                continue;
            };
            data.add(new WebArticle(this.boilerpiped, parsed_url));
            System.out.println("Article #" + counter++);
            boilerpiped = "";
        }
        reader.close();


        return data;
    }


    /**
     * Sends an API call to boilerpipe with our page's source(via {@code URL}) and then
     * the data we get as a response are the clean data(articles/text) we want.
     *
     * @param s_url    url given from the user in String form
     * @param filename the name of the generated output file
     * @throws IOException on wrong url
     */
    private void getContent(String s_url, String filename, boolean write_to_file) throws IOException {
        String API = "http://boilerpipe-web.appspot.com/extract?url=";

        if (s_url.charAt(0) == '#') return;

        System.out.println("[Parser][API] Begin");
        String out = Jsoup.connect(API + s_url).get().select(".x-boilerpipe-mark1").html();

        if (write_to_file) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(out);
            writer.close();
            System.out.println("[Parser] A file named " + filename + " was created for output! ");
        }
        System.out.println("[Parser][API] Done!");
        this.boilerpiped = out;
    }
}

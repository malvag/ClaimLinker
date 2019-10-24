package csd.uoc.gr.thesis.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private String boilerpiped;
    final static private String URLregex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    public Parser(String s_url) {
        // URL FORMAT CHECK
        if (!isMatch(s_url)) {
            System.exit(1);
        }
        // Call a method that uses Boilerpipe API URL
        getURLcontent(s_url);
    }

    public String getBoilerPipedString() {
        return boilerpiped;
    }

    private static boolean isMatch(String s) {
        try {
            Pattern patt = Pattern.compile(URLregex);
            Matcher matcher = patt.matcher(s);
            return matcher.matches();
        } catch (RuntimeException e) {
            System.err.println("The URL doesn't have the right format!\nExiting...");
            return false;
        }
    }

    private void getURLcontent(String s_url) {
        String urlString = s_url;
        String API = "http://boilerpipe-web.appspot.com/extract?url= ";
        String line;
        URL url = null;
        StringBuilder sb = new StringBuilder();

        try {
            url = new URL(API + urlString);
            System.out.println("API url call: \n" + url.toString());
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            // write the output to StringBuilder
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("API url call successful");
        boilerpiped = sb.toString();
    }
}

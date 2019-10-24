package csd.uoc.gr.thesis.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class Parser {
    private String boilerpiped;

    public Parser(String s_url){
        getURLcontent(s_url);
    }

    public String getBoilerPipedString(){
        return boilerpiped;
    }

    private void getURLcontent(String s_url) {
        String urlString = s_url;
        String API = "http://boilerpipe-web.appspot.com/extract?url= ";
        String line;
        URL url = null;
        StringBuilder sb = new StringBuilder();

        try {
            url = new URL(API + urlString);
            System.out.println("API url call: \n"+ url.toString());
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

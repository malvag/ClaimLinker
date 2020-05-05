package csd.thesis.servlet;

import csd.thesis.ClaimLinker;
import csd.thesis.model.WebArticle;
import csd.thesis.tools.URL_Parser;
import org.elasticsearch.common.xcontent.XContentFactory;

import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;

@WebServlet(name = "claimLinker_Servlet")
public class claimLinker_Servlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();

        try {
            ClaimLinker claimLinker_tool = new ClaimLinker(getServletContext().getResource("/WEB-INF/Properties.xml").getPath(),getServletContext().getResource("/WEB-INF/data/stopwords.txt").getPath());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        PrintWriter out = response.getWriter();
        String param_url = request.getParameter("url");


        JsonObjectBuilder factory = Json.createObjectBuilder();
        factory.add("message", "API ClaimLinker")
                .add("written by", "Evangelos Maliaroudakis");

        if(param_url!=null){
            WebArticle webArticle = new WebArticle(param_url);
            factory.add("url",param_url).add("cleaned_text_from_url",webArticle.getCleaned());
        }

        JsonObject respose_json = factory.build();
        response.setContentType("text/json");
        out.println(respose_json.toString());
        response.setStatus(200);
    }
}

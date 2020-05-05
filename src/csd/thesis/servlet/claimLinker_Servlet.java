package csd.thesis.servlet;

import csd.thesis.ClaimLinker;
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
        response.setContentType("text/json");
        JsonObjectBuilder factory = Json.createObjectBuilder();
        JsonObject respose_json = factory.add("message", "API ClaimLinker")
                .add("written by", "Evangelos Maliaroudakis").build();


        PrintWriter out = response.getWriter();
        out.println(respose_json.toString());
        response.setStatus(200);
    }
}

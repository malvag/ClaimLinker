package csd.thesis.servlet;

import csd.thesis.model.Association_type;

import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "BookmarkletHandler_Servlet")
public class BookmarkletHandler_Servlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println(getServletName() + " initialization finished! ");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getParameter("url") == null) {
            response.setStatus(400);
            response.flushBuffer();
        }
        JsonObject response_json = claimLinker_Servlet.ClaimLinkHandler(request, Association_type.topic_of);
//        StringBuilder ss = new StringBuilder();
//        URL url = new URL(request.getParameter("url"));
//        try (
//                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
//        ) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                ss.append(line).append("\n");
//            }
//            System.out.println("Page downloaded.");
//        }
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        assert response_json != null;
        String front = "<!doctype html>\n" +
                "<html lang=\"en\">\n" +
                "  <head>\n" +
                "    <!-- Required meta tags -->\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\n" +
                "\n" +
                "    <!-- Bootstrap CSS -->\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"resource/css/bootstrap.min.css\">\n" +
                "\n" +
                "    <title>Hello, world!</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "<h2> Selection  : " + response_json.getString("selection") + " </h2>\n" +
                "<h2> URL : " + response_json.getString("url") + " </h2>\n" +
                "<h2> Request timeElapsed: " + response_json.getJsonNumber("timeElapsed") + "ms </h2>\n" +
                "<table class=\"table\">\n" +
                "    <thead>\n" +
                "      <tr>\n" +
                "        <th scope=\"col\">#</th>\n" +
                "        <th scope=\"col\">Claim</th>\n" +
                "        <th scope=\"col\">NLP_score</th>\n" +
                "        <th scope=\"col\">Elastic_Score</th>\n" +
                "      </tr>\n" +
                "    </thead>\n" +
                "    <tbody>\n";

        String end = "    </tbody>\n" +
                "  </table>" + "\n    </div>\n" +
                "\n" +
                "    <!-- Optional JavaScript -->\n" +
                "    <!-- jQuery first, then Popper.js, then Bootstrap JS -->\n" +
                "    <script src=\"https://code.jquery.com/jquery-3.4.1.slim.min.js\" integrity=\"sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n\" crossorigin=\"anonymous\"></script>\n" +
                "    <script src=\"https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js\" integrity=\"sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo\" crossorigin=\"anonymous\"></script>\n" +
                "    <script src=\"resource/js/bootstrap.min.js\" ></script>\n" +
                "  </body>\n" +
                "</html>";
//        String results;
        StringBuilder results = new StringBuilder();
        for (int i = 0; i < response_json.getJsonArray("results").size(); i++) {
            JsonObject obj = response_json.getJsonArray("results").getJsonObject(i);
            results.append("      <tr>\n" + "        <th scope=\"row\">")
                    .append(i).append("</th>\n").append("        <td>")
                    .append(obj.getString("claimReview_claimReviewed"))
                    .append("</td>\n").append("        <td>")
                    .append(obj.getJsonNumber("NLP_score"))
                    .append("</td>\n").append("        <td>")
                    .append(obj.getJsonNumber("ElasticScore"))
                    .append("</td>\n")
                    .append("      </tr>\n");
        }
        out.println(front + results.toString() + end);
        response.setStatus(200);
    }
}

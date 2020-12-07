package csd.claimlinker.web.servlet;

import csd.claimlinker.model.Association_type;

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
        JsonObject response_json;
        if (request.getParameter("text") == null)
            response_json = claimLinker_Servlet.ClaimLinkfromURLHandler(request, Association_type.same_as);
        else
            response_json = claimLinker_Servlet.ClaimLinkHandler(request, Association_type.same_as);
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        assert response_json != null;
        String header = "<!doctype html>\n" +
                "<html lang=\"en\">\n" +
                "  <head>\n" +
                "    <!-- Required meta tags -->\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\n" +
                "\n" +
                "    <!-- Bootstrap CSS -->\n" +
                "   <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/css/bootstrap.min.css\" integrity=\"sha384-TX8t27EcRE3e/ihU7zmQxVncDAy5uIKz4rEkgIXeMed4M0jlfIDPvg6uqKI2xXr2\" crossorigin=\"anonymous\">" +
                "\n" +
                "    <title>Hello, world!</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "<h2> URL : " + request.getParameter("url") + " </h2>\n" +
                "<h2> Request timeElapsed: " + response_json.getJsonNumber("timeElapsed") + "ms </h2>\n" ;
        String front = "<div class=\"jumbotron jumbotron-fluid\">\n" +
                "  <div class=\"container\">\n" ;
        String table=
                "<table class=\"table\">\n" +
                "    <thead>\n" +
                "      <tr>\n" +
                "        <th scope=\"col\">#</th>\n" +
                "        <th scope=\"col\">Claim</th>\n" +
                "        <th scope=\"col\">NLP_score</th>\n" +
                "      </tr>\n" +
                "    </thead>\n" +
                "    <tbody>\n";

        String end = "    </tbody>\n" +
                "  </table>" + "\n    </div>\n" +
                "  </div>\n" +
                "</div>" +
                "\n";
        String footer = " <script src=\"https://code.jquery.com/jquery-3.5.1.slim.min.js\" integrity=\"sha384-DfXdz2htPH0lsSSs5nCTpuj/zy4C+OGpamoFVy38MVBnE+IbbVYUew+OrCXaRkfj\" crossorigin=\"anonymous\"></script>\n" +
                "<script src=\"https://cdn.jsdelivr.net/npm/bootstrap@4.5.3/dist/js/bootstrap.bundle.min.js\" integrity=\"sha384-ho+j7jyWK8fNQe+A12Hb8AhRq26LrZ/JpcUGGOn+Y7RsweNrtN/tE3MoK7ZeZDyx\" crossorigin=\"anonymous\"></script>" +

                "  </body>\n" +
                "</html>";

        StringBuilder results = new StringBuilder();
        for (int i = 0; i < response_json.getJsonArray("_results").size(); i++) {
//            JsonObject obj = response_json.getJsonArray("_results").getJsonObject(i).getJsonArray("linkedClaims").getJsonObject(0);
            JsonObject annotation = response_json.getJsonArray("_results").getJsonObject(i);
            results.append(front);
            results.append("<p class=\"lead\">").append(annotation.getString("text")).append("</p>").append(table);
            for (int k = 0; k < annotation.getJsonArray("linkedClaims").size(); k++) {
                JsonObject claim = annotation.getJsonArray("linkedClaims").getJsonObject(k);
                results.append("      <tr>\n" + "        <th scope=\"row\">")
                        .append(k).append("</th>\n").append("        <td>")
                        .append(claim.getString("claimReview_claimReviewed"))
                        .append("</td>\n").append("        <td>")
                        .append(claim.getJsonNumber("_score"))
                        .append("</td>\n")
                        .append("      </tr>\n");
            }
            results.append(end);

        }
        out.println(header + front + results.toString() + end + footer);
        response.setStatus(200);
    }
}

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "ConfirmServlet", urlPatterns = "/confirm")
public class ConfirmServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession cartSession = request.getSession();
        HttpSession id = request.getSession();
        HttpSession totalSession = request.getSession();

        ArrayList<String> previousItems = (ArrayList<String>) cartSession.getAttribute("previousItems");
        ArrayList<String> previousTitle = (ArrayList<String>) cartSession.getAttribute("previousTitle");
        ArrayList<Integer> previousPrice = (ArrayList<Integer>) cartSession.getAttribute("previousPrice");
        ArrayList<Integer> previousQuant = (ArrayList<Integer>) cartSession.getAttribute("previousQuant");
        String customerId = id.getAttribute("id").toString();
        int total = Integer.parseInt(totalSession.getAttribute("total").toString());
        PrintWriter out = response.getWriter();

        try {
            for (String previousItem : previousItems) {
                // Get a connection from dataSource
                Connection dbcon = dataSource.getConnection();

                // Construct a query with parameter represented by "?"
                String query = "INSERT INTO sales(customerId,movieId,saleDate) VALUES (?,?,CURDATE())";

                // Declare our statement
                PreparedStatement statement = dbcon.prepareStatement(query);

                // Set the parameter represented by "?" in the query to the id we get from url,
                // num 1 indicates the first "?" in the query
                statement.setString(1, customerId);
                statement.setString(2, previousItem);

                // Perform the query
                statement.executeUpdate();
            }
        }catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
        }

        out.println(String.format("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n" +
        "<html>\n" +
        "   <head>" +
        "<meta charset=\"utf-8\">" +
        "<meta name=\"viewport\"" +
        "          content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">" +
        "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\"\n" +
        "          integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">\n" +
        "<meta charset=\"UTF-8\">" +
        "<title>Confirmation</title>" +
        "       <h1>Confirmation</h1>" +
        "       <h3>Total = $" + total + "</h3>" +
        "<h3><a href='index.html'>Home</a></h3><br>"));
        out.println("<table id=cart_table class=\"table table-striped\">");
        out.println("<thead>");
        out.println("<tr>");
        out.println("<th>Title</th>");
        out.println("<th>Price</th>");
        out.println("<th>Quantity</th>");
        out.println("</tr>");
        out.println("</thead>");
        out.println("<tbody id=cart_table_body>");
        int i = 0;
        for (String previousItem : previousItems) {
            out.println("<tr>");
            out.println("<th>" + previousTitle.get(i) + "</th>");
            out.println("<th>" + previousPrice.get(i) + "</th>");
            out.println("<th>" + previousQuant.get(i) + "</th>");
            String decQuant = "cart?newItem=" + previousItem + "&newPrice=" + previousPrice.get(i) +
                    "&newQuant=" + String.valueOf(previousQuant.get(i) - 1);
            String incQuant = "cart?newItem=" + previousItem + "&newPrice=" + previousPrice.get(i) +
                    "&newQuant=" + String.valueOf(previousQuant.get(i) + 1);
            String remove = "cart?newItem=" + previousItem + "&newPrice=" + previousPrice.get(i) +
                    "&newQuant=-1";
            out.println("</tr>");
            i++;
        }
        out.println("</tbody>");
        out.println("</table>");
        out.println("</body></html>");
    }
}

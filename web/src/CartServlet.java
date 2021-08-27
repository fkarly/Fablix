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
@WebServlet(name = "CartServlet", urlPatterns = "/cart")
public class CartServlet extends HttpServlet {
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
        HttpSession session = request.getSession();
        String completeUrl = "";
        if(session.getAttribute("completeUrl") != null) {
            completeUrl = session.getAttribute("completeUrl").toString();
        }
        else{
            completeUrl = "index.html";
        }

        String newItem = request.getParameter("newItem"); // Get parameter that sent by GET request url
        String newPrice = request.getParameter("newPrice"); // Get parameter that sent by GET request url
        String newQuant = request.getParameter("newQuant"); // Get parameter that sent by GET request url

        PrintWriter out = response.getWriter();
        String title = "";

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Construct a query with parameter represented by "?"
            String query = "SELECT * from movies where id = ?";

            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, newItem);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                title = rs.getString("title");
            }

            rs.close();
            statement.close();
            dbcon.close();
        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
        }

        // Get a instance of current session on the request
        HttpSession cartSession = request.getSession();
        HttpSession totalSession = request.getSession();

        // Retrieve data named "previousItems" from session
        ArrayList<String> previousItems = (ArrayList<String>) cartSession.getAttribute("previousItems");
        ArrayList<String> previousTitle = (ArrayList<String>) cartSession.getAttribute("previousTitle");
        ArrayList<Integer> previousPrice = (ArrayList<Integer>) cartSession.getAttribute("previousPrice");
        ArrayList<Integer> previousQuant = (ArrayList<Integer>) cartSession.getAttribute("previousQuant");

        String stotal = String.valueOf(totalSession.getAttribute("total"));
        int total = 0;
        if(previousQuant != null && newQuant != null && Integer.parseInt(newQuant) > 0) {
            total = Integer.parseInt(stotal) + 20;
            totalSession.setAttribute("total", total);
        }
        else if(previousQuant != null && newQuant != null && Integer.parseInt(newQuant) == -1){
            total = Integer.parseInt(stotal) - 20;

            totalSession.setAttribute("total", total);
        }
        else if(previousQuant != null && newQuant != null && Integer.parseInt(newQuant) == -2) {
            int curQuant = previousQuant.get(previousItems.indexOf(newItem));
            total = Integer.parseInt(stotal) - curQuant * 20;

            totalSession.setAttribute("total", total);
        }
        else if(newQuant != null){
            total = 20;
            totalSession.setAttribute("total", total);
        }
        else if(previousQuant != null){
            total = Integer.parseInt(stotal);
        }
        System.out.println("total = " + total);

        // If "previousItems" is not found on session, means this is a new user, thus we createcreateStatement a new previousItems
        // ArrayList for the user
        if (previousItems == null) {
            // Add the newly created ArrayList to session, so that it could be retrieved next time
            previousItems = new ArrayList<>();
            previousTitle = new ArrayList<>();
            previousPrice = new ArrayList<>();
            previousQuant = new ArrayList<>();
            cartSession.setAttribute("previousItems", previousItems);
            cartSession.setAttribute("previousTitle", previousTitle);
            cartSession.setAttribute("previousPrice", previousPrice);
            cartSession.setAttribute("previousQuant", previousQuant);
        }

        response.setContentType("text/html");

        out.println(
                "<html>\n" +
                "   <head>" +
                "<meta charset=\"utf-8\">" +
                "<meta name=\"viewport\"" +
                        "          content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">" +
                "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\"\n" +
                        "          integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">\n" +
                "<meta charset=\"UTF-8\">" +
                "<title>Shopping Cart</title>" +
                "       <h1>Shopping Cart</h1>" +
                "<h3><a href=" + completeUrl + ">Back to search</a></h3><br>");

        // In order to prevent multiple clients, requests from altering previousItems ArrayList at the same time, we
        // lock the ArrayList while updating

        if(newItem == null && newPrice == null && newQuant == null && previousItems.isEmpty()) {
            out.println("<i>No items</i>");
        }
        else {
            synchronized (previousItems) {
                // Prevent from adding the same title
                if (newItem != null && previousItems.indexOf(newItem) == -1) {
                    previousItems.add(newItem); // Add the new item to the previousItems ArrayList
                    previousTitle.add(title);
                    previousPrice.add(Integer.parseInt(newPrice)); // Add the new item to the previousPrice ArrayList
                    previousQuant.add(Integer.parseInt(newQuant)); // Add the new item to the previousQuant ArrayList

                }
                else if (newQuant != null){
                    int index = previousItems.indexOf(newItem);
                    if(Integer.parseInt(newQuant) == 1){
                        int num = previousQuant.get(index) + 1;
                        previousQuant.set(index, num);
                    }
                    else {
                        previousQuant.set(index, previousQuant.get(index) - 1);
                    }
                }
                // Deletion
                if (newQuant != null && previousQuant.get(previousItems.indexOf(newItem)) < 0) {
                    int index = previousItems.indexOf(newItem);
                    previousItems.remove(index);
                    previousTitle.remove(index);
                    previousPrice.remove(index);
                    previousQuant.remove(index);
                }
                if(newQuant != null && Integer.parseInt(newQuant) == -2){
                    int index = previousItems.indexOf(newItem);
                    previousItems.remove(index);
                    previousTitle.remove(index);
                    previousPrice.remove(index);
                    previousQuant.remove(index);
                }

                // Display the current previousItems ArrayList
                if (previousItems.size() == 0) {
                    System.out.println("size = " + previousItems.size());
                    out.println("<i>No items</i>");
                } else {
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
                                "&newQuant=-1";
                        String incQuant = "cart?newItem=" + previousItem + "&newPrice=" + previousPrice.get(i) +
                                "&newQuant=1";
                        String remove = "cart?newItem=" + previousItem + "&newPrice=" + previousPrice.get(i) +
                                "&newQuant=-2";
                        out.println("<th>" + "<a href=\"" + decQuant + "\">Decrese Quantity" + "</a>");
                        out.println("<th>" + "<a href=\"" + incQuant + "\">Increase Quantity" + "</a></th>");
                        out.println("<th>" + "<a href=\"" + remove + "\">Remove Movie" + "</a></th>");
                        out.println("</tr>");
                        i++;
                    }
                    out.println("</tbody>");
                    out.println("</table>");
                    out.println("<label><b><a href='checkout.html'>Proceed to Checkout</a></b><label>");
                }
            }
        }
        out.println("</body></html>");
    }

}

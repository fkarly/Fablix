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

// Declaring a WebServlet called ItemServlet, which maps to url "/items"
@WebServlet(name = "CheckoutServlet", urlPatterns = "/api/checkout")

public class CheckoutServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
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

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Retrieve parameter username, password from url request.
        String fname = request.getParameter("fname");
        String lname = request.getParameter("lname");
        String ccid = request.getParameter("ccid");
        String expiration = request.getParameter("expiration");

        System.out.println("fname = " + fname);
        System.out.println("lname = " + lname);
        System.out.println("ccid = " + ccid);
        System.out.println("expiration = " + expiration);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();  // Output stream to STDOUT

        JsonObject responseJsonObject = new JsonObject();

        // Get a instance of current session on the request
        HttpSession totalSession = request.getSession();


        // Retrieve data named "previousItems" from session
        int total = Integer.parseInt(totalSession.getAttribute("total").toString());

        System.out.println("total = " + total);

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Construct a query with parameter represented by "?"
            String query = "SELECT * from creditcards where id = ? AND firstName = ? AND lastName = ? and expiration = ?";

            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(query);


            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, ccid);
            statement.setString(2, fname);
            statement.setString(3, lname);
            statement.setString(4, expiration);

            ResultSet rs = statement.executeQuery();

            System.out.println(statement);

            if (rs.next()) {
                // Login success
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
                responseJsonObject.addProperty("total", total);
            } else {
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Invalid Data");
                responseJsonObject.addProperty("total", total);
            }

            dbcon.close();
            rs.close();
            statement.close();
        } catch (Exception e) {
            // write error message JSON object to output
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Invalid Data");
            responseJsonObject.addProperty("total", total);
        }
        response.getWriter().write(responseJsonObject.toString());
        out.close();
    }
}

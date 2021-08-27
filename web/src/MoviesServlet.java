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
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;

// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
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

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // Create session to keep the URL
        String url = request.getRequestURL().toString();
        url = url.substring(0, url.length() - 10);
        String queryString = request.getQueryString();
        String completeUrl = url + "movies.html?" + queryString;
        HttpSession session = request.getSession();
        session.setAttribute("completeUrl", completeUrl);

        response.setContentType("application/json"); // Response mime type
        String title = request.getParameter("title");
        String charTitle = request.getParameter("charTitle");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String genre = request.getParameter("genre");
        String star = request.getParameter("starName");
        String nList = request.getParameter("nList");
        String pageNum = request.getParameter("pageNum");
        int offset = Integer.parseInt(nList) * (Integer.parseInt(pageNum) - 1);

        if(title == null ||title.equals("null") || title.equals("")) {
            if(charTitle.equals("*"))
                title = "RLIKE '^[^a-zA-Z0-9]'";
            else if(!charTitle.equals("null"))
                title = charTitle + "%";
            else
                title = "%";
        }
        else
            title = "%" + title + "%";
        if(year == null || year.equals("null") || year.equals(""))
            year = "%";
        if(director == null || director.equals("null") || director.equals(""))
            director = "%";
        else
            director = "%" + director + "%";
        if(genre == null || genre.equals("null"))
            genre = "%";
        else
            genre = "%" + genre + "%";
        if(star == null || star.equals("null") || star.equals(""))
            star = "%";
        else
            star = "%" + star + "%";

        System.out.println(title);
        System.out.println(year);
        System.out.println(director);
        System.out.println(genre);
        System.out.println(star);
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            String query = "";
            Connection dbcon = dataSource.getConnection();

            query = "select id, title, year, director, rating from movies as m, ratings as r where m.id = " +
                    "r.movieId and title LIKE ? and year Like ? and director LIKE ? order by r.rating desc, title " +
                    "limit 100 offset ?";

            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, title);
            statement.setString(2, year);
            statement.setString(3, director);
            statement.setInt(4, offset);

            if(charTitle.equals("*")){
                query = "select id, title, year, director, rating from movies as m, ratings as r where m.id = " +
                        "r.movieId and title RLIKE '^[^a-zA-Z0-9]' and year Like ? and director LIKE ? order by " +
                        "rating desc, title limit 100 offset ?";

                statement = dbcon.prepareStatement(query);
                statement.setString(1, year);
                statement.setString(2, director);
                statement.setInt(3, offset);
            }
            else if(!genre.equals("%") || !star.equals("%")){
                query = "select m.id, title, year, director, rating from movies as m, stars_in_movies as sim, " +
                        "stars as s, ratings as r, genres as g, genres_in_movies as gim where g.id = gim.genreId " +
                        "and gim.movieId = sim.movieId and sim.starId = s.id and sim.movieId = r.movieId and " +
                        "sim.movieId = m.id and title LIKE ? and year LIKE ? and director LIKE ? and s.name LIKE ? AND " +
                        "g.name LIKE ? group by title order by rating desc, title limit 100 offset ?";
                // Declare our statement
                statement = dbcon.prepareStatement(query);


                // Set the parameter represented by "?" in the query to the id we get from url,
                // num 1 indicates the first "?" in the query
                statement.setString(1, title);
                statement.setString(2, year);
                statement.setString(3, director);
                statement.setString(4, star);
                statement.setString(5, genre);
                statement.setInt(6, offset);

                System.out.println("STATEMENT = " + statement);
            }

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");

                // get the list of stars
                String query2 = "select * from stars_in_movies as sim, stars as s where sim.starId = s.id and " +
                                "sim.movieId = ? order by s.name limit 100";

                PreparedStatement statement2 = dbcon.prepareStatement(query2);
                statement2.setString(1, movie_id);
                ResultSet rs2 = statement2.executeQuery();

                int[] appNum = new int[3];
                String[] artists = new String[3];
                String[] artistsId = new String[3];

                // for each stars get the num of appearance
                while(rs2.next()){
                    String query4 = "select count(starId), s.name, s.id from stars_in_movies as sim, stars as s where " +
                                    "sim.starId = ? and sim.starId = s.id group by sim.starId limit 100";
                    PreparedStatement statement4 = dbcon.prepareStatement(query4);
                    statement4.setString(1, rs2.getString("starId"));
                    ResultSet rs4 = statement4.executeQuery();
                    rs4.next();
                    String num = rs4.getString("count(starId)");
                    String name = rs4.getString("name");
                    String id = rs4.getString("id");
                    int numOfApp = Integer.parseInt(num);
                    if(numOfApp > appNum[0] && numOfApp <= appNum[1]){
                        appNum[0] = numOfApp;
                        artists[0] = name;
                        artistsId[0] = id;
                    }
                    else if(numOfApp > appNum[0] && numOfApp <= appNum[2]){
                        appNum[0] = appNum[1];
                        artists[0] = artists[1];
                        artistsId[0] = artistsId[1];
                        appNum[1] = numOfApp;
                        artists[1] = name;
                        artistsId[1] = id;
                    }
                    else if(numOfApp > appNum[2]){
                        appNum[0] = appNum[1];
                        artists[0] = artists[1];
                        artistsId[0] = artistsId[1];
                        appNum[1] = appNum[2];
                        artists[1] = artists[2];
                        artistsId[1] = artistsId[2];
                        appNum[2] = numOfApp;
                        artists[2] = name;
                        artistsId[2] = id;
                    }
                    rs4.close();
                    statement4.close();
                }

                rs2.close();
                statement2.close();
                String movie_artist1 = artists[2];
                String movie_artistId1 = artistsId[2];
                String movie_artist2 = "";
                String movie_artistId2 = "";
                String movie_artist3 = "";
                String movie_artistId3 = "";
                if(appNum[1] != 0) {
                    movie_artist2 = artists[1];
                    movie_artistId2 = artistsId[1];
                }
                if(appNum[0] != 0) {
                    movie_artist3 = artists[0];
                    movie_artistId3 = artistsId[0];
                }

                String query3 = "select * from genres_in_movies as gim, genres as g where gim.genreId = g.id and " +
                                "movieId = ? order by g.name limit 100";

                PreparedStatement statement3 = dbcon.prepareStatement(query3);
                statement3.setString(1, movie_id);
                ResultSet rs3 = statement3.executeQuery();

                rs3.next();
                String movie_genre1 = rs3.getString("name");
                String movie_genreId1 = rs3.getString("id");
                String movie_genre2 = "";
                String movie_genreId2 = "";
                String movie_genre3 = "";
                String movie_genreId3 = "";
                if(rs3.next()) {
                    movie_genre2 = rs3.getString("name");
                    movie_genreId2 = rs3.getString("id");
                }
                if(rs3.next()) {
                    movie_genre3 = rs3.getString("name");
                    movie_genreId3 = rs3.getString("id");
                }
                rs3.close();
                statement3.close();

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_genre1", movie_genre1);
                jsonObject.addProperty("movie_genreId1", movie_genreId1);
                if(!movie_genre2.equals("")) {
                    jsonObject.addProperty("movie_genreId2", movie_genreId2);
                    jsonObject.addProperty("movie_genre2", movie_genre2);
                }
                if(!movie_genre3.equals("")) {
                    jsonObject.addProperty("movie_genre3", movie_genre3);
                    jsonObject.addProperty("movie_genreId3", movie_genreId3);
                };
                jsonObject.addProperty("movie_artist1", movie_artist1);
                jsonObject.addProperty("movie_artistId1", movie_artistId1);
                if(!movie_artist2.equals("")) {
                    jsonObject.addProperty("movie_artist2", movie_artist2);
                    jsonObject.addProperty("movie_artistId2", movie_artistId2);
                }
                if(!movie_artist3.equals("")) {
                    jsonObject.addProperty("movie_artist3", movie_artist3);
                    jsonObject.addProperty("movie_artistId3", movie_artistId3);
                }
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }

            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            rs.close();
            statement.close();
            dbcon.close();
        } catch (Exception e) {

            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
        }
        out.close();

    }
}

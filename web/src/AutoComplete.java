import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.HashMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;


// server endpoint URL
@WebServlet("/autocomplete")
public class AutoComplete extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/*
	 * populate the Super hero hash map.
	 * Key is hero ID. Value is hero name.
	 */
//	public static HashMap<Integer, String> superHeroMap = new HashMap<>();
//
//	static {
//		superHeroMap.put(1, "Blade");
//		superHeroMap.put(2, "Ghost Rider");
//		superHeroMap.put(3, "Luke Cage");
//		superHeroMap.put(4, "Silver Surfer");
//		superHeroMap.put(5, "Beast");
//		superHeroMap.put(6, "Thing");
//		superHeroMap.put(7, "Black Panther");
//		superHeroMap.put(8, "Invisible Woman");
//		superHeroMap.put(9, "Nick Fury");
//		superHeroMap.put(10, "Storm");
//		superHeroMap.put(11, "Iron Man");
//		superHeroMap.put(12, "Professor X");
//		superHeroMap.put(13, "Hulk");
//		superHeroMap.put(14, "Cyclops");
//		superHeroMap.put(15, "Thor");
//		superHeroMap.put(16, "Jean Grey");
//		superHeroMap.put(17, "Wolverine");
//		superHeroMap.put(18, "Daredevil");
//		superHeroMap.put(19, "Captain America");
//		superHeroMap.put(20, "Spider-Man");
//
//		superHeroMap.put(101, "Superman");
//		superHeroMap.put(102, "Batman");
//		superHeroMap.put(103, "Wonder Woman");
//		superHeroMap.put(104, "Flash");
//		superHeroMap.put(105, "Green Lantern");
//		superHeroMap.put(106, "Catwoman");
//		superHeroMap.put(107, "Nightwing");
//		superHeroMap.put(108, "Captain Marvel");
//		superHeroMap.put(109, "Aquaman");
//		superHeroMap.put(110, "Green Arrow");
//		superHeroMap.put(111, "Martian Manhunter");
//		superHeroMap.put(112, "Batgirl");
//		superHeroMap.put(113, "Supergirl");
//		superHeroMap.put(114, "Black Canary");
//		superHeroMap.put(115, "Hawkgirl");
//		superHeroMap.put(116, "Cyborg");
//		superHeroMap.put(117, "Robin");
//	}
//
//    public HeroSuggestion() {
//        super();
//    }

	// Create a dataSource which registered in web.xml
	private DataSource dataSource;

	public void init(ServletConfig config) {
		try {
			dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

    /*
     * 
     * Match the query against superheroes and return a JSON response.
     * 
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "heroID": 101 } },
     * 	{ "value": "Supergirl", "data": { "heroID": 113 } }
     * ]
     * 
     * The format is like this because it can be directly used by the 
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *   
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     * 
     * 
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// setup the response json arrray
			JsonArray jsonArray = new JsonArray();
			
			// get the query string from parameter
			String query = request.getParameter("query");
			System.out.println("Query");

			// tokenizer
			StringTokenizer st = new StringTokenizer(query, " ");
			String titleQuery = "";
			while(st.hasMoreTokens())
			{
				titleQuery +=  "+" + st.nextToken() + "* ";
			}

			System.out.println("titleQuery = " + titleQuery);
			System.out.println("query = " + query);

			// return the empty json array if query is null or empty
			if (query == null || query.trim().isEmpty()) {
				response.getWriter().write(jsonArray.toString());
				return;
			}
			
			// search on superheroes and add the results to JSON Array
			// this example only does a substring match
			// TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars

			String sql_query = "SELECT * FROM movies WHERE MATCH (title) AGAINST (? in boolean mode)";

			// Declare our statement
			Connection dbcon = dataSource.getConnection();
			PreparedStatement statement = dbcon.prepareStatement(sql_query);

			statement.setString(1, titleQuery);

			System.out.println("statement = " + statement);

			ResultSet rs = statement.executeQuery();

			int count = 0;
			while(rs.next() && count < 10)
			{
				count++;
				// Create a JsonObject based on the data we retrieve from rs
				JsonObject jsonObject = new JsonObject();
				String movie_id = rs.getString("id");
				String movie_title = rs.getString("title");
				jsonArray.add(generateJsonObject(movie_id, movie_title));
			}

//			for (Integer id : superHeroMap.keySet()) {
//				String heroName = superHeroMap.get(id);
//				if (heroName.toLowerCase().contains(query.toLowerCase())) {
//					jsonArray.add(generateJsonObject(id, heroName));
//				}
//			}

			System.out.println(jsonArray.toString());
			
			response.getWriter().write(jsonArray.toString());
			return;
		} catch (Exception e) {
			System.out.println(e);
			response.sendError(500, e.getMessage());
		}
	}
	
	/*
	 * Generate the JSON Object from hero to be like this format:
	 * {
	 *   "value": "Iron Man",
	 *   "data": { "heroID": 11 }
	 * }
	 * 
	 */
	private static JsonObject generateJsonObject(String movie_id, String movie_title) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", movie_title);

		JsonObject additionalDataJsonObject = new JsonObject();
		additionalDataJsonObject.addProperty("movie_id", movie_id);

		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}


}

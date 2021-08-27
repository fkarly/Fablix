/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#movie_info");

    // create variables for genre
    let num = 0;
    let string = "movie_genre" + num;
    let genre = resultData[resultData.length - 2][string];
    let genreStrings = "<p>Genre: <a href='movies.html?genre=" + genre + "'>" + genre + "</a>";
    num++;
    string = "movie_genre" + num;
    genre = resultData[resultData.length - 2][string];
    while(Boolean(genre)){
            genreStrings += ", <a href='movies.html?genre=" + genre + "'>" + genre + "</a>";
            num++;
            string = "movie_genre" + num;
            genre = resultData[resultData.length - 2][string];
    }
    genreStrings += "</p>";


    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p>Movie Title: " + resultData[0]["movie_title"] + "</p>" +
        "<p>Year: " + resultData[0]["movie_year"] + "</p>" +
        "<p>Director: " + resultData[0]["movie_director"] + "</p>" + genreStrings +
        "<p>Rating: " + resultData[0]["movie_rating"] + "</p>" +
        "<p><a href='cart?newItem=" + resultData[0]['movie_id'] + "&newPrice=20&newQuant=1'>Add to cart</a></p>" +
        "<p><a href='" + resultData[resultData.length - 1]["completeUrl"] + "'>Back to Search</a></p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#star_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length - 2; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
                    "<th>" +
                    // Add a link to single-movie.html with id passed with GET url parameter
                    '<a href="single-star.html?id=' + resultData[i]['star_id'] + '">'
                    + resultData[i]["star_name"] +     // display movie_name for the link text
                    '</a>' +
                    "</th>";
        rowHTML += "<th>" + resultData[i]["star_dob"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});
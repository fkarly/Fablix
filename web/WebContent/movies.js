/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
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
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, lowest between nList and data length
    for (let i = 0; i < Math.min(nList, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display movie_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML +=
                    "<th>" +
                    // Add a link to single-star.html with id passed with GET url parameter
                    '<a href="movies.html?genre=' + resultData[i]['movie_genre1'] + '">'
                    + resultData[i]["movie_genre1"] +     // display movie_artist for the link text
                    '</a>'
        if(Boolean(resultData[i]["movie_genre2"])){
            rowHTML += ', ' + '<a href="movies.html?genre=' + resultData[i]['movie_genre2'] + '">'
                       + resultData[i]["movie_genre2"] +     // display movie_artist for the link text
                       '</a>'
        }
        if(Boolean(resultData[i]["movie_genre3"])){
            rowHTML += ', ' + '<a href="movies.html?genre=' + resultData[i]['movie_genre3'] + '">'
                       + resultData[i]["movie_genre3"] +     // display movie_artist for the link text
                       '</a>'
        }
        rowHTML += "</th>";
        rowHTML += "<th>" +
                   // Add a link to single-star.html with id passed with GET url parameter
                   '<a href="single-star.html?id=' + resultData[i]['movie_artistId1'] + '">'
                   + resultData[i]["movie_artist1"] +     // display movie_artist for the link text
                   '</a>'
        if(Boolean(resultData[i]["movie_artist2"])){
            rowHTML += ', ' + '<a href="single-star.html?id=' + resultData[i]['movie_artistId2'] + '">'
                       + resultData[i]["movie_artist2"] +     // display movie_artist for the link text
                       '</a>'
        }
        if(Boolean(resultData[i]["movie_artist3"])){
            rowHTML += ', ' + '<a href="single-star.html?id=' + resultData[i]['movie_artistId3'] + '">'
                       + resultData[i]["movie_artist3"] +     // display movie_artist for the link text
                       '</a>'
        }
        rowHTML += "</th>";
        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "<th>" + "<a href='cart?newItem=" + resultData[i]['movie_id'] + "&newPrice=20&newQuant=1'>" +
                   "Add to cart" + "</a></th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}
/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Get genre from URL
let movieGenre = getParameterByName('genre');
let movieTitle = getParameterByName('title');
let charTitle = getParameterByName('charTitle');
let movieYear = getParameterByName('year');
let movieDirector = getParameterByName('director');
let movieStar = getParameterByName('starName');
let pageNum = getParameterByName('pageNum');
let nList = getParameterByName('nList');
let flip = getParameterByName('flip');

// By default nList = 10
if(!Boolean(nList)){
    nList = 10;
}
if(!Boolean(pageNum)){
    pageNum = 1;
}

// Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies?genre=" + movieGenre + "&title=" + movieTitle + "&year=" + movieYear + "&director=" +
          movieDirector + "&starName=" + movieStar + "&charTitle=" + charTitle + "&nList=" + nList + "&pageNum=" + pageNum,
          // Setting request url, which is mapped by MoviesServlet in Movies.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the MoviesServlet
});
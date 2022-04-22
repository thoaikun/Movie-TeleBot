package Modals;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

public class MovieModal implements Modal {

    @Override
    public JSONArray searchMovie(String movieName) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                                                        "3/search/movie" +
                                                        "?api_key=" + this.API +
                                                        "&query=" + movieName)
                                                 .asJson();
        if (response.getStatus() != 200)
            return null;
        JSONArray results = response.getBody()
                                    .getObject()
                                    .getJSONArray("results");
        return results;
    }

    @Override
    public JSONArray getUpcoming() throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                                                        "3/movie/upcoming" +
                                                        "?api_key=" + this.API +
                                                        "&language=en-US" +
                                                        "&page=1" + "&region=US")
                                                 .asJson();
        if (response.getStatus() != 200)
            return null;
        JSONArray results = response.getBody()
                                    .getObject()
                                    .getJSONArray("results");
        return results;
    }

    @Override
    public JSONArray getTrending() throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                                                        "3/trending/movie/week" +
                                                        "?api_key=" + this.API)
                                                 .asJson();
        if (response.getStatus() != 200)
            return null;
        JSONArray results = response.getBody()
                                    .getObject()
                                    .getJSONArray("results");
        return results;
    }

    @Override
    public JSONArray getUserReview(String movieName, String movieYear) throws UnirestException {
        movieYear = movieYear.split("-")[0];
        movieName = movieName.replace(" ", "%20");
        HttpResponse<JsonNode> response = Unirest.get("https://imdb8.p.rapidapi.com/title/find?q="+movieName)
                                                .header("x-rapidapi-host", "imdb8.p.rapidapi.com")
                                                .header("x-rapidapi-key", "3793fd6a8bmsh3992e80f2f92f34p1ee63ajsn3b0fe00f7804")
                                                .asJson();
        if (response.getStatus() != 200)
            return null;
        JSONObject object = response.getBody().getObject();
        if (!object.has("results"))
            return null;

        JSONArray movies = object.getJSONArray("results");
        JSONObject foundMovie = null;
        for (int i=0; i < movies.length(); i++) {
            JSONObject t = movies.getJSONObject(i);
            if (t.has("titleType") && t.has("year")) {
                if (t.get("titleType").toString().equals("movie") && t.get("year").toString().equals(movieYear)) {
                    foundMovie = t;
                    break;
                }
            }
        }

        if (foundMovie == null)
            return null;

        String movieID = foundMovie.get("id").toString().split("/")[2];
        // get user review form movieId
        response = Unirest.get("https://imdb8.p.rapidapi.com/title/get-user-reviews?tconst=" + movieID)
                        .header("x-rapidapi-host", "imdb8.p.rapidapi.com")
                        .header("x-rapidapi-key", "3793fd6a8bmsh3992e80f2f92f34p1ee63ajsn3b0fe00f7804")
                        .asJson();
        JSONObject reviews = response.getBody().getObject();
        if (reviews.has("reviews"))
            return reviews.getJSONArray("reviews");
        return null;
    }

    @Override
    public String getTrailer(String movieID) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                                                        "3/movie/" + movieID + "/videos" +
                                                        "?api_key=" + this.API)
                                                 .asJson();
        if (response.getStatus() != 200)
            return "No trailer found";
        JSONObject trailerVideo = new JSONObject();
        JSONArray videos = response.getBody()
                                    .getObject()
                                    .getJSONArray("results");
        // check whether this movie has trailer
        if (videos.isEmpty()) {
            return "No trailer found";
        }

        // find video that has type trailer
        for (int i=0; i < videos.length(); i++) {
            if (videos.getJSONObject(i).get("type").equals("Trailer")) {
                trailerVideo = videos.getJSONObject(i);
                break;
            }
        }

        if (trailerVideo.has("key"))
            return trailerVideo.get("key").toString();
        return "No trailer found";
    }
}

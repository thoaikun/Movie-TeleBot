package Modals;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

public class ShowModal implements Modal {
    @Override
    public JSONArray searchMovie(String showName) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get("https://api.themoviedb.org/" +
                                                        "3/search/tv" +
                                                        "?api_key=" + API +
                                                        "&query=" + showName)
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
        return null;
    }

    @Override
    public JSONArray getTrending() throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                                                        "3/trending/tv/week" +
                                                        "?api_key=" + API)
                                                .asJson();
        if (response.getStatus() != 200)
            return null;

        JSONArray results = response.getBody()
                                    .getObject()
                                    .getJSONArray("results");
        return results;
    }

    public JSONArray getOnAir() throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get("https://api.themoviedb.org/" +
                                                        "3/tv/on_the_air" +
                                                        "?api_key=" + API).asJson();
        if (response.getStatus() != 200)
            return null;

        JSONArray results = response.getBody()
                                    .getObject()
                                    .getJSONArray("results");
        return results;
    }

    @Override
    public JSONArray getUserReview(String showName, String showYear) throws UnirestException {
        showYear = showYear.split("-")[0];
        showName = showName.replace(" ", "%20");

        HttpResponse<JsonNode> response = Unirest.get("https://imdb8.p.rapidapi.com/title/find?q=" + showName)
                                                .header("X-RapidAPI-Host", "imdb8.p.rapidapi.com")
                                                .header("X-RapidAPI-Key", "3793fd6a8bmsh3992e80f2f92f34p1ee63ajsn3b0fe00f7804")
                                                .asJson();
        if (response.getStatus() != 200)
            return null;
        JSONObject object =  response.getBody().getObject();
        if (!object.has("results"))
            return null;

        JSONArray shows = object.getJSONArray("results");
        JSONObject foundShow = null;
        for (int i=0; i < shows.length(); i++) {
            JSONObject t = shows.getJSONObject(i);
            if (t.has("titleType") && t.has("seriesStartYear")) {
                if (t.get("titleType").toString().contains("tvSeries") && t.get("seriesStartYear").toString().equals(showYear)) {
                    foundShow = t;
                    break;
                }
            }
        }
        if (foundShow == null)
            return null;

        String showID = foundShow.get("id").toString().split("/")[2];

        // get user review form movieId
        response = Unirest.get("https://imdb8.p.rapidapi.com/title/get-user-reviews?tconst=" +showID)
                        .header("X-RapidAPI-Host", "imdb8.p.rapidapi.com")
                        .header("X-RapidAPI-Key", "3793fd6a8bmsh3992e80f2f92f34p1ee63ajsn3b0fe00f7804")
                        .asJson();
        if (response.getStatus() != 200)
                return null;

        JSONObject reviews = response.getBody().getObject();
        if (reviews.has("reviews"))
            return reviews.getJSONArray("reviews");
        return null;
    }

    @Override
    public String getTrailer(String showID) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                                                        "3/tv/" + showID +"/videos" +
                                                        "?api_key=" + this.API )
                                                .asJson() ;
        if (response.getStatus() != 200)
            return "No trailer found";

        JSONObject trailerVideo = new JSONObject();
        JSONArray videos = response.getBody()
                                    .getObject()
                                    .getJSONArray("results") ;

        if(videos.isEmpty()) {
            return "No trailer found" ;
        }
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

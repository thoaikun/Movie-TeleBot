package Modals;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;

public interface Modal {
    public final String API = "16e8d32a627987825706488073388e2e";
    JSONArray searchMovie(String movieName) throws UnirestException;
    JSONArray getUpcoming() throws UnirestException;
    JSONArray getTrending() throws UnirestException;
    JSONArray getUserReview(String movieName, String movieYear) throws UnirestException;
    String getTrailer(String movieID) throws UnirestException;
}

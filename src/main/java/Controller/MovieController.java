package Controller;

import Modals.MovieModal;
import Views.MovieView;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.text.ParseException;

public class MovieController {
    private MovieModal movieModal;
    private MovieView movieView;

    public MovieController() {
        this.movieModal = new MovieModal();
        this.movieView = new MovieView();
    }

    public MovieView getMovieView() { return this.movieView; }

    public SendMessage sendWelcome(String chatID) {
        return this.movieView.getStart(chatID);
    }

    public SendMessage sendSearchList(String receiveMessage, String chatID) throws UnirestException {
        String movieName = receiveMessage.split(" ", 2)[1];
        movieName = movieName.replace(" ", "%20");
        if (movieName.isEmpty())
            return new SendMessage(chatID, "Please enter movie name");

        // get an array of seached movie
        JSONArray results = this.movieModal.searchMovie(movieName);
        if (results == null)
            return new SendMessage(chatID, "Opp!! Something wrong, please try again");
        this.movieView.setObj(results, chatID);
        return this.movieView.displaySearchList(0, chatID);
    }

    public EditMessageText sendEditList(String receiveMessage, String chatID, long messageID) {
        int index = Integer.parseInt(receiveMessage.split("_")[2]);
        return this.movieView.displaySearchList(index, chatID, messageID);
    }

    public SendMessage sendTrendingList(String chatID) throws UnirestException {
        JSONArray results = this.movieModal.getTrending();
        if (results == null)
            return new SendMessage(chatID, "Opp!! Something wrong, please try again");
        this.movieView.setObj(results, chatID);
        return this.movieView.displaySearchList(0, chatID);
    }

    public SendMessage sendUpcomingList(String chatID) throws UnirestException {
        JSONArray results = this.movieModal.getUpcoming();
        if (results == null)
            return new SendMessage(chatID, "Opp!! Something wrong, please try again");
        this.movieView.setObj(results, chatID);
        return this.movieView.displaySearchList(0, chatID);
    }

    public SendPhoto sendDetail(String receiveMessage, String chatID) throws UnirestException, ParseException {
        int index = Integer.parseInt(receiveMessage.split("_")[1]);

        JSONArray results = this.movieView.getMovie(chatID);
        JSONObject movie = results.getJSONObject(index);
        JSONArray reviews = this.movieModal.getUserReview(  movie.get("original_title").toString(),
                movie.get("release_date").toString());
        if (reviews != null)
            this.movieView.setReview(reviews, chatID);
        String trailerKey = this.movieModal.getTrailer(movie.get("id").toString());

        return this.movieView.displayMovieDetail(movie, index, chatID, trailerKey, !(reviews == null));
    }

    public DeleteMessage deleteMessage(String chatID, long messageID) {
        return new DeleteMessage(chatID, Math.toIntExact(messageID));
    }

    public SendMessage returnToList(String chatID) {
        return this.movieView.returnToList(chatID);
    }

    public SendMessage sendReviews(String chatID) {
        return this.movieView.displayReview(0, chatID);
    }

    public EditMessageText sendReviews(String receiveMessage, String chatID, long messageID) {
        String page = receiveMessage.split("_")[2];
        if (receiveMessage.contains("movieReview_forward_"))
            return this.movieView.displayReview(Integer.parseInt(page), chatID, Math.toIntExact(messageID), false);
        else
            return this.movieView.displayReview(Integer.parseInt(page), chatID, Math.toIntExact(messageID), true);
    }
}


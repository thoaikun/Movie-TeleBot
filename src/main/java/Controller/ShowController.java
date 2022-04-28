package Controller;

import Modals.ShowModal;
import Views.ShowView;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.text.ParseException;

public class ShowController {
    private ShowModal showModal;
    private ShowView showView;

    public ShowController() {
        this.showModal = new ShowModal();
        this.showView = new ShowView();
    }

    public ShowView getShowView() { return this.showView; }

    public SendMessage sendWelcome(String chatID) {
        return this.showView.getStart(chatID);
    }

    public SendMessage sendSearchList(String receiveMessage, String chatID) throws UnirestException {
        String showName = receiveMessage.split(" ", 2)[1];
        showName = showName.replace(" ", "%20");
        if (showName.isEmpty())
            return new SendMessage(chatID, "Please enter TVshow name");

        JSONArray results = this.showModal.searchMovie(showName);
        if (results == null)
            return new SendMessage(chatID, "Opp!! Something wrong, please try again");

        this.showView.setObj(results, chatID);
        return this.showView.displaySearchList(0, chatID);
    }

    public EditMessageText sendEditList(String receiveMessage, String chatID, long messageID) {
        int index = Integer.parseInt(receiveMessage.split("_")[2]);
        return this.showView.displaySearchList(index, chatID, messageID);
    }

    public SendMessage sendOnAirList(String chatID) throws UnirestException {
        JSONArray results = this.showModal.getOnAir();
        if (results == null)
            return new SendMessage(chatID, "Opp!! Something wrong, please try again");
        this.showView.setObj(results, chatID);
        return this.showView.displaySearchList(0, chatID);
    }

    public SendMessage sendTrendingList(String chatID) throws UnirestException {
        JSONArray results = this.showModal.getTrending();
        if (results == null)
            return new SendMessage(chatID, "Opp!! Something wrong, please try again");
        this.showView.setObj(results, chatID);
        return this.showView.displaySearchList(0, chatID);
    }

    public SendPhoto sendDetail(String receiveMessage, String chatID) throws UnirestException, ParseException {
        int index = Integer.parseInt(receiveMessage.split("_")[1]);

        JSONArray results = this.showView.getShow(chatID);
        JSONObject show = results.getJSONObject(index);
        JSONArray reviews = this.showModal.getUserReview(   show.get("original_name").toString(),
                                                            show.get("first_air_date").toString());
        if (reviews != null)
            this.showView.setReview(reviews, chatID);
        String trailerKey = this.showModal.getTrailer(show.get("id").toString());

        return this.showView.displayMovieDetail(show, index, chatID, trailerKey, !(reviews == null));
    }

    public DeleteMessage deleteMessage(String chatID, long messageID) {
        return new DeleteMessage(chatID, Math.toIntExact(messageID));
    }

    public SendMessage returnToList(String chatID) {
        return this.showView.returnToList(chatID);
    }

    public SendMessage sendReviews(String chatID) {
        return this.showView.displayReview(0, chatID);
    }

    public EditMessageText sendReviews(String receiveMessage, String chatID, long messageID) {
        String page = receiveMessage.split("_")[2];
        if (receiveMessage.contains("TVshowReview_forward_"))
            return this.showView.displayReview(Integer.parseInt(page), chatID, Math.toIntExact(messageID), false);
        else
            return this.showView.displayReview(Integer.parseInt(page), chatID, Math.toIntExact(messageID), true);
    }
}

package Views;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.text.ParseException;

public interface View {
    void setObj(JSONArray obj, String chatID);
    void setReview(JSONArray obj, String chatID);
    SendMessage getStart(String chatID);
    SendMessage displaySearchList(int index, String chatID);
    EditMessageText displaySearchList(int index, String chatID, long messageID);
    SendPhoto displayMovieDetail(JSONObject detailMovie, int index, String chatID, String movieTrailerKey, boolean hasReview) throws ParseException;
    SendMessage displayReview(int index, String chatID);
    EditMessageText displayReview(int index, String chatID, int messageID, boolean isBackward);
    SendMessage returnToList(String chatId);
}

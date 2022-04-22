package Views;

import Objects.UserData;
import com.vdurmont.emoji.EmojiParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ShowView implements View {
    private HashMap<String, UserData> userData;
    private HashMap<String, Object> messageHistory;
    private int code;

    public ShowView() {
        this.userData = new HashMap<>();
        this.messageHistory = new HashMap<>();
    }

    @Override
    public void setObj(JSONArray obj, String chatID) {
        if (this.userData.containsKey(chatID))
            this.userData.get(chatID).setMovieObjs(obj);
        else
            this.userData.put(chatID, new UserData(obj));
    }

    @Override
    public void setReview(JSONArray obj, String chatID) {
        this.userData.get(chatID).setReviewObjs(obj);
    }

    public JSONArray getShow(String chatID) {
        return this.userData.get(chatID).getMovieObjs();
    }

    @Override
    public SendMessage getStart(String chatID) {
        String message = EmojiParser.parseToUnicode(
                "You want to search some thing about TV Shows? Please enter :\n" +
                        "Type /TVshow + 'your season TV Show you want to find'\n"+
                        "Type /trending_TVshow to see which TV Show is hot in this weekend\n"
        );
        SendMessage messageStart = new SendMessage(chatID, message);
        return messageStart ;
    }

    @Override
    public SendMessage displaySearchList(int index, String chatID) {
        JSONArray searchedList = this.userData.get(chatID).getMovieObjs();
        if (searchedList.isEmpty()) {
            SendMessage replyMessage = new SendMessage();
            replyMessage.setChatId(chatID);
            replyMessage.setText(EmojiParser.parseToUnicode("Sorry I can't find this TvShows. Can you check your name or season of this TV show ? "));
            return replyMessage;
        }

        String text = "";
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        for (int i=5*index; i < Math.min(5*index + 5, searchedList.length()); i++) {
            JSONObject Show = searchedList.getJSONObject(i);
            String nameShow = Show.get("original_name").toString();

            text += i + "/ "+ nameShow + "\n";
            row1.add(new InlineKeyboardButton(String.valueOf(i), null, "TVshowIndex_" + i,
                    null, null, null, null, null));
        }

        if ((index+1)*5 < searchedList.length() - 1)
            row2.add(new InlineKeyboardButton(">>", null, "TVshow_forward_" + (index + 1),
                    null, null, null, null, null));


        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        btnList.add(row1);
        btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup(btnList);

        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(chatID);
        replyMessage.setReplyMarkup(allBtn);
        replyMessage.setText(text);

        this.messageHistory.put(replyMessage.getChatId(), replyMessage);
        this.code = 0;

        return replyMessage;
    }

    @Override
    public EditMessageText displaySearchList(int index, String chatID, long messageID) {
        JSONArray searchedList = this.userData.get(chatID).getMovieObjs();
        String text = "";
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        for (int i=5*index; i < Math.min(5*index + 5, searchedList.length()); i++) {
            JSONObject movie = searchedList.getJSONObject(i);
            String movieName = movie.get("original_title").toString();

            text += i + "/ " + movieName + "\n";
            row1.add(new InlineKeyboardButton(String.valueOf(i), null, "movieIndex_" + i,
                    null, null, null, null, null));
        }

        if ((index-1)*5 >= 0)
            row2.add(new InlineKeyboardButton("<<", null, "movieList_backward_" + (index - 1),
                    null, null, null, null, null));
        if ((index+1)*5 < searchedList.length() - 1)
            row2.add(new InlineKeyboardButton(">>", null, "movieList_forward_" + (index + 1),
                    null, null, null, null, null));

        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        btnList.add(row1); btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup(btnList);

        EditMessageText replyMessage = new EditMessageText();
        replyMessage.setChatId(chatID);
        replyMessage.setMessageId(Math.toIntExact(messageID));
        replyMessage.setReplyMarkup(allBtn);
        replyMessage.setText(text);

        this.messageHistory.put(replyMessage.getChatId(), replyMessage);
        this.code = 1;

        return replyMessage;
    }

    @Override
    public SendPhoto displayMovieDetail(JSONObject detailShow, int index, String chatID, String showTrailerKey, boolean hasReview) throws ParseException {
        String showName =  detailShow.get("original_name").toString();
        String showOverView = detailShow.get("overview").toString();
        String showVote = detailShow.get("vote_average").toString();
        String showFirstAirDate = detailShow.get("first_air_date").toString();
        String showImg = detailShow.get("poster_path").toString();

        // add trailer and review button
        InlineKeyboardButton trailerBtn = new InlineKeyboardButton("Trailer", "https://www.youtube.com/watch?v=" + showTrailerKey, null,
                null, null, null, null, null);
        InlineKeyboardButton watchReviewBtn = new InlineKeyboardButton("Watch reviews", null, "TVshow_get_user_review",
                null, null, null, null, null);
        InlineKeyboardButton addToListBtn = new InlineKeyboardButton("Add to list", null, "add_TV_myList_" + index,
                null, null, null, null, null);

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();

        // check if it has no trailer
        if (!showTrailerKey.equals("No trailer found"))
            row1.add(trailerBtn);
        // check if is has no reviews
        if (hasReview)
            row1.add(watchReviewBtn);
        // check date if it is an upcoming movie
        if (!showFirstAirDate.isEmpty()) {
            LocalDate releaseDate = LocalDate.parse(showFirstAirDate);
            if (LocalDate.now().isBefore(releaseDate))
                row1.add(addToListBtn);
        }

        row2.add(new InlineKeyboardButton("Return", null, "TVshowList_return",
                null, null, null, null, null));

        btnList.add(row1);
        btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();
        allBtn.setKeyboard(btnList);

        // Format date
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd");
        Date valueDate = input.parse(showFirstAirDate);
        SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy");
        showFirstAirDate = output.format(valueDate);

        InputFile image = new InputFile("https://image.tmdb.org/t/p/w500" + showImg);
        SendPhoto reply = new SendPhoto(chatID, image);
        reply.setCaption(showName.toUpperCase() + "\n\n" +
                "First air date: " + showFirstAirDate + "\n\n" +
                //   "episode run time" + episode_run_time + "\n\n" +
                //     "Last air date: " + last_air_date + "\n\n" +
                "Rating: " + showVote  + "\n\n" +
                "Overview: " + showOverView);
        reply.setReplyMarkup(allBtn);
        return reply;
    }

    @Override
    public SendMessage displayReview(int index, String chatID) {
        JSONArray reviewObjs = this.userData.get(chatID).getReviewObjs();
        String userPoint = "unknown", userName = "", userReview = "", reviewTitle = "";

        // check whether it has review or not
        if (reviewObjs.length() == 0) {
            SendMessage replyMessage = new SendMessage();
            replyMessage.setChatId(chatID);
            replyMessage.setText(EmojiParser.parseToUnicode("No one watches this movie you stupid head  :clown: :clown: !!\n\nGet you ass out of here"));
            return replyMessage;
        }

        // check the length of each review, if it to long, pass to another review
        JSONObject review;
        int length;
        do {
            review = reviewObjs.getJSONObject(index);
            length = review.get("reviewText").toString().length();
            index ++;
        } while (length > 4000);

        // take review value
        if (review.has("author"))
            userName = review.getJSONObject("author")
                    .get("displayName")
                    .toString();
        if (review.has("reviewText"))
            userReview = review.get("reviewText").toString();
        if (review.has("reviewTitle"))
            reviewTitle = review.get("reviewTitle").toString();
        if (review.has("authorRating"))
            userPoint = String.valueOf(review.get("authorRating"));

        // create button to switch to next review
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        if (reviewObjs.length() > (index + 1)) {
            InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "TVshowReview_forward_" + (index+1),
                    null, null, null, null, null);
            row1.add(forwardBtn);
        }
        row2.add(new InlineKeyboardButton("Return to show", null, "delete_TVshowReview",
                null, null, null, null, null));
        btnList.add(row1);
        btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();
        allBtn.setKeyboard(btnList);

        // return a message
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(chatID);
        replyMessage.setText(EmojiParser.parseToUnicode(":bust_in_silhouette: Account: " + userName + "\n\n"
                + "\t:+1: User score: " + userPoint + "\n\n"
                + "\t:speaking_head_in_silhouette: Review: " + reviewTitle.toUpperCase() + "\n\n" + userReview));
        replyMessage.setReplyMarkup(allBtn);
        return replyMessage;
    }

    @Override
    public EditMessageText displayReview(int index, String chatID, int messageID, boolean isBackward) {
        JSONArray reviewObjs = this.userData.get(chatID).getReviewObjs();
        String userPoint = "unknown", userName = "", userReview = "", reviewTitle = "";
        JSONObject review;
        int length;

        // check the length of each review, if it to long, pass to another review
        do {
            review = reviewObjs.getJSONObject(index);
            length = review.get("reviewText").toString().length();
            if (!isBackward)
                ++index;
            else
                --index;
        } while (length > 4000);

        // take review value
        if (review.has("author"))
            userName = review.getJSONObject("author")
                    .get("displayName")
                    .toString();
        if (review.has("reviewText"))
            userReview = review.get("reviewText").toString();
        if (review.has("reviewTitle"))
            reviewTitle = review.get("reviewTitle").toString();
        if (review.has("authorRating"))
            userPoint = String.valueOf(review.get("authorRating"));


        // create button to switch to next review
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        if (index-1 >= 0) {
            InlineKeyboardButton backwardBtn = new InlineKeyboardButton("<<", null, "TVshowReview_backward_" + (index - 1),
                    null, null, null, null, null);
            row1.add(backwardBtn);
        }
        if (index+1 < reviewObjs.length()) {
            InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "TVshowReview_forward_" + (index+1),
                    null, null, null, null, null);
            row1.add(forwardBtn);
        }
        row2.add(new InlineKeyboardButton("Return to movie", null, "delete_TVshowReview",
                null, null, null, null, null));
        btnList.add(row1);
        btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();
        allBtn.setKeyboard(btnList);

        // return a edit message
        EditMessageText editText = new EditMessageText();
        editText.setChatId(chatID);
        editText.setMessageId(messageID);
        editText.setText(EmojiParser.parseToUnicode(":bust_in_silhouette: Account: " + userName + "\n\n"
                + "\t:+1: User score: " + userPoint + "\n\n"
                + "\t:speaking_head_in_silhouette: Review: " + reviewTitle.toUpperCase() + "\n\n" + userReview));
        editText.setReplyMarkup(allBtn);
        return editText;
    }

    @Override
    public SendMessage returnToList(String chatId) {
        String text = "";
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();

        if (this.code == 0) {
            SendMessage message = (SendMessage) this.messageHistory.get(chatId);
            text = message.getText();
            allBtn = (InlineKeyboardMarkup) message.getReplyMarkup();
        }
        else {
            EditMessageText message = (EditMessageText) this.messageHistory.get(chatId);
            text = message.getText();
            allBtn = message.getReplyMarkup();
        }
        SendMessage replyMessage = new SendMessage(chatId, text);
        replyMessage.setReplyMarkup(allBtn);
        return replyMessage;
    }
}

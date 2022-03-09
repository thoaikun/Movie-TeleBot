import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.vdurmont.emoji.EmojiParser;
import org.checkerframework.checker.units.qual.A;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BotMovie {
    private final static String API = "16e8d32a627987825706488073388e2e";

    private JSONArray movieObjs;
    private JSONArray reviewObjs;
    private Object previousMessage;
    private int code;

    public BotMovie() {
        this.movieObjs = new JSONArray();
        this.reviewObjs = new JSONArray();
    }

    public JSONArray getMovieObjs() { return this.movieObjs; }
    public JSONArray getReviewObjs() { return this.reviewObjs; }

    public void searchMovie(String movieName) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                                                        "3/search/movie" +
                                                        "?api_key=" + this.API +
                                                        "&query=" + movieName)
                                                 .asJson();

        this.movieObjs = response.getBody()
                                 .getObject()
                                 .getJSONArray("results");
    }

    public void getUpcoming() throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                                                        "3/movie/upcoming" +
                                                        "?api_key=" + this.API)
                                                 .asJson();

        this.movieObjs = response.getBody()
                .getObject()
                .getJSONArray("results");
    }

    public void getTrending() throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                        "3/trending/movie/week" +
                        "?api_key=" + this.API)
                .asJson();

        this.movieObjs = response.getBody()
                                 .getObject()
                                 .getJSONArray("results");
    }

    public boolean getUserReview(int index) throws UnirestException {
        /*
            Because MovieDb has small number of review so we will use RapidAPI to get Review from IMDB
            So first we need to get movie id on rapidAPI then get it review
         */
        String movieName = this.movieObjs.getJSONObject(index).get("original_title").toString();
        String movieYear = this.movieObjs.getJSONObject(index).get("release_date").toString();
        movieYear = movieYear.split("-")[0];
        movieName = movieName.replace(" ", "%20");


        HttpResponse<JsonNode> response = Unirest.get("https://imdb8.p.rapidapi.com/title/find?q="+movieName)
                .header("x-rapidapi-host", "imdb8.p.rapidapi.com")
                .header("x-rapidapi-key", "3793fd6a8bmsh3992e80f2f92f34p1ee63ajsn3b0fe00f7804")
                .asJson();
        System.out.println(response.toString());
        JSONObject temp2 =  response.getBody().getObject();
        if (!temp2.has("results"))
            return false;

        JSONArray foundMovies = temp2.getJSONArray("results");
        JSONObject foundMovie = null;
        for (int i=0; i < foundMovies.length(); i++) {
            JSONObject t = foundMovies.getJSONObject(i);
            if (t.has("titleType") && t.has("year")) {
                if (t.get("titleType").toString().equals("movie") && t.get("year").toString().equals(movieYear)) {
                    foundMovie = t;
                    break;
                }
            }
        }
        if (foundMovie == null)
                return false;

        String movieId = foundMovie.get("id").toString().split("/")[2];

        // get user review form movieId
        response = Unirest.get("https://imdb8.p.rapidapi.com/title/get-user-reviews?tconst=" + movieId)
                          .header("x-rapidapi-host", "imdb8.p.rapidapi.com")
                          .header("x-rapidapi-key", "3793fd6a8bmsh3992e80f2f92f34p1ee63ajsn3b0fe00f7804")
                          .asJson();

        JSONObject temp = response.getBody().getObject();
        if (temp.has("reviews")) {
            this.reviewObjs = temp.getJSONArray("reviews");
            return true;
        }
        return false;
    }

    public JSONObject getMovie(int index) { return this.movieObjs.getJSONObject(index); }

    private String getTrailer(int index) throws UnirestException {
        JSONObject detailMovie = this.movieObjs.getJSONObject(index);
        String movieId = detailMovie.get("id").toString();

        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                                                        "3/movie/" + movieId + "/videos" +
                                                        "?api_key=" + this.API)
                                                 .asJson();

        // take json object with have video type is Trailer
        JSONObject trailerVideo = new JSONObject();
        JSONArray videos = response.getBody()
                .getObject()
                .getJSONArray("results");


        // check weather movie has trailer or not
        if (videos.isEmpty()) {
            return "No trailer found";
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

    public EditMessageText getStart(String chatID, int messageID) {
        // setting up for message
        String textMessage = EmojiParser.parseToUnicode(
                "This is Mozziess :dog: :dog:, one of my bot cousin. He knows a lot of movies. If you want to find something, ask himm\n\n" +
                        "Type /movie + 'your movie you want to find' to ask\n" +
                        "Type /trending_movie to see which movie is hot this weekend\n" +
                        "Type /upcoming_movie to see which movie will release");

        // creat a editMessage
        EditMessageText editText = new EditMessageText();
        editText.setChatId(chatID);
        editText.setMessageId(messageID);
        editText.setText(textMessage);
        return editText;
    }

    public SendMessage displaySearchList(int index, String chatID) {
        if (this.movieObjs.isEmpty()) {
            SendMessage replyMessage = new SendMessage();
            replyMessage.setChatId(chatID);
            replyMessage.setText(EmojiParser.parseToUnicode("OPPP!!! Sorry I don't see that movie :cry: :cry: \n " +
                    "Can you please check you movie name again, it maybe wrong :thinking: :thinking:"));
            return replyMessage;
        }

        String text = "";
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        for (int i=5*index; i < (5*index + 5); i++) {
            JSONObject movie = this.movieObjs.getJSONObject(i);
            String movieName = movie.get("original_title").toString();

            text += i + "/ " + movieName + "\n";
            row1.add(new InlineKeyboardButton(String.valueOf(i), null, "movieIndex_" + i,
                    null, null, null, null, null));
        }

        if ((index+1)*5 < this.movieObjs.length() - 1)
            row2.add(new InlineKeyboardButton(">>", null, "movieList_forward_" + (index + 1),
                    null, null, null, null, null));

        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        btnList.add(row1); btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup(btnList);

        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(chatID);
        replyMessage.setReplyMarkup(allBtn);
        replyMessage.setText(text);

        this.previousMessage = (Object) replyMessage;
        this.code = 0;

        return replyMessage;
    }

    public EditMessageText displaySearchList(int index, String chatID, long messageID) {
        String text = "";
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        for (int i=5*index; i < (5*index + 5); i++) {
            JSONObject movie = this.movieObjs.getJSONObject(i);
            String movieName = movie.get("original_title").toString();

            text += i + "/ " + movieName + "\n";
            row1.add(new InlineKeyboardButton(String.valueOf(i), null, "movieIndex_" + i,
                    null, null, null, null, null));
        }

        if ((index-1)*5 >= 0)
            row2.add(new InlineKeyboardButton("<<", null, "movieList_backward_" + (index - 1),
                    null, null, null, null, null));
        if ((index+1)*5 < this.movieObjs.length() - 1)
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

        this.previousMessage = (Object) replyMessage;
        this.code = 1;

        return replyMessage;
    }

    public SendPhoto displayMovieDetail(int index, String chatID) throws ParseException, UnirestException {
        JSONObject detailMovie = this.movieObjs.getJSONObject(index);
        String movieName = detailMovie.get("original_title").toString();
        String movieReleaseDate = detailMovie.get("release_date").toString();
        String movieOverview = detailMovie.get("overview").toString();
        String movieRating = detailMovie.get("vote_average").toString();
        String movieImg = detailMovie.get("poster_path").toString();
        String movieTrailerKey = this.getTrailer(index);

        // add trailer and review button
        InlineKeyboardButton trailerBtn = new InlineKeyboardButton("Trailer", "https://www.youtube.com/watch?v=" + movieTrailerKey, null,
                null, null, null, null, null);
        InlineKeyboardButton watchReviewBtn = new InlineKeyboardButton("Watch reviews", null, "get_movieReview_" + index,
                null, null, null, null, null);
        InlineKeyboardButton addToListBtn = new InlineKeyboardButton("Add to list", null, "add_to_list_" + index,
                null, null, null, null, null);

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();

        // check if it has no trailer
        if (!movieTrailerKey.equals("No trailer found"))
            row1.add(trailerBtn);
        // check if is has no reviews
        if (this.getUserReview(index))
            row1.add(watchReviewBtn);
        // check date if it is an upcoming movie
        if (!movieReleaseDate.isEmpty()) {
            LocalDate releaseDate = LocalDate.parse(movieReleaseDate);
            if (LocalDate.now().isBefore(releaseDate))
                row1.add(addToListBtn);
        }

        row2.add(new InlineKeyboardButton("Return", null, "movieList_return",
                null, null, null, null, null));

        btnList.add(row1);
        btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();
        allBtn.setKeyboard(btnList);

        // Format date
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd");
        Date valueDate = input.parse(movieReleaseDate);
        SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy");
        movieReleaseDate = output.format(valueDate);

        InputFile image = new InputFile("https://image.tmdb.org/t/p/original/" + movieImg);
        SendPhoto reply = new SendPhoto(chatID, image);
        reply.setCaption(movieName.toUpperCase() + "\n\n" +
                        "Release date: " + movieReleaseDate + "\n\n" +
                        "Rating: " + movieRating + "\n\n" +
                        "Overview: " + movieOverview);
        reply.setReplyMarkup(allBtn);
        return reply;
    }

    public SendMessage displayReview(int index, String chatID, long messageId) {
        String userPoint = "unknown", userName = "", userReview = "", reviewTitle = "";

        // check whether it has review or not
        if (this.reviewObjs.length() == 0) {
            SendMessage replyMessage = new SendMessage();
            replyMessage.setChatId(chatID);
            replyMessage.setText(EmojiParser.parseToUnicode("No one watches this movie you stupid head  :clown: :clown: !!\n\nGet you ass out of here"));
            return replyMessage;
        }

        // check the length of each review, if it to long, pass to another review
        JSONObject review;
        int length;
        do {
            review = this.reviewObjs.getJSONObject(index);
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
        if (this.reviewObjs.length() > (index + 1)) {
            InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "movieReview_forward_" + (index+1),
                                                            null, null, null, null, null);
            row1.add(forwardBtn);
        }
        row2.add(new InlineKeyboardButton("Return to movie", null, "delete_movieReview",
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

    public EditMessageText displayReview(int index, String chatID, int messageID, boolean isBackward) {
        String userPoint = "unknown", userName = "", userReview = "", reviewTitle = "";
        JSONObject review;
        int length;

        // check the length of each review, if it to long, pass to another review
        do {
            review = this.reviewObjs.getJSONObject(index);
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
            InlineKeyboardButton backwardBtn = new InlineKeyboardButton("<<", null, "movieReview_backward_" + (index - 1),
                                                            null, null, null, null, null);
            row1.add(backwardBtn);
        }
        if (index+1 < this.reviewObjs.length()) {
            InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "movieReview_forward_" + (index+1),
                                                            null, null, null, null, null);
            row1.add(forwardBtn);
        }
        row2.add(new InlineKeyboardButton("Return to movie", null, "delete_movieReview",
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

    public SendMessage returnToList() {
        String text = "";
        String chatId = "";
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();

        if (this.code == 0) {
            SendMessage message = (SendMessage) this.previousMessage;
            text = message.getText();
            chatId = message.getChatId();
            allBtn = (InlineKeyboardMarkup) message.getReplyMarkup();
        }
        else {
            EditMessageText message = (EditMessageText) this.previousMessage;
            text = message.getText();
            chatId = message.getChatId();
            allBtn = message.getReplyMarkup();
        }
        SendMessage replyMessage = new SendMessage(chatId, text);
        replyMessage.setReplyMarkup(allBtn);
        return replyMessage;
    }
}

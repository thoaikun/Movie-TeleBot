import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.vdurmont.emoji.EmojiParser;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class BotMovie {
    private final String API = "16e8d32a627987825706488073388e2e";

    private JSONArray movieObjs;
    private JSONArray reviewObjs;
    private int currentMovieIndex;

    public BotMovie() {
        this.movieObjs = new JSONArray();
        this.reviewObjs = new JSONArray();
        this.currentMovieIndex = 0;
    }

    public JSONArray getMovieObjs() { return this.movieObjs; }
    public JSONArray getReviewObjs() { return this.reviewObjs; }
    public void increaseIndex() { this.currentMovieIndex ++; }
    public void decreaseIndex() { this.currentMovieIndex --; }

    public void searchMovie(String movieName) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                                                        "3/search/movie" +
                                                        "?api_key=" + this.API +
                                                        "&query=" + movieName)
                                                 .asJson();

        this.movieObjs = response.getBody()
                                 .getObject()
                                 .getJSONArray("results");
        this.currentMovieIndex = 0;
    }

    public void getUserReview() throws UnirestException {
        /*
            Because MovieDb has small number of review so we will use RapidAPI to get Review from IMDB
            So first we need to get movie id on rapidAPI then get it review
         */
        String movieName = this.movieObjs.getJSONObject(this.currentMovieIndex).get("original_title").toString();
        movieName = movieName.replace(" ", "%20");


        HttpResponse<JsonNode> response = Unirest.get("https://imdb8.p.rapidapi.com/title/find?q="+movieName)
                .header("x-rapidapi-host", "imdb8.p.rapidapi.com")
                .header("x-rapidapi-key", "3793fd6a8bmsh3992e80f2f92f34p1ee63ajsn3b0fe00f7804")
                .asJson();
        System.out.println(response.toString());
        JSONObject foundMovies = (JSONObject) response.getBody()
                                                      .getObject()
                                                      .getJSONArray("results")
                                                      .get(0);

        String movieId = foundMovies.get("id").toString()
                                              .split("/")[2];

        // get user review form movieId
        response = Unirest.get("https://imdb8.p.rapidapi.com/title/get-user-reviews?tconst=" + movieId)
                .header("x-rapidapi-host", "imdb8.p.rapidapi.com")
                .header("x-rapidapi-key", "3793fd6a8bmsh3992e80f2f92f34p1ee63ajsn3b0fe00f7804")
                .asJson();
        this.reviewObjs = response.getBody()
                                    .getObject()
                                    .getJSONArray("reviews");
    }

    public void getTrending() throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                                                        "3/trending/movie/week" +
                                                        "?api_key=" + this.API)
                                                 .asJson();

        this.movieObjs = response.getBody()
                                 .getObject()
                                 .getJSONArray("results");
        this.currentMovieIndex = 0;
    }

    public JSONObject getMovie() { return this.movieObjs.getJSONObject(this.currentMovieIndex); }

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

    public SendPhoto displayMovieDetail(String chatID) {
        if (this.movieObjs.isEmpty()) {
            SendPhoto replyMessage = new SendPhoto(chatID, new InputFile("https://kbimages.dreamhosters.com/images/Site_Not_Found_Dreambot.fw.png"));
            replyMessage.setCaption(EmojiParser.parseToUnicode("OPPP!!! Sorry I don't see that movie :cry: :cry: \n Can you please check you movie name again, it maybe wrong :thinking: :thinking:"));
            return replyMessage;
        }

        JSONObject detailMovie = this.movieObjs.getJSONObject(this.currentMovieIndex);
        String movieName = detailMovie.get("original_title").toString();
        String movieYear = "Release date:  " + detailMovie.get("release_date");
        String movieOverview = "Overview:  " + detailMovie.get("overview");
        String movieRating = "Ratting: " + detailMovie.get("vote_average");
        String movieImg = detailMovie.get("poster_path").toString();

        // add trailer and review button
        InlineKeyboardButton trailerBtn = new InlineKeyboardButton("Trailer", null, "get_movieTrailer",
                                                        null, null, null, null, null);
        InlineKeyboardButton watchReviewBtn = new InlineKeyboardButton("Watch reviews", null, "get_movieReview",
                                                            null, null, null, null, null);
        InlineKeyboardButton addToListBtn = new InlineKeyboardButton("Add to list", null, "add_to_list",
                                                          null, null, null, null, null);

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        row1.add(trailerBtn);
        row1.add(watchReviewBtn);
        // check date if it is an upcoming movie
        LocalDate releaseDate = LocalDate.parse((CharSequence) detailMovie.get("release_date"));
        if (LocalDate.now().isBefore(releaseDate))
            row1.add(addToListBtn);

        if (this.currentMovieIndex > 0)
            row2.add(new InlineKeyboardButton("<<", null, "movieList_backward",
                    null, null, null, null, null));
        if (this.currentMovieIndex < this.movieObjs.length())
            row2.add(new InlineKeyboardButton(">>", null, "movieList_forward",
                    null, null, null, null, null));

        btnList.add(row1);
        btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup(btnList);

        InputFile image = new InputFile("https://image.tmdb.org/t/p/original/" + movieImg);
        SendPhoto reply = new SendPhoto(chatID, image);
        reply.setCaption(movieName.toUpperCase() + "\n\n" + movieYear + "\n\n" + movieRating + "\n\n" + movieOverview);
        reply.setReplyMarkup(allBtn);
        return reply;
    }

    public EditMessageMedia displayMovieDetail(String chatID, long messageID) {
        JSONObject detailMovie = this.movieObjs.getJSONObject(this.currentMovieIndex);
        String movieName = detailMovie.get("original_title").toString();
        String movieYear = "Release date:  " + detailMovie.get("release_date");
        String movieOverview = "Overview:  " + detailMovie.get("overview");
        String movieRating = "Ratting: " + detailMovie.get("vote_average");
        String movieImg = detailMovie.get("poster_path").toString();

        // add trailer and review button
        InlineKeyboardButton trailerBtn = new InlineKeyboardButton("Trailer", null, "get_movieTrailer",
                null, null, null, null, null);
        InlineKeyboardButton watchReviewBtn = new InlineKeyboardButton("Watch reviews", null, "get_movieReview",
                null, null, null, null, null);
        InlineKeyboardButton addToListBtn = new InlineKeyboardButton("Add to list", null, "add_to_list",
                null, null, null, null, null);

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        row1.add(trailerBtn);
        row1.add(watchReviewBtn);
        // check date if it is an upcoming movie
        LocalDate releaseDate = LocalDate.parse((CharSequence) detailMovie.get("release_date"));
        if (LocalDate.now().isBefore(releaseDate))
            row1.add(addToListBtn);

        if (this.currentMovieIndex > 0)
            row2.add(new InlineKeyboardButton("<<", null, "movieList_backward",
                    null, null, null, null, null));
        if (this.currentMovieIndex < this.movieObjs.length())
            row2.add(new InlineKeyboardButton(">>", null, "movieList_forward",
                    null, null, null, null, null));

        btnList.add(row1);
        btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();
        allBtn.setKeyboard(btnList);

        InputMediaPhoto nextMovie = new InputMediaPhoto();
        nextMovie.setCaption(movieName.toUpperCase() + "\n\n" + movieYear + "\n\n" + movieRating + "\n\n" + movieOverview);
        nextMovie.setMedia("https://image.tmdb.org/t/p/original/" + movieImg);
        EditMessageMedia reply = new EditMessageMedia(  chatID,
                                                        Math.toIntExact(messageID),
                                                        null,
                                                        nextMovie,
                                                        allBtn);
        return reply;
    }

    public SendMessage displayTrailer(String chatId) throws UnirestException {
        JSONObject detailMovie = this.movieObjs.getJSONObject(this.currentMovieIndex);
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
            SendMessage replyMessage = new SendMessage();
            replyMessage.setChatId(chatId);
            replyMessage.setText("OUCHHH!!! This movie is has no trailer. So mysterious");
            return replyMessage;
        }

        for (int i=0; i < videos.length(); i++) {
            if (videos.getJSONObject(i).get("type").equals("Trailer")) {
                trailerVideo = videos.getJSONObject(i);
                break;
            }
        }

        // send message with video url
        String videoKey = trailerVideo.get("key").toString();
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(chatId);
        replyMessage.setText("https://www.youtube.com/watch?v=" + videoKey);
        return replyMessage;
    }

    public SendMessage displayReview(int index, String chatID) {
        String userPoint = "unknown", userName = "", userReview = "", reviewTitle = "";

        // check whether it has review or not
        if (this.reviewObjs.length() == 0) {
            SendMessage replyMessage = new SendMessage();
            replyMessage.setChatId(chatID);
            replyMessage.setText(EmojiParser.parseToUnicode("No one watches this movie you stupid head  :clown: :clown: !!\n\nGet you ass out of here"));
            return replyMessage;
        }
        JSONObject review = this.reviewObjs.getJSONObject(index);
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
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        if (this.reviewObjs.length() > (this.currentMovieIndex + 1)) {
            InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "movieReview_forward_" + (index+1),
                                                            null, null, null, null, null);
            row1.add(forwardBtn);
        }
        btnList.add(row1);
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
        btnList.add(row1);
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

    public void getUpcoming() throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                                                        "3/movie/upcoming" +
                                                        "?api_key=" + this.API)
                                                .asJson();

        this.movieObjs = response.getBody()
                                 .getObject()
                                 .getJSONArray("results");
        this.currentMovieIndex = 0;
    }
}

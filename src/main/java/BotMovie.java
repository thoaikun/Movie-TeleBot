import com.vdurmont.emoji.EmojiParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class BotMovie {
    private final String API = "16e8d32a627987825706488073388e2e";
    private final String REQUEST_URL = "https://api.themoviedb.org/3";

    private JSONObject movieObjs;
    private JSONObject reviewObjs;

    public BotMovie() {
        this.movieObjs = new JSONObject();
        this.reviewObjs = new JSONObject();
    }

    public void setMovieObjs(JSONObject object) { this.movieObjs = object; }
    public void setReviewObjs(JSONObject object) { this.reviewObjs = object; }
    public JSONObject getMovieObjs() { return this.movieObjs; }
    public JSONObject getReviewObjs() { return this.reviewObjs; }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    public EditMessageText getStart(String chatID, int messageID) {
        // setting up for message
        String textMessage = EmojiParser.parseToUnicode(
        "This is Mozziess :dog: :dog:, one of my bot cousin. He knows a lot of movies. If you want to find something, ask himm\n\n" +
                             "Type /movie + 'your movie you want to find' to ask\n" +
                             "Type /trending to see which movie is hot this weekend");

        // creat a editMessage
        EditMessageText editText = new EditMessageText();
        editText.setChatId(chatID);
        editText.setMessageId(messageID);
        editText.setText(textMessage);
        return editText;
    }

    public JSONObject searchMovie(String movieName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = readJsonFromUrl(this.REQUEST_URL + "/search/movie?api_key=" + this.API + "&query=" + movieName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public SendMessage displaySearchedMovie(int page, String chatID) {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        JSONArray searchedMovies = this.movieObjs.getJSONArray("results");

        // set string to display movie seached name
        String movieNames = "";
        for (int i=page; i < Math.min(searchedMovies.length(), page*5+5); i++) {
            movieNames += i + "/ " + searchedMovies.getJSONObject(i).get("original_title") + "\n";
            row1.add(new InlineKeyboardButton(String.valueOf(i), null, "movie_index_" + String.valueOf(i), null, null, null, null, null));
        }

        // create inline button to select movie ans switch to next page
        InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "movie_forward_" + String.valueOf(page+1), null, null, null, null, null);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        if ((page+1)*5 < searchedMovies.length())
            row2.add(forwardBtn);
        btnList.add(row1); btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();
        allBtn.setKeyboard(btnList);

        // create a edit message
        SendMessage editText = new SendMessage();
        editText.setChatId(chatID);
        editText.setText(movieNames);
        editText.setReplyMarkup(allBtn);
        return editText;
    }

    public EditMessageText displaySearchedMovie(int page, String chatID, int messageID) {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        JSONArray searchedMovies = this.movieObjs.getJSONArray("results");

        // set string to display movie seached name
        String movieNames = "";

        for (int i=page*5; i < Math.min(searchedMovies.length(), page*5 + 5); i++) {
            movieNames += i + "/ " + searchedMovies.getJSONObject(i).get("original_title") + "\n";
            row1.add(new InlineKeyboardButton(String.valueOf(i), null, "movie_index_" + String.valueOf(i), null, null, null, null, null));
        }

        InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "movie_forward_" + (page+1), null, null, null, null, null);
        InlineKeyboardButton backwardBtn = new InlineKeyboardButton("<<", null, "movie_backward_" + (page-1), null, null, null, null, null);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        if ((page-1)*5 >= 0)
            row2.add(backwardBtn);
        if ((page+1)*5 < searchedMovies.length())
            row2.add(forwardBtn);
        btnList.add(row1); btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();
        allBtn.setKeyboard(btnList);

        EditMessageText editText = new EditMessageText();
        editText.setChatId(chatID);
        editText.setMessageId(messageID);
        editText.setText(movieNames);
        editText.setReplyMarkup(allBtn);
        return editText;
    }

    public SendMessage[] displayMovieDetail(int index, String chatID) {
        JSONObject detailMovie = this.movieObjs.getJSONArray("results").getJSONObject(index);
        String movieName = detailMovie.get("original_title").toString();
        String movieYear = "Release date:  " + detailMovie.get("release_date");
        String movieOverview = "Overview:  " + detailMovie.get("overview");
        String movieRating = "Ratting: " + detailMovie.get("vote_average");
        String movieImg = detailMovie.get("poster_path").toString();

        // send movie image link
        SendMessage img = new SendMessage();
        img.setChatId(chatID);
        img.setText("https://image.tmdb.org/t/p/original/" + movieImg);

        // add trailer and review button
        InlineKeyboardButton trailerBtn = new InlineKeyboardButton("Trailer", null, "get trailer " + String.valueOf(index), null, null, null, null, null);
        InlineKeyboardButton watchReviewBtn = new InlineKeyboardButton("Watch reviews", null, "get review " + String.valueOf(index), null, null, null, null, null);
        InlineKeyboardButton addReviewBtn = new InlineKeyboardButton("Add review", null, "set review " + String.valueOf(index), null, null, null, null, null);
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        row1.add(trailerBtn); row1.add(watchReviewBtn); row1.add(addReviewBtn);
        btnList.add(row1);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();
        allBtn.setKeyboard(btnList);

        // send infomation
        SendMessage info = new SendMessage();
        info.setChatId(chatID);
        info.setText(movieName.toUpperCase() + "\n\n" + movieYear + "\n\n" + movieRating + "\n\n" + movieOverview);
        info.setReplyMarkup(allBtn);

        SendMessage[] reply = new SendMessage[2];
        reply[0] = img;
        reply[1] = info;

        return reply;
    }

    public SendMessage displayTrailer(int index, String chatId) {
        JSONObject detailMovie = this.movieObjs.getJSONArray("results").getJSONObject(index);
        String moiveId = detailMovie.get("id").toString();

        // read json file to get an array obj of videos
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = readJsonFromUrl("https://api.themoviedb.org/3/movie/" + moiveId + "/videos?api_key=16e8d32a627987825706488073388e2e");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // take json object with have video type is Trailer
        JSONObject trailerVideo = new JSONObject();
        JSONArray objects = jsonObject.getJSONArray("results");

        // check weather movie has trailer or not
        if (objects.isEmpty()) {
            SendMessage replyMessage = new SendMessage();
            replyMessage.setChatId(chatId);
            replyMessage.setText("OUCHHH!!! This movie is has no trailer. So mysterious");
            return replyMessage;
        }

        for (int i=0; i < objects.length(); i++) {
            if (objects.getJSONObject(i).get("type").equals("Trailer")) {
                trailerVideo = objects.getJSONObject(i);
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
        JSONArray reviews = this.movieObjs.getJSONArray("results");

        // check whether it has review or not
        if (reviews.length() == 0) {
            SendMessage replyMessage = new SendMessage();
            replyMessage.setChatId(chatID);
            replyMessage.setText(EmojiParser.parseToUnicode("No one watches this movie you stupid head  :clown: :clown: !!\n\nGet you ass out of here"));
            return replyMessage;
        }

        JSONObject review = reviews.getJSONObject(index);
        String userName = review.get("author").toString();
        String userReview = review.get("content").toString();

        // create button to switch to next review
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "review_forward_" + String.valueOf(index+1), null, null, null, null, null);
        row1.add(forwardBtn);
        btnList.add(row1);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();
        allBtn.setKeyboard(btnList);

        // return a message
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(chatID);
        replyMessage.setText(EmojiParser.parseToUnicode(":bust_in_silhouette: Account: " + userName + "\n\n" + "\t:speaking_head_in_silhouette: Review: \n\n" + userReview));
        replyMessage.setReplyMarkup(allBtn);
        return replyMessage;
    }

    public EditMessageText displayReview(int index, String chatID, int messageID, boolean isBackward) {
        JSONArray reviews = this.movieObjs.getJSONArray("results");
        JSONObject review = reviews.getJSONObject(index);
        int length = review.get("content").toString().length();
        if (length > 4000) {
            if (!isBackward)
                review = reviews.getJSONObject(++index);
            else
                review = reviews.getJSONObject(--index);
        }
        String userName = review.get("author").toString();
        String userReview = review.get("content").toString();


        // create button to switch to next review
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "review_forward_" + String.valueOf(index+1), null, null, null, null, null);
        InlineKeyboardButton backwardBtn = new InlineKeyboardButton("<<", null, "review_backward_" + String.valueOf(index-1), null, null, null, null, null);
        if (index-1 >= 0)
            row1.add(backwardBtn);
        if (index+1 < reviews.length())
            row1.add(forwardBtn);
        btnList.add(row1);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();
        allBtn.setKeyboard(btnList);

        // return a edit message
        EditMessageText editText = new EditMessageText();
        editText.setChatId(chatID);
        editText.setMessageId(messageID);
        editText.setText(EmojiParser.parseToUnicode(":bust_in_silhouette: Account: " + userName + "\n\n" + "\t:speaking_head_in_silhouette: Review: \n\n" + userReview));
        editText.setReplyMarkup(allBtn);
        return editText;
    }

    public JSONObject getUserReview(int index) {
        JSONObject detailMovie = this.movieObjs.getJSONArray("results").getJSONObject(index);
        String moiveId = detailMovie.get("id").toString();

        // read json file to get an array obj of review
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = readJsonFromUrl(this.REQUEST_URL + "/movie/" + moiveId + "/reviews?api_key=" + this.API);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public SendMessage displayTrendingMovie(int page, String chatID) {
        JSONArray trendingMovies = this.movieObjs.getJSONArray("results");
        String moviesName = "";
        List<InlineKeyboardButton> row1 = new ArrayList<>();

        for (int i=0; i < 5; i++) {
            JSONObject movie = trendingMovies.getJSONObject(i);
            moviesName += String.valueOf(i) + "/ " + movie.get("original_title") + "\n";
            row1.add(new InlineKeyboardButton(String.valueOf(i), null, "movie_index_" + String.valueOf(i), null, null, null, null, null));
        }

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "trending_forward_" + String.valueOf(page+1), null, null, null, null, null);
        row2.add(forwardBtn);
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        btnList.add(row1);
        btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup(btnList);

        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(chatID);
        replyMessage.setText("Trending movie of the week: \n" + moviesName);
        replyMessage.setReplyMarkup(allBtn);
        return replyMessage;
    }

    public EditMessageText displayTrendingMovie(int page, String chatID, int messageID) {
        JSONArray trendingMovies = this.movieObjs.getJSONArray("results");
        String moviesName = "";
        List<InlineKeyboardButton> row1 = new ArrayList<>();

        for (int i=page*5; i < page*5+5; i++) {
            JSONObject movie = trendingMovies.getJSONObject(i);
            moviesName += String.valueOf(i) + "/ " + movie.get("original_title") + "\n";
            row1.add(new InlineKeyboardButton(String.valueOf(i), null, "movie_index_" + String.valueOf(i), null, null, null, null, null));
        }

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton backwardBtn = new InlineKeyboardButton("<<", null, "trending_backward_" + String.valueOf(page-1), null, null, null, null, null);
        InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "trending_forward_" + String.valueOf(page+1), null, null, null, null, null);
        if (page > 0)
            row2.add(backwardBtn);
        if (page < 3)
            row2.add(forwardBtn);
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        btnList.add(row1);
        btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup(btnList);

        EditMessageText editText = new EditMessageText();
        editText.setChatId(chatID);
        editText.setMessageId(messageID);
        editText.setText("Trending movie of the week: \n" + moviesName);
        editText.setReplyMarkup(allBtn);
        return editText;
    }

    public JSONObject getTrending() {
        // read json file to get an array obj of review
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = readJsonFromUrl(this.REQUEST_URL + "/trending/movie/week?api_key=" + this.API);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}

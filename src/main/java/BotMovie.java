import com.vdurmont.emoji.EmojiParser;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbSearch;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.Video;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class BotMovie {
    private final TmdbApi tmdbApi = new TmdbApi("16e8d32a627987825706488073388e2e");
    private TmdbSearch theSeacher = new TmdbSearch(this.tmdbApi);

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
                             "Type /movie + 'your movie you want to find' to ask");

        // creat a editMessage
        EditMessageText editText = new EditMessageText();
        editText.setChatId(chatID);
        editText.setMessageId(messageID);
        editText.setText(textMessage);
        return editText;
    }

    public MovieResultsPage searchMovie(String movieName) {
       return this.theSeacher.searchMovie(movieName, null, null, false, null);
    }

    public SendMessage displaySearchedMovie(MovieResultsPage movieDbs, int page, String chatID) {
        List<InlineKeyboardButton> row1 = new ArrayList<>();

        // set string to display movie seached name
        String movieNames = "";
        List<MovieDb> movieDbList = movieDbs.getResults();
        for (int i=page; i < Math.min(movieDbList.size(), page*5+5); i++) {
            movieNames += i + "/ " + movieDbList.get(i).toString() + "\n";
            row1.add(new InlineKeyboardButton(String.valueOf(i), null, "movie index " + String.valueOf(i), null, null, null, null, null));
        }

        // create inline button to select movie ans switch to next page
        InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "movie_next-page_" + String.valueOf(page+1), null, null, null, null, null);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        if ((page+1)*5 < movieDbs.getResults().size())
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

    public EditMessageText displaySearchedMovie(MovieResultsPage movieDbs, int page, String chatID, int messageID) {
        List<InlineKeyboardButton> row1 = new ArrayList<>();

        // set string to display movie seached name
        String movieNames = "";
        List<MovieDb> movieDbList = movieDbs.getResults();

        for (int i=page*5; i < Math.min(movieDbList.size(), page*5 + 5); i++) {
            movieNames += i + "/ " + movieDbList.get(i).toString() + "\n";
            row1.add(new InlineKeyboardButton(String.valueOf(i), null, "movie index " + String.valueOf(i), null, null, null, null, null));
        }

        InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "movie_next-page_" + (page+1), null, null, null, null, null);
        InlineKeyboardButton backwardBtn = new InlineKeyboardButton("<<", null, "movie_previous-page_" + (page-1), null, null, null, null, null);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        if ((page-1)*5 >= 0)
            row2.add(backwardBtn);
        if ((page+1)*5 < movieDbs.getResults().size())
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

    public SendMessage[] displayMovieDetail(MovieResultsPage movieDbs, int index, String chatID) {
        MovieDb detailMovie = movieDbs.getResults().get(index);
        String movieName = detailMovie.getTitle();
        String movieYear = "Release date:  " + detailMovie.getReleaseDate();
        String movieOverview = "Overview:  " + detailMovie.getOverview();
        String movieRating = "Ratting: " + detailMovie.getVoteAverage();
        String movieImg = detailMovie.getPosterPath();

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

    public SendMessage displayTrailer(MovieResultsPage movieDbs, int index, String chatId) {
        MovieDb detailMovie = movieDbs.getResults().get(index);
        int moiveId = detailMovie.getId();

        // read json file to get an array obj of videos
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = readJsonFromUrl("https://api.themoviedb.org/3/movie/" + String.valueOf(moiveId) + "/videos?api_key=16e8d32a627987825706488073388e2e");
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

    public SendMessage displayReview(JSONObject object, int index, String chatID) {
        JSONArray reviews = object.getJSONArray("results");

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

    public EditMessageText displayReview(JSONObject object, int index, String chatID, int messageID, boolean isBackward) {
        JSONArray reviews = object.getJSONArray("results");
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

    public JSONObject getUserReview(MovieResultsPage movieDbs, int index) {
        MovieDb detailMovie = movieDbs.getResults().get(index);
        int moiveId = detailMovie.getId();

        // read json file to get an array obj of review
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = readJsonFromUrl("https://api.themoviedb.org/3/movie/" + String.valueOf(moiveId) + "/reviews?api_key=16e8d32a627987825706488073388e2e");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

}

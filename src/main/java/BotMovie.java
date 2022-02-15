import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class BotMovie {
    private final String API = "16e8d32a627987825706488073388e2e";
    private final String REQUEST_URL = "https://api.themoviedb.org/3";

    private JSONArray movieObjs;
    private JSONArray reviewObjs;

    public BotMovie() {
        this.movieObjs = new JSONArray();
        this.reviewObjs = new JSONArray();
    }

    public JSONArray getMovieObjs() { return this.movieObjs; }
    public JSONArray getReviewObjs() { return this.reviewObjs; }

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

    public void searchMovie(String movieName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = readJsonFromUrl(this.REQUEST_URL + "/search/movie?api_key=" + this.API + "&query=" + movieName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        this.movieObjs = jsonObject.getJSONArray("results");
    }

    public void getUserReview(int index) throws Exception {
        /*
            Because MovieDb has small number of review so we will use RapidAPI to get Review from IMDB
            So first we need to get movie id on rapidAPI then get it review
         */
        String movieName = this.movieObjs.getJSONObject(index).get("original_title").toString();
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

    public void getTrending() {
        // read json file to get an array obj of review
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = readJsonFromUrl(this.REQUEST_URL + "/trending/movie/week?api_key=" + this.API);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        this.movieObjs =  jsonObject.getJSONArray("results");
    }

    public JSONObject getMovie(int index) { return this.movieObjs.getJSONObject(index); }

    public EditMessageText getStart(String chatID, int messageID) {
        // setting up for message
        String textMessage = EmojiParser.parseToUnicode(
                "This is Mozziess :dog: :dog:, one of my bot cousin. He knows a lot of movies. If you want to find something, ask himm\n\n" +
                        "Type /movie + 'your movie you want to find' to ask\n" +
                        "Type /trending_movie to see which movie is hot this weekend");

        // creat a editMessage
        EditMessageText editText = new EditMessageText();
        editText.setChatId(chatID);
        editText.setMessageId(messageID);
        editText.setText(textMessage);
        return editText;
    }

    public SendMessage displayMovieList(int page, String chatID) {
        List<InlineKeyboardButton> row1 = new ArrayList<>();

        // set string to display movie seached name
        String movieNames = "";
        for (int i=page; i < Math.min(this.movieObjs.length(), page*5+5); i++) {
            movieNames += i + "/ " + this.movieObjs.getJSONObject(i).get("original_title") + "\n";
            row1.add(new InlineKeyboardButton(String.valueOf(i), null, "movieList_index_" + i,
                                                         null, null, null, null, null));
        }

        // create inline button to select movie ans switch to next page
        InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "movieList_forward_" + (page+1),
                                                        null, null, null, null, null);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        if ((page+1)*5 < this.movieObjs.length())
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

    public EditMessageText displayMovieList(int page, String chatID, int messageID) {
        List<InlineKeyboardButton> row1 = new ArrayList<>();

        // set string to display movie seached name
        String movieNames = "";

        for (int i=page*5; i < Math.min(this.movieObjs.length(), page*5 + 5); i++) {
            movieNames += i + "/ " + this.movieObjs.getJSONObject(i).get("original_title") + "\n";
            row1.add(new InlineKeyboardButton(String.valueOf(i), null, "movieList_index_" + i,
                                                        null, null, null, null, null));
        }

        InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "movieList_forward_" + (page+1),
                                                        null, null, null, null, null);
        InlineKeyboardButton backwardBtn = new InlineKeyboardButton("<<", null, "movieList_backward_" + (page-1),
                                                         null, null, null, null, null);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        if ((page-1)*5 >= 0)
            row2.add(backwardBtn);
        if ((page+1)*5 < this.movieObjs.length())
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
        JSONObject detailMovie = this.movieObjs.getJSONObject(index);
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
        InlineKeyboardButton trailerBtn = new InlineKeyboardButton("Trailer", null, "get_movieTrailer_" + index,
                                                        null, null, null, null, null);
        InlineKeyboardButton watchReviewBtn = new InlineKeyboardButton("Watch reviews", null, "get_movieReview_" + index,
                                                            null, null, null, null, null);
        InlineKeyboardButton addToListBtn = new InlineKeyboardButton("Add to list", null, "add_to_list_" + index,
                                                          null, null, null, null, null);

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        row1.add(trailerBtn);
        row1.add(watchReviewBtn);
        // check date if it is an upcoming movie
        LocalDate releaseDate = LocalDate.parse((CharSequence) detailMovie.get("release_date"));
        if (LocalDate.now().isBefore(releaseDate))
            row1.add(addToListBtn);

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
        JSONObject detailMovie = this.movieObjs.getJSONObject(index);
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
        if (this.reviewObjs.length() > (index + 1)) {
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

    public JSONObject getUpcoming() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = readJsonFromUrl(this.REQUEST_URL + "/movie/upcoming?api_key=" + this.API);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}

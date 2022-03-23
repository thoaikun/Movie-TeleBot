import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class BotTVShow {
        private final static String API = "5c54b7e4adc51f25bb50d1136235c1ff";
    private JSONArray TVShowObject ;
    private JSONArray TVShowReview ;
    public int code ;
    public Object previousMessage;
    //constructor
    public BotTVShow(){
       this.TVShowObject = new JSONArray() ;
       this.TVShowReview = new JSONArray() ;
    }

    public JSONArray getTVShowObject(){
        return this.TVShowObject ;
    }

    public JSONArray getTVShowReview() {
        return this.TVShowReview ;
    }

    public void searchSeason(String nameSeason) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                                                        "3/search/tv" +
                                                        "?api_key=" + API +
                                                        "&query=" + nameSeason)
                                                         .asJson();
        //get result
        this.TVShowObject = response.getBody().getObject().getJSONArray("results");
    }

    public void seachByName(String nameTVShow) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(  "https://api.themoviedb.org/" +
                                                        "3/search/tv" +
                                                        "?api_key=" + API +
                                                        "&query=" + nameTVShow)
                                                        .asJson();

        this.TVShowObject = response.getBody()
                .getObject()
                .getJSONArray("results");
    }

    public void SearchTrendingTVShows() throws UnirestException {
        HttpResponse<JsonNode> response    = Unirest.get(   "https://api.themoviedb.org/" +
                                                            "3/trending/tv/week" +
                                                            "?api_key=" + API).asJson();
        this.TVShowObject = response.getBody().getObject().getJSONArray("results");
    }

    public JSONObject getTvShow(int index) {
        return this.TVShowObject.getJSONObject(index);
    }

    private String getTrailer(int index) throws UnirestException {
        JSONObject allTV = this.TVShowObject.getJSONObject(index) ;
        String tvID = allTV.get("id").toString() ;
        //search detail TV show
        HttpResponse<JsonNode> DetailOfShow = Unirest.get("https://api.themoviedb.org/3/tv/" +
                                                          tvID +"/videos" + "?api_key=" + API ).asJson() ;
        JSONObject trailerVideo = new JSONObject();
        JSONArray result = DetailOfShow.getBody()
                                       .getObject()
                                       .getJSONArray("results") ;

        if(result.isEmpty()) {
            return "No trailer found" ;
        }
        for (int i=0; i < result.length(); i++) {
            if (result.getJSONObject(i).get("type").equals("Trailer")) {
                trailerVideo = result.getJSONObject(i);
                break;
            }
        }
        if (trailerVideo.has("key"))
            return trailerVideo.get("key").toString();
        return "No trailer found";
    }

    public SendMessage getStart(String chatID, int messageID){
        String message = EmojiParser.parseToUnicode(
                "You want to search some thing about TV Shows? Please enter :\n" +
                        "Type /TVshowSeason + 'your season TV Show you want to find'\n"+
                        "Type /TVshowName + 'your TV Show Name you want to find'\n"+
                        "Type /trending_TVshow to see which TV Show is hot in this weekend\n"
        );
        SendMessage messageStart = new SendMessage();
        messageStart.setChatId(chatID);
        messageStart.setText(message);
        return messageStart ;
    }

    public SendMessage ResultList(int index , String chatID){
       if (this.TVShowObject.isEmpty()) {
           SendMessage replyMessage = new SendMessage();
           replyMessage.setChatId(chatID);
           replyMessage.setText(EmojiParser.parseToUnicode("Sorry I can't find this TvShows. Can you check your name or season of this TV show ? "));
           return replyMessage;
       }
       String text = "";
       List<InlineKeyboardButton> row1 = new ArrayList<>();
       List<InlineKeyboardButton> row2 = new ArrayList<>();

       for (int i=5*index; i < (5*index + 5); i++) {
           JSONObject Show = this.TVShowObject.getJSONObject(i);
           String nameShow = Show.get("name").toString();

           text += i + "/ " + nameShow + "\n";
           row1.add(new InlineKeyboardButton(String.valueOf(i), null, "TVshowIndex_" + i,
                   null, null, null, null, null));
       }

       if ((index+1)*5 < this.TVShowObject.length() - 1)
           row2.add(new InlineKeyboardButton(">>", null, "TVshow_forward_" + (index + 1),
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

    public EditMessageText OtherResult(int index , String chatID , long messageID){
        String text = "";
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        for (int i=5*index; i < (5*index + 5); i++) {
            JSONObject Show = this.TVShowObject.getJSONObject(i);
            String nameShow = Show.get("original_title").toString();

            text += i + "/ " + nameShow + "\n";
            row1.add(new InlineKeyboardButton(String.valueOf(i), null, "TVshowIndex_" + i,
                    null, null, null, null, null));
        }

        if ((index-1)*5 >= 0)
            row2.add(new InlineKeyboardButton("<<", null, "TVshow_backward_" + (index - 1),
                    null, null, null, null, null));
        if ((index+1)*5 < this.TVShowObject.length() - 1)
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
}

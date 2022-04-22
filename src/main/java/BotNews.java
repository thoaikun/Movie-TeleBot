import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import com.mashape.unirest.http.exceptions.UnirestException;

import com.vdurmont.emoji.EmojiParser;
import org.json.JSONArray;
import org.json.JSONObject;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.*;

public class BotNews {
    private String APIkey = "8d4ad00739mshb31c1b6a520f47dp1b103cjsn2fa6a12fc8bd";
    private JSONArray hotNews = new JSONArray();
    private Map<Long, JSONArray> searchNewsTable = new HashMap<Long, JSONArray>();
    private Map<Long, Integer> hotNewsPageTable = new HashMap<Long, Integer>();
    private Map<Long, Integer> searchNewsPageTable = new HashMap<Long, Integer>();
    private Map<Long, Integer> numSearchNewsTable = new HashMap<Long, Integer>();
    private int numHotNews = 0;
    public static final int maxNumHotNews = 40;
    public static final int maxNumSearchNews = 20;

    public SendMessage introMessage(String chatID) {
        // setting up for message
        String textMessage = "Where are you all the time! Hurry up\n" +
                "Let catch up with the SHOWBIZ\n" +
                "Type /hot_news to read the hotest right now\n" +
                "Type /search_news + any thing you wanna know to search for news";

        // creat a editMessage
        SendMessage message = new SendMessage();
        message.setChatId(chatID);
        message.setText(textMessage);
        return message;
    }

    public void getHotNews() throws UnirestException {
    	HttpResponse<JsonNode> response = Unirest.get("https://bing-news-search1.p.rapidapi.com/news?"
                        + "count=" + BotNews.maxNumHotNews
                        + "&category=Entertainment_MovieAndTV&mkt=en-US&safeSearch=Off&textFormat=Raw")
    			.header("X-BingApis-SDK", "true")
    			.header("X-RapidAPI-Host", "bing-news-search1.p.rapidapi.com")
    			.header("X-RapidAPI-Key", this.APIkey)
    			.asJson();
        this.hotNews = response.getBody().getObject().getJSONArray("value");
        this.numHotNews = this.hotNews.length();

        for (Long x : hotNewsPageTable.keySet()) {
            this.updatePageTable(x.toString(), this.movePageHotNews(x.toString(), 0), true);
        }
    }

    public int searchNews(String chatID, String key) throws UnirestException, UnsupportedEncodingException {
        HttpResponse<JsonNode> response = Unirest.get("https://bing-news-search1.p.rapidapi.com/news/search?q="
                        + URLEncoder.encode(key, StandardCharsets.UTF_8)
                        + "&count=" + BotNews.maxNumSearchNews
                        + "&mkt=en-US&freshness=Day&textFormat=Raw&safeSearch=Off")
                .header("X-BingApis-SDK", "true")
                .header("X-RapidAPI-Host", "bing-news-search1.p.rapidapi.com")
                .header("X-RapidAPI-Key", "8d4ad00739mshb31c1b6a520f47dp1b103cjsn2fa6a12fc8bd")
                .asJson();

        if (this.searchNewsTable.containsKey(Long.parseLong(chatID)))
            this.searchNewsTable.replace(Long.parseLong(chatID), response.getBody().getObject().getJSONArray("value"));
        else
            this.searchNewsTable.put(Long.parseLong(chatID), response.getBody().getObject().getJSONArray("value"));

        int n = this.searchNewsTable.get(Long.parseLong(chatID)).length();
        this.updateSearchResult(chatID, n);
        return n;
    }

    public static InlineKeyboardMarkup displayBtn(String link, boolean isHotNews) {
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        row1.add(new InlineKeyboardButton("READ MORE ...",
                link, null, null, null,
                null, null, null));
        row2.add(new InlineKeyboardButton("<<", null, "previous_" + (isHotNews?"hot_news":"search_news"),
                null, null, null, null, null));
        row2.add(new InlineKeyboardButton(">>", null, "next_" + (isHotNews?"hot_news":"search_news"),
                null, null, null, null, null));

        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        btnList.add(row1);
        btnList.add(row2);
        return new InlineKeyboardMarkup(btnList);
    }

    public SendPhoto displayNews(String chatID, int page, boolean isHotNews) {
        this.updatePageTable(chatID, page, isHotNews);

        JSONObject news = isHotNews
                ?this.hotNews.getJSONObject(page)
                :this.searchNewsTable.get(Long.parseLong(chatID)).getJSONObject(page);

        InputFile img = new InputFile("https://img.freepik.com/free-vector/lightbulb-with-liquid-inside-steps-creativity-concept-get-ideas_180264-11.jpg?w=2000");
        if (news.toMap().containsKey("image")) {
            img.setMedia(news.getJSONObject("image").getJSONObject("thumbnail").getString("contentUrl"));
        }

        String textMessage = news.getString("name").toUpperCase(Locale.ROOT)
                + "\n\n" + news.getString("description")
                + "\n\n" + news.getString("datePublished").substring(0, 10)
                + "\nBy: " + news.getJSONArray("provider").getJSONObject(0).getString("name");

        InlineKeyboardMarkup btn = BotNews.displayBtn(news.getString("url"), isHotNews);

        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatID);
        photo.setPhoto(img);
        photo.setCaption(textMessage);
        photo.setReplyMarkup(btn);
        return photo;
    }

    public EditMessageMedia displayNews(String chatID, int messageID, int page, boolean isHotNews) {
        this.updatePageTable(chatID, page, isHotNews);

        JSONObject news = isHotNews
                ?this.hotNews.getJSONObject(page)
                :this.searchNewsTable.get(Long.parseLong(chatID)).getJSONObject(page);

        InputMediaPhoto img = new InputMediaPhoto("https://img.freepik.com/free-vector/lightbulb-with-liquid-inside-steps-creativity-concept-get-ideas_180264-11.jpg?w=2000");
        if (news.toMap().containsKey("image")) {
            img.setMedia(news.getJSONObject("image").getJSONObject("thumbnail").getString("contentUrl"));
        }

        String textMessage = news.getString("name").toUpperCase(Locale.ROOT)
                + "\n\n" + news.getString("description")
                + "\n\n" + news.getString("datePublished").substring(0, 10)
                + "\nBy: " + news.getJSONArray("provider").getJSONObject(0).getString("name");

        img.setCaption(textMessage);

        InlineKeyboardMarkup btn = BotNews.displayBtn(news.getString("url"), isHotNews);

        EditMessageMedia updateMessage = new EditMessageMedia();
        updateMessage.setChatId(chatID);
        updateMessage.setMessageId(messageID);
        updateMessage.setMedia(img);
        updateMessage.setReplyMarkup(btn);
        return updateMessage;
    }

    public int movePageHotNews(String chatID, int step) {
        int desPage = this.hotNewsPageTable.get(Long.parseLong(chatID)) + step;
        if (desPage >= this.numHotNews)
            return desPage%this.numHotNews;
        else if (desPage < 0)
            return this.numHotNews - (-desPage)%this.numHotNews;
        else return desPage;
    }

    public int movePageSearchNews(String chatID, int step) {
        int desPage = this.searchNewsPageTable.get(Long.parseLong(chatID)) + step;
        if (desPage >= this.numSearchNewsTable.get(Long.parseLong(chatID)))
            return desPage%this.numSearchNewsTable.get(Long.parseLong(chatID));
        else if (desPage < 0)
            return this.numSearchNewsTable.get(Long.parseLong(chatID)) - (-desPage)%BotNews.this.numSearchNewsTable.get(Long.parseLong(chatID));
        else return desPage;
    }

    public void updatePageTable(String chatID, int page, boolean isHotNews) {
        if (isHotNews) {
            if (this.hotNewsPageTable.containsKey(Long.parseLong(chatID)))
                this.hotNewsPageTable.replace(Long.parseLong(chatID), page);
            else
                this.hotNewsPageTable.put(Long.parseLong(chatID), page);
        }
        else {
            if (this.searchNewsPageTable.containsKey(Long.parseLong(chatID)))
                this.searchNewsPageTable.replace(Long.parseLong(chatID), page);
            else
                this.searchNewsPageTable.put(Long.parseLong(chatID), page);
        }
    }

    public void updateSearchResult(String chatID, int n) {
        if (this.numSearchNewsTable.containsKey(Long.parseLong(chatID)))
            this.numSearchNewsTable.replace(Long.parseLong(chatID), n);
        else
            this.numSearchNewsTable.put(Long.parseLong(chatID), n);
    }
}
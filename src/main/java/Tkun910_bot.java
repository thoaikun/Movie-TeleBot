import Controller.MovieController;
import Controller.ShowController;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.vdurmont.emoji.EmojiParser;

import java.util.*;
import Objects.UpComing;

public class Tkun910_bot extends TelegramLongPollingBot {
    private MovieController movieController;
    private ShowController showController;
    private  BotWaiting botWaiting;
    private  BotNews botNews;

    public Tkun910_bot() {
        this.movieController = new MovieController();
        this.showController = new ShowController();
        this.botWaiting = new BotWaiting();
        this.botNews = new BotNews();
    }

    @Override
    public String getBotUsername() {
        return "MovieBot";
    }

    @Override
    public String getBotToken() {
        return "5065559354:AAH1_fHycshH3GmcGI8ndzhgJ2BHS26x9gQ";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String receiveMessage = update.getMessage().getText();
            String chatID = update.getMessage().getChatId().toString();

            // check receive message
            if (receiveMessage.equals("/start")) {
                SendMessage replyMessage = welcomeMessage(update.getMessage().getChatId().toString());
                try {
                    execute(replyMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else if (receiveMessage.contains("movie")) {
                try {
                    callBotMovie(receiveMessage, chatID, 0);
                } catch (Exception e) {
                    try {
                        execute(new SendMessage(chatID, "Something wrong happened, please try again"));
                        execute(this.movieController.returnToList(chatID));
                    } catch (TelegramApiException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else if (receiveMessage.contains("TVshow")){
                try {
                    callBotTV(receiveMessage , chatID , 0) ;
                }
                catch (Exception e){
                    try {
                        execute(new SendMessage(chatID, "Something wrong happened, please try again"));
                        execute(this.showController.returnToList(chatID));
                    } catch (TelegramApiException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else if (receiveMessage.equals("/news")
                    || receiveMessage.equals("/hot_news")
                    || receiveMessage.startsWith("/search_news")){
                try {
                    callBotNews(receiveMessage , chatID , 0);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if (receiveMessage.equals("/mylist")) {
                try {
                    callBotWaiting(receiveMessage, chatID, 0);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                try {
                    execute(new SendMessage(chatID, "OPP! Wrong comment, please check again !!"));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (update.hasCallbackQuery()) {
            String receiveMessage = update.getCallbackQuery().getData();
            String chatID = update.getCallbackQuery().getMessage().getChatId().toString();
            long messageID = update.getCallbackQuery().getMessage().getMessageId();

            if (receiveMessage.contains("movie")) {
                try {
                    callBotMovie(receiveMessage, chatID, messageID);
                } catch (Exception e) {
                    try {
                        execute(new SendMessage(chatID, "Something wrong happened, please try again"));
                        execute(this.movieController.returnToList(chatID));
                    } catch (TelegramApiException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else if (receiveMessage.contains("TVshow")) {
                try {
                    callBotTV(receiveMessage, chatID, messageID);
                } catch (Exception e) {
                    try {
                        execute(new SendMessage(chatID, "Something wrong happened, please try again"));
                        execute(this.showController.returnToList(chatID));
                    } catch (TelegramApiException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else if (receiveMessage.contains("news")) {
                try {
                    callBotNews(receiveMessage, chatID, (int)messageID);
                } catch (Exception e) {
                    try {
                        execute(new SendMessage(chatID, "Something wrong happened, please try again"));
                    } catch (TelegramApiException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else if (receiveMessage.contains("myList")) {
                try {
                    callBotWaiting(receiveMessage, chatID, messageID);
                } catch (Exception e) {
                    try {
                        execute(new SendMessage(chatID, "Something wrong happened, please try again"));
                    } catch (TelegramApiException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private SendMessage welcomeMessage(String chatID) {
        // create InlineButton
        InlineKeyboardButton movieBtn = new InlineKeyboardButton("Movies", null, "get_movie", null, null, null, null, null);
        InlineKeyboardButton TvshowBtn = new InlineKeyboardButton("TVShows", null, "get_TVshow", null, null, null, null, null);
        InlineKeyboardButton newsBtn = new InlineKeyboardButton("News", null, "get_news", null, null, null, null, null);
        InlineKeyboardButton upcommingBtn = new InlineKeyboardButton("My List", null, "get_myList", null, null, null, null, null);

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        row1.add(movieBtn);
        row1.add(TvshowBtn);
        row2.add(newsBtn);
        row2.add(upcommingBtn);
        btnList.add(row1);
        btnList.add(row2);

        // create a reply welcoming chat
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();
        allBtn.setKeyboard(btnList);
        String welcome = EmojiParser.parseToUnicode("Hello :wave: \n\nI'm Movie, an bot that know every thing about movies :sunglasses: \n\nWhat !! Don't believe me :upside_down: :upside_down: How dare you??? :rage: :rage: \nAsk me anything you want");
        SendMessage message = new SendMessage();
        message.setChatId(chatID);
        message.setText(welcome);
        message.setReplyMarkup(allBtn);

        return message;
    }

    private void callBotMovie(String receiveMessage, String chatID, long messageID) throws Exception {
        if (receiveMessage.equals("get_movie"))
            execute(this.movieController.sendWelcome(chatID));
        else if (receiveMessage.equals("/trending_movie"))
            execute(this.movieController.sendTrendingList(chatID));
        else if (receiveMessage.equals("/upcoming_movie"))
            execute(this.movieController.sendUpcomingList(chatID));
        else if (receiveMessage.contains("movieList_forward_") || receiveMessage.contains("movieList_backward_"))
            execute(this.movieController.sendEditList(receiveMessage, chatID, messageID));
        else if (receiveMessage.contains("movieIndex_")) {
            execute(this.movieController.deleteMessage(chatID, messageID));
            execute(this.movieController.sendDetail(receiveMessage, chatID));
        }
        else if (receiveMessage.contains("movieList_return")) {
            execute(this.movieController.deleteMessage(chatID, messageID));
            execute(this.movieController.returnToList(chatID));
        }
        else if (receiveMessage.contains("get_movieReview"))
            execute(this.movieController.sendReviews(chatID));
        else if (receiveMessage.contains("movieReview_forward_") || receiveMessage.contains("movieReview_backward_"))
            execute(this.movieController.sendReviews(receiveMessage, chatID, messageID));
        else if (receiveMessage.equals("delete_movieReview"))
            execute(this.movieController.deleteMessage(chatID, messageID));
        else if (receiveMessage.contains("/movie"))
            execute(this.movieController.sendSearchList(receiveMessage, chatID));
    }

    private void callBotTV(String receiveMessage , String chatID , long messageID) throws Exception{
        if (receiveMessage.equals("get_TVshow"))
            execute(this.showController.sendWelcome(chatID));
        if (receiveMessage.contains("/TVshowOnTheAir"))
            execute(this.showController.sendOnAirList(chatID));
        else if (receiveMessage.contains("/trending_TVshow"))
            execute(this.showController.sendTrendingList(chatID));
        else if (receiveMessage.contains("TVshow_forward_") || receiveMessage.contains("TVshow_backward_"))
            execute(this.showController.sendEditList(receiveMessage, chatID, messageID));
        else if (receiveMessage.contains("TVshowIndex_")) {
            execute(this.showController.deleteMessage(chatID, messageID));
            execute(this.showController.sendDetail(receiveMessage, chatID));
        }
        else if (receiveMessage.contains("TVshowList_return")) {
            execute(this.showController.deleteMessage(chatID, messageID));
            execute(this.showController.returnToList(chatID));
        }
        else if (receiveMessage.contains("TVshow_get_user_review"))
            execute(this.showController.sendReviews(chatID));
        else if (receiveMessage.contains("TVshowReview_forward_") || receiveMessage.contains("TVshowReview_backward_"))
            execute(this.showController.sendReviews(receiveMessage, chatID, messageID));
        else if (receiveMessage.equals("delete_TVshowReview"))
            execute(this.showController.deleteMessage(chatID, messageID));
        else if (receiveMessage.contains("/TVshow"))
            execute(this.showController.sendSearchList(receiveMessage, chatID));
    }

    public void callBotNews(String receiveMessage, String chatID, int messageID) throws Exception {
        if (receiveMessage.equals("get_news") || receiveMessage.equals("/news")) {
            SendMessage replyMessage = this.botNews.introMessage(chatID);
            execute(replyMessage);
        }
        else if (receiveMessage.startsWith("/search_news")) {
            // take movie name from user message
            String[] arr = receiveMessage.split(" ", 2);
            if (arr.length<2) {
                execute(new SendMessage(chatID, "Please enter keywords for us to searching"));
                return;
            }

            int n = this.botNews.searchNews(chatID, arr[1]);
            if (n==0) {
                this.botNews.updatePageTable(chatID, 0, false);
                execute(new SendMessage(chatID, "Sorry, we don't find any information about your search"));
            }
            else {
                SendPhoto replyMessage = this.botNews.displaySearchNews(chatID, 0);
                execute(replyMessage);
            }
        }
        else if (receiveMessage.equals("/hot_news")) {
            this.botNews.getHotNews();
            SendPhoto replyMessage = this.botNews.displayHotNews(chatID, 0);
            execute(replyMessage);
        }
        else if (receiveMessage.equals("previous_hot_news")) {
            int desPage = botNews.movePage(chatID, -1, true);
            EditMessageMedia replyMessage = botNews.displayHotNews(chatID, messageID, desPage);
            execute(replyMessage);
        }
        else if (receiveMessage.equals("next_hot_news")) {
            int desPage = botNews.movePage(chatID, 1, true);
            EditMessageMedia replyMessage = botNews.displayHotNews(chatID, messageID, desPage);
            execute(replyMessage);
        }
        else if (receiveMessage.equals("previous_search_news")) {
            int desPage = botNews.movePage(chatID, -1, false);
            EditMessageMedia replyMessage = botNews.displaySearchNews(chatID, messageID, desPage);
            execute(replyMessage);
        }
        else if (receiveMessage.equals("next_search_news")) {
            int desPage = botNews.movePage(chatID, 1, false);

            EditMessageMedia replyMessage = botNews.displaySearchNews(chatID, messageID, desPage);
            execute(replyMessage);
        }
    }

    public void callBotWaiting(String receiveMessage, String chatID, long messageID) throws TelegramApiException {
        if (receiveMessage.equals("get_myList") || receiveMessage.equals("/mylist")) {
            SendMessage message = this.botWaiting.displayMyList(chatID);
            execute(message);
        }
        else if (receiveMessage.contains("myListIndex_")) {
            DeleteMessage deleteMessage = new DeleteMessage(chatID, Math.toIntExact(messageID));
            execute(deleteMessage);

            int index = Integer.parseInt(receiveMessage.split("_")[1]);
            this.botWaiting.removeFromList(index);
            SendMessage message = this.botWaiting.displayMyList(chatID);
            execute(message);
        }
        else if (receiveMessage.contains("add_m_myList_")) {
            int index = Integer.parseInt(receiveMessage.split("_")[3]);
            JSONObject movie = this.movieController.getMovieView()
                                                    .getMovie(chatID)
                                                    .getJSONObject(index);
            UpComing upComing = new UpComing(movie.get("original_title").toString(),
                    movie.get("release_date").toString() + " 00:00:00",
                    chatID);
            this.botWaiting.addToList(new UpComing("testing day 1", "2022-04-30 00:00:00", chatID));
            this.botWaiting.addToList(new UpComing("testing day 2", "2022-05-2 00:00:00", chatID));
            if (!this.botWaiting.isExist(upComing)) {
                this.botWaiting.addToList(upComing);
                SendMessage message = new SendMessage();
                message.setChatId(chatID);
                message.setText("add successful");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else {
                SendMessage message = new SendMessage();
                message.setChatId(chatID);
                message.setText("Movie has already in list");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (receiveMessage.contains("add_TV_myList_")) {
            int index = Integer.parseInt(receiveMessage.split("_")[3]);
            JSONObject show = this.showController.getShowView()
                                                .getShow(chatID)
                                                .getJSONObject(index);
            UpComing upComing = new UpComing(show.get("original_name").toString(),
                                                        show.get("first_air_date").toString() + " 00:00:00",
                                                        chatID);
            if (!this.botWaiting.isExist(upComing)) {
                this.botWaiting.addToList(upComing);
                SendMessage message = new SendMessage();
                message.setChatId(chatID);
                message.setText("add successful");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else {
                SendMessage message = new SendMessage();
                message.setChatId(chatID);
                message.setText("TVShow has already in list");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Checker extends TimerTask {
        /*
            Class use to check whether a movie in BotWaiting is releases
         */
        private Queue<UpComing> list;

        public Checker(Queue<UpComing> l) {
            this.list = l;
        }

        @Override
        public void run() {
            List<UpComing> temp = new ArrayList<>();
            while (!this.list.isEmpty()) {
                UpComing movie = this.list.poll();
                temp.add(movie);
                SendMessage message = new SendMessage();
                message.setChatId(movie.getChatId());
                message.setText("Movie: " + movie.getName() + " has been released, check it now!!");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

            for (int i=0; i < temp.size(); i++)
                this.list.add(temp.get(i));
        }
    }

    public void runningCheck() {
        long delay = 1000L;
        long period = 1000L * 60L * 60L * 12L;
        new Timer().scheduleAtFixedRate(this.botWaiting, 0 ,period);
        new Timer().scheduleAtFixedRate(new Checker(this.botWaiting.getNotifyList()), delay, period);
    }
}



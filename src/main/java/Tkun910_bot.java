import org.json.JSONObject;
import org.quartz.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.vdurmont.emoji.EmojiParser;

import java.util.*;
import Objects.UpComingMovie;

public class Tkun910_bot extends TelegramLongPollingBot {
    private  BotMovie botMovie;
    private  BotTVShow botTV ;
    private  BotWaiting botWaiting;
    JobDetail job;

    public Tkun910_bot() {
        this.botMovie = new BotMovie();
        this.botWaiting = new BotWaiting();
        this.botTV = new BotTVShow();
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
            String chatId = update.getMessage().getChatId().toString();

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
                    callBotMovie(receiveMessage, chatId, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (receiveMessage.contains("TVshow")){
                try {
                    callBotTV(receiveMessage , chatId , 0) ;
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if (receiveMessage.equals("/mylist")) {
                try {
                    callBotWaiting(receiveMessage, chatId, 0);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else if (update.hasCallbackQuery()) {
            String receiveMessage = update.getCallbackQuery().getData();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();

            if (receiveMessage.contains("movie")) {
                try {
                    callBotMovie(receiveMessage, chatId, messageId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (receiveMessage.contains("TVshow")) {
                try {
                    callBotTV(receiveMessage, chatId, messageId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (receiveMessage.contains("myList")) {
                try {
                    callBotWaiting(receiveMessage, chatId, messageId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public SendMessage welcomeMessage(String chatID) {
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

    public void callBotMovie(String receiveMessage, String chatId, long messageId) throws Exception {
        if (receiveMessage.equals("get_movie")) {
            SendMessage replyMessage = this.botMovie.getStart(chatId);
            execute(replyMessage);
        }
        else if (receiveMessage.contains("/movie")) {
            // take movie name from user message
            String movieName = receiveMessage.split(" ", 2)[1];
            if (movieName.isEmpty()) {
                execute(new SendMessage(chatId, "Please enter movie name"));
                return;
            }
            movieName = movieName.replace(" ", "%20");

            // get an array of seached movie
            this.botMovie.searchMovie(movieName, chatId);
            SendMessage replyMessage = this.botMovie.displaySearchList(0, chatId);
            execute(replyMessage);
        }
        else if (receiveMessage.equals("/trending_movie")) {
            this.botMovie.getTrending(chatId);
            SendMessage replyMessage = this.botMovie.displaySearchList(0, chatId);
            execute(replyMessage);
        }
        else if (receiveMessage.equals("/upcoming_movie")) {
            this.botMovie.getUpcoming(chatId);
            SendMessage replyMessage = this.botMovie.displaySearchList(0, chatId);
            execute(replyMessage);
        }
        else if (receiveMessage.contains("movieList_forward_") || receiveMessage.contains("movieList_backward_")) {
            int index = Integer.parseInt(receiveMessage.split("_")[2]);
            EditMessageText replyMessage = this.botMovie.displaySearchList(index, chatId, messageId);
            execute(replyMessage);
        }
        else if (receiveMessage.contains("movieIndex_")) {
            int index = Integer.parseInt(receiveMessage.split("_")[1]);
            SendPhoto replyMessage = this.botMovie.displayMovieDetail(index, chatId);
            DeleteMessage deleteMessage = new DeleteMessage(chatId, Math.toIntExact(messageId));
            execute(deleteMessage);
            execute(replyMessage);
        }
        else if (receiveMessage.contains("movieList_return")) {
            SendMessage replyMessage = this.botMovie.returnToList(chatId);
            DeleteMessage deleteMessage = new DeleteMessage(chatId, Math.toIntExact(messageId));
            execute(deleteMessage);
            execute(replyMessage);
        }
        else if (receiveMessage.contains("get_movieReview")) {
            SendMessage message = this.botMovie.displayReview(0, chatId, messageId);
            execute(message);
        }
        else if (receiveMessage.contains("movieReview_forward_") || receiveMessage.contains("movieReview_backward_")) {
            EditMessageText message;
            String page = receiveMessage.split("_")[2];
            if (receiveMessage.contains("movieReview_forward_"))
                message = this.botMovie.displayReview(Integer.parseInt(page), chatId, Math.toIntExact(messageId), false);
            else
                message = this.botMovie.displayReview(Integer.parseInt(page), chatId, Math.toIntExact(messageId), true);
            execute(message);
        }
        else if (receiveMessage.equals("delete_movieReview")) {
            DeleteMessage deleteMessage = new DeleteMessage(chatId, Math.toIntExact(messageId));
            execute(deleteMessage);
        }
    }

    public void callBotTV(String receiveMessage , String chatId , long messageId) throws Exception{
        if (receiveMessage.equals("get_TVshow")) {
            //send message when click button
            SendMessage replyMessage = this.botTV.getStart(chatId, Math.toIntExact(messageId));
            execute(replyMessage);
        }
        else if (receiveMessage.contains("/TVshowSeason")){
            String TVShowSeason = receiveMessage.split(" " , 2)[1] ;
            TVShowSeason = TVShowSeason.replace(" " , "%20") ;
            this.botTV.searchSeason(TVShowSeason) ;
            SendMessage replyMessage = this.botTV.displaySearchList(0, chatId);
            execute(replyMessage);

        }
        else if (receiveMessage.contains("/TVshowName")){
            String TVShowName = receiveMessage.split(" ", 2)[1] ;
            TVShowName = TVShowName.replace(" " , "%20")  ;
            this.botTV.seachByName(TVShowName) ;
            SendMessage replyMessage = this.botTV.displaySearchList(0, chatId);
            execute(replyMessage);
        }
        else if (receiveMessage.contains("/trending_TVshow")) {
            this.botTV.SearchTrendingTVShows();
            SendMessage replyMessage = this.botTV.displaySearchList(0, chatId);
            execute(replyMessage);
        }
    }

    public void callBotWaiting(String receiveMessage, String chatId, long messageId) throws TelegramApiException {
        if (receiveMessage.equals("get_myList") || receiveMessage.equals("/mylist")) {
            SendMessage message = this.botWaiting.displayMyList(chatId);
            execute(message);
        }
        else if (receiveMessage.contains("myListIndex_")) {
            DeleteMessage deleteMessage = new DeleteMessage(chatId, Math.toIntExact(messageId));
            execute(deleteMessage);

            int index = Integer.parseInt(receiveMessage.split("_")[1]);
            this.botWaiting.removeFromList(index);
            SendMessage message = this.botWaiting.displayMyList(chatId);
            execute(message);
        }
        else if (receiveMessage.contains("add_myList_")) {
            int index = Integer.parseInt(receiveMessage.split("_")[2]);
            JSONObject movie = this.botMovie.getMovie(index, chatId);
            UpComingMovie upComingMovie = new UpComingMovie(movie.get("original_title").toString(),
                    movie.get("release_date").toString() + " 00:00:00",
                    chatId);
            this.botWaiting.addToList(new UpComingMovie("hello", "2022-04-19 00:00:00", chatId));
            this.botWaiting.addToList(new UpComingMovie("hello2", "2022-04-17 00:00:00", chatId));
            if (!this.botWaiting.isExist(upComingMovie)) {
                this.botWaiting.addToList(upComingMovie);
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText("add successful");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
            else {
                SendMessage message = new SendMessage();
                message.setChatId(chatId);
                message.setText("Movie has already in list");
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
        private Queue<UpComingMovie> list;

        public Checker(Queue<UpComingMovie> l) {
            this.list = l;
        }

        @Override
        public void run() {
            List<UpComingMovie> temp = new ArrayList<>();
            while (!this.list.isEmpty()) {
                UpComingMovie movie = this.list.poll();
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



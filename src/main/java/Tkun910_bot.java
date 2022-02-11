import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.List;

public class Tkun910_bot extends TelegramLongPollingBot {
    private final BotMovie botMovie = new BotMovie();

    @Override
    public String getBotUsername() {
        return "Tkun910";
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
                executeMessage(replyMessage);
            }
            else if (receiveMessage.contains("movie")) {
                try {
                    callBotMovie(receiveMessage, chatId, 0);
                } catch (Exception e) {
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
        }
    }

    public SendMessage welcomeMessage(String chatID) {
        // create InlineButton
        InlineKeyboardButton movieBtn = new InlineKeyboardButton("Movies", null, "get_movie", null, null, null, null, null);
        InlineKeyboardButton TvshowBtn = new InlineKeyboardButton("TVShows", null, "get_TVshow", null, null, null, null, null);
        InlineKeyboardButton newsBtn = new InlineKeyboardButton("News", null, "get_news", null, null, null, null, null);
        InlineKeyboardButton upcommingBtn = new InlineKeyboardButton("Upcoming", null, "get_upcoming", null, null, null, null, null);

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

    public void executeMessage(SendMessage message) {
        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void executeMessage(EditMessageText message) {
        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void callBotMovie(String receiveMessage, String chatId, long messageId) throws Exception {
        if (receiveMessage.contains("/movie")) {
            SendMessage replyMessage = new SendMessage();
            // take movie name from user message
            String movieName = receiveMessage.split(" ", 2)[1];
            movieName = movieName.replace(" ", "%20");

            // get an array of seached movie
            this.botMovie.searchMovie(movieName);

            // check if not movie found
            if (this.botMovie.getMovieObjs().isEmpty()) {
                replyMessage.setChatId(chatId);
                replyMessage.setText(EmojiParser.parseToUnicode("OPPP!!! Sorry I don't see that movie :cry: :cry: \n Can you please check you movie name again, it maybe wrong :thinking: :thinking:"));
                executeMessage(replyMessage);
            }
            else {
                replyMessage = this.botMovie.displayMovieList(0, chatId);
                executeMessage(replyMessage);
            }
        }
        else if (receiveMessage.equals("/trending_movie")) {
            this.botMovie.getTrending();
            SendMessage replyMessage = this.botMovie.displayMovieList(0, chatId);
            executeMessage(replyMessage);
        }
        else if (receiveMessage.equals("get_movie")) {
            EditMessageText replyMessage = this.botMovie.getStart(chatId, Math.toIntExact(messageId));
            executeMessage(replyMessage);
        }
        else if (receiveMessage.contains("movieList_forward") || receiveMessage.contains("movieList_backward")) {
            String page = receiveMessage.split("_")[2];
            EditMessageText replyMessage = this.botMovie.displayMovieList(Integer.parseInt(page), chatId, Math.toIntExact(messageId));
            executeMessage(replyMessage);
        }
        else if (receiveMessage.contains("movieList_index")) {
            String index = receiveMessage.split("_")[2];
            SendMessage[] reply = this.botMovie.displayMovieDetail(Integer.parseInt(index), chatId);
            for (int i=0; i < reply.length; i++)
                executeMessage(reply[i]);
        }
        else if (receiveMessage.contains("get_movieTrailer")) {
            String index = receiveMessage.split("_")[2];
            SendMessage message = this.botMovie.displayTrailer(Integer.parseInt(index), chatId);
            executeMessage(message);
        }
        else if (receiveMessage.contains("get_movieReview")) {
            String index = receiveMessage.split("_")[2];
            this.botMovie.getUserReview(Integer.parseInt(index));
            SendMessage message = this.botMovie.displayReview(0, chatId);
            executeMessage(message);
        }
        else if (receiveMessage.contains("movieReview_forward_") || receiveMessage.contains("movieReview_backward_")) {
            EditMessageText message;
            String page = receiveMessage.split("_")[2];
            if (receiveMessage.contains("movieReview_forward_"))
                message = this.botMovie.displayReview(Integer.parseInt(page), chatId, Math.toIntExact(messageId), false);
            else
                message = this.botMovie.displayReview(Integer.parseInt(page), chatId, Math.toIntExact(messageId), true);
            executeMessage(message);
        }
    }
}

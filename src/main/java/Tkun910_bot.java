import info.movito.themoviedbapi.model.core.MovieResultsPage;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tkun910_bot extends TelegramLongPollingBot {
    private final BotMovie botMovie = new BotMovie();
    private MovieResultsPage movieDbs;

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
            SendMessage replyMessage = new SendMessage();

            // check receive message
            if (receiveMessage.equals("/start")) {
                replyMessage = welcomeMessage(update.getMessage().getChatId().toString());
                executeMessage(replyMessage);
            }
            else if (receiveMessage.contains("/movie")) {
                // take movie name from user message
                String movieName = receiveMessage.split(" ", 2)[1];

                // get an array of seached movie
                this.movieDbs = this.botMovie.searchMovie(movieName);
                replyMessage = this.botMovie.displaySearchedMovie(this.movieDbs, update.getMessage().getChatId().toString());
                executeMessage(replyMessage);
            }

        }
        else if (update.hasCallbackQuery()) {
            String receiveMessage = update.getCallbackQuery().getData();
            EditMessageText replyMessage = new EditMessageText();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();

            if (receiveMessage.equals("get movie")) {
                replyMessage = this.botMovie.getStart(chatId, Math.toIntExact(messageId));
                executeMessage(replyMessage);
            }
            else if (receiveMessage.contains("movie_next-page")) {
                String page = receiveMessage.split("_")[2];
                if (page.equals("0"))
                    page = "1";
                replyMessage = this.botMovie.displaySearchedMovie(this.movieDbs, Integer.parseInt(page), chatId, Math.toIntExact(messageId));
                executeMessage(replyMessage);
            }
            else if (receiveMessage.contains("movie_previous-page")) {
                String page = receiveMessage.split("_")[2];
                replyMessage = this.botMovie.displaySearchedMovie(this.movieDbs, Integer.parseInt(page), chatId, Math.toIntExact(messageId));
                executeMessage(replyMessage);
            }
            else if (receiveMessage.contains("movie index")) {
                String index = receiveMessage.split(" ")[2];
                SendMessage[] reply = this.botMovie.getMovieDetail(this.movieDbs, Integer.parseInt(index), chatId);
                for (int i=0; i < reply.length; i++)
                    executeMessage(reply[i]);
            }

        }
    }

    public SendMessage welcomeMessage(String chatID) {
        // create InlineButton
        InlineKeyboardButton movieBtn = new InlineKeyboardButton("Movies", null, "get movie", null, null, null, null, null);
        InlineKeyboardButton TvshowBtn = new InlineKeyboardButton("TVShows", null, "get TVshow", null, null, null, null, null);
        InlineKeyboardButton newsBtn = new InlineKeyboardButton("News", null, "get news", null, null, null, null, null);
        InlineKeyboardButton upcommingBtn = new InlineKeyboardButton("Upcomming", null, "get upcomming", null, null, null, null, null);

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
        String welcome = "Hello!! I'm Movie Bot. What can I help you";
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
}

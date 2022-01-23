import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Objects;

public class Tkun910_bot extends TelegramLongPollingBot {
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
    }
}

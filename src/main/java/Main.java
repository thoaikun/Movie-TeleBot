import org.quartz.SchedulerException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            Tkun910_bot myBot = new Tkun910_bot();
            botsApi.registerBot(myBot);
            myBot.runningCheck();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.util.*;

class upComingMovie {
    String name;
    String releaseDate;
    String chatId;

    public upComingMovie(String n, String r, String chatId) {
        this.name = n;
        this.releaseDate = r;
        this.chatId = chatId;
    }
}

class MovieComparater implements Comparator<upComingMovie> {
    @Override
    public int compare(upComingMovie o1, upComingMovie o2) {
        LocalDate o1Date = LocalDate.parse(o1.releaseDate);
        LocalDate o2Date = LocalDate.parse(o2.releaseDate);
        if (o1Date.isBefore(o2Date))
            return -1;
        else if (o1Date.isAfter(o2Date))
            return 1;
        else
            return 0;
    }
}

public class BotWaiting extends TimerTask {
    private PriorityQueue<upComingMovie> myWaitingList;
    private Queue<upComingMovie> notifyList;

    public BotWaiting() {
        this.myWaitingList = new PriorityQueue<upComingMovie>(5, new MovieComparater());
        this.notifyList = new ArrayDeque<>();
    }

    public boolean addToList(String name, String releaseDate, String chatId) {
        if (this.myWaitingList.add(new upComingMovie(name, releaseDate, chatId)))
            return true;
        return false;
    }

    public boolean addToList(upComingMovie movie) {
        if (this.myWaitingList.add(movie))
            return true;
        return false;
    }

    public Queue<upComingMovie> getNotifyList() {
        return this.notifyList;
    }

    public boolean isExist(upComingMovie movie) {
        Object[] arrays = this.myWaitingList.toArray();
        for (int i=0; i < arrays.length; i++) {
            upComingMovie upComingMovie = (upComingMovie) arrays[i];
            if (upComingMovie.name.equals(movie.name))
                return true;
        }
        return false;
    }

    public SendMessage displayMyList(String chatID) {
        Object[] arrays = this.myWaitingList.toArray();
        if (arrays.length == 0)
            return new SendMessage(chatID, "List empty");

        List<InlineKeyboardButton> row = new ArrayList<>();
        String text = "";
        for (int i=0; i < arrays.length; i++) {
            upComingMovie movie = (upComingMovie) arrays[i];
            text += i + "/ " + movie.name + "\n";
            row.add(new InlineKeyboardButton(String.valueOf(i), null, "myListIndex_" + i,
                    null, null, null, null, null));
        }
        String removeText = "Want remove some thing ? Click below: ";
        text += removeText.toUpperCase(Locale.ROOT);

        List<List<InlineKeyboardButton>> allBtn = new ArrayList<>();
        allBtn.add(row);

        SendMessage replyMessage = new SendMessage(chatID, text);
        replyMessage.setReplyMarkup(new InlineKeyboardMarkup(allBtn));
        return replyMessage;
    }

    public boolean removeFromList(int index) {
        if (this.myWaitingList.isEmpty())
            return false;

        int count = 0;
        List<upComingMovie> temp = new ArrayList<>();
        while (!this.myWaitingList.isEmpty()) {
            upComingMovie movie = this.myWaitingList.poll();
            if (count != index) {
                temp.add(movie);
            }
            count ++;
        }

        for (int i=0; i < temp.size(); i++)
            this.myWaitingList.add(temp.get(i));
        return true;
    }

    @Override
    public void run() {
        while (!this.notifyList.isEmpty()) {
            upComingMovie movie = this.notifyList.peek();
            LocalDate date = LocalDate.parse((CharSequence) movie.releaseDate);
            if (LocalDate.now().isAfter(date))
                this.notifyList.poll();
            else
                break;
        }
        while (!this.myWaitingList.isEmpty()) {
            upComingMovie movie = this.myWaitingList.peek();
            LocalDate date = LocalDate.parse((CharSequence) movie.releaseDate);
            if (LocalDate.now().isEqual(date))
                this.notifyList.add(this.myWaitingList.poll());
            else
                break;
        }
    }
}
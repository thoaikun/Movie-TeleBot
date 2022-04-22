import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import Objects.UpComingMovie;

class MovieComparater implements Comparator<UpComingMovie> {
    @Override
    public int compare(UpComingMovie o1, UpComingMovie o2) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date o1Date = null;
        Date o2Date = null;
        try {
            o1Date = format.parse(o1.getReleaseDate());
            o2Date = format.parse(o2.getReleaseDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (o1Date.before(o2Date))
            return -1;
        else if (o1Date.after(o2Date))
            return 1;
        else
            return 0;
    }
}

public class BotWaiting extends TimerTask {
    private PriorityQueue<UpComingMovie> myWaitingList;
    private Queue<UpComingMovie> notifyList;

    public BotWaiting() {
        this.myWaitingList = new PriorityQueue<UpComingMovie>(5, new MovieComparater());
        this.notifyList = new ArrayDeque<>();
    }

    public boolean addToList(String name, String releaseDate, String chatId) {
        if (this.myWaitingList.add(new UpComingMovie(name, releaseDate, chatId)))
            return true;
        return false;
    }

    public boolean addToList(UpComingMovie movie) {
        if (this.myWaitingList.add(movie))
            return true;
        return false;
    }

    public Queue<UpComingMovie> getNotifyList() {
        return this.notifyList;
    }

    public boolean isExist(UpComingMovie movie) {
        Object[] arrays = this.myWaitingList.toArray();
        for (int i=0; i < arrays.length; i++) {
            UpComingMovie UpComingMovie = (UpComingMovie) arrays[i];
            if (UpComingMovie.getName().equals(movie.getName()))
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
            UpComingMovie movie = (UpComingMovie) arrays[i];
            text += i + "/ " + movie.getName() + "\n";
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
        List<UpComingMovie> temp = new ArrayList<>();
        while (!this.myWaitingList.isEmpty()) {
            UpComingMovie movie = this.myWaitingList.poll();
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (!this.notifyList.isEmpty()) {
            UpComingMovie movie = this.notifyList.peek();
            Date date = null;
            try {
                date = format.parse(movie.getReleaseDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (new Date().after(date))
                this.notifyList.poll();
            else
                break;
        }
        while (!this.myWaitingList.isEmpty()) {
            UpComingMovie movie = this.myWaitingList.peek();
            Date date = null;
            try {
                date = format.parse(movie.getReleaseDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (new Date().equals(date) || new Date().after(date))
                this.notifyList.add(this.myWaitingList.poll());
            else
                break;
        }
    }
}
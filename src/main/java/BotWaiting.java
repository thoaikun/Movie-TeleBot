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



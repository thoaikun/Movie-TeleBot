package Objects;
/*
 * UpComingMovie is a class to store information about movie that will be release
 * soon and user are looking forward to it.
 * It contains chatId to identify which movie that each user is interested in,
 * Name for movie name and release date
 */
public class UpComing {
    String name;
    String releaseDate;
    String chatId;

    public UpComing(String n, String r, String chatId) {
        this.name = n;
        this.releaseDate = r;
        this.chatId = chatId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}

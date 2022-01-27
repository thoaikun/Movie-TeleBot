import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbSearch;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;
import org.checkerframework.checker.units.qual.A;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class BotMovie {
    private final TmdbApi tmdbApi = new TmdbApi("16e8d32a627987825706488073388e2e");
    private TmdbSearch theSeacher = new TmdbSearch(this.tmdbApi);

    public EditMessageText getStart(String chatID, int messageID) {
        // setting up for message
        String textMessage = "This is Movies, one of my bot cousin. He knows a lot of movies. If you want to find something, ask himm\n" +
                             "Type /movie + 'your movie you want to find' to get started";

        // creat a editMessage
        EditMessageText editText = new EditMessageText();
        editText.setChatId(chatID);
        editText.setMessageId(messageID);
        editText.setText(textMessage);
        return editText;
    }

    public MovieResultsPage searchMovie(String movieName) {
       return this.theSeacher.searchMovie(movieName, null, null, false, null);
    }

    public SendMessage displaySearchedMovie(MovieResultsPage movieDbs, String chatID) {
        // set string to display movie seached name
        String movieNames = "";
        List<MovieDb> movieDbList = movieDbs.getResults();
        for (int i=0; i < Math.min(movieDbList.size(), 5); i++) {
            movieNames += i + "/ " + movieDbList.get(i).toString() + "\n";
        }

        // create inline button to select movie ans switch to next page
        InlineKeyboardButton btn1 = new InlineKeyboardButton("0", null, "movie index 0", null, null, null, null, null);
        InlineKeyboardButton btn2 = new InlineKeyboardButton("1", null, "movie index 1", null, null, null, null, null);
        InlineKeyboardButton btn3 = new InlineKeyboardButton("2", null, "movie index 2", null, null, null, null, null);
        InlineKeyboardButton btn4 = new InlineKeyboardButton("3", null, "movie index 3", null, null, null, null, null);
        InlineKeyboardButton btn5 = new InlineKeyboardButton("4", null, "movie index 4", null, null, null, null, null);
        InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "movie_next-page_0", null, null, null, null, null);
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        row1.add(btn1); row1.add(btn2); row1.add(btn3); row1.add(btn4); row1.add(btn5);
        row2.add(forwardBtn);
        btnList.add(row1); btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();
        allBtn.setKeyboard(btnList);

        // create a edit message
        SendMessage editText = new SendMessage();
        editText.setChatId(chatID);
        editText.setText(movieNames);
        editText.setReplyMarkup(allBtn);
        return editText;
    }

    public EditMessageText displaySearchedMovie(MovieResultsPage movieDbs, int page, String chatID, int messageID) {
        // set string to display movie seached name
        String movieNames = "";
        List<MovieDb> movieDbList = movieDbs.getResults();

        // check if there is no more movie
        if (page*5 >= movieDbList.size()) {
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
            InlineKeyboardButton backwardBtn = new InlineKeyboardButton("<<", null, "movie_previous-page_" + (page-1), null, null, null, null, null);
            row1.add(backwardBtn);
            btnList.add(row1);
            InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();
            allBtn.setKeyboard(btnList);

            EditMessageText editText = new EditMessageText();
            editText.setChatId(chatID);
            editText.setMessageId(messageID);
            editText.setText("There is no more movie");
            editText.setReplyMarkup(allBtn);
            return editText;
        }

        for (int i=page*5; i < Math.min(movieDbList.size(), page*5 + 5); i++) {
            movieNames += i + "/ " + movieDbList.get(i).toString() + "\n";
        }

        // create inline button to select movie ans switch to next page
        InlineKeyboardButton btn1 = new InlineKeyboardButton(String.valueOf(page*5), null, "movie index " + String.valueOf(page*5), null, null, null, null, null);
        InlineKeyboardButton btn2 = new InlineKeyboardButton(String.valueOf(page*5+1), null, "movie index " + String.valueOf(page*5+1), null, null, null, null, null);
        InlineKeyboardButton btn3 = new InlineKeyboardButton(String.valueOf(page*5+2), null, "movie index " + String.valueOf(page*5+2), null, null, null, null, null);
        InlineKeyboardButton btn4 = new InlineKeyboardButton(String.valueOf(page*5+3), null, "movie index " + String.valueOf(page*5+3), null, null, null, null, null);
        InlineKeyboardButton btn5 = new InlineKeyboardButton(String.valueOf(page*5+4), null, "movie index " + String.valueOf(page*5+4), null, null, null, null, null);
        InlineKeyboardButton forwardBtn = new InlineKeyboardButton(">>", null, "movie_next-page_" + (page+1), null, null, null, null, null);
        InlineKeyboardButton backwardBtn = new InlineKeyboardButton("<<", null, "movie_previous-page_" + (page-1), null, null, null, null, null);
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<List<InlineKeyboardButton>> btnList = new ArrayList<>();
        row1.add(btn1); row1.add(btn2); row1.add(btn3); row1.add(btn4); row1.add(btn5);
        if (page != 0)
            row2.add(backwardBtn);
        row2.add(forwardBtn);
        btnList.add(row1); btnList.add(row2);
        InlineKeyboardMarkup allBtn = new InlineKeyboardMarkup();
        allBtn.setKeyboard(btnList);

        EditMessageText editText = new EditMessageText();
        editText.setChatId(chatID);
        editText.setMessageId(messageID);
        editText.setText(movieNames);
        editText.setReplyMarkup(allBtn);
        return editText;
    }

    public SendMessage[] getMovieDetail(MovieResultsPage movieDbs, int index, String chatID) {
        MovieDb detailMovie = movieDbs.getResults().get(index);
        String movieName = detailMovie.getTitle();
        String movieYear = "Release date: " + detailMovie.getReleaseDate();
        String movieOverview = "Overview: " + detailMovie.getOverview();
        String movieRating = "Ratting: " + detailMovie.getVoteAverage();
        String movieImg = detailMovie.getPosterPath();

        // send movie image link
        SendMessage img = new SendMessage();
        img.setChatId(chatID);
        img.setText("https://image.tmdb.org/t/p/original/" + movieImg);
        // send infomation
        SendMessage info = new SendMessage();
        info.setChatId(chatID);
        info.setText(movieName + "\n" + movieYear + "\n" + movieRating + "\n" + movieOverview);

        SendMessage[] reply = new SendMessage[2];
        reply[0] = img;
        reply[1] = info;

        return reply;
    }
}

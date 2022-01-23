import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbSearch;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;

import java.util.List;
import java.util.ListIterator;

public class BotMovie {
    private final TmdbApi tmdbApi = new TmdbApi("16e8d32a627987825706488073388e2e");

    public void getMovie(String movieName) {
        TmdbSearch theSeacher = tmdbApi.getSearch();
        MovieResultsPage resultsPage = theSeacher.searchMovie(movieName, null, null, true, null);
        List movies = resultsPage.getResults();
        for (ListIterator<MovieDb> iterator = movies.listIterator(); iterator.hasNext();) {
            MovieDb movie = iterator.next();
            System.out.println(movie.getTitle());
        }
    }
}

package Objects;

import org.json.JSONArray;


/*
 *  UserData is a class to store the search movie and its review for each
 *  user when they search for a film or get trending/upcoming movie
 */
public class UserData {
    JSONArray movieObjs;
    JSONArray reviewObjs;

    public UserData() {
        this.movieObjs = new JSONArray();
        this.reviewObjs = new JSONArray();
    }
    public UserData(JSONArray movieObjs, JSONArray reviewObjs) {
        this.movieObjs = movieObjs;
        this.reviewObjs = reviewObjs;
    }
    public UserData(JSONArray movieObjs) {
        this.movieObjs = movieObjs;
        this.reviewObjs = new JSONArray();
    }

    public void setReviewObjs(JSONArray reviewObjs) {
        this.reviewObjs = reviewObjs;
    }

    public void setMovieObjs(JSONArray movieObjs) {
        this.movieObjs = movieObjs;
    }

    public JSONArray getMovieObjs() {
        return movieObjs;
    }

    public JSONArray getReviewObjs() {
        return reviewObjs;
    }
}

package Objects;

import org.json.JSONArray;


/*
 *  UserData is a class to store the search movie and its review for each
 *  user when they search for a film or get trending/upcoming movie
 */
public class UserData {
    JSONArray objects;
    JSONArray reviewObjs;

    public UserData() {
        this.objects = new JSONArray();
        this.reviewObjs = new JSONArray();
    }
    public UserData(JSONArray objects, JSONArray reviewObjs) {
        this.objects = objects;
        this.reviewObjs = reviewObjs;
    }
    public UserData(JSONArray objects) {
        this.objects = objects;
        this.reviewObjs = new JSONArray();
    }

    public void setReviewObjs(JSONArray reviewObjs) {
        this.reviewObjs = reviewObjs;
    }

    public void setMovieObjs(JSONArray objects) {
        this.objects = objects;
    }

    public JSONArray getMovieObjs() {
        return objects;
    }

    public JSONArray getReviewObjs() {
        return reviewObjs;
    }
}

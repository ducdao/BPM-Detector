package dao.duc.bpmdetector;

/**
 * Created by Duc Dao on 6/3/2017.
 */

public class Note {
    private String title = "";
    private String id = "";

    public Note(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

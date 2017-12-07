package sample;

/**
 * Created by Alper on 7.12.2017.
 */
public class Commit {
    private String committer;
    private String date;
    private String message;

    public Commit(String committer, String message, String date) {
        this.committer = committer;
        this.date = date;
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(String committer) {
        this.committer = committer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

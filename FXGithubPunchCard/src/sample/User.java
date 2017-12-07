package sample;
import javafx.scene.image.Image;

/**
 * Created by Alper on 18.11.2017.
 */
public class User {

    private String name;
    private String username;
    private long publicRepos;
    private long followers;
    private long following;
    Image userImg;

    public User(String name, String username, long publicRepos, long followers, long following, Image userImg) {
        this.name = name;
        this.username = username;
        this.publicRepos = publicRepos;
        this.followers = followers;
        this.following = following;
        this.userImg = userImg;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getPublicRepos() {
        return publicRepos;
    }

    public void setPublicRepos(int publicRepos) {
        this.publicRepos = publicRepos;
    }

    public long getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public long getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public Image getUserImg() {
        return userImg;
    }

    public void setUserImg(Image userImg) {
        this.userImg = userImg;
    }
}

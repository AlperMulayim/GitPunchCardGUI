package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;


public class Main extends Application {

    private JSONObject jsonObject;
    private JSONArray repoJsonArray;
    private Parent root;
    private  Parent repoRoot;
    private Button btnGetUser;
    private TextArea txtArea;
    private Text txtStatus;
    private Text txtName;
    private Text txtUserName;
    private ImageView userImage;
    private Text txtFollower;
    private Text txtFollowing;
    private Text txtRepo;
    private TextField txtFieldUser;
    private ListView listView;
    private Button btnRepoDetails;

    private ExecutorService requestExecutor;
    private ExecutorService userImageRequest;
    private ExecutorService repoRequest;


    private  RepoPage repoPage;
    private String userRepoDetails;


    @Override
    public void start(Stage primaryStage) throws Exception{
        root = FXMLLoader.load(getClass().getResource("sample.fxml"));

        Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("GitHub PunchCard");
        primaryStage.setScene(new Scene(root, 670, 480));

        primaryStage.setResizable(false);
        primaryStage.show();

        prepareGUIElements();
        textAreaOperations();
        buttonActions();

    }


    public static void main(String[] args) {
        launch(args);
    }

    public void userRequesting() {
        requestExecutor   = Executors.newSingleThreadExecutor();

        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    userHTTPRequest();
                } catch (IOException e) {
                    e.printStackTrace();
                    txtStatus.setText("Cannot Connect Internet");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        requestExecutor.execute(task);
        requestExecutor.shutdown();

    }

    public  void userHTTPRequest() throws IOException, ParseException {
        String userName = txtFieldUser.getText();

        if(!txtFieldUser.getText().isEmpty()) {
            URL url = new URL("https://api.github.com/users/" + userName);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();

            httpConnection.setDoOutput(true);

            httpConnection.setRequestMethod("GET");
            //System.out.println("RESPONSE :: " + httpConnection.getResponseCode());
            txtStatus.setText("Response : " + httpConnection.getResponseCode());

            InputStreamReader reader = new InputStreamReader(httpConnection.getInputStream(),"UTF8");
            StringBuilder strB = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(reader);

            if (bufferedReader != null) {
                int cp;
                while ((cp = bufferedReader.read()) != -1) {
                    strB.append((char) cp);
                }
                bufferedReader.close();
            }

            //txtArea.setText(strB.toString());
            JSONParser jsonParser = new JSONParser();
            jsonObject = (JSONObject) jsonParser.parse(strB.toString());

            String repoStr = (String) jsonObject.get("repos_url");

            userImageRequest = Executors.newSingleThreadExecutor();
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    try {
                        userImageHttpRequesting();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };

            userImageRequest.execute(task);
            userImageRequest.shutdown();
            repoRequest(repoStr);
            createUser();
        }
        else {
            txtStatus.setText("User Field is Empty");
        }

    }

    public void repoRequest(String url){
        repoRequest = Executors.newSingleThreadExecutor();
        String theURL = url;

        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    repoHttpRequesting(theURL);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        repoRequest.execute(task);
        repoRequest.shutdown();


    }

    public void repoHttpRequesting(String theURL) throws IOException, ParseException {
        URL url = new URL(theURL);

        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

        httpURLConnection.setRequestMethod("GET");

        // TODO: 20.11.2017  kill the thread
        // TODO: 20.11.2017 put them to listviews
        InputStreamReader reader = new InputStreamReader(httpURLConnection.getInputStream(),"UTF8");
        BufferedReader bufferedReader = new BufferedReader(reader);

        StringBuilder strB = new StringBuilder();

        if(bufferedReader != null){
            int cp;
            while ((cp = bufferedReader.read()) != -1){
                strB.append((char) cp);
            }
            bufferedReader.close();
        }

        userRepoDetails = strB.toString();
        httpURLConnection.disconnect();

        JSONParser jsonParser = new JSONParser();
        repoJsonArray= (JSONArray) jsonParser.parse(strB.toString());

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    listView.getItems().clear();
                    for(int i =0 ; i< repoJsonArray.size(); ++i) {
                        JSONObject jsObj = (JSONObject) repoJsonArray.get(i);
                        listView.getItems().add(jsObj.get("name"));
                    }

                }
            });
    }

    public void userImageHttpRequesting() throws IOException {

            String userImageURL = (String) jsonObject.get("avatar_url");

            System.out.println(userImageURL);
            URL url = new URL(userImageURL);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();

            httpConnection.setRequestMethod("GET");


            BufferedImage img = ImageIO.read(httpConnection.getInputStream());

            Image image = SwingFXUtils.toFXImage(img, null);

            userImage.setImage(image);
            //imageRequest Shutdown
            userImageRequest.shutdownNow();

    }


    public  void createUser(){
        String name = (String) jsonObject.get("name");
        txtName.setText(name);

        String userName = (String) jsonObject.get("login");
        txtUserName.setText(userName);

        long follower = (long) jsonObject.get("followers");
        txtFollower.setText(String.valueOf(follower));

        long following = (long) jsonObject.get("following");
        txtFollowing.setText(String.valueOf(following));

        long repos = (long) jsonObject.get("public_repos");
        txtRepo.setText(String.valueOf(repos));

        User user = null;
        if(userImageRequest.isShutdown()){
             user = new User(name,userName,repos,follower,following,userImage.getImage());

        }
        requestExecutor.shutdownNow();

        if(user != null){
            System.out.println(user.getName());
        }


    }
    public  void prepareGUIElements(){

        //buttons
        btnGetUser = (Button) root.lookup("#btnGetUser");
        btnRepoDetails = (Button) root.lookup("#btnRepoDetails");
        //text
        //txtArea = (TextArea) root.lookup("#txtArea");
        txtStatus = (Text) root.lookup("#txtStatus");
        txtName  = (Text) root.lookup("#txtName");
        txtUserName = (Text) root.lookup("#txtUserName");
        txtFollower = (Text) root.lookup("#txtFollower");
        txtFollowing = (Text) root.lookup("#txtFollowing");
        txtRepo = (Text) root.lookup("#txtRepo");
        //images
        userImage = (ImageView) root.lookup("#imgView");

        //textField
        txtFieldUser = (TextField) root.lookup("#txtFieldUser");

        listView = (ListView) root.lookup("#listViewRepos");
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    }



    public void  textAreaOperations(){
     //   txtArea.setEditable(false);
    }

    public void buttonActions(){
        btnGetUser.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               userRequesting();
            }
        });

        btnRepoDetails.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // TODO: 7.12.2017 add here the repo page
                ObservableList<String> selectedList = getListViewSelections();

                try {
                    repoPage = new RepoPage(selectedList.get(0),userRepoDetails);
                    repoPage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    public ObservableList<String> getListViewSelections(){
        ObservableList<String> observableList;
        observableList = listView.getSelectionModel().getSelectedItems();
        return  observableList;
    }


}



//https://api.github.com/repos/AlperMulayim/OperatingSystem/commits


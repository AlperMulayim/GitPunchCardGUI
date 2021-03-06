package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Alper on 7.12.2017.
 */
public class RepoPage {
    private Parent repoRoot;
    private Stage repoStage;
    private Text txtRepoName;
    private Text txtRepoDesctiption;
    private Text txtRepoLang;
    private Text txtRepoCreateDate;
    private  Text txtRepoUpdateDate;
    private JSONArray repoJsonArray;
    private JSONArray commitsJsonArr;
    private String repoName;
    private ExecutorService commitRequestExecutor;
    private  ExecutorService readmeExecutor;
    private TableView<Commit> tableView;
    private Text txtCommitCount;
    private Button btnReadMe;
    private TextArea txtAreaReadMe;

    String repoUrl;

    /**
     * https://api.github.com/repos/AlperMulayim/OperatingSystem
     * https://api.github.com/repos/AlperMulayim/OperatingSystem/readme
     * @param selectedRepo
     * @param repoJsonStr
     * @throws IOException
     * @throws ParseException
     */

    public RepoPage( String selectedRepo,String repoJsonStr) throws IOException, ParseException {
        repoRoot = FXMLLoader.load(getClass().getResource("repodetails.fxml"));
        repoStage = new Stage();
        Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
        repoStage.getIcons().add(icon);
        repoStage.setTitle("Repo");
        repoStage.setScene(new Scene(repoRoot, 1054, 756));

        commitsJsonArr = new JSONArray();
        this.repoName = selectedRepo;

        JSONParser jsonParser = new JSONParser();
        repoJsonArray = (JSONArray) jsonParser.parse(repoJsonStr);
        prepareGUIElements();



    }

    public void prepareGUIElements() throws IOException {
        txtRepoName = (Text) repoRoot.lookup("#txtRepoName");
        txtRepoDesctiption = (Text) repoRoot.lookup("#txtRepoDetail");
        txtRepoLang = (Text) repoRoot.lookup("#txtRepoLang");
        tableView = (TableView) repoRoot.lookup("#tableVRepo");
        txtCommitCount = (Text) repoRoot.lookup("#txtCommitCount");
        txtRepoCreateDate = (Text) repoRoot.lookup("#txtRepoDate");
        txtRepoUpdateDate = (Text) repoRoot.lookup("#txtRepoUpdateDate");
        txtAreaReadMe = (TextArea) repoRoot.lookup("#txtAreaReadMe");

        btnReadMe = (Button) repoRoot.lookup("#btnReadMe");
        buttonActions();
        repoDetailPageSetting();
    }

    public void show(){
        repoStage.show();
    }

    public void repoDetailPageSetting() throws IOException {

        //txtStatus.setText(selectedRepo.get(0));
        txtRepoName.setText(repoName);
        JSONObject jsonObject = getSelectedRepoJson(repoName);

        txtRepoDesctiption.setText((String) jsonObject.get("description"));
        txtRepoLang.setText((String) jsonObject.get("language"));


        String createdDate = (String) jsonObject.get("created_at");
        String updateDate = (String) jsonObject.get("updated_at");

        txtRepoCreateDate.setText(createdDate);
        txtRepoUpdateDate.setText(updateDate);
        String commitsURL = (String) jsonObject.get("commits_url");
        commitsURL = commitsURL.substring(0,commitsURL.length() - 6);

        System.out.println(commitsURL);

        repoUrl = (String) jsonObject.get("url");
        System.out.println(repoUrl);
        repoUrl += "/readme";
        System.out.println(repoUrl);
        commitRequest(commitsURL);


        // TODO: 13.12.2017  Read Me request add here
    }

    public ObservableList<Commit> getCommitList(){
         ObservableList<Commit> commitList = FXCollections.observableArrayList();


        for(int i = 0 ; i< commitsJsonArr.size(); ++i){
            txtCommitCount.setText("There is " + commitsJsonArr.size() + " commit .");
            JSONObject jsonObject = (JSONObject) commitsJsonArr.get(i);

            JSONObject commitObj = (JSONObject) jsonObject.get("commit");
            JSONObject committerObj = (JSONObject) commitObj.get("committer");
            String date = (String) committerObj.get("date");
            String  committerName = (String) committerObj.get("name");
            String  message = (String) commitObj.get("message");

            commitList.add(new Commit(committerName,message,date));
        }
        return commitList;
    }

    public JSONObject getSelectedRepoJson(String repoName){
        JSONObject result = null;
        for(int i = 0 ; i < repoJsonArray.size(); ++i){
            JSONObject jsonObject = (JSONObject) repoJsonArray.get(i);
            if(repoName.equals(jsonObject.get("name"))){
                result = jsonObject;
            }
        }
        return result;
    }

    public void commitRequest(String commitURL){
        commitRequestExecutor = Executors.newSingleThreadExecutor();

        Runnable task = new Runnable() {
            String url = commitURL;
            @Override
            public void run() {
                try {
                    commitHttpRequest(url);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        commitRequestExecutor.execute(task);
        commitRequestExecutor.shutdown();
    }

    public  void commitHttpRequest(String urlStr) throws IOException, ParseException {

        URL url = new URL(urlStr);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();

        httpConnection.setDoOutput(true);

        httpConnection.setRequestMethod("GET");

        InputStreamReader reader = new InputStreamReader(httpConnection.getInputStream(), "UTF8");

        StringBuilder strB = new StringBuilder();

        BufferedReader bufferedReader = new BufferedReader(reader);

        if (bufferedReader != null) {
            int cp;
            while ((cp = bufferedReader.read()) != -1) {
                strB.append((char) cp);
            }
            bufferedReader.close();
        }


        JSONParser jsonParser = new JSONParser();
        commitsJsonArr = (JSONArray) jsonParser.parse(strB.toString());

        setTableView();
    }


    public void readMeRequest(String readMeURL){
        readmeExecutor = Executors.newSingleThreadExecutor();

        Runnable task = new Runnable() {
            String url = readMeURL;
            @Override
            public void run() {
                try {
                    readMHTTPRequest(url);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        };

        readmeExecutor.execute(task);
        readmeExecutor.shutdown();
    }


    public void readMHTTPRequest(String urlStr) throws IOException, ParseException {
        URL url = new URL(urlStr);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();

        httpConnection.setDoOutput(true);

        httpConnection.setRequestMethod("GET");

        InputStreamReader reader = new InputStreamReader(httpConnection.getInputStream(), "UTF8");

        StringBuilder strB = new StringBuilder();

        BufferedReader bufferedReader = new BufferedReader(reader);

        if (bufferedReader != null) {
            int cp;
            while ((cp = bufferedReader.read()) != -1) {
                strB.append((char) cp);
            }
            bufferedReader.close();
        }


        JSONParser jsonParser = new JSONParser();
        JSONObject readMEObj = (JSONObject) jsonParser.parse(strB.toString());

       // System.err.println(readMEObj.toString());
        rawReadMeRequest((String) readMEObj.get("download_url"));


    }

    public void rawReadMeRequest(String urlStr) throws IOException, ParseException {
        URL url = new URL(urlStr);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();

        httpConnection.setDoOutput(true);

        httpConnection.setRequestMethod("GET");

        InputStreamReader reader = new InputStreamReader(httpConnection.getInputStream(), "UTF8");

        StringBuilder strB = new StringBuilder();

        BufferedReader bufferedReader = new BufferedReader(reader);

        if (bufferedReader != null) {
            int cp;
            while ((cp = bufferedReader.read()) != -1) {
                strB.append((char) cp);
            }
            bufferedReader.close();
        }


        //System.err.println(strB.toString());

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                txtAreaReadMe.setText(strB.toString());
            }
        });
    }




    public void setTableView(){
        TableColumn<Commit,String> nameColumn = new TableColumn<>("Name");
        nameColumn.setMinWidth(80);
        nameColumn.setCellValueFactory(new PropertyValueFactory<Commit, String>("committer"));

        TableColumn<Commit,String> dateColumn = new TableColumn<>("Date");
        dateColumn.setMinWidth(80);
        dateColumn.setCellValueFactory(new PropertyValueFactory<Commit, String>("date"));

        TableColumn<Commit,String> messageColumn = new TableColumn<>("Commit");
        dateColumn.setMinWidth(200);
        messageColumn.setCellValueFactory(new PropertyValueFactory<Commit, String>("message"));

        tableView.setItems(getCommitList());
        tableView.getColumns().addAll(dateColumn,nameColumn,messageColumn);

    }

    public void buttonActions(){
        btnReadMe.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(repoUrl != null) {
                    readMeRequest(repoUrl);
                }
            }
        });
    }
}

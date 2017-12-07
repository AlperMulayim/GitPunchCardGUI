package sample;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private JSONArray repoJsonArray;
    private JSONArray commitsJsonArr;
    private String repoName;
    private ExecutorService commitRequestExecutor;



    public RepoPage( String selectedRepo,String repoJsonStr) throws IOException, ParseException {
        repoRoot = FXMLLoader.load(getClass().getResource("repodetails.fxml"));
        repoStage = new Stage();
        repoStage.setTitle("Repo");
        repoStage.setScene(new Scene(repoRoot, 1054, 756));

        this.repoName = selectedRepo;
        JSONParser jsonParser = new JSONParser();
        repoJsonArray = (JSONArray) jsonParser.parse(repoJsonStr);
        prepareGUIElements();


    }

    public void prepareGUIElements() throws IOException {
        txtRepoName = (Text) repoRoot.lookup("#txtRepoName");
        txtRepoDesctiption = (Text) repoRoot.lookup("#txtRepoDetail");
        txtRepoLang = (Text) repoRoot.lookup("#txtRepoLang");

        txtRepoLang.setText("*");
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


        String commitsURL = (String) jsonObject.get("commits_url");
        commitsURL = commitsURL.substring(0,commitsURL.length() - 6);

        System.out.println(commitsURL);


        commitRequest(commitsURL);

        // TODO: 30.11.2017 add the commit request here
    }

    public void commitShowing(){
        JSONObject jsonObj = (JSONObject) commitsJsonArr.get(2);
        JSONObject jsonObjCommit = (JSONObject) jsonObj.get("commit");

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                txtRepoDesctiption.setText((String) jsonObjCommit.get("message"));
            }
        });
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

        InputStreamReader reader = new InputStreamReader(httpConnection.getInputStream());

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
        commitShowing();
    }

}
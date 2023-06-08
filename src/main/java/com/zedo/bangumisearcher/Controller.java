package com.zedo.bangumisearcher;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    CloseableHttpClient httpClient = HttpClients.createDefault();

    public Controller() {
    }

    @FXML
    protected TextField searchField;
    @FXML
    protected TextField searchTagField;
    @FXML
    protected FlowPane tagPane;
    @FXML
    protected FlowPane searchTagPane;

    @FXML
    protected ListView bangumiList;
    @FXML
    protected TextField filterField;

    private ArrayList<BangumiItem> bangumiItems=new ArrayList<>();

    @FXML//标题搜索
    protected void search() {
        //{"query":"Yuusha, Yamemasu"}
        bangumiList.getItems().clear();
        Main.setTitle("正在查找番剧.");
        ObservableList<Node> searchTag = searchTagPane.getChildren();
        JSONObject searchJSON = new JSONObject();
        searchJSON.put("query", new String(searchField.getText().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        searchJSON.put("p", 1);
        searchTag.clear();
        new Thread(() -> {
            HttpPost post = new HttpPost("https://bangumi.moe/api/v2/torrent/search");
            post.setEntity(new StringEntity(searchJSON.toJSONString(), ContentType.APPLICATION_JSON));
            post.addHeader("accept", "application/json, text/plain, */*");
            post.addHeader("content-type", "text/plain;charset=UTF-8");
            CloseableHttpResponse httpResponse = null;
            JSONObject jsonObject = null;
            try {
                httpResponse = httpClient.execute(post);
                jsonObject = JSON.parseObject(new String(httpResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));

            } catch (Exception e) {
                Main.setTitle("查找番剧遇到问题.");
                throw new RuntimeException(e);
            } finally {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (jsonObject.getLong("count") == 0) {
                Main.setTitle("没有找到番剧.");
                return;
            }
            JSONArray torrents = jsonObject.getJSONArray("torrents");
            for (int i = 0; i < torrents.size(); i++) {
                int finalI = i;
                Platform.runLater(() -> bangumiItems.add(new BangumiItem(torrents.getJSONObject(finalI), bangumiList)));
            }
            //jsonArrayToTags(jsonObject.getJSONArray("tag"));
            Main.setTitle("查找番剧成功.");
        }).start();


        //{"name":"异世界","multi":true}

        //{"tag_id":["548ee0ea4ab7379536f56354"]}
        /*HttpPost post = new HttpPost("https://bangumi.moe/api/v2/torrent/search");
        post.setEntity(new StringEntity(""));*/
    }

    @FXML//搜索补全标签
    protected void searchTag() {
//{"name":"异世","keywords":true,"multi":true}
        ObservableList<Node> searchTag = searchTagPane.getChildren();
        JSONObject searchJSON = new JSONObject();
        searchJSON.put("name", new String(searchTagField.getText().getBytes(StandardCharsets.UTF_8)));
        searchJSON.put("keywords", true);
        searchJSON.put("multi", true);
        searchTag.clear();
        new Thread(() -> {
            HttpPost post = new HttpPost("https://bangumi.moe/api/tag/search");
            post.setEntity(new StringEntity(searchJSON.toJSONString(), ContentType.APPLICATION_JSON));
            post.addHeader("accept", "application/json, text/plain, */*");
            post.addHeader("content-type", "text/plain;charset=UTF-8");
            CloseableHttpResponse httpResponse = null;
            JSONObject jsonObject = null;
            try {
                httpResponse = httpClient.execute(post);
                jsonObject = JSON.parseObject(new String(httpResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));

            } catch (Exception e) {
                Main.setTitle("查找标签遇到问题.");
                throw new RuntimeException(e);
            } finally {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (!jsonObject.getBoolean("found")) {
                Main.setTitle("没有找到标签.");
                return;
            }
            jsonArrayToTags(jsonObject.getJSONArray("tag"));
            Main.setTitle("查找标签成功.");
        }).start();

    }

    @FXML//刷新标签
    protected void refreshTags() {
        Main.setTitle("等待更新标签.");
        new Thread(() -> {
            HttpGet get = new HttpGet("https://bangumi.moe/api/tag/common");
            CloseableHttpResponse httpResponse = null;
            JSONArray jsonArray = null;
            try {
                httpResponse = httpClient.execute(get);
                jsonArray = JSON.parseArray(new String(httpResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));
            } catch (Exception e) {
                Main.setTitle("更新标签遇到问题.");
                throw new RuntimeException(e);
            } finally {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            jsonArrayToTags(jsonArray);
            Main.setTitle("更新标签成功.");
        }).start();
    }

    private void jsonArrayToTags(JSONArray jsonArray) {
        if (jsonArray == null)
            return;
        Platform.runLater(() -> {
            ObservableList<Node> tagPaneList = searchTagPane.getChildren();
            tagPaneList.clear();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                String name = object.getJSONObject("locale").getString("zh_cn");
                if (name == null)
                    name = object.getString("name");
                Button tagButton = new Button(name);
                tagButton.setOnMouseClicked(event -> {
                    addTag(tagButton.getText(), tagButton.getEllipsisString());
                });
                tagButton.setEllipsisString(object.getString("_id"));
                tagPaneList.add(tagButton);

            }
        });
    }

    private void searchBanGu(JSONObject search) {

    }


    public void addTag(String name, String id) {
        ObservableList<Node> tagPaneList = tagPane.getChildren();
        for (Node node : tagPaneList) {
            Button button = (Button) node;
            if (button.getText().equals(name))
                return;
        }
        Button tagButton = new Button(name);
        tagButton.setOnMouseClicked(event -> {
            tagPaneList.remove(tagButton);
        });
        tagButton.setEllipsisString(id);
        tagPaneList.add(tagButton);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //初始化tags
        bangumiItems.add(new BangumiItem(JSON.parseObject(Controller.class.getResource("temp.json")), bangumiList));
        bangumiItems.add(new BangumiItem(JSON.parseObject(Controller.class.getResource("temp.json")), bangumiList));

        filterField.textProperty().addListener(event -> {
            for (BangumiItem item : bangumiItems) {
                item.checkFilter(filterField.getText());
            }
        });
    }


    private String formatFileSize(long bytes) {
        if (bytes < 1024L) {
            return bytes + " B";
        } else if (bytes < 1024L * 1024) {
            return (int) (bytes / 1024L) + " KB";
        } else if (bytes < 1024L * 1024 * 1024) {
            return (int) (bytes / 1024L / 1024L) + " MB";
        } else if (bytes < 1024L * 1024 * 1024 * 1024) {
            return (int) (bytes / 1024L / 1024L / 1024L) + " GB";
        } else if (bytes < 1024L * 1024 * 1024 * 1024 * 1024) {
            return (int) (bytes / 1024L / 1024L / 1024L / 1024L) + " TB";
        }
        return "KN";
    }
}
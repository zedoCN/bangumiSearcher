package com.zedo.bangumisearcher;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class BangumiItem extends HBox {
    JSONObject json;
    TextArea titleTextArea;
    ListView listView;

    public String getTitle() {
        return titleTextArea.getText();
    }

    private boolean filter(String filter, String str) {
        if (filter.equals(""))
            return true;
        String[] filters = filter.split(" ");
        for (String aFilter : filters) {
            if (!str.toLowerCase().contains(aFilter.toLowerCase()))
                return false;
        }
        return true;
    }

    public void checkFilter(String filter) {

        if (filter(filter, titleTextArea.getText()))
            addToView();
        else
            removeToView();
    }

    public void addToView() {
        if (!listView.getItems().contains(this))
            listView.getItems().add(this);
    }

    public void removeToView() {
        listView.getItems().remove(this);
    }

    public BangumiItem(JSONObject json, ListView listView) {
        this.listView = listView;
        this.json = json;
        double height = 80;
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(height);

        setBackground(new Background(new BackgroundFill(Color.rgb(30, 30, 30), new CornerRadii(4.0), new Insets(0.0))));
        setPadding(new Insets(5, 5, 5, 5));
        setSpacing(5);
        setPrefWidth(0);


        String iconURL = "";
        JSONObject teamJSON = json.getJSONObject("team");
        if (teamJSON != null) {
            iconURL = "https://static.bangumi.moe/avatar/" + json.getJSONObject("uploader").getString("emailHash");
            if (teamJSON.getString("icon") != null)
                iconURL = "https://static.bangumi.moe/" + teamJSON.getString("icon");
        }

        Image iconImg = new Image(iconURL);
        ImageView icon = new ImageView(iconImg);
        icon.setPreserveRatio(true);

        icon.setFitWidth(height - 10);
        icon.setFitHeight(height - 10);
        Rectangle rectangle = new Rectangle(icon.prefWidth(-1), icon.prefHeight(-1));
        rectangle.setArcWidth(10);
        rectangle.setArcHeight(10);
        icon.setClip(rectangle);
        getChildren().add(icon);

        String title = json.getString("title") + "\n\n";
        try {
            String tag = json.getJSONObject("category_tag").getJSONObject("locale").getString("zh_cn");
            if (tag != null)
                title += "  类别: " + tag;
        } catch (Exception e) {

        }
        try {
            String team = json.getJSONObject("team").getString("name");
            if (team != null)
                title += "  团队: " + team;
        } catch (Exception e) {

        }
        try {
            String uploader = json.getJSONObject("uploader").getString("username");
            if (uploader != null)
                title += "  上传者: " + uploader;
        } catch (Exception e) {

        }

        titleTextArea = new TextArea(title);
        titleTextArea.setWrapText(true);
        titleTextArea.setPrefWidth(0);
        titleTextArea.setEditable(false);
        titleTextArea.setFocusTraversable(false);
        titleTextArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue)
                titleTextArea.positionCaret(0);
        });

        HBox.setHgrow(titleTextArea, Priority.ALWAYS);
        getChildren().add(titleTextArea);


        VBox infoBox = new VBox();
        infoBox.setPrefWidth(-1);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setSpacing(6);

        try {


            String dateTime = json.getString("publish_time");
            dateTime = dateTime.replace("Z", " UTC");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
            Date time = format.parse(dateTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(time);

            LocalDate today = LocalDate.now();
            LocalDate oldDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
            Period p = Period.between(oldDate, today);
            String result = null;
            if (p.getYears() != 0)
                result = p.getYears() + "年前";
            else if (p.getMonths() != 0)
                result = p.getMonths() + "个月前";
            else if (p.getDays() != 0)
                result = p.getDays() + "天前";

            Label uploadDate = new Label(result);

            uploadDate.setTextFill(Color.WHITE);
            infoBox.getChildren().add(uploadDate);


        } catch (ParseException e) {
            //throw new RuntimeException(e);
        }


        JSONArray filesJsonArray = json.getJSONArray("content");


        Label files = new Label(filesJsonArray.size() + "个文件 " + json.getString("size"));
        files.setTextFill(Color.WHITE);
        infoBox.getChildren().add(files);


        HBox magnetInfo = new HBox();
        magnetInfo.setAlignment(Pos.CENTER);
        magnetInfo.setPrefWidth(-1);
        Label downloads = new Label(String.valueOf(json.getLong("downloads")));
        Label finished = new Label(String.valueOf(json.getLong("finished")));
        Label seeders = new Label(String.valueOf(json.getLong("seeders")));

        downloads.setPrefSize(-1, -1);
        finished.setPrefSize(-1, -1);
        seeders.setPrefSize(-1, -1);

        downloads.setBackground(new Background(new BackgroundFill(Color.rgb(79, 118, 155), new CornerRadii(6, 0, 0, 6, false), new Insets(0.0))));
        finished.setBackground(new Background(new BackgroundFill(Color.rgb(63, 154, 149), new CornerRadii(0), new Insets(0.0))));
        seeders.setBackground(new Background(new BackgroundFill(Color.rgb(98, 151, 109), new CornerRadii(0, 6, 6, 0, false), new Insets(0.0))));

        downloads.setPadding(new Insets(2, 5, 2, 5));
        finished.setPadding(new Insets(2, 5, 2, 5));
        seeders.setPadding(new Insets(2, 5, 2, 5));
        magnetInfo.getChildren().addAll(downloads, finished, seeders);
        infoBox.getChildren().add(magnetInfo);
        getChildren().add(infoBox);
        listView.getItems().add(this);
    }
}

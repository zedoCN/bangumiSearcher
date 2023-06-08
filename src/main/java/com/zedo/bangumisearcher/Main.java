package com.zedo.bangumisearcher;

import com.zedo.fxcomponent.components.CustomStage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static Label titleLabel;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));
        CustomStage customStage = new CustomStage(null, stage, null);
        Pane body =
                ((Pane) customStage.rootContainer.findNode(".window-body"));
        Pane bodyC=fxmlLoader.load();
        VBox.setVgrow(bodyC,Priority.ALWAYS);
        body.getChildren().add(bodyC);
        body.getStylesheets().add(Main.class.getResource("vscode.css").toExternalForm());

        customStage.setMinSize(800, 800);
        customStage.setSize(800, 800);
        customStage.registerResize();
        customStage.registerControlButton();
        customStage.registerLayoutManagement(null);
        customStage.registerDragMove(null);


        Pane titleBar = ((Pane) customStage.rootContainer.findNode(".window-titleBar"));
        HBox titleBarBox = new HBox();
        titleBarBox.setAlignment(Pos.CENTER_LEFT);
        titleBarBox.setSpacing(6);
        titleBarBox.setPadding(new Insets(0, 4, 0, 4));
        titleLabel = new Label("BanGuMiSearcher");
        titleLabel.setTextFill(Color.WHITE);
        titleBarBox.getChildren().add(titleLabel);
        HBox.setHgrow(titleBarBox, Priority.ALWAYS);
        titleBar.getChildren().add(0, titleBarBox);
        stage.show();
    }

    public static void setTitle(String v) {
        Platform.runLater(() -> titleLabel.setText("BanGuMiSearcher  " + v));
    }

    public static void main(String[] args) {
        launch();
    }
}
package com.nobrain.nodemanager;

import com.nobrain.nodemanager.clasz.Background;
import com.nobrain.nodemanager.clasz.FileStream;
import com.nobrain.nodemanager.controllers.Controller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            Font.loadFont(getClass().getResourceAsStream("resources/main.ttf"), 10);
            Parent root = FXMLLoader.load(getClass().getResource("fxml/Main.fxml"));
            root.getStylesheets().add(this.getClass().getResource("css/style.css").toExternalForm());
            primaryStage.getIcons().addAll(new Image(Main.class.getResourceAsStream("resources/icon.png")));
            primaryStage.setResizable(false);
            primaryStage.setTitle("Node Manager");
            primaryStage.setScene(new Scene(root, 855, 530));
            primaryStage.show();
            primaryStage.setOnCloseRequest(we -> {
                if (Controller.thread != null) {
                    Controller.thread.interrupt();
                }
            });
        } catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("에러");
            alert.setHeaderText("");
            alert.setContentText(e.toString());
            alert.show();
        }

    }
    public static String getPrintStackTrace(Exception e) {

        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));

        return errors.toString();
    }




    public static void main(String[] args) {
        launch(args);
    }
}

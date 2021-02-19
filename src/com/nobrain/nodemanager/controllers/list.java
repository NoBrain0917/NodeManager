package com.nobrain.nodemanager.controllers;

import com.nobrain.nodemanager.Main;
import com.nobrain.nodemanager.clasz.Background;
import com.nobrain.nodemanager.clasz.ScriptInfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class list extends ListCell<ScriptInfo> {

    @FXML
    private Label name;

    @FXML
    private Label path;

    @FXML
    private Button button;

    @FXML
    private Pane pane;

    public Label console;

    @FXML
    private MenuItem delete;
    @FXML
    private MenuItem cmd;

    private FXMLLoader mLLoader;

    public list(Label c){
        this.console = c;
    }

    @Override
    protected void updateItem(ScriptInfo scriptInfo, boolean empty) {
        super.updateItem(scriptInfo, empty);

        if(empty || scriptInfo == null) {

            setText(null);
            setGraphic(null);

        } else {
            if (mLLoader == null) {
                mLLoader = new FXMLLoader(Main.class.getResource("fxml/list.fxml"));
                mLLoader.setController(this);

                try {
                    mLLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            delete.setOnAction(event -> {
                Background.killScript(scriptInfo.id);
                getListView().getItems().remove(getItem());
                if(Background.scriptList().size()==0) {
                    if(this.console==null) return;
                    this.console.setText("");
                }
            });
            cmd.setOnAction(event -> {
                try {
                    ProcessBuilder builder =new ProcessBuilder("cmd.exe","/c","start");
                    String status = Background.run("pm2.cmd describe " + scriptInfo.id);
                    String path = status.replaceAll("│", "").split("script path")[1].split("\n")[0].trim();
                    String[] paths = path.split("\\\\");
                    String asd = System.getProperty("user.home");
                    for(int n=paths.length-1;n>0;n--){
                        String p = paths[n];
                        boolean module = new File(path.split(p)[0]+"/node_modules").canRead();
                        if(module) {
                            asd = path.split(p)[0];
                            break;
                        }
                    }

                    builder.directory(new File(asd));
                    builder.start();

                } catch (IOException e) {
                }

            });
            name.setText(scriptInfo.name);
            if(!scriptInfo.isOnline) name.setTextFill(Color.RED);
            path.setText(scriptInfo.path);


            setText(null);
            setGraphic(pane);
        }

    }

}
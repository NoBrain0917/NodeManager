package com.nobrain.nodemanager.controllers;

import com.nobrain.nodemanager.clasz.Background;
import com.nobrain.nodemanager.clasz.FileStream;
import com.nobrain.nodemanager.clasz.ScriptInfo;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import sun.font.Script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {


    @FXML
    private ListView<ScriptInfo> listView;

    @FXML
    public Label console;

    private ObservableList<ScriptInfo> studentObservableList;

    private Stage primaryStage;

    public static Thread thread;
    public Boolean isStop = true;

    public Controller()  {

        studentObservableList = FXCollections.observableArrayList();

        //add some Students


    }

    @Override
    public void initialize(URL location, ResourceBundle resources) { ;
        ArrayList<ScriptInfo> scriptInfos = Background.scriptList();
            if (scriptInfos.size() != 0) {
                for (ScriptInfo info : scriptInfos) {
                    try {
                        info.path = info.path.replace(System.getProperty("user.home"),"").replace("Desktop\\","").split(info.name)[0];
                    } catch (Exception e) {}
                    studentObservableList.addAll(info);
                }
        }
        listView.setItems(studentObservableList);
        listView.setCellFactory(studentListView -> new list(console));


        boolean node = Background.isNodeDownload();
        boolean module = Background.isModuleDownload();
        if(!node) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("경고");
            alert.setHeaderText("");
            alert.setContentText("Node.js가 설치되어 있지 않습니다.");
            alert.showAndWait();
        }
        if(!module) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("경고");
            alert.setHeaderText("");
            alert.setContentText("pm2 모듈을 글로벌로 설치해주세요.");
            alert.showAndWait();
        }


    }


    @FXML public void onCancelClick(){
        boolean node = Background.isNodeDownload();
        boolean module = Background.isModuleDownload();
        if(!node||!module) return;
        ScriptInfo item = listView.getSelectionModel().getSelectedItem();
        if(item==null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("경고");
            alert.setHeaderText("");
            alert.setContentText("정말 모든 스크립트를 종료할까요?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                Background.stopScript();
                for(int n=0;n<studentObservableList.size();n++) {
                    ScriptInfo it = studentObservableList.get(n);
                    it.isOnline = false;
                    studentObservableList.set(n,it);
                }
            }
            listView.setItems(studentObservableList);
            listView.setCellFactory(studentListView -> new list(console));
            return;
        }
        int line = listView.getItems().indexOf(item);
        item.isOnline = false;
        studentObservableList.set(line,item);
        listView.setItems(studentObservableList);
        listView.setCellFactory(studentListView -> new list(console));
        Background.stopScript(item.id);
    }

    @FXML public void onStartClick(){
        boolean node = Background.isNodeDownload();
        boolean module = Background.isModuleDownload();
        if(!node||!module) return;
        ScriptInfo item = listView.getSelectionModel().getSelectedItem();
        if(item==null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("경고");
            alert.setHeaderText("");
            alert.setContentText("정말 모든 스크립트를 재시작할까요?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                for (ScriptInfo it : studentObservableList) {
                    Background.run("pm2.cmd restart " + it.id);
                }
            }
            ArrayList<ScriptInfo> scriptInfos = Background.scriptList();
            refresh(scriptInfos);
            return;
        }

        Background.run("pm2.cmd restart " + item.id);
        ArrayList<ScriptInfo> scriptInfos = Background.scriptList();
        refresh(scriptInfos);

    }

    @FXML public void onFileClick(){
        boolean node = Background.isNodeDownload();
        boolean module = Background.isModuleDownload();
        if(!node||!module) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Javascript", "*.js","*.ts"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if(file==null) return;
        String name = file.getName();
        new File("C:\\Users\\All User/.pm2/logs/index-out.log").delete();
        FileStream.delete(System.getProperty( "user.home" )+"/.pm2/logs/"+name.replace(".js","").replace(".ts","")+"-out.log");
        FileStream.delete(System.getProperty( "user.home" )+"/.pm2/logs/"+name.replace(".js","").replace(".ts","")+"-error.log");
        String path = file.getAbsolutePath().split(":")[1].replace(name,"");
        if(path.contains("Desktop")) {
            path = path.split("Desktop")[1];
        }
        Background.runScript(file.getAbsolutePath());
        ArrayList<ScriptInfo> info = Background.scriptList();
        ScriptInfo scriptInfo = null;
        if(info.size()!=0) {
            for (ScriptInfo it : info) {
                if (it.path.equalsIgnoreCase(file.getAbsolutePath())) {
                    scriptInfo = it;
                }
            }
            scriptInfo.path = path;
            scriptInfo.name = name;
        } else {
            scriptInfo = new ScriptInfo(name,path,false);
        }
        studentObservableList.add(scriptInfo);
        refresh(info);
        console.setText("");
        if(thread!=null) {
            thread.interrupt();
            Thread.currentThread().interrupt();
            ScriptInfo finalScriptInfo1 = scriptInfo;
            thread = new Thread(() -> {
                    try {
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("pm2.cmd log "+info.get(info.size()-1).id).getInputStream(), StandardCharsets.UTF_8));
                        String s = null;
                        while (!Thread.currentThread().isInterrupted()) {
                            s = stdInput.readLine();
                            if(s==null) {
                                break;
                            }
                            String finalS = s;
                            if(s.contains(finalScriptInfo1.name.replace(".js", "").replace(".ts", ""))) {

                                if (s.contains("|")) {
                                    if(!s.split("\\|")[1].startsWith(finalScriptInfo1.name.replace(".js", "").replace(".ts", ""))) continue;
                                    if (s.trim().startsWith("at")) {
                                        Platform.runLater(() -> consoleAppend( finalS.trim().split("\\|")[2] + "\n",finalS));
                                    } else {
                                        Platform.runLater(() -> consoleAppend((finalS.length() > 20 ? "\n" : "") + finalS.split("\\|")[2] + "\n",finalS));
                                    }
                                }
                            }
                        }

                    } catch (IOException e) {
                    }
                });
                thread.start();

        } else {
            ScriptInfo finalScriptInfo2 = scriptInfo;
            thread = new Thread(() -> {
                try {
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("pm2.cmd log "+info.get(info.size()-1).id).getInputStream(),StandardCharsets.UTF_8));
                    String s = null;
                    while (!Thread.currentThread().isInterrupted()) {
                        s = stdInput.readLine();
                        if(s==null) {
                            break;
                        }
                        String finalS = s;
                        if(s.contains(finalScriptInfo2.name.replace(".js", "").replace(".ts", ""))) {

                            if (s.contains("|")) {
                                if(!s.split("\\|")[1].startsWith(finalScriptInfo2.name.replace(".js", "").replace(".ts", ""))) continue;
                                if (s.trim().startsWith("at")) {
                                    Platform.runLater(() -> consoleAppend( finalS.trim().split("\\|")[2] + "\n",finalS));
                                } else {
                                    Platform.runLater(() -> consoleAppend((finalS.length() > 20 ? "\n" : "") + finalS.split("\\|")[2] + "\n",finalS));
                                }
                            }
                        }
                    }

                } catch (IOException e) {
                }
            });
            thread.start();
        }


    }


    @FXML public void onListClick(MouseEvent arg) {
        boolean node = Background.isNodeDownload();
        boolean module = Background.isModuleDownload();
        if(!node||!module) return;
        ScriptInfo item = listView.getSelectionModel().getSelectedItem();
        if(item==null) return;
        console.setText("");
        isStop = true;
        FileStream.delete(System.getProperty( "user.home" )+"/.pm2/logs/"+item.name.replace(".js","").replace(".ts","")+"-out.log");
        FileStream.delete(System.getProperty( "user.home" )+"/.pm2/logs/"+item.name.replace(".js","").replace(".ts","")+"-error.log");
        if(thread!=null) {
            thread.interrupt();
            Thread.currentThread().interrupt();
                thread = new Thread(() -> {
                    try {
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("pm2.cmd log "+item.id).getInputStream(),StandardCharsets.UTF_8));
                        String s = null;
                        while (!Thread.currentThread().isInterrupted()) {
                            s = stdInput.readLine();
                            if(s==null) {
                                break;
                            }
                            String finalS = s;
                            if(s.contains(item.name.replace(".js", "").replace(".ts", ""))) {
                                if (s.contains("|")) {
                                    if(!s.split("\\|")[1].startsWith(item.name.replace(".js", "").replace(".ts", ""))) continue;
                                    if (s.trim().startsWith("at")) {
                                        Platform.runLater(() -> consoleAppend( finalS.trim().split("\\|")[2] + "\n",finalS,true));
                                    } else {
                                        Platform.runLater(() -> consoleAppend((finalS.length() > 20 ? "\n" : "") + finalS.split("\\|")[2] + "\n",finalS,true));
                                    }
                                }
                            }
                        }

                    } catch (IOException e) {
                    }
                });

                thread.start();

        } else {
            thread = new Thread(() -> {
                try {
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("pm2.cmd log "+item.id).getInputStream(),StandardCharsets.UTF_8));
                    String s = null;
                    while (!Thread.currentThread().isInterrupted()) {
                        s = stdInput.readLine();
                        if(s==null) {
                            break;
                        }
                        String finalS = s;
                        if(s.contains(item.name.replace(".js", "").replace(".ts", ""))) {

                            if (s.contains("|")) {
                                if(!s.split("\\|")[1].startsWith(item.name.replace(".js", "").replace(".ts", ""))) continue;
                                if (s.trim().startsWith("at")) {
                                    Platform.runLater(() -> consoleAppend( finalS.trim().split("\\|")[2] + "\n",finalS,true));
                                } else {
                                    Platform.runLater(() -> consoleAppend((finalS.length() > 20 ? "\n" : "") + finalS.split("\\|")[2] + "\n",finalS,true));
                                }
                            }
                        }

                    }

                } catch (IOException e) {
                }
            });
            thread.start();
        }

    }

    @FXML public void onRefreshClick(){
        boolean node = Background.isNodeDownload();
        boolean module = Background.isModuleDownload();
        if(!node||!module) return;
        ArrayList<ScriptInfo> items = Background.scriptList();
        refresh(items);

    }


    public void refresh(ArrayList<ScriptInfo> infos) {
        studentObservableList = FXCollections.observableArrayList();
        for(int n=0;n<infos.size();n++) {
            ScriptInfo info = infos.get(n);
            try {
                info.path = info.path.replace(System.getProperty("user.home"),"").replace("Desktop\\","").split(info.name)[0];
            } catch (Exception e) {}
            studentObservableList.addAll(info);
        }

        listView.setItems(studentObservableList);
        listView.setCellFactory(studentListView -> new list(console));
        if(infos.size()==0) {
            console.setText("");
        }
    }

    public void consoleAppend(String text,String text2) {
    consoleAppend(text,text2,false);
    }

    public void consoleAppend(String text,String text2,boolean can) {
        if(can) {
            ScriptInfo item = listView.getSelectionModel().getSelectedItem();
            if (text2.contains("|"))
                if (!text2.split("\\|")[1].startsWith(item.name.replace(".js", "").replace(".ts", ""))) return;
        }
        String deText = console.getText().replaceAll("\n   ","");
        console.setText(deText+text+"\n   ");
    }
}

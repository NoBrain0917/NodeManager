package com.nobrain.nodemanager.clasz;

import javafx.scene.control.Alert;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class Background {

    public static boolean stopScript(){
        try {
            Runtime.getRuntime().exec("pm2.cmd stop all");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public static boolean stopScript(String id) {
        try {
            Runtime.getRuntime().exec("pm2.cmd stop "+id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean killScript(String id) {
        try {
            Runtime.getRuntime().exec("pm2.cmd delete "+id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isNodeDownload(){
        try {
            Runtime.getRuntime().exec("node");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isModuleDownload() {
        try {
            Runtime.getRuntime().exec("pm2.cmd");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static ArrayList<ScriptInfo> scriptList(){
        String strList = run("pm2.cmd list").replaceAll(" ","");

        String text = "───┤";
        String text2 = "└──";
        ArrayList<ScriptInfo> result = new ArrayList<>();
        try {
            strList = strList.split(text)[1].split(text2)[0].trim();
        } catch (Exception e) {
            return result;
        }
        String[] list = strList.split("\n");
        if(strList.contains("id")) return result;
        try {
            for (String info : list) {
                ScriptInfo scriptInfo = new ScriptInfo();
                scriptInfo.name = info.split("│")[2].trim();
                scriptInfo.id = info.split("│")[1].trim();
                scriptInfo.isOnline = info.split("│")[9].trim().equalsIgnoreCase("online");
                scriptInfo.cpu = info.split("│")[10].trim();
                scriptInfo.mem = info.split("│")[11].trim();
                scriptInfo.pid = info.split("│")[6].trim();
                String status = Background.run("pm2.cmd describe " + scriptInfo.id);
                scriptInfo.path = status.replaceAll("│", "").split("script path")[1].split("\n")[0].trim();
                String js = scriptInfo.path.split(scriptInfo.name)[1];
                scriptInfo.name = scriptInfo.name + js;
                result.add(scriptInfo);
            }
        } catch (Exception e) {
            return result;
        }
        return result;


    }

    public static boolean downloadModule(String name) {
        try {
            ProcessBuilder builder =new ProcessBuilder("cmd.exe","/c","npm.cmd i \"+name+\"-g");
            builder.directory(new File("C:\\Users\\All User"));
            builder.start();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public static void runScript(String path){
        String asd = System.getProperty("user.home");
        String[] paths = path.split("\\\\");
        for(int n=paths.length-1;n>0;n--){
            String p = paths[n];
            boolean module = new File(path.split(p)[0]+"/node_modules").canRead();
            if(module) {
                asd = path.split(p)[0];
                break;
            }
        }
        try {
        ProcessBuilder processBuilder = new ProcessBuilder("pm2.cmd","start","\""+path.replace(asd,"")+"\"");
        processBuilder.directory(new File(asd));
        InputStream inputStream = processBuilder.start().getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,StandardCharsets.UTF_8));
            String s=null;
            String result ="";
            while ((s = bufferedReader.readLine()) != null) {
                result = result+s+"\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static String run(String cmd) {
        try {
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(Runtime.getRuntime().exec(cmd).getInputStream(), StandardCharsets.UTF_8));
            String s = null;
            String result = "";
            while ((s = stdInput.readLine()) != null) {
                result = result+s+"\n";
            }
            return result;
        } catch (IOException e) {

            return e.toString();
        }

    }
}

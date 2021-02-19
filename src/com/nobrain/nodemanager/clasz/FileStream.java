package com.nobrain.nodemanager.clasz;

import java.io.*;
import java.util.Scanner;

public class FileStream {
    public static void folder(String path) {

        File file = new java.io.File(path);
        if(!file.canRead()) file.mkdir();
    }

    public static void delete(String path) {
        File file = new java.io.File(path);
        file.delete();
    }
    public static void write(String path, String content){
        try {
            FileWriter fw = new FileWriter(new java.io.File(path));
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.flush();
            bw.close();
        } catch (Exception e) {
        }
    }

    public static String read(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(new java.io.File(path)))) {

            String result = "";
            String line;
            while ((line = reader.readLine()) != null)
                result += line+"\n";

            return result;

        } catch (IOException e) {

            return "";
        }
    }

}

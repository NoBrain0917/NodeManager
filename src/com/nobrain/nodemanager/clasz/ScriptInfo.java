package com.nobrain.nodemanager.clasz;

public class ScriptInfo {
    public String path;
    public String name;
    public String id;
    public boolean isOnline;
    public String cpu;
    public String mem;
    public String pid;

    public ScriptInfo(String name, String path){
        this.name = name;
        this.path = path;
        this.isOnline = false;
    }

    public ScriptInfo(String name, String path,Boolean isOnline){
        this.name = name;
        this.path = path;
        this.isOnline = isOnline;
    }

    public ScriptInfo(){
    }

}

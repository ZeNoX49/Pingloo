package com.dbeditor.util;

public class JSON_Manager {
    private static JSON_Manager instance;
    public static JSON_Manager getInstance() {
        if (instance == null) {
            instance = new JSON_Manager();
        }
        return instance;
    }

    private JSON_Manager() {}

    /* ================================================== */

    public void load() {

    }

    public void save() {
        
    }
    
}
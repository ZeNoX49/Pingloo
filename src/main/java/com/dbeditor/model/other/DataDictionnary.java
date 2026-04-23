package com.dbeditor.model.other;

public class DataDictionnary {

    public enum DD_Type { A, AN, N }
    public enum DD_Nature { E, CA, CON }
    
    public String description;
    public DD_Type type;
    public DD_Nature nature;

    public DataDictionnary(String description, DD_Type type, DD_Nature nature) {
        this.description = description;
        this.type = type;
        this.nature = nature;
    }
    
}
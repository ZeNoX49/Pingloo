package com.dbeditor.model;

/**
 * Représente un attribut (propriété) d'une entité dans un MCD
 * Un attribut devient une colonne lors du passage au MLD
 */
public class Attribute {
    private String name;
    private String type; // Type conceptuel (texte, nombre, date, etc.)
    private boolean isIdentifier; // Est-ce l'identifiant de l'entité ?
    
    public Attribute(String name, String type) {
        this.name = name;
        this.type = type;
        this.isIdentifier = false;
    }
    
    /**
     * Constructeur de copie
     */
    public Attribute(Attribute other) {
        this.name = other.name;
        this.type = other.type;
        this.isIdentifier = other.isIdentifier;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public boolean isIdentifier() { return isIdentifier; }
    public void setIdentifier(boolean isIdentifier) { this.isIdentifier = isIdentifier; }
    
    @Override
    public String toString() {
        return name + (isIdentifier ? " (id)" : "");
    }
}
package com.dbeditor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une entité dans un MCD (Modèle Conceptuel de Données)
 * Une entité devient une table lors du passage au MLD
 */
public class Entity {
    private String name;
    private List<Attribute> attributes;
    private String identifier; // L'identifiant unique (souligné dans le MCD)
    
    public Entity(String name) {
        this.name = name;
        this.attributes = new ArrayList<>();
    }
    
    /**
     * Constructeur de copie
     */
    public Entity(Entity other) {
        this.name = other.name;
        this.identifier = other.identifier;
        this.attributes = new java.util.ArrayList<>();
        for (Attribute attr : other.attributes) {
            this.attributes.add(new Attribute(attr));
        }
    }
    
    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
    }
    
    public void removeAttribute(Attribute attribute) {
        this.attributes.remove(attribute);
    }
    
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    
    public String getIdentifier() { return this.identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    
    public java.util.List<Attribute> getAttributes() { return this.attributes; }
    
    /**
     * Trouve un attribut par son nom
     */
    public Attribute getAttribute(String name) {
        for (Attribute attr : attributes) {
            if (attr.getName().equals(name)) {
                return attr;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
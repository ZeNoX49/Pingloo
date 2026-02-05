package com.dbeditor.model;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une association (relation) entre entités dans un MCD
 * Une association a un nom (verbe à l'infinitif) et des cardinalités
 */
public class Association {
    private String name; // Verbe à l'infinitif (ex: "posseder", "commander", "appartenir")
    private Map<Entity, Cardinality> participations; // Entités participantes avec leurs cardinalités
    private List<Attribute> attributes; // Attributs de l'association (optionnel)
    
    public Association(String name) {
        this.name = name;
        this.participations = new HashMap<>();
        this.attributes = new ArrayList<>();
    }
    
    /**
     * Ajoute une entité à l'association avec sa cardinalité
     * @param entity l'entité qui participe
     * @param cardinality la cardinalité (0,1 ou 1,1 ou 0,n ou 1,n)
     */
    public void addParticipation(Entity entity, Cardinality cardinality) {
        this.participations.put(entity, cardinality);
    }
    
    /**
     * Retire une entité de l'association
     */
    public void removeParticipation(Entity entity) {
        this.participations.remove(entity);
    }
    
    /**
     * Ajoute un attribut à l'association
     * (utilisé pour les associations porteuses de données)
     */
    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Map<Entity, Cardinality> getParticipations() { return participations; }
    public List<Attribute> getAttributes() { return attributes; }
    
    /**
     * Retourne la cardinalité d'une entité dans cette association
     */
    public Cardinality getCardinality(Entity entity) {
        return participations.get(entity);
    }
    
    /**
     * Vérifie si l'association est binaire (2 entités)
     */
    public boolean isBinary() {
        return participations.size() == 2;
    }
    
    /**
     * Vérifie si l'association est ternaire (3 entités)
     */
    public boolean isTernary() {
        return participations.size() == 3;
    }
    
    /**
     * Vérifie si l'association est réflexive (une entité se relie à elle-même)
     */
    public boolean isReflexive() {
        if (participations.size() != 2) return false;
        
        List<Entity> entities = new ArrayList<>(participations.keySet());
        return entities.get(0) == entities.get(1);
    }
    
    /**
     * Retourne le type d'association pour le passage MCD→MLD
     * @return "0n-0n", "0n-1n", "1n-1n", "01-0n", "11-0n", etc.
     */
    public String getAssociationType() {
        if (!isBinary()) {
            return "N-AIRE"; // Ternaire ou plus
        }
        
        List<Cardinality> cards = new ArrayList<>(participations.values());
        Cardinality c1 = cards.get(0);
        Cardinality c2 = cards.get(1);
        
        return c1.toString() + "-" + c2.toString();
    }
    
    @Override
    public String toString() {
        return name;
    }
}
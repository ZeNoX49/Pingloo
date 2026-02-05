package com.dbeditor.model;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un Modèle Conceptuel de Données (MCD) complet
 * selon la méthodologie Merise
 */
public class ConceptualSchema {
    private String name;
    private Map<String, Entity> entities;
    private List<Association> associations;
    
    public ConceptualSchema(String name) {
        this.name = name;
        if (name == null || name.isBlank()) {
            this.name = "schema";
        }
        this.entities = new HashMap<>();
        this.associations = new ArrayList<>();
    }
    
    // === Gestion des entités ===
    
    public void addEntity(Entity entity) {
        this.entities.put(entity.getName(), entity);
    }
    
    public void removeEntity(String name) {
        Entity entity = this.entities.remove(name);
        
        // Supprimer toutes les associations impliquant cette entité
        if (entity != null) {
            associations.removeIf(assoc -> assoc.getParticipations().containsKey(entity));
        }
    }
    
    public Entity getEntity(String name) {
        return this.entities.get(name);
    }
    
    public Map<String, Entity> getEntities() {
        return this.entities;
    }
    
    // === Gestion des associations ===
    
    public void addAssociation(Association association) {
        this.associations.add(association);
    }
    
    public void removeAssociation(Association association) {
        this.associations.remove(association);
    }
    
    public List<Association> getAssociations() {
        return this.associations;
    }
    
    /**
     * Retourne toutes les associations impliquant une entité donnée
     */
    public List<Association> getAssociationsForEntity(Entity entity) {
        List<Association> result = new ArrayList<>();
        for (Association assoc : associations) {
            if (assoc.getParticipations().containsKey(entity)) {
                result.add(assoc);
            }
        }
        return result;
    }
    
    /**
     * Trouve une association par son nom
     */
    public Association getAssociation(String name) {
        for (Association assoc : associations) {
            if (assoc.getName().equals(name)) {
                return assoc;
            }
        }
        return null;
    }
    
    // === Général ===
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void clear() {
        this.name = "schema";
        this.entities.clear();
        this.associations.clear();
    }
    
    /**
     * Convertit le MCD en MLD (Modèle Logique de Données)
     * Génère les tables avec clés primaires et étrangères
     * 
     * Règles de conversion :
     * - Entité → Table
     * - Association 0,n-0,n ou 1,n-1,n → Table d'association
     * - Association X,1-0,n ou X,1-1,n → Clé étrangère côté n
     */
    public DatabaseSchema convertToMLD() {
        DatabaseSchema mld = new DatabaseSchema(this.name);
        
        // 1. Convertir toutes les entités en tables
        for (Entity entity : entities.values()) {
            Table table = entityToTable(entity);
            mld.addTable(table);
        }
        
        // 2. Traiter les associations
        for (Association assoc : associations) {
            processAssociation(assoc, mld);
        }
        
        return mld;
    }
    
    /**
     * Convertit une entité en table
     */
    private Table entityToTable(Entity entity) {
        Table table = new Table(entity.getName());
        
        for (Attribute attr : entity.getAttributes()) {
            Column col = new Column(attr.getName(), attr.getType());
            
            // Si c'est l'identifiant, c'est une clé primaire
            if (attr.isIdentifier()) {
                col.setPrimaryKey(true);
                col.setNotNull(true);
            }
            
            table.addColumn(col);
        }
        
        return table;
    }
    
    /**
     * Traite une association selon ses cardinalités
     */
    private void processAssociation(Association assoc, DatabaseSchema mld) {
        if (!assoc.isBinary()) {
            // Association n-aire → toujours une table
            createAssociationTable(assoc, mld);
            return;
        }
        
        // Association binaire : analyser les cardinalités
        List<Entity> entities = new ArrayList<>(assoc.getParticipations().keySet());
        Entity entity1 = entities.get(0);
        Entity entity2 = entities.get(1);
        
        Cardinality card1 = assoc.getCardinality(entity1);
        Cardinality card2 = assoc.getCardinality(entity2);
        
        // Cas 1: X,n - X,n → Table d'association
        if (card1.isMultiple() && card2.isMultiple()) {
            createAssociationTable(assoc, mld);
        }
        // Cas 2: X,1 - X,n → Clé étrangère côté n
        else if (card1.isOne() && card2.isMultiple()) {
            addForeignKey(mld, entity2, entity1, assoc.getName());
        }
        else if (card2.isOne() && card1.isMultiple()) {
            addForeignKey(mld, entity1, entity2, assoc.getName());
        }
        // Cas 3: X,1 - X,1 → Clé étrangère au choix (on choisit côté optionnel si possible)
        else if (card1.isOne() && card2.isOne()) {
            if (card1.isOptional()) {
                addForeignKey(mld, entity1, entity2, assoc.getName());
            } else {
                addForeignKey(mld, entity2, entity1, assoc.getName());
            }
        }
    }
    
    /**
     * Crée une table d'association
     */
    private void createAssociationTable(Association assoc, DatabaseSchema mld) {
        String tableName = assoc.getName();
        Table table = new Table(tableName);
        
        // Ajouter les clés étrangères vers chaque entité participante
        for (Entity entity : assoc.getParticipations().keySet()) {
            String fkName = entity.getName().toLowerCase() + "_id";
            Column fkCol = new Column(fkName, "INT");
            fkCol.setPrimaryKey(true); // Part de la clé primaire composée
            fkCol.setNotNull(true);
            table.addColumn(fkCol);
            
            // Ajouter la foreign key
            ForeignKey fk = new ForeignKey(fkName, entity.getName(), entity.getName(), getIdentifierColumn(entity));
            table.addForeignKey(fk);
        }
        
        // Ajouter les attributs de l'association
        for (Attribute attr : assoc.getAttributes()) {
            Column col = new Column(attr.getName(), attr.getType());
            table.addColumn(col);
        }
        
        mld.addTable(table);
    }
    
    /**
     * Ajoute une clé étrangère dans une table
     */
    private void addForeignKey(DatabaseSchema mld, Entity from, Entity to, String assocName) {
        Table fromTable = mld.getTable(from.getName());
        if (fromTable == null) return;
        
        String fkName = to.getName().toLowerCase() + "_id";
        Column fkCol = new Column(fkName, "INT");
        fkCol.setNotNull(false); // Peut être null selon la cardinalité
        fromTable.addColumn(fkCol);
        
        ForeignKey fk = new ForeignKey(fkName, to.getName(), to.getName(), getIdentifierColumn(to));
        fromTable.addForeignKey(fk);
    }
    
    /**
     * Retourne le nom de la colonne identifiant d'une entité
     */
    private String getIdentifierColumn(Entity entity) {
        for (Attribute attr : entity.getAttributes()) {
            if (attr.isIdentifier()) {
                return attr.getName();
            }
        }
        // Par défaut, chercher "id"
        return "id";
    }
    
    @Override
    public String toString() {
        return "MCD: " + name + " (" + entities.size() + " entités, " + 
               associations.size() + " associations)";
    }
}
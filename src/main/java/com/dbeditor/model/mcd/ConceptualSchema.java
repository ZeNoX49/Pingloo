package com.dbeditor.model.mcd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dbeditor.MainApp;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;

/**
 * Permet de représenter un MCD
 */
public class ConceptualSchema {
    private final Map<String, Table> entities = new HashMap<>();
    private final Map<String, Table> associations = new HashMap<>();

    public ConceptualSchema(DatabaseSchema schema) {
        for(Table table : schema.tables.values()) {
            if(this.isAssociativeTable(table)) {
                this.associations.put(table.name, table);
            } else {
                this.entities.put(table.name, table);
            }
        }
    }

    /**
     * Vérifie si la table est un association
     * telle que -> (fk -> pk) >= 2
     */
    private boolean isAssociativeTable(Table table) {
        int nb = 0;
        
        for(ForeignKey fk : table.getForeignKeys()) {
            if(fk.isPrimaryKey) {
                nb++;
            }
        }

        return nb >= 2;
    }

    public boolean nameExists(String name) {
        return this.entities.containsKey(name) || this.associations.containsKey(name);
    }

    /**
     * Permet d'ajouter une entité.
     * L'ajoute aussi dans le schema de MainApp
     */
    public void addEntity(Table table) {
        this.entities.put(table.name, table);
        MainApp.schema.addTable(table);
    }

    /**
     * Met à jour une entité et toutes les associations associés.
     * Le fait aussi dans le schema de MainApp
     */
    public void updateEntity(String oldName, Table updatedTable) {
        Table old = this.entities.remove(oldName);
        if (old == null) return;

        this.entities.put(updatedTable.name, updatedTable);

        MainApp.schema.tables.remove(oldName);
        MainApp.schema.addTable(updatedTable);

        // mettre à jour les associations qui référencent l'ancienne entité
        for(Table t : this.associations.values()) {
            for(ForeignKey fk : t.getForeignKeys()) {
                if(oldName.equals(fk.referencedTable)) {
                    fk.referencedTable = updatedTable.name;
                }
            }
        }
    }

    /**
     * Supprime une entité et toutes les associations associés.
     * La supprime aussi dans le schema de MainApp
     */
    public void removeEntity(String name) {
        Table e = this.entities.remove(name);
        if (e == null) return;

        MainApp.schema.tables.remove(name);

        // supprimer les associations qui contiennent cette entité
        Iterator<Entry<String, Table>> itAssociation = this.associations.entrySet().iterator();
        while (itAssociation.hasNext()) {
            Table asso =  itAssociation.next().getValue();
            
            Iterator<Entry<String, ForeignKey>> itFk = asso.foreignKeys.entrySet().iterator();
            while(itFk.hasNext()) {
                ForeignKey fk = itFk.next().getValue();

                if(name.equals(fk.referencedTable)) {
                    itFk.remove();

                    if(asso.foreignKeys.size() < 2) {
                        itAssociation.remove();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Ajoute une association entre des entités
     */
    public void addAssociation(Table table) {
        this.associations.put(table.name, table);
        MainApp.schema.addTable(table);
    }

    /**
     * Modifie les participants d'une association existante.
     * Utile après édition dans AssociationEditorDialog.
     */
    public void updateAssociation(String oldName, Table updatedTable) {
        Table old = this.associations.remove(oldName);
        if (old == null) return;

        this.associations.put(updatedTable.name, updatedTable);

        MainApp.schema.tables.remove(oldName);
        MainApp.schema.addTable(updatedTable);
    }

    /**
     * Supprime une association par son nom.
     */
    public void removeAssociation(String name) {
        Table asso = this.associations.remove(name);
        if (asso == null) return;

        MainApp.schema.tables.remove(name);
    }

    /**
     * Retourne la table associé au nom de l'entité,
     * null si elle n'existe pas
     */
    public Table getEntity(String name) {
        return this.entities.get(name);
    }

    /**
     * Retourne toutes les tables associés aux entités
     */
    public List<Table> getAllEntities() {
        return new ArrayList<>(this.entities.values());
    }

    /**
     * Retourne la table associé au nom de l'entité,
     * null si elle n'existe pas
     */
    public Table getAssociation(String name) {
        return this.associations.get(name);
    }
    
    /**
     * Retourne toutes les tables associés aux associations
     */
    public List<Table> getAllAssociations() {
        return new ArrayList<>(this.associations.values());
    }
}
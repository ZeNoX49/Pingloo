package com.dbeditor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.dbeditor.sql.DbType;

public class ConceptualSchema {
    public String name;
    public DbType type;
    private final Map<String, Entity> entities;
    private final Map<String, Association> associations;
    
    public ConceptualSchema(String name) {
        this.name = (name == null || name.isBlank()) ? "db" : name.strip();
        this.type = DbType.MySql;
        this.entities = new HashMap<>();
        this.associations = new HashMap<>();
    }

    public void addEntity(Entity entity) {
        this.entities.put(entity.name, entity);
    }

    public void updateEntity(String oldName, Entity newEntity) {
        this.entities.remove(oldName);
        this.entities.put(newEntity.name, newEntity);
        
        for(Association a : this.associations.values()) {
            for(ForeignKey fk : a.foreignKeys.values()) {
                if(fk.referencedEntity.equals(oldName)) {
                    fk.referencedEntity = newEntity.name;
                }
            }
        }
    }

    public void removeEntity(String entityName) {
        this.entities.remove(entityName);
        
        Iterator<Map.Entry<String, Association>> itAsso = this.associations.entrySet().iterator();
        while(itAsso.hasNext()) {
            Association a = itAsso.next().getValue();

            Iterator<Map.Entry<String, ForeignKey>> itFk = a.foreignKeys.entrySet().iterator();
            while(itFk.hasNext()) {
                ForeignKey fk = itFk.next().getValue();
                if(fk.referencedEntity.equals(entityName)) {
                    itFk.remove();

                    if(a.foreignKeys.size() == 1) {
                        itAsso.remove();
                        break;
                    }
                }
            }
        }
    }

    public void addAssociation(Association association) {
        this.associations.put(association.name, association);
    }

    public void updateAssociation(String oldName, Association newAssociation) {
        this.removeAssociation(oldName);
        this.addAssociation(newAssociation);
    }

    public void removeAssociation(String associationName) {
        this.associations.remove(associationName);
    }

    public boolean nameExists(String name) {
        return (this.entities.containsKey(name) || this.associations.containsKey(name));
    }

    public Entity getEntitiy(String name) {
        return this.entities.get(name);
    }

    public List<Entity> getEntities() {
        return new ArrayList<>(this.entities.values());
    }

    public Association getAssociation(String name) {
        return this.associations.get(name);
    }

    public List<Association> getAssociations() {
        return new ArrayList<>(this.associations.values());
    }
}
package com.dbeditor.model;

public class Entity extends ConceptualNode {

    public Entity(String name) {
        super(name);
    }

    public Entity(Entity other) {
        super(other);
    }
}
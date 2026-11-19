package com.dbeditor.controller.modifier;

import com.dbeditor.model.DatabaseSchema;

public interface DbUpdate {

    /**
     * Permet de charger une bdd
     */
    public abstract void open(DatabaseSchema schema);

}

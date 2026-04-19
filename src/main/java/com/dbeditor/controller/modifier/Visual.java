package com.dbeditor.controller.modifier;

import com.dbeditor.sql.DbType;

public interface Visual {

    /**
     * Permet de mettre à jour le style au lancement de l'app
     * ou lors d'un changement de style
     */
    public void updateStyle();

    /**
     * Permet de mettre a jour les textes lors d'un changement de type de bdd
     * @param type
     */
    public abstract void updateType();
    
}
package com.dbeditor.controller.modifier;

import java.io.IOException;

import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.sql.DbType;

public interface DbUpdate {

    /**
     * Permet de charger une bdd
     * @param schema DatabaseSchema de la bdd
     */
    public abstract void open(DatabaseSchema schema) throws IOException;

    /**
     * Permet de mettre a jour les textes lors d'un changement de type de bdd
     * @param type
     */
    public abstract void updateType(DbType type);
    
    // /**
    //  * Envoie une notif autre vues pour toutes les mettre a jour
    //  * @param view view envoyant la notif pour qu'elle ne recoive pas la notif
    //  */
    // public abstract DatabaseSchema onSyncGoing(View view);

    // /**
    //  * Recoit une notif de mettre a jour sa version
    //  */
    // public abstract void onSyncComing(DatabaseSchema dbS);
    
}
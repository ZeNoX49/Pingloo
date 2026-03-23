package com.dbeditor.controller.modifier;

import java.io.IOException;

import com.dbeditor.controller.view.View;
import com.dbeditor.model.DatabaseSchema;

public interface Update {

    /**
     * Permet de charger une bdd
     * @param dbS -> le DatabaseSchema de la bdd
     */
    public abstract void open(DatabaseSchema dbS) throws IOException;

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
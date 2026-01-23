package com.dbeditor.sql.file.exporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;

public abstract class SQL_Exporter {
    
    /**
     * Permet de faire un export de la bdd si elle est valide
     * @param schema schema de la bdd
     * @param filepath chemin de sauvegarde
     */
    public abstract void exportToSQL(DatabaseSchema schema, String filepath) throws IOException;

    /**
     * Permet de trier les tables dans l'ordre de création pour la bdd
     * Renvoie une erreur si il y a un problème
     * @param tables
     * @return
     */
    protected List<Table> sortTables(Collection<Table> tables) {
        List<Table> res = new ArrayList<>();
        List<Table> toAdd = new ArrayList<>(tables);

        // 1 - On ajoute toutes les tables sans FK
        Iterator<Table> it = toAdd.iterator();
        while(it.hasNext()) {
            Table t = it.next();
            if(t.getForeignKeys().isEmpty()) {
                res.add(t);
                it.remove();
            }
        }

        // 2 - On ajoute les tables dont toutes les FK sont déjà dans res
        boolean added;
        do {
            added = false;
            it = toAdd.iterator();
            while(it.hasNext()) {
                Table t = it.next();
                boolean allFKResolved = true;

                for(ForeignKey fk : t.getForeignKeys()) {
                    String refTable = fk.getReferencedTable();

                    // si c'est notre table actuelle : on passe
                    if(refTable.equals(t.getName())) continue;

                    boolean fkFound = false;
                    for(Table r : res) {
                        if(refTable.equals(r.getName())) {

                            fkFound = true;
                            break;
                        }
                    }

                    if(!fkFound) {
                        allFKResolved = false;
                        break;
                    }
                }

                if(allFKResolved) {
                    res.add(t);
                    it.remove();
                    added = true;
                }
            }

            // Si aucune table n'a été ajoutée, on a une FK circulaire
            if(!added && !toAdd.isEmpty()) {
                throw new IllegalStateException("FK circulaire détectée : " + toAdd);
            }

        } while(!toAdd.isEmpty());

        return res;
    }
}
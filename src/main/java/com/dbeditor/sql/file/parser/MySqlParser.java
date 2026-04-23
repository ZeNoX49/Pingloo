package com.dbeditor.sql.file.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dbeditor.MainApp;
import com.dbeditor.model.ConceptualSchema;
import com.dbeditor.model.Entity;

import net.sf.jsqlparser.statement.create.table.CreateTable;

public class MySqlParser extends SqlParser {
    private static final Logger LOGGER = Logger.getLogger(SqlParser.class.getName());
    
    @Override
    public void loadFromFile(String filePath) {
        ConceptualSchema schema = new ConceptualSchema("");

        try {
            String sql = Files.readString(Path.of(filePath));

            // Split SQL statements manually
            String[] statements = sql.split(";");

            for (String stmtSql : statements) {
                String trimmed = stmtSql.trim();
                if (trimmed.isEmpty()) continue;

                // ---- Handle USE db; manually ----
                if (trimmed.toUpperCase().startsWith("USE ")) {
                    String dbName = trimmed.substring(4).trim();
                    schema.name = dbName;
                    continue;
                }

                // ---- Skip unsupported statements ----
                if (trimmed.toUpperCase().matches("^(CREATE|DROP)\\s+DATABASE.*")) {
                    continue;
                }

                // try {
                //     Statement stmt = CCJSqlParserUtil.parse(trimmed);

                //     if (stmt instanceof CreateTable createTable) {
                //         schema.addTable(getTable(createTable));
                //     }

                // } catch (JSQLParserException ignored) {
                //     // Skip unsupported statements
                // }
            }

            MainApp.schema = schema;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            MainApp.schema = new ConceptualSchema("");
        }
    }

    /**
     * permet de créer une table avec tout ce qu'elle possède
     * @param createTable sortie du stmt
     * @return la table créer
     */
    private Entity getTable(CreateTable createTable) {
        Entity table = new Entity(createTable.getTable().getName());
        // /* ---- Columns ---- */
        // if (createTable.getColumnDefinitions() != null) {
        //     for (ColumnDefinition col : createTable.getColumnDefinitions()) {
        //         String type = col.getColDataType().getDataType();
        //         Column column = new Column(
        //             col.getColumnName(),
        //             __SqlType.get(type, DbType.MySql)
        //         );

        //         // si la colonne n'a pas de specs
        //         if(col.getColumnSpecs() == null || col.getColumnSpecs().isEmpty()) {
        //             table.addColumn(column);
        //             continue;
        //         }

        //         if(col.getColumnSpecs().contains("PRIMARY") && col.getColumnSpecs().contains("KEY")) {
        //             column.isPrimaryKey = true;
        //             column.isUnique = true;
        //             column.isNotNull = true;
        //         }

        //         if(col.getColumnSpecs().contains("AUTO_INCREMENT")) {
        //             column.isAutoIncrementing = true;
        //         }

        //         if(col.getColumnSpecs().contains("NOT") && col.getColumnSpecs().contains("NULL")) {
        //             column.isNotNull = true;
        //         }

        //         if(col.getColumnSpecs().contains("UNIQUE")) {
        //             column.isUnique = true;
        //         }

        //         // if(col.getColumnSpecs().contains("DEFAULT")) {
        //         //     column.
        //         // }

        //         // if(col.getColumnSpecs().contains("CHECK")) {
        //         //     column.
        //         // }

        //         table.addColumn(column);
        //     }
        // }

        // /* ---- Foreign keys ---- */
        // if (createTable.getIndexes() != null) {
        //     for (Index idx : createTable.getIndexes()) {
        //         if (idx instanceof ForeignKeyIndex fk) {
        //             // Much safer than parsing strings
        //             String fkName = fk.getName();

        //             String columnName = fk.getColumnsNames().get(0);
        //             String refTable = fk.getTable().getName();
        //             String refColumn = fk.getReferencedColumnNames().get(0);

        //             if(fkName == null || fkName.isEmpty()) {
        //                 fkName = "fk_" + table.name.toLowerCase() + "_" + refTable.toLowerCase();
        //             }

        //             table.addForeignKey(
        //                 new ForeignKey(fkName, columnName, refTable, refColumn)
        //             );
        //         }
        //     }
        // }

        return table;
    }
}
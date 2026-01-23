package com.dbeditor.sql.file.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.dbeditor.model.Column;
import com.dbeditor.model.DatabaseSchema;
import com.dbeditor.model.ForeignKey;
import com.dbeditor.model.Table;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;

public class MYSQL_FileParser implements SQL_FileParser {
    
    @Override
    public DatabaseSchema loadFromFile(String filePath) {
        DatabaseSchema schema = new DatabaseSchema();

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
                    schema.setName(dbName);
                    continue;
                }

                // ---- Skip unsupported statements ----
                if (trimmed.toUpperCase().matches("^(CREATE|DROP)\\s+DATABASE.*")) {
                    continue;
                }

                try {
                    Statement stmt = CCJSqlParserUtil.parse(trimmed);

                    if (stmt instanceof CreateTable createTable) {
                        schema.addTable(getTable(createTable));
                    }

                } catch (JSQLParserException ignored) {
                    // Skip unsupported statements
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return schema;
    }

    /**
     * permet de créer une table avec tout ce qu'elle possède
     * @param createTable sortie du stmt
     * @return la table créer
     */
    private Table getTable(CreateTable createTable) {
        Table table = new Table(createTable.getTable().getName());
        /* ---- Columns ---- */
        if (createTable.getColumnDefinitions() != null) {
            for (ColumnDefinition col : createTable.getColumnDefinitions()) {
                Column column = new Column(
                    col.getColumnName(),
                    col.getColDataType().getDataType()
                );

                // si la colonne n'a pas de specs
                if(col.getColumnSpecs() == null || col.getColumnSpecs().isEmpty()) {
                    table.addColumn(column);
                    continue;
                }

                if(col.getColumnSpecs().contains("PRIMARY") && col.getColumnSpecs().contains("KEY")) {
                    column.setPrimaryKey(true);
                }

                if(col.getColumnSpecs().contains("AUTO_INCREMENT")) {
                    column.setAutoIncrementing(true);
                }

                if(col.getColumnSpecs().contains("NOT") && col.getColumnSpecs().contains("NULL")) {
                    column.setNotNull(true);
                }

                if(col.getColumnSpecs().contains("UNIQUE")) {
                    column.setUnique(true);
                }

                // if(col.getColumnSpecs().contains("DEFAULT")) {
                //     column.
                // }

                // if(col.getColumnSpecs().contains("CHECK")) {
                //     column.
                // }

                table.addColumn(column);
            }
        }

        /* ---- Foreign keys ---- */
        if (createTable.getIndexes() != null) {
            for (Index idx : createTable.getIndexes()) {
                if (idx instanceof ForeignKeyIndex fk) {
                    // Much safer than parsing strings
                    String fkName = fk.getName();

                    String columnName = fk.getColumnsNames().get(0);
                    String refTable = fk.getTable().getName();
                    String refColumn = fk.getReferencedColumnNames().get(0);

                    if(fkName == null || fkName.isEmpty()) {
                        fkName = "fk_" + table.getName().toLowerCase() + "_" + refTable.toLowerCase();
                    }

                    table.addForeignKey(
                        new ForeignKey(fkName, columnName, refTable, refColumn)
                    );
                }
            }
        }

        return table;
    }
}
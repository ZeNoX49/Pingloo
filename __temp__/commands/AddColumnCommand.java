// package com.dbeditor.commands;

// import com.dbeditor.model.Column;
// import com.dbeditor.model.DatabaseSchema;
// import com.dbeditor.model.Table;

// public class AddColumnCommand implements Command {
//     private DatabaseSchema schema;
//     private String tableName;
//     private Column column;
    
//     public AddColumnCommand(DatabaseSchema schema, String tableName, Column column) {
//         this.schema = schema;
//         this.tableName = tableName;
//         this.column = column;
//     }
    
//     @Override
//     public void execute() {
//         Table table = schema.getTable(tableName);
//         if (table != null) {
//             table.addColumn(column);
//         }
//     }
    
//     @Override
//     public void undo() {
//         Table table = schema.getTable(tableName);
//         if (table != null) {
//             table.removeColumn(column);
//         }
//     }
    
//     @Override
//     public DatabaseSchema getSchema() { return schema; }
// }
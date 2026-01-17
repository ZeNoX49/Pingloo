// package com.dbeditor.commands;

// import java.util.Stack;

// public class CommandHistory {
//     private Stack<Command> undoStack = new Stack<>();
//     private Stack<Command> redoStack = new Stack<>();
    
//     public void execute(Command command) {
//         command.execute();
//         undoStack.push(command);
//         redoStack.clear();
//     }
    
//     public void undo() {
//         if (!undoStack.isEmpty()) {
//             Command command = undoStack.pop();
//             command.undo();
//             redoStack.push(command);
//         }
//     }
    
//     public void redo() {
//         if (!redoStack.isEmpty()) {
//             Command command = redoStack.pop();
//             command.execute();
//             undoStack.push(command);
//         }
//     }
    
//     public boolean canUndo() { return !undoStack.isEmpty(); }
//     public boolean canRedo() { return !redoStack.isEmpty(); }
//     public void clear() { undoStack.clear(); redoStack.clear(); }
// }
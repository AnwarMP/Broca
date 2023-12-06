package repl;

import java.util.*;

public class SymbolTable {
    private Map<String, Object> symbols = new HashMap<>();
    private SymbolTable parent; // Reference to parent symbol table

    private static final SymbolTable globalInstance = new SymbolTable();

    SymbolTable() {
        this.parent = null; // Global symbol table has no parent
    }

    // Constructor for creating a local scope symbol table with a reference to its parent
    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public Object get(String varName) {
        Object value = symbols.get(varName);
        if (value == null && parent != null) {
            return parent.get(varName); // Check in the parent symbol table
        }
        return value;
    }

    public void put(String varName, Object value) {
        symbols.put(varName, value);
    }

    public void remove(String varName) {
        symbols.remove(varName);
    }

    public boolean isEmpty() {
        return symbols.isEmpty();
    }

    public boolean hasSymbol(String key) {
        return symbols.containsKey(key) || (parent != null && parent.hasSymbol(key));
    }

    public static SymbolTable getInstance() {
        return globalInstance;
    }
}

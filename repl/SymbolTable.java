package repl;

import java.util.*;
import parser.*;
import lexer.*;

public class SymbolTable{
    private Map<String, Object> symbols = new HashMap<>();

    private static final SymbolTable instance = new SymbolTable();

    public SymbolTable(){

    }

    public Object get(String varName){
        Object value = symbols.get(varName);
        return value;
    }

    public void put(String varName, Object value){
        symbols.put(varName, value);
    }

    public void remove(String varName){
        symbols.remove(varName);
    }

    public boolean isEmpty(){
        return symbols.isEmpty();
    }

    public static SymbolTable getInstance(){
        return instance;
    }
}

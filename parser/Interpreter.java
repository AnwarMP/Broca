package parser;

import java.util.*;

import lexer.*;
import parser.*;
import repl.*;

public class Interpreter {

    private static SymbolTable symbolTable = SymbolTable.getInstance();

    public Object interpret(ASTNode node) {
        Object result = visitNode(node);
        System.out.println(result);
        return result;
    }

    private Object visitNode(ASTNode node){ //Visit the ASTNodes of the AST
        if (node instanceof NumberNode) {
            return visitNumberNode((NumberNode) node);
        } 
        else if (node instanceof UnaryOpNode) {
            return visitUnaryNode((UnaryOpNode) node);
        } 
        else if (node instanceof BinOpNode) {
            return visitBinOpNode((BinOpNode) node);
        }
        else if(node instanceof VarAccessNode){
            return visitVarAccessNode((VarAccessNode) node);
        }
        else if(node instanceof VarAssignNode){
            return visitVarAssignNode((VarAssignNode) node);
        }
        else {
            throw new RuntimeException("Unknown node type");
        }
    }

    private Object visitVarAccessNode(VarAccessNode node){
        String varName = (String) node.getToken().getLiteral();
        System.out.println("hashmap is empty: " + symbolTable.isEmpty());
        Object value = symbolTable.get(varName);

        if(value == null){
            throw new RuntimeException(varName + " is null");
        }

        return value;
    }

    private Object visitVarAssignNode(VarAssignNode node){
        String varName = node.getVarName();
        Object value = visitNode(node.getValueNode());

        symbolTable.put(varName, value);
        return value;
    }

    private Object visitNumberNode(NumberNode node) {
        //System.out.println("Number Node.");
        Object literal = node.getToken().getLiteral();
    
        // Literals are stored as Strings, so parse accordingly and return
        if (node.getToken().getType() == TokenType.DOUBLE) {
            return Double.parseDouble(literal.toString());
        } else if (node.getToken().getType() == TokenType.INT) {
            return Integer.parseInt(literal.toString());
        }
    
        throw new RuntimeException("Unknown number type");
    }
    
    private Object visitUnaryNode(UnaryOpNode node) {
        //System.out.println("Unary Node.");
        Object operand = visitNode(node.getNode());
    
        if (node.getOperator().getType() == TokenType.MINUS) {
            if (operand instanceof Double) {
                return -(Double) operand;
            } else if (operand instanceof Integer) {
                return -(Integer) operand;
            }
        }
    
        throw new RuntimeException("Unsupported unary operation");
    }

    private Object visitBinOpNode(BinOpNode node) {
        //System.out.println("BinOp Node.");
        Object leftObject = visitNode(node.getLeftNode());
        Object rightObject = visitNode(node.getRightNode());
    
        TokenType operator = node.getOperator().getType();
    
        // Handling different combinations of operand types
        if (leftObject instanceof Integer && rightObject instanceof Integer) {
            int left = (Integer) leftObject;
            int right = (Integer) rightObject;
            return performIntArithmetic(left, right, operator);
        } else {
            // Promote to double if mixed types or both are doubles
            double left = leftObject instanceof Double ? (Double) leftObject : (Integer) leftObject;
            double right = rightObject instanceof Double ? (Double) rightObject : (Integer) rightObject;
            return performDoubleArithmetic(left, right, operator);
        }
    }
    
    /*
     * Arithmetic Functions for Binary Operator Node
     */
    private Integer performIntArithmetic(int left, int right, TokenType operator) {
        switch (operator) {
            case PLUS: return left + right;
            case MINUS: return left - right;
            case MUL: return left * right;
            case DIV: return left / right;  // Handle division by zero appropriately
            default: throw new RuntimeException("Unknown operator for integers");
        }
    }
    
    private Double performDoubleArithmetic(double left, double right, TokenType operator) {
        switch (operator) {
            case PLUS: return left + right;
            case MINUS: return left - right;
            case MUL: return left * right;
            case DIV: return left / right;  // Handle division by zero appropriately
            default: throw new RuntimeException("Unknown operator for doubles");
        }
    }
    
}

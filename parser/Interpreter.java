package parser;


import java.util.*;

import lexer.*;
import parser.*;
import repl.*;

public class Interpreter {

    private static SymbolTable symbolTable = SymbolTable.getInstance();

    public Object interpret(ASTNode node) {
        Object result = visitNode(node);
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
    
        Token operator = node.getOperator();
    
        // Handling different combinations of operand types
        if (operator.getType() == TokenType.EE) {
            return compare(leftObject, rightObject) == 0 ? 1 : 0;
        }
        else if (operator.getType() == TokenType.NE) {
            return compare(leftObject, rightObject) != 0 ? 1 : 0;
        }
        else if (operator.getType() == TokenType.LT) {
            return compare(leftObject, rightObject) < 0 ? 1 : 0;
        }
        else if (operator.getType() == TokenType.GT) {
            return compare(leftObject, rightObject) > 0 ? 1 : 0;
        }
        else if (operator.getType() == TokenType.LTE) {
            return compare(leftObject, rightObject) <= 0 ? 1 : 0;
        }
        else if (operator.getType() == TokenType.GTE) {
            return compare(leftObject, rightObject) >= 0 ? 1 : 0;
        }
        else if (operator.getType() == TokenType.KEYWORD && operator.getLexeme().equals("AND")) {
            return (isTruth(leftObject) && isTruth(rightObject)) ? 1 : 0;
        }
        else if (operator.getType() == TokenType.KEYWORD && operator.getLexeme().equals("OR")) {
            return (isTruth(leftObject) || isTruth(rightObject)) ? 1 : 0;
        }
        
        else if (leftObject instanceof Integer && rightObject instanceof Integer) {
            int left = (Integer) leftObject;
            int right = (Integer) rightObject;
            return performIntArithmetic(left, right, operator.getType());
        } else {
            // Promote to double if mixed types or both are doubles
            double left = leftObject instanceof Double ? (Double) leftObject : (Integer) leftObject;
            double right = rightObject instanceof Double ? (Double) rightObject : (Integer) rightObject;
            return performDoubleArithmetic(left, right, operator.getType());
        }
    }
    
    /*
     * Arithmetic and Helper Functions for Binary Operator Node
     */
    private int compare(Object a, Object b) {
        double aDouble = toDouble(a);
        double bDouble = toDouble(b);
        return Double.compare(aDouble, bDouble);
    }
    
    private boolean isTruth(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue() != 0;
        }
        throw new RuntimeException("Unsupported type for truth evaluation");
    }
    
    private double toDouble(Object obj) {
        if (obj instanceof Double) {
            return (Double) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).doubleValue();
        }
        throw new RuntimeException("Unsupported type for conversion to double");
    }
    
    private Integer performIntArithmetic(int left, int right, TokenType operator) {
        switch (operator) {
            case PLUS: return left + right;
            case MINUS: return left - right;
            case MUL: return left * right;
            case DIV: return left / right;  // Handle division by zero
            default: throw new RuntimeException("Unknown operator for integers");
        }
    }
    
    private Double performDoubleArithmetic(double left, double right, TokenType operator) {
        switch (operator) {
            case PLUS: return left + right;
            case MINUS: return left - right;
            case MUL: return left * right;
            case DIV: return left / right;  // Handle division by zero
            default: throw new RuntimeException("Unknown operator for doubles");
        }
    }
    
}

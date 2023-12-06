package parser;


import java.util.*;
import java.util.stream.Collectors;

import lexer.*;
import parser.*;
import repl.*;

public class Interpreter {

    //private static SymbolTable symbolTable = SymbolTable.getInstance();
    private SymbolTable symbolTable;

    public Interpreter() {
        this.symbolTable = SymbolTable.getInstance(); // Use the global symbol table instance
    }

    // Constructor for local scope
    private Interpreter(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

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
        else if(node instanceof IfNode){
            return visitIfNode((IfNode) node);
        }
        else if(node instanceof ForNode){
            return visitForNode((ForNode) node);
        }
        else if(node instanceof WhileNode){
            return visitWhileNode((WhileNode) node);
        }
        else if(node instanceof FuncDefNode){
            return visitFuncDefNode((FuncDefNode) node);
        }
        else if(node instanceof CallNode){
            return visitCallNode((CallNode) node);
        }
        else {
            throw new RuntimeException("Unknown node type");
        }
    }

    private Object visitFuncDefNode(FuncDefNode node) {
        String funcName = node.getVarToken().getLexeme();
        List<String> argNames = node.getArgNameTokens().stream()
                                    .map(Token::getLexeme)
                                    .collect(Collectors.toList());

        // Here you store the function definition in your symbol table.
        // You need to define a class or a structure to store function definitions.
        FunctionDefinition funcDef = new FunctionDefinition(argNames, node.getBodyNode());
        symbolTable.put(funcName, funcDef);

        return "<function: " + funcName +">"; // Function definition doesn't return a value during definition time
    }

    private Object visitCallNode(CallNode node) {
        Object toCall = visitNode(node.getCallFuncNode());
        
        // Check if 'toCall' is a function
        if (!(toCall instanceof FunctionDefinition)) {
            throw new RuntimeException("Attempt to call a non-function");
        }

        FunctionDefinition funcDef = (FunctionDefinition) toCall;
        List<Object> argValues = new ArrayList<>();
        for (ASTNode argNode : node.getArgNodes()) {
            argValues.add(visitNode(argNode));
        }

        // Create a new symbol table for the function scope
        SymbolTable functionSymbolTable = new SymbolTable(symbolTable);

        // Assign argument values to names in the function scope
        if (argValues.size() != funcDef.getArgNames().size()) {
            throw new RuntimeException("Incorrect number of arguments provided");
        }
        for (int i = 0; i < argValues.size(); i++) {
            functionSymbolTable.put(funcDef.getArgNames().get(i), argValues.get(i));
        }

        // Execute the function with its own symbol table
        return new Interpreter(functionSymbolTable).visitNode(funcDef.getBodyNode());
    }




    private Object visitForNode(ForNode node){

        Object startValue = this.visitNode(node.getStartNode());
        Object endValue = this.visitNode(node.getEndNode());
        
        Object stepValue;
        if(node.getStepNode() != null){
            stepValue = this.visitNode(node.getStepNode());
        }
        else {
            stepValue = 1;
        }

        // Convert to double
        double i = ((Number) startValue).doubleValue();
        double end = ((Number) endValue).doubleValue();
        double step = ((Number) stepValue).doubleValue();

        // Determine the loop condition based on the step value
        Object bodyResult = 0;
        while ((step >= 0) ? i < end : i > end) {
            // Update the variable in the symbol table
            
            symbolTable.put(node.getVarToken().getLexeme(), i);

            // Visit the body of the for loop
            bodyResult = this.visitNode(node.getBodyNode());

            // Increment the loop variable
            i += step;
        }

        return bodyResult;
    }

    private Object visitWhileNode(WhileNode node){
        // Continuously evaluate the condition
        Object bodyResult = 0;
        while (true) {
            // Evaluate the condition node
            Object conditionValue = this.visitNode(node.getConditionNode());

            // Check if the condition is true
            if (!isTruth(conditionValue)) {
                break; // Exit the loop if the condition is false
            }

            // Execute the body of the loop
            bodyResult = this.visitNode(node.getBodyNode());
        }

        return bodyResult; 
    }


    private Object visitIfNode(IfNode node){
        List<Case> cases = node.getCases();

        for(int i = 0; i < cases.size(); i++){
            Case currCase = cases.get(i);
            Object conditionValue = this.visitNode(currCase.getCondition());

            if(isTruth(conditionValue)){
                Object expressionValue = this.visitNode(currCase.getExpression());
                return expressionValue;
            }
        }

        if(node.getElseCase() != null){
            Object expressionValue = this.visitNode(node.getElseCase());
            return expressionValue;
        }

        return null;
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

class FunctionDefinition {
    private List<String> argNames; // Names of the arguments
    private ASTNode bodyNode; // The body of the function

    public FunctionDefinition(List<String> argNames, ASTNode bodyNode) {
        this.argNames = argNames;
        this.bodyNode = bodyNode;
    }

    public List<String> getArgNames() {
        return argNames;
    }

    public ASTNode getBodyNode() {
        return bodyNode;
    }
}

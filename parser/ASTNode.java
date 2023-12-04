package parser;

import java.util.*;

import lexer.Token;
import lexer.TokenType;

/*
 * Abstract Syntax Tree Nodes
 */
public class ASTNode {
    // Constructor and other methods

    @Override
    public String toString() {
        return "";
    }
}

class NumberNode extends ASTNode {
    private Token token;

    public NumberNode(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public String toString() {
        return token.toString();
    }
}

class VarAccessNode extends ASTNode{
    private Token token;

    public VarAccessNode (Token token){
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    @Override
    public String toString() {
        return token.toString();
    }
}

class VarAssignNode extends ASTNode{
    private String variableName;
    private ASTNode valueNode;

    public VarAssignNode(String variableName, ASTNode valueNode){
        this.variableName = variableName;
        this.valueNode = valueNode;
    }

    public String getVarName(){
        return variableName;
    }

    public ASTNode getValueNode(){
        return valueNode;
    }

    @Override
    public String toString() {
        return "(VAR: "+ variableName + ", "+ valueNode.toString()+")";
    }
}

class BinOpNode extends ASTNode {
    private ASTNode leftNode;
    private ASTNode rightNode;
    private Token operator;

    public BinOpNode(ASTNode leftNode, Token operator, ASTNode rightNode) {
        this.leftNode = leftNode;
        this.operator = operator;
        this.rightNode = rightNode;
    }


    public ASTNode getLeftNode(){
        return this.leftNode;
    }

    public ASTNode getRightNode(){
        return this.rightNode;
    }

    public Token getOperator(){
        return this.operator;
    }

    @Override
    public String toString() {
        return "(" + leftNode.toString() + ", " + operator.getType() + ", " + rightNode.toString() + ")";
    }
}

class UnaryOpNode extends ASTNode {
    private Token operator;
    private ASTNode node;

    public UnaryOpNode(Token operator, ASTNode node) {
        this.operator = operator;
        this.node = node;
    }

    public ASTNode getNode(){
        return this.node;
    }

    public Token getOperator(){
        return this.operator;
    }

    @Override
    public String toString() {
        return "(" + operator.getType() + ", " + node.toString() + ")";
    }
}

class IfNode extends ASTNode {
    private List<Case> cases;
    private ASTNode elseCase;

    public IfNode(List<Case> cases, ASTNode elseCase) {
        this.cases = cases;
        this.elseCase = elseCase;
    }

    public List<Case> getCases(){
        return this.cases;
    }

    public ASTNode getElseCase(){
        return this.elseCase;
    }
}

class Case {
    private ASTNode condition;
    private ASTNode expression;

    public Case(ASTNode condition, ASTNode expression) {
        this.condition = condition;
        this.expression = expression;
    }

    public ASTNode getCondition(){
        return this.condition;
    }

    public ASTNode getExpression(){
        return this.expression;
    }
}
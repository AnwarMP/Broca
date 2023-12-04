package parser;

import java.util.List;
import lexer.Token;
import lexer.TokenType;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private Token currToken;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currToken = tokens.get(current);
    }

    //To parse, follow the grammar --> start with expr
    public ASTNode parse() {
        ASTNode parsedExpression = this.expr();
        //System.out.println("Parser: " + parsedExpression.toString());
        return parsedExpression;
    }

    public Token advance() {
        this.current++;
        if(this.current < tokens.size()){
            this.currToken = tokens.get(this.current);
        }
        else{
            this.currToken =  null;
        }
        return null;
    }

    /*
     * Turning our grammar to parsing functions
     */

    public ASTNode expr(){

        //"KEYWORD:VAR" "IDENTIFIER" "EQUAL" <arith-expr>
        if(this.tokens.get(current).getType() == TokenType.KEYWORD && this.currToken.getLiteral().equals("VAR")){
            //get indentifier 
            this.advance();
            String variableName = (String)this.currToken.getLiteral();

            //check if theres an equal
            this.advance();
            if(this.currToken.getType() != TokenType.EQUAL){
                throw new RuntimeException("Expected '='");
            }

            //get expression to assign variable
            this.advance();
            ASTNode expression = this.expr();
            return new VarAssignNode(variableName, expression);
        }

        //<comp-expr> (("KEYWORD:AND"|"KEYWORD:OR") <comp-expr>)*
        ASTNode left = compExpr();
        while(current < tokens.size() 
            && this.currToken.getType() == TokenType.KEYWORD 
            && (this.currToken.getLexeme().equals("AND") || this.currToken.getLexeme().equals("OR") )){
            
            Token op = this.currToken;
            this.advance();
            ASTNode right = compExpr();
            left = new BinOpNode(left, op, right);
        }

        return left;
    }

    public ASTNode compExpr(){

        //NOT <comp-expr>
        if(this.currToken.getType() == TokenType.KEYWORD && this.currToken.getLexeme().equals("NOT")){
            Token op = this.currToken;
            this.advance();

            ASTNode node = compExpr();
            return new UnaryOpNode(op, node);
        }

        //<arthi-expr> (("EE"|"LT"|"GT"|"LTE"|"GTE") <arthi-expr>)*
        ASTNode left = arithExpr();
        while(current < tokens.size() 
            && (this.currToken.getType() == TokenType.EE 
            || this.currToken.getType() == TokenType.NE
            || this.currToken.getType() == TokenType.LT
            || this.currToken.getType() == TokenType.GT
            || this.currToken.getType() == TokenType.LTE
            || this.currToken.getType() == TokenType.GTE
            )){
            
            Token op = this.currToken;
            this.advance();
            ASTNode right = arithExpr();
            left = new BinOpNode(left, op, right);
        }

        return left;
    }

    public ASTNode arithExpr(){
        // <term> (("PLUS" | "MINUS") <term>)*
        ASTNode left = term();
        while(current < tokens.size() && (this.currToken.getType() == TokenType.PLUS || this.currToken.getType() == TokenType.MINUS)){
            Token op = this.currToken;
            this.advance();
            ASTNode right = term();
            left = new BinOpNode(left, op, right);
        }

        return left;
    }

    public ASTNode term(){

        //<factor> (("MUL" | "DIV") <factor>)*
        ASTNode left = factor();
        while(current < tokens.size() && (this.currToken.getType() == TokenType.MUL || this.currToken.getType() == TokenType.DIV)){
            Token op = this.currToken;
            this.advance();
            ASTNode right = factor();
            left = new BinOpNode(left, op, right);
        }

        return left;
    }

    public ASTNode factor(){
        Token tok = tokens.get(current);

        //("PLUS" | "MINUS") <factor>
        if(tok.getType() == TokenType.PLUS || tok.getType() == TokenType.MINUS){
            this.advance();
            ASTNode factor = this.factor();
            return new UnaryOpNode(tok, factor);
        }
        //"INT" | "DOUBLE" | "IDENTIFIER"
        else if(tok.getType() == TokenType.INT || tok.getType() == TokenType.DOUBLE){
            this.advance();
            return new NumberNode(tok);
        }
        else if(tok.getType() == TokenType.IDENTIFIER){
            this.advance();
            return new VarAccessNode(tok);
        }
        //"LEFT_PAREN" <expr> "RIGHT_PAREN"
        else if(tok.getType() == TokenType.LEFT_PAREN){
            this.advance();
            ASTNode exprNode = this.expr(); //Return the expression inside the parentheses
            if(this.currToken != null && this.currToken.getType() == TokenType.RIGHT_PAREN){
                this.advance();
                return exprNode;
            }
        }
        return null;
    }
}
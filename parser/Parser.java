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
        System.out.println("Parser: " + parsedExpression.toString());
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
     * Turning our grammar / BNF to parsing functions
     */

    //factor  : INT|FLOAT
    //        : (PLUS|MINUS) factor
    //        : LEFT_PAREN expr RIGHT_PAREN
    public ASTNode factor(){
        Token tok = tokens.get(current);

        if(tok.getType() == TokenType.PLUS || tok.getType() == TokenType.MINUS){
            this.advance();
            ASTNode factor = this.factor();
            return new UnaryOpNode(tok, factor);
        }
        else if(tok.getType() == TokenType.INT || tok.getType() == TokenType.DOUBLE){
            this.advance();
            return new NumberNode(tok);
        }
        else if(tok.getType() == TokenType.IDENTIFIER){
            this.advance();
            return new VarAccessNode(tok);
        }
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

    //term:  factor((MUL|DIV) factor)*
    public ASTNode term(){
        ASTNode left = factor();

        while(current < tokens.size() && (this.currToken.getType() == TokenType.MUL || this.currToken.getType() == TokenType.DIV)){
            Token op = this.currToken;
            this.advance();
            ASTNode right = factor();
            left = new BinOpNode(left, op, right);
        }

        return left;
    }

    //expr: term ((PLUS|MINUS) term)*
    public ASTNode expr(){

        //check first if there is a variable
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

        ASTNode left = term();
        while(current < tokens.size() && (this.currToken.getType() == TokenType.PLUS || this.currToken.getType() == TokenType.MINUS)){
            Token op = this.currToken;
            this.advance();
            ASTNode right = term();
            left = new BinOpNode(left, op, right);
        }

        return left;
    }
}
package parser;

import java.util.ArrayList;
import java.util.List;
import lexer.Token;
import lexer.TokenType;
import repl.SymbolTable;

public class Parser {
    private final List<Token> tokens;
    private static SymbolTable symbolTable = SymbolTable.getInstance();
    private int current = 0;
    private Token currToken;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.currToken = tokens.get(current);
    }

    //To parse, follow the grammar --> start with expr
    public ASTNode parse() {
        ASTNode parsedExpression = this.expr();
        //Return root node of AST
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
            ASTNode expression = this.arithExpr();
            return new VarAssignNode(variableName, expression);
        }

        //if identifier already initialized: "IDENTIFIER" "EQUAL" <arith-expr>
        if(this.currToken.getType() == TokenType.IDENTIFIER && this.tokens.get(current+1).getType() == TokenType.EQUAL && symbolTable.hasSymbol(currToken.getLexeme())){
            String variableName = this.currToken.getLexeme();

            //chech if there is an equal
            this.advance();
            if(this.currToken.getType() != TokenType.EQUAL){
                throw new RuntimeException("Expected '='");
            }

            //get expression to assign variable
            this.advance();
            ASTNode expression = this.arithExpr();
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
        //<call-func> (("MUL" | "DIV") <factor>)*
        ASTNode left = callFunc();
        while(current < tokens.size() && (this.currToken.getType() == TokenType.MUL || this.currToken.getType() == TokenType.DIV)){
            Token op = this.currToken;
            this.advance();
            ASTNode right = factor();
            left = new BinOpNode(left, op, right);
        }

        return left;
    }

    public ASTNode callFunc(){
        ASTNode factor = factor();

        List<ASTNode> argNodes = new ArrayList<>();
        if(currToken.getType() == TokenType.LEFT_PAREN){
            this.advance();

            if(currToken.getType() == TokenType.RIGHT_PAREN){
                this.advance();
            }
            else{
                argNodes.add(this.expr());

                while(currToken.getType() == TokenType.COMMA){
                    this.advance();

                    argNodes.add(this.expr());
                }

                if(currToken.getType() != TokenType.RIGHT_PAREN){
                    throw new RuntimeException("Expected ')'");
                }
                this.advance();
            }

            return new CallNode(factor, argNodes);
        }
        return factor;
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
        //<if-expr>
        else if(tok.getType() == TokenType.KEYWORD && tok.getLexeme().equals("IF")){
            return ifExpr();
        }
        //<for-expr>
        else if(tok.getType() == TokenType.KEYWORD && tok.getLexeme().equals("FOR")){
            return forExpr();
        }
        //<while-loop>
        else if(tok.getType() == TokenType.KEYWORD && tok.getLexeme().equals("WHILE")){
            return whileExpr();
        }
        //<func-def>
        else if(tok.getType() == TokenType.KEYWORD && tok.getLexeme().equals("FN")){
            return funcDef();
        }
        return null;
    }

    /*
     * "KEYWORD:FN" "IDENTIFIER"?
        "LEFT_PAREN" ("IDENTIFIER" ("COMMA" "IDENTIFIER")*)? RIGHT_PAREN
        "ARROW" <expr>
     */
    public ASTNode funcDef(){
        //FN
        if(currToken.getType() == TokenType.KEYWORD && currToken.getLexeme().equals("FN")){
            this.advance();
        }
        else{
            throw new RuntimeException("Expected 'FN'");
        }

        //identifier is optional, check for '('
        Token varToken;
        if(currToken.getType() == TokenType.IDENTIFIER){
            varToken = currToken;
            this.advance();
            if(currToken.getType() != TokenType.LEFT_PAREN){
                throw new RuntimeException("Expected '('");
            }
        }
        else {
            varToken = null;
            if(currToken.getType() != TokenType.LEFT_PAREN){
                throw new RuntimeException("Expected '('");
            }
        }

        this.advance();
        //Get arguments and check ')'
        List<Token> argNameTokens = new ArrayList<>();
        if(currToken.getType() == TokenType.IDENTIFIER){
            argNameTokens.add(currToken);
            this.advance();

            while(currToken.getType() == TokenType.COMMA){
                this.advance();
                if(currToken.getType() != TokenType.IDENTIFIER){
                    throw new RuntimeException("Expected IDENTIFIER");
                }

                argNameTokens.add(currToken);
                this.advance();
            }

            if(currToken.getType() != TokenType.RIGHT_PAREN){
                throw new RuntimeException("Expected ',' or ')");
            }
        }
        else {
            if(currToken.getType() != TokenType.RIGHT_PAREN){
                throw new RuntimeException("Expected ')");
            }
        }

        // ->
        this.advance();
        if(currToken.getType() != TokenType.ARROW){
            throw new RuntimeException("Expected '->'");
        }

        //execute expression, the node to return
        this.advance();
        ASTNode bodyNode = this.expr();

        return new FuncDefNode(varToken, argNameTokens, bodyNode);
    }

    //<while-expr> ::= "KEYWORD:WHILE" <expr> "KEYWORD:THEN" <expr>
    public ASTNode whileExpr(){
        
        //WHILE
        if(currToken.getType() == TokenType.KEYWORD && currToken.getLexeme().equals("WHILE")){
            this.advance();
        }
        else{
            throw new RuntimeException("Expected 'FOR'");
        }

        ASTNode conditionNode = this.expr();

        //THEN
        if(currToken.getType() == TokenType.KEYWORD && currToken.getLexeme().equals("THEN")){
            this.advance();
        }
        else{
            throw new RuntimeException("Expected 'TO'");
        }

        ASTNode bodyNode = this.expr();

        return new WhileNode(conditionNode, bodyNode);
    }

    /*
     * <for-expr> ::= "KEYWORD:FOR" "IDENTIFIER:EQ" <expr> "KEYWORD:TO" <expr> 
		("KEYWORD:STEP" <expr>)? "KEYWORD:THEN" <expr>
     */
     public ASTNode forExpr(){

        //FOR
        if(currToken.getType() == TokenType.KEYWORD && currToken.getLexeme().equals("FOR")){
            this.advance();
        }
        else{
            throw new RuntimeException("Expected 'FOR'");
        }

        //IDENTIFER such as i
        if(currToken.getType() != TokenType.IDENTIFIER){
            throw new RuntimeException("Expected Indentifier");
        }

        Token varToken = currToken;
        this.advance();

        //=
        if(currToken.getType() == TokenType.EQUAL){
            this.advance();
        }
        else{
            throw new RuntimeException("Expected '='");
        }

        //Retrieve start value
        ASTNode startValue = this.expr();

        //TO
        if(currToken.getType() == TokenType.KEYWORD && currToken.getLexeme().equals("TO")){
            this.advance();
        }
        else{
            throw new RuntimeException("Expected 'TO'");
        }

        //Retrieve end value
        ASTNode EndValue = this.expr();

        //If applicable, get the step value (increment amount)
        ASTNode stepNode = null;
        if(currToken.getType() == TokenType.KEYWORD && currToken.getLexeme().equals("STEP")){
            this.advance();
            stepNode = this.expr();
        }

        //THEN
        if(currToken.getType() == TokenType.KEYWORD && currToken.getLexeme().equals("THEN")){
            this.advance();
        }
        else{
            throw new RuntimeException("Expected 'TO'");
        }

        //Body, what to execute
        ASTNode bodyNode = this.expr();

        return new ForNode(varToken, startValue, EndValue, stepNode, bodyNode);

     }

    /*
     * "KEYWORD:IF" <expr> "KEYWORD:THEN" <expr>
                ("KEYWORD:ELIF" <expr> "KEYWORD:THEN" expr)*
                ("KEYWORD:ELSE" <expr>)?
     */
    public ASTNode ifExpr(){
        List<Case> cases = new ArrayList<>();
        ASTNode elseCase = null;

        //check if
        if(currToken.getType() == TokenType.KEYWORD && currToken.getLexeme().equals("IF")){
            this.advance();
        }
        else{
            throw new RuntimeException("Expected 'IF'");
        }

        //get conditional statement
        ASTNode condition = this.expr();  
        
        //check then
        if(currToken.getType() == TokenType.KEYWORD && currToken.getLexeme().equals("THEN")){
            this.advance();
        }
        else{
            throw new RuntimeException("Expected 'THEN'");
        }

        //get expression after conditional statement
        ASTNode expression = this.expr();

        //get case to cases
        cases.add(new Case(condition, expression));

        //add subsequent cases if 'ELIF'
        while(currToken.getType() == TokenType.KEYWORD && currToken.getLexeme().equals("ELIF")){
            this.advance();
            
            ASTNode condition1 = this.expr();

            if(currToken.getType() == TokenType.KEYWORD && currToken.getLexeme().equals("THEN")){
                this.advance();
            }
            else{
                throw new RuntimeException("Expected 'THEN'");
            }
            
            ASTNode expression1 = this.expr();
            cases.add(new Case(condition1, expression1));
        }

        //check Else
        if(currToken.getType() == TokenType.KEYWORD && currToken.getLexeme().equals("ELSE")){
            this.advance();

            elseCase = this.expr();  
        }
        return new IfNode(cases, elseCase);
    } 


}
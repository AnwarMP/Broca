package lexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private final List<String> keywords = new ArrayList<>(Arrays.asList("VAR", "AND", "OR", "NOT", "IF", "THEN", "ELIF", "ELSE"));
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        //Indicate the End of File
        tokens.add(new Token(TokenType.EOF, "", null, line));
        //Print tokens
        // for(Token s: tokens){
        //     System.out.println(s.toString());
        // }
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        //System.out.println("index: " + current + ", char: " + c);
        switch (c) {
            //case '=': addToken(TokenType.EQUAL); break;
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.MUL); break;
            case '/': addToken(TokenType.DIV); break;
            default:
                if (isDigit(c)) {
                    createNumber();
                } 
                else if(isLetter(c)){
                    createIdentifierOrKeyword(c);
                }
                else if (c == '!'){
                    createNotEquals();
                }
                else if (c == '='){
                    createEquals();
                }
                else if(c == '<'){
                    createLessThan();
                }
                else if(c == '>'){
                    createGreaterThan();
                }
                else if (!isWhitespace(c)) { //Unexpected Character
                    System.out.println("Illegal Character: " + "'" + c + "'");
                }
                break;
        }
    }

    private void createGreaterThan(){
        //if >=
        if(peek() == '='){
            advance();
            addToken(TokenType.GTE);
        }
        else { //if >
            addToken(TokenType.GT);
        }
    }

    private void createLessThan(){
        //if <=
        if(peek() == '='){
            advance();
            addToken(TokenType.LTE);
        }
        else { // if <
            addToken(TokenType.LT);
        }
    }

    private void createEquals(){
        //if double =
        if(peek() == '='){
            advance();
            addToken(TokenType.EE);
        }
        else { // if =
            addToken(TokenType.EQUAL);
        }
    }

    private void createNotEquals(){
        //if !=
        if(peek() == '='){
            advance();
            addToken(TokenType.NE);
        }
    }

    private void createIdentifierOrKeyword(Character c){
        String idString = "" + c;

        while(isLetter(peek())){
            idString += peek();
            advance();
        }

        if(isKeyword(idString)){
            addToken(TokenType.KEYWORD, idString);
        }
        else {
            addToken(TokenType.IDENTIFIER, idString);
        }
        
    }

    private void createNumber() {
        Boolean isDouble = false;
        while (isDigit(peek())) advance();
        
        if(peek() == '.' && isDigit(peekNext())) {
            isDouble = true;
            advance();
            while (isDigit(peek())) advance();
        }

        if(isDouble){
            addToken(TokenType.DOUBLE, source.substring(start, current));
        }
        else{
            addToken(TokenType.INT, source.substring(start, current));
        }
    }


    /*
     * HELPER METHODS
     */

    private char advance() {
        return source.charAt(current++);
    }

    private boolean isKeyword(String s){
        return keywords.contains(s);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isLetter(char c) {
        return Character.isLetter(c);
    }

    private boolean isWhitespace(char c) {
        return c == ' ' || c == '\r' || c == '\n' || c == '\t';
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    public List<Token> getTokens(){
        return tokens;
    }
}

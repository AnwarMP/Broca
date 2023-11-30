package lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
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

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '=': addToken(TokenType.EQUAL); break;
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.MUL); break;
            case '/': addToken(TokenType.DIV); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (!isWhitespace(c)) { //Unexpected Character
                    System.out.println("Illegal Character: " + "'" + c + "'");
                }
                break;
        }
    }

    private void number() {
        Boolean isDouble = false;
        while (isDigit(peek())) advance();
        
        if (peek() == '.' && isDigit(peekNext())) {
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

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
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

    private char advance() {
        return source.charAt(current++);
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

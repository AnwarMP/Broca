package lexer;

public enum TokenType {
    // Keywords, operators, literals, etc.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET,
    COMMA, ARROW, DOT, MINUS, PLUS, SEMICOLON, DIV, MUL, 
    STRING, NUMBER, INT, DOUBLE,
    AND, OR, IF, THEN, ELSE, TRUE, FALSE, FN, LET, NIL,

    //==, !=, <, >, <=, >=
    EE, NE, LT, GT, LTE, GTE,

    KEYWORD, IDENTIFIER, EQUAL, 

    EOF;
}

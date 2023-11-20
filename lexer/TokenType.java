package lexer;

public enum TokenType {
    // Keywords, operators, literals, etc.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACKET, RIGHT_BRACKET,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,
    IDENTIFIER, STRING, NUMBER,
    AND, OR, IF, THEN, ELSE, TRUE, FALSE, FN, LET, NIL,
    EOF
}

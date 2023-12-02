package repl;

import lexer.*;
import parser.*;

import java.util.Scanner;

public class Repl {
    public static SymbolTable symbolTable = new SymbolTable();
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Broca > ");
            String line = scanner.nextLine();
            if (line == null) break;

            Lexer lexer = new Lexer(line);
            Parser parser = new Parser(lexer.scanTokens());
            //Root node of AST
            ASTNode rootNode = parser.parse();
            Interpreter interpreter = new Interpreter();
            interpreter.interpret(rootNode);

        }
    }
}

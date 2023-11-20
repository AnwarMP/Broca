package repl;

import interpreter.Interpreter;
import lexer.Lexer;
import parser.Parser;
import lexer.Token;

import java.util.Scanner;

public class Repl {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Interpreter interpreter = new Interpreter();

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line == null) break;

            Lexer lexer = new Lexer(line);
            Parser parser = new Parser(lexer.scanTokens());
            interpreter.interpret(parser.parse());
        }
    }
}

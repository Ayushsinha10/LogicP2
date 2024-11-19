package interpreter;

import java.io.FileReader;
import java.io.StringReader;
import java.util.Scanner;

import parser.FProlog;
import parser.ast.FPProg;

public class Main {

  public static void main(String[] args) {
    try(Scanner s = new Scanner(new FileReader("interpreter/sketchbook.fp"));) {

      StringBuilder sb = new StringBuilder();
      while (s.hasNext()) {
        sb.append(s.next());
      }
      String str = sb.toString();
     // System.out.println(str);

      FPProg ast = new FProlog(new StringReader(str)).P();

      System.out.println(ast.toString());

     Interpreter inter = new Interpreter();
     inter.interpret(ast);

    } catch (Exception e) {
      System.err.println(e.toString());
    }
  }
}
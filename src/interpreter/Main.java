package interpreter;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Scanner;

import parser.FProlog;
import parser.ast.FPProg;

public class Main {

  public static void main(String[] args) {
    boolean isTraceEnabled = false;
    String filepath = null;
    for (String arg : args) {
      if (arg.equals("--trace")) {
          isTraceEnabled = true;
        
      }
      else{
        filepath = arg;
      }

  }
  if(filepath == null){
    System.out.println("No file specified!");
    return;
  
  }
     File file = new File(filepath);

            if (file.exists()) {
                //System.out.println("File exists at the specified path: " + filepath);
            } else {
                System.out.println("File does not exist at the specified path: " + filepath);
                return;
            }
    try(Scanner s = new Scanner(new FileReader(filepath));) {

      StringBuilder sb = new StringBuilder();
      while (s.hasNext()) {
        sb.append(s.next());
      }
      String str = sb.toString();
     // System.out.println(str);

      FPProg ast = new FProlog(new StringReader(str)).P();
      
     // System.out.println(ast.toString());


     Interpreter inter = new Interpreter(isTraceEnabled);
     inter.interpret(ast);

    } catch (Exception e) {
      System.err.println(e.toString());
    }
  }
}
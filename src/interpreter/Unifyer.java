package interpreter;
import java.util.*;

import parser.ast.*;

public class Unifyer {
    private static Stack<Map<String, FPTerm>> bindingsStack = new Stack<>();
    private static Map<String, FPTerm> previousBindings = new HashMap<>();
    public static Stack<Map<String, FPTerm>> fBindings = new Stack<>();
    public static  Map<String, FPTerm> restoreBindings = new HashMap<>();
    private static Set<Map<String, FPTerm>> attemptedStates = new HashSet<>();


    // Check if a string represents a variable (starts with an uppercase letter or underscore)
    private static boolean isVariable(String str) {
        return Character.isUpperCase(str.charAt(0)) || str.charAt(0) == '_';
    }

    // Main method to unify two FPTerm objects
    public static boolean unify(FPTerm term1, FPTerm term2, Map<String, FPTerm> bindings) {
        // Print the current unification attempt
     //  System.out.println("Attempting to unify: " + term1 + " with " + term2);
      //  System.out.println("Current bindings: " + bindings);
    
        // If both terms are identical, no need to unify
   //         System.out.println(term1.getName());
   //         System.out.println(term2.getName());
            restoreBindings = bindings;
  //          System.out.println(fBindings+"-------------------------");
        if(!bindings.isEmpty()){
            bindingsStack.push(bindings);
        //    System.out.println(bindings);
        }
       


       if (term1.getName().equals("write")) {
        if (term1.getTerms().size() != 1) {
            throw new IllegalArgumentException("write/1 expects exactly one argument.");
        }

        // Resolve the term to be written based on bindings
        

        // Print the resolved term
        System.out.println(term2.getName());

        // write/1 always succeeds
        return true;
    }
        if (term1.equals(term2)) {
       //    System.out.println("Terms are already identical.");
            return true;
        }

        // If term1 is a variable, unify it with term2
        if (isVariable(term1.getName())) {
       //   System.out.println("Unifying variable " + term1 + " with " + term2);
            return unifyVariable(term1.getName(), term2, bindings, term1);
        }
    
        // If term2 is a variable, unify it with term1
        if (isVariable(term2.getName())) {
      //    System.out.println("Unifying variable " + term2 + " with " + term1);
            return unifyVariable(term2.getName(), term1, bindings, term2);
        }
     
    
        // If both terms are constants, they must be identical
        if (term1.getKind() == TKind.CONST && term2.getKind() == TKind.CONST) {
            boolean isEqual = term1.getName().equals(term2.getName());
      //      System.out.println("Both terms are constants, comparing: " + term1.getName() + " == " + term2.getName() + " -> " + isEqual);
            return isEqual;
        }
    
        // Both terms must be compound terms; they must have the same functor and unify their subterms
        if (term1.getKind() == TKind.CTERM && term2.getKind() == TKind.CTERM) {
            if (!term1.getName().equals(term2.getName())) {
      //        System.out.println("Functors do not match: " + term1.getName() + " != " + term2.getName());
       //        System.out.println(term1);
       //        System.out.println(bindings);

               return false;

               
            }
    
            // Recursively unify the arguments (subterms)
            List<FPTerm> args1 = term1.getTerms();
            List<FPTerm> args2 = term2.getTerms();
            if (args1.size() != args2.size()) {
       //        System.out.println("Number of arguments do not match: " + args1.size() + " != " + args2.size());
               
                return false; // Number of arguments must match
            }
    
            // Recursively unify corresponding subterms
            for (int i = 0; i < args1.size(); i++) {
               System.out.println("Unifying subterm " + args1.get(i) + " with " + args2.get(i));
                if (!unify(args1.get(i), args2.get(i), bindings)) {
                  
                    return false; // If any subterm fails, the unification fails
                }
            }
            return true;
        }
    
   //    System.out.println("Unification failed: terms are not unifiable.");
        return false; // If none of the above conditions matched, unification fails
    }
    

    // Unify a variable with a term (either a constant or another variable)
    public static boolean unifyVariable(String var, FPTerm term, Map<String, FPTerm> bindings, FPTerm varterm) {
        // Print the unification attempt for debugging
   //     System.out.println("Unifying variable: " + var + " with " + term);
    
        // If the variable is already bound to a term, unify the term with its current binding
        if (bindings.containsKey(var)) {
            return unify(bindings.get(var), term, bindings);
        }



    
        // A variable cannot unify with itself (no self-binding)
        if (term.getName().equals(var)) {
            System.out.println("Variable " + var + " cannot unify with itself.");
            return false; // Prevent self-unification
        }
        if (resolveTerm(term, bindings).getName().equals(var)) {
            return false; // Prevent self-binding through resolution
        }
    
        // Otherwise, bind the variable to the term
         System.out.println("Binding variable " + var + " to " + term);
        bindings.put(var, term);
        return true;  // Successful binding
    }
    public static  Stack<Map<String, FPTerm>> getStack(){
        return fBindings;

    }
    public static Map<String, FPTerm> getBinding(){
        return restoreBindings;
    }
    private static FPTerm resolveTerm(FPTerm term, Map<String, FPTerm> bindings) {
        while (bindings.containsKey(term.getName())) {
            term = bindings.get(term.getName());
        }
        return term;
    }
    public static void pushBindings(Map<String, FPTerm> bindings) {
        fBindings.push(new HashMap<>(bindings)); // Push a copy to avoid reference issues
    }
    
    public static Map<String, FPTerm> popBindings() {
        if (fBindings.isEmpty()) {
            return null; // No more bindings to backtrack
        }
        return fBindings.pop();
    }
    public static boolean hasAttempted(Map<String, FPTerm> bindings) {
        return attemptedStates.contains(bindings);
    }

    public static void markAttempted(Map<String, FPTerm> bindings) {
        attemptedStates.add(new HashMap<>(bindings));
    }
    public static Set<Map<String, FPTerm>> getAttempted(){
        return attemptedStates;
    }
}
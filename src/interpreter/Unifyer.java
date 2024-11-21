package interpreter;
import java.util.*;

import parser.ast.*;

public class Unifyer {
    private static Stack<Map<String, FPTerm>> bindingsStack = new Stack<>();
    private static Map<String, FPTerm> previousBindings = new HashMap<>();
    private static Set<Map<String, FPTerm>> fBindings = new HashSet<>();

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
            System.out.println(fBindings+"-------------------------");
        if(!bindings.isEmpty()){
            bindingsStack.push(bindings);
            System.out.println(bindings);
        }
       

        if(fBindings.contains(bindings)){
         //  return false;
          
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
           System.out.println("Terms are already identical.");
            return true;
        }

        // If term1 is a variable, unify it with term2
        if (isVariable(term1.getName())) {
          System.out.println("Unifying variable " + term1 + " with " + term2);
            return unifyVariable(term1.getName(), term2, bindings, term1);
        }
    
        // If term2 is a variable, unify it with term1
        if (isVariable(term2.getName())) {
          System.out.println("Unifying variable " + term2 + " with " + term1);
            return unifyVariable(term2.getName(), term1, bindings, term2);
        }
     
    
        // If both terms are constants, they must be identical
        if (term1.getKind() == TKind.CONST && term2.getKind() == TKind.CONST) {
            boolean isEqual = term1.getName().equals(term2.getName());
            System.out.println("Both terms are constants, comparing: " + term1.getName() + " == " + term2.getName() + " -> " + isEqual);
            return isEqual;
        }
    
        // Both terms must be compound terms; they must have the same functor and unify their subterms
        if (term1.getKind() == TKind.CTERM && term2.getKind() == TKind.CTERM) {
            if (!term1.getName().equals(term2.getName())) {
               System.out.println("Functors do not match: " + term1.getName() + " != " + term2.getName());
       //        System.out.println(term1);
       //        System.out.println(bindings);

               return false;

               
            }
    
            // Recursively unify the arguments (subterms)
            List<FPTerm> args1 = term1.getTerms();
            List<FPTerm> args2 = term2.getTerms();
            if (args1.size() != args2.size()) {
         //       System.out.println("Number of arguments do not match: " + args1.size() + " != " + args2.size());
               
                return false; // Number of arguments must match
            }
    
            // Recursively unify corresponding subterms
            for (int i = 0; i < args1.size(); i++) {
            //    System.out.println("Unifying subterm " + args1.get(i) + " with " + args2.get(i));
                if (!unify(args1.get(i), args2.get(i), bindings)) {
                  
                    return false; // If any subterm fails, the unification fails
                }
            }
            return true;
        }
    
     //   System.out.println("Unification failed: terms are not unifiable.");
        return false; // If none of the above conditions matched, unification fails
    }
    public static boolean unify2(FPTerm term1, FPTerm term2, Map<String, FPTerm> bindings, String exl, FPTerm term3) {
    // Print the current unification attempt
    // System.out.println("Attempting to unify: " + term1 + " with " + term2);
    // System.out.println("Current bindings: " + bindings);
    
    // Check if exl is being bound to term3 and skip this unification attempt
    if (isVariable(term1.getName()) && term1.getName().equals(exl) && term2.equals(term3)) {
        System.out.println("Skipping unification of " + term1 + " and " + term2 + " because " + exl + " would be bound to " + term3);
        return false; // Skip this binding and try another one
    }
    if (isVariable(term2.getName()) && term2.getName().equals(exl) && term1.equals(term3)) {
        System.out.println("Skipping unification of " + term1 + " and " + term2 + " because " + exl + " would be bound to " + term3);
        return false; // Skip this binding and try another one
    }

    // Store the current bindings before attempting to unify
    Map<String, FPTerm> savedBindings = new HashMap<>(bindings);

    // If both terms are identical, no need to unify
    if (term1.equals(term2)) {
        return true;
    }

    // If term1 is a variable, unify it with term2
    if (isVariable(term1.getName())) {
        return unifyVariable(term1.getName(), term2, bindings, term1);
    }

    // If term2 is a variable, unify it with term1
    if (isVariable(term2.getName())) {
        return unifyVariable(term2.getName(), term1, bindings, term2);
    }

    // If both terms are constants, they must be identical
    if (term1.getKind() == TKind.CONST && term2.getKind() == TKind.CONST) {
        return term1.getName().equals(term2.getName());
    }

    // Both terms must be compound terms; they must have the same functor and unify their subterms
    if (term1.getKind() == TKind.CTERM && term2.getKind() == TKind.CTERM) {
        if (!term1.getName().equals(term2.getName())) {
            System.out.println("Functors do not match: " + term1.getName() + " != " + term2.getName());
            return false; // Functors must match
        }

        // Recursively unify the arguments (subterms)
        List<FPTerm> args1 = term1.getTerms();
        List<FPTerm> args2 = term2.getTerms();
        if (args1.size() != args2.size()) {
            return false; // Argument counts must match
        }

        // Recursively unify corresponding subterms
        for (int i = 0; i < args1.size(); i++) {
            if (!unify(args1.get(i), args2.get(i), bindings)) {
                return false; // If any subterm fails, the unification fails
            }
        }
        return true;
    }

    // If none of the above conditions matched, unification fails
    return false;
}

    // Unify a variable with a term (either a constant or another variable)
    public static boolean unifyVariable(String var, FPTerm term, Map<String, FPTerm> bindings, FPTerm varterm) {
        // Print the unification attempt for debugging
         System.out.println("Unifying variable: " + var + " with " + term);
    
        // If the variable is already bound to a term, unify the term with its current binding
        if (bindings.containsKey(var)) {
            // Store the current state of the bindings for backtracking
            Map<String, FPTerm> backupBindings = new HashMap<>(bindings);
            fBindings.add(backupBindings);
            System.out.println("Variable " + var + " is already bound to: " + bindings.get(var));
            
            // Try unifying the already bound term with the new term
            System.out.println(bindings.get(var)+"ttt");
            System.out.println(term);
            boolean result = unify(bindings.get(var), term, bindings);
            

            
            return result;  // Return the result of the unification attempt (true or false)
        }
    
        // A variable cannot unify with itself (no self-binding)
        if (term.getName().equals(var)) {
            // System.out.println("Variable " + var + " cannot unify with itself.");
            return false; // Prevent self-unification
        }
    
        // Otherwise, bind the variable to the term
        // System.out.println("Binding variable " + var + " to " + term);
        bindings.put(var, term);
        return true;  // Successful binding
    }
    private static void restoreBindings() {
        if (!bindingsStack.isEmpty()) {
            // Pop the last set of bindings from the stack and restore them
            Map<String, FPTerm> previousBindings = bindingsStack.pop();
            System.out.println("Restoring bindings: " + previousBindings);
        }
    }
}
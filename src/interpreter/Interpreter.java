package interpreter;
import java.util.*;
import java.util.stream.Collectors;

import parser.ast.*;

public class Interpreter {
    private final Map<String, List<FPClause>> knowledgeBase = new HashMap<>();
    private Set<Map<String, FPTerm>> previousBindings = new HashSet<>();


    private String lastPredicate;
    private ArrayList<FPTerm> lastArguments;





    public void interpret(FPProg program) {
        for (FPClause clause : program.getClauses()) {
            if (clause.getHead() != null && clause.getBody() == null) {
                // Fact: Add to knowledge base.
                addFact(clause);
            } else if (clause.getHead() != null && clause.getBody() != null) {
                // Rule: Add to knowledge base.
                addRule(clause);
            } else if (clause.getBody() != null) {
                // Query: Evaluate and print results.
               // System.out.println(clause.getBody());
                evaluateQuery(clause.getBody());
                
            }
        }
    }

    private void addFact(FPClause clause) {
        knowledgeBase
            .computeIfAbsent(clause.getHead().getName(), k -> new ArrayList<>())
            .add(clause);
    }

    private void addRule(FPClause clause) {
        knowledgeBase
            .computeIfAbsent(clause.getHead().getName(), k -> new ArrayList<>())
            .add(clause);
    }

    private void evaluateQuery(FPBody query) {
        if (query.getTerms().isEmpty()){
            
            Map<String, FPTerm> bindings = new HashMap<>();
            boolean result = solvePredicateEmpty(lastPredicate, lastArguments, bindings);

           if (result) {
            //   System.out.println("Result: Yes");
               if (!bindings.isEmpty()) {
                   bindings.forEach((var, value) -> System.out.println(var + " = " + value));
                  return;
                   
               }
           } else {
               System.out.println("Result: No");
           }
         }

        
        for (FPTerm term : query.getTerms()) {

            String predicate = term.getName();
            
           // System.out.println(term+"rt");

            ArrayList<FPTerm> arguments = (term.getTerms());
        

       
            lastPredicate = predicate;
            lastArguments = arguments;
            if(isVariable(arguments.get(0))){
                Map<String, FPTerm> bindings = new HashMap<>();
                boolean result = solvePredicate(predicate, arguments, bindings);
   
               if (result) {
                //   System.out.println("Result: Yes");
                   if (!bindings.isEmpty()) {
                       bindings.forEach((var, value) -> System.out.println(var + " = " + value));
                      
                       return;
                   }
               } else {
                   System.out.println("Result: No");
                   return;
               }

            }
   

      //      System.out.println("Query: " + predicate + "(" + String.join(", ", arguments) + ")");
            Map<String, FPTerm> bindings = new HashMap<>();
             boolean result = solvePredicate(predicate, arguments, bindings);

            if (result) {
                System.out.println("Result: Yes");
                if (!bindings.isEmpty()) {
                  //  bindings.forEach((var, value) -> System.out.println(var + " = " + value));
                }
            } else {
                System.out.println("Result: No");
            }
        
        }
    }

    public void solve(String predicate, ArrayList<FPTerm> arguments) {
        Map<String, FPTerm> bindings = new HashMap<>();
        boolean result = solvePredicate(predicate, arguments, bindings);

        if (!result) {
            System.out.println("no");
        }
    }
    
    private boolean solvePredicate(String predicate, ArrayList<FPTerm> arguments, Map<String, FPTerm> bindings) {
        // Retrieve the clauses for the given predicate from the knowledge base
        List<FPClause> clauses = knowledgeBase.get(predicate);
    
        if (clauses == null) return false; // No clauses for this predicate
    
        boolean resultFound = false;
        
        // Track previously attempted bindings to avoid revisiting the same state

  
        // Iterate over the clauses for the predicate
        for (FPClause clause : clauses) {
            Map<String, FPTerm> newBindings = bindings;
    
            // Convert the head of the clause to an FPTerm (used for unification)
            FPTerm headTerm = clause.getHead().convertToTerm();
            
            // Create the query term using the provided arguments (already in FPTerm form)
            FPTerm queryTerm = new FPTerm(TKind.CTERM, predicate, arguments);
      
    
            // Check if this set of bindings has already been attempted
    
    
            // Mark this set of bindings as attempted
      
    
            // Attempt unification between the head of the clause and the query term
            if (Unifyer.unify(headTerm, queryTerm, newBindings)) {
                // If the body is null, it's a fact (success)
                if (clause.getBody() == null) {
                    bindings.putAll(newBindings);  // Add new bindings
                    resultFound = true;
                } else {
                    // Otherwise, recursively evaluate the body (rule)
                    if (evaluateBody(clause.getBody(), newBindings)) {
                        bindings.putAll(newBindings);  // Add new bindings
                        resultFound = true;
                    }
                }
            }

    
            // If no solution was found, backtrack by restoring the bindings
        }
        return resultFound;
        }

    private boolean solvePredicateR(String predicate, ArrayList<FPTerm> arguments, Map<String, FPTerm> bindings, Set<Map<String, FPTerm>> bindings2) {
        // Retrieve the clauses for the given predicate from the knowledge base
        List<FPClause> clauses = knowledgeBase.get(predicate);
    
        if (clauses == null) return false; // No clauses for this predicate
    
        boolean resultFound = false;
        
        // Track previously attempted bindings to avoid revisiting the same state
        Set<Map<String, FPTerm>> attemptedBindings = bindings2;
        Stack<Map<String, FPTerm>> bindingStack = new Stack<>();
    
        // Iterate over the clauses for the predicate
        for (FPClause clause : clauses) {
            Map<String, FPTerm> newBindings = bindings;
    
            // Convert the head of the clause to an FPTerm (used for unification)
            FPTerm headTerm = clause.getHead().convertToTerm();
            
            // Create the query term using the provided arguments (already in FPTerm form)
            FPTerm queryTerm = new FPTerm(TKind.CTERM, predicate, arguments);
            bindingStack.push(new HashMap<>(newBindings));
    
            // Check if this set of bindings has already been attempted
            if (attemptedBindings.contains(newBindings)) {
                continue; // Skip this clause as it leads to previously tried bindings
            }
    
            // Mark this set of bindings as attempted
            attemptedBindings.add(new HashMap<>(newBindings));
    
            // Attempt unification between the head of the clause and the query term
            if (Unifyer.unify(headTerm, queryTerm, newBindings)) {
                // If the body is null, it's a fact (success)
                if (clause.getBody() == null) {
                    bindings.putAll(newBindings);  // Add new bindings
                    resultFound = true;
                } else {
                    // Otherwise, recursively evaluate the body (rule)
                    if (evaluateBody(clause.getBody(), newBindings)) {
                        bindings.putAll(newBindings);  // Add new bindings
                        resultFound = true;
                    }
                }
            }
    
            // If no solution was found, backtrack by restoring the bindings
            if (!resultFound) {
                System.out.println("Backtracking: Reverting to previous bindings.");
                if (!bindingStack.isEmpty()) {
                    bindings = bindingStack.pop();  // Restore previous bindings
                }
                return solvePredicateR(predicate, arguments, bindings,attemptedBindings);
            }
        }
        return resultFound;
        }  
    private boolean solvePredicateEmpty(String predicate, ArrayList<FPTerm> arguments, Map<String, FPTerm> bindings) {
        // Retrieve the clauses for the given predicate from the knowledge base
        List<FPClause> clauses = knowledgeBase.get(predicate);
        
        if (clauses == null) return false; // No clauses for this predicate
        
        boolean resultFound = false;
        
        // Iterate over the clauses for the predicate
        for (FPClause clause : clauses) {
            // Create a fresh map of newBindings that will store bindings for this resolution attempt
            Map<String, FPTerm> newBindings = new HashMap<>(bindings);
            
            // Check if the new bindings conflict with the previous ones (skip if it does)
            if (previousBindings.contains(newBindings)) {
                continue; // Skip this clause as it leads to previously found bindings
            }
            
            // Add newBindings to previousBindings so it can be ignored in future attempts
            previousBindings.add(new HashMap<>(newBindings));  // Clone newBindings to avoid reference issues
        
            // Convert the head of the clause to an FPTerm (used for unification)
            FPTerm headTerm = clause.getHead().convertToTerm();
        
            // Create the query term using the provided arguments (already in FPTerm form)
            FPTerm queryTerm = new FPTerm(TKind.CTERM, predicate, arguments);
        
            // Attempt unification between the head of the clause and the query term
            if (Unifyer.unify(headTerm, queryTerm, newBindings)) {
                // If the body is null, it's a fact (success)
                if (clause.getBody() == null) {
                    
                    bindings.putAll(newBindings);  // Add new bindings
                    resultFound = true;
                } else {
                    // Otherwise, recursively evaluate the body (rule)
                    if (evaluateBody(clause.getBody(), newBindings)) {
                       
                        bindings.putAll(newBindings);  // Add new bindings
                        resultFound = true;
                    }
                }
            }
        }
        
        return resultFound;  // Return true if a solution was found, otherwise false
    }
    

    private boolean evaluateBody(FPBody body, Map<String, FPTerm> bindings) {
        // Recursively evaluate each subgoal (term) in the body
        
        for (FPTerm goal : body.getTerms()) {
            // If the goal is a "write" predicate, handle it as a side effect
            Map<String, FPTerm> bindings2 = bindings;
            if (goal.getName().equals("write")) {
                if (goal.getTerms().size() != 1) {
                    throw new IllegalArgumentException("write/1 expects exactly one argument.");
                }
                
    
                FPTerm termToWrite = goal.getTerms().get(0);

                // Replace variable names with their bound values in the current bindings
                String output = resolveTerm(termToWrite, bindings2).toString();
    
                // Print the result
                System.out.println(output);
    
                // Print the result
                
    
                // "write" always succeeds, so continue to the next goal
                continue;
            }
    
            // Convert goal arguments (which are FPTerms) to a new ArrayList of FPTerms
            ArrayList<FPTerm> goalArgs = new ArrayList<>();
            for (FPTerm arg : goal.getTerms()) {
                goalArgs.add(new FPTerm(arg.getKind(), arg.getName(), arg.getTerms()));
            }
    
            // Now pass the goal's name and converted arguments to solvePredicate
            if (!solvePredicate(goal.getName(), goalArgs, bindings)) {
                return false; // If any subgoal fails, return false
            }
        }
        return true; // All subgoals in the body were successfully satisfied
    }
    private static FPTerm resolveTerm(FPTerm term, Map<String, FPTerm> bindings) {
        // Treat terms of kind IDENT as variables and resolve them using bindings
        if (bindings.containsKey(term.getName())) {
            // Resolve the variable to its bound value
            return bindings.get(term.getName());   
        }
        return term; // Return the term itself if not a variable or not bound
    }
    // Helper method to resolve a term based on current bindings

    private boolean evaluateBodyWithBacktracking(FPBody body, Map<String, FPTerm> bindings) {
        // Set to track attempted bindings to backtrack and retry the same subgoal with different bindings
        Set<Map<String, FPTerm>> attemptedBindings = new HashSet<>();
        
        return evaluateSubgoalRecursive(body.getTerms(), bindings, 0, attemptedBindings);
    }
    
    private boolean evaluateSubgoalRecursive(List<FPTerm> goals, Map<String, FPTerm> bindings, int goalIndex, Set<Map<String, FPTerm>> attemptedBindings) {
        if (goalIndex >= goals.size()) {
            // All goals have been successfully satisfied
            return true;
        }
    
        FPTerm goal = goals.get(goalIndex);
        
        // Clone the current bindings to avoid modifying them directly
        Map<String, FPTerm> newBindings = new HashMap<>(bindings);
    
        // Convert goal arguments (which are FPTerms) to a new ArrayList of FPTerms
        ArrayList<FPTerm> goalArgs = new ArrayList<>();
        for (FPTerm arg : goal.getTerms()) {
            goalArgs.add(new FPTerm(arg.getKind(), arg.getName(), arg.getTerms()));
        }
    
        // Check if we've already tried these bindings for this goal
        if (attemptedBindings.contains(newBindings)) {
            // If the bindings have already been attempted, backtrack by trying the next set of bindings
            return evaluateSubgoalRecursive(goals, bindings, goalIndex + 1, attemptedBindings);
        }
    
        // Mark this set of bindings as attempted
        attemptedBindings.add(new HashMap<>(newBindings));
    
        // Attempt to solve the goal (subgoal) using solvePredicate
        boolean goalSucceeded = solvePredicate(goal.getName(), goalArgs, newBindings);
    
        if (goalSucceeded) {
            // If the subgoal is successful, recursively try the next goal in the body
            bindings.putAll(newBindings);  // Update the original bindings with new ones
            return evaluateSubgoalRecursive(goals, bindings, goalIndex + 1, attemptedBindings); // Move to the next goal
        } else {
            // If the subgoal fails, backtrack by restoring the previous bindings
            System.out.println("Backtracking: Goal " + goal.getName() + " failed, restoring previous bindings.");
            
            // Try the next alternative set of bindings for the current goal
            return evaluateSubgoalRecursive(goals, bindings, goalIndex, attemptedBindings);
        }
    }
    // Format and output the result for queries with variables
    private String formatResult(Map<String, FPTerm> bindings) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, FPTerm> entry : bindings.entrySet()) {
            if (!first) sb.append(", ");
            sb.append(entry.getKey()).append(" = ").append(entry.getValue());
            first = false;
        }
        return sb.toString();
    }

    // Trace call for debugging (optional)
    private void traceCall(FPTerm term) {
        System.out.println("Call: " + term);
    }
    

    


    
    private boolean isVariable(FPTerm term) {
        String test = term.getName();
        return Character.isUpperCase(test.charAt(0));
    }

    private List<String> convertTermsToStrings(List<FPTerm> terms) {
        List<String> result = new ArrayList<>();
        for (FPTerm term : terms) {
            result.add(term.getName());
        }
        return result;
    }


}

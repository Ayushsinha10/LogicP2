package interpreter;
import java.util.*;

import parser.ast.*;

public class Interpreter {
    private final Map<String, List<FPClause>> knowledgeBase = new HashMap<>();
    private Set<Map<String, String>> previousBindingsSet = new HashSet<>();


    private String lastPredicate;
    private List<String> lastArguments;



// This method stores the query's state after each call to evaluateVariableQuery.
private void storeLastQueryState(String predicate, List<String> arguments) {
    lastPredicate = predicate;
    lastArguments = arguments;  
}

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
            
            evaluateEmptyQuery(lastPredicate, lastArguments);
         }

        
        for (FPTerm term : query.getTerms()) {

            String predicate = term.getName();
            
           // System.out.println(term+"rt");

            List<String> arguments = convertTermsToStrings(term.getTerms());
          //  System.out.println(arguments+"rt");

       
            lastPredicate = predicate;
            lastArguments = arguments;
            if (isVariable(arguments.get(0))){
                evaluateVariableQuery(predicate, arguments);
            }
            else{

            System.out.println("Query: " + predicate + "(" + String.join(", ", arguments) + ")");
            Map<String, String> bindings = new HashMap<>();
            boolean result = solve(predicate, arguments, bindings);

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
    }
    private boolean evaluateEmptyQuery(String predicate, List<String> arguments) {
        List<FPClause> clauses = knowledgeBase.get(predicate);
        //System.out.println("Knowledge Base: " + knowledgeBase);
    
        boolean foundSolution = false;
    
        // 1. First, search for facts that match the query directly.
        if (clauses != null) {
            for (FPClause clause : clauses) {
                Map<String, String> bindings = new HashMap<>();
    
                // If it's a fact (no body), try matching it directly.
                if (clause.getBody() == null) {
                    if (matchWithBinding(clause.getHead().getTerms(), arguments, bindings) && !previousBindingsSet.contains(bindings)) {
                        if (allArgumentsHaveAssignments(arguments, bindings)) {
                            foundSolution = true;
                            previousBindingsSet.add((bindings));
                            printBindings(arguments, bindings);
                            return true;
                        }
                    }
                }
            }
        }
    
        // 2. If no facts matched, search for rules that might indirectly satisfy the query.
        if (!foundSolution && clauses != null) {
            for (FPClause clause : clauses) {
                Map<String, String> bindings = new HashMap<>();
    
                // If it's a rule (has a body), match the head and evaluate the body.
                if (clause.getBody() != null) {
                    if (matchWithBinding(clause.getHead().getTerms(), arguments, bindings) &&
                        evaluateBody(clause.getBody(), bindings) && !previousBindingsSet.contains(bindings)) {
                        if (allArgumentsHaveAssignments(arguments, bindings)) {
                            foundSolution = true;

                            previousBindingsSet.add((bindings));
                            printBindings(arguments, bindings);
                            return true;
                        }
                    }
                }
            }
        }
    
        // 3. If no direct solutions were found, attempt recursively evaluating each predicate in the body as a query.
        if (!foundSolution) {
    //        System.out.println("No direct solutions found for " + predicate + "(" + arguments + "). Checking alternative predicates...");
    
            // Recursively evaluate each predicate in the body of any clause as a potential solution.
            if (clauses != null) {
                for (FPClause clause : clauses) {
                    Map<String, String> bindings = new HashMap<>();
                    if (clause.getBody() != null) {
                        // Get each term in the body and recursively call evaluateVariableQuery on it.
                        for (FPTerm term : clause.getBody().getTerms()) {
                            String alternativePredicate = term.getName();
                            List<String> altArguments = convertTermsToStrings(term.getTerms());
    
                            // Recursively evaluate the alternative predicate
                            previousBindingsSet.add((bindings));
                            if(evaluateEmptyQuery(alternativePredicate, arguments)){
                                return true;
                            }
                            
                             // Setting foundSolution to true so we know we entered the recursive call
                        }
                    }
                }
            }
        }
    
        if (!foundSolution) {
        //    System.out.println("No solutions found.");
        }
        return false;
    }
    private boolean evaluateVariableQuery(String predicate, List<String> arguments) {
        List<FPClause> clauses = knowledgeBase.get(predicate);
        //System.out.println("Knowledge Base: " + knowledgeBase);
    
        boolean foundSolution = false;
    
        // 1. First, search for facts that match the query directly.
        if (clauses != null) {
            for (FPClause clause : clauses) {
                Map<String, String> bindings = new HashMap<>();
    
                // If it's a fact (no body), try matching it directly.
                if (clause.getBody() == null) {
                    if (matchWithBinding(clause.getHead().getTerms(), arguments, bindings) && !previousBindingsSet.contains(bindings)) {
                        if (allArgumentsHaveAssignments(arguments, bindings)) {
                            foundSolution = true;
                            previousBindingsSet.add((bindings));
                            System.out.println(clause);
                            printBindings(arguments, bindings);
                            return true;
                        }
                    }
                }
            }
        }
    
        // 2. If no facts matched, search for rules that might indirectly satisfy the query.
        if (!foundSolution && clauses != null) {
            for (FPClause clause : clauses) {
                Map<String, String> bindings = new HashMap<>();
    
                // If it's a rule (has a body), match the head and evaluate the body.
                if (clause.getBody() != null) {
                    if (matchWithBinding(clause.getHead().getTerms(), arguments, bindings) &&
                        evaluateBodyWithBacktracking(clause.getBody(), bindings) && !previousBindingsSet.contains(bindings)) {
                        if (allArgumentsHaveAssignments(arguments, bindings)) {
                            foundSolution = true;
                            //System.out.println("test");
                            previousBindingsSet.add((bindings));
                            printBindings(arguments, bindings);
                            return true;
                        }
                    }
                }
            }
        }
    
        // 3. If no direct solutions were found, attempt recursively evaluating each predicate in the body as a query.
        if (!foundSolution) {
         //   System.out.println("No direct solutions found for " + predicate + "(" + arguments + "). Checking alternative predicates...");
    
            // Recursively evaluate each predicate in the body of any clause as a potential solution.
            if (clauses != null) {
                for (FPClause clause : clauses) {
                    Map<String, String> bindings = new HashMap<>();
                    if (clause.getBody() != null) {
                        // Get each term in the body and recursively call evaluateVariableQuery on it.
                        for (FPTerm term : clause.getBody().getTerms()) {
                            String alternativePredicate = term.getName();
                            List<String> altArguments = convertTermsToStrings(term.getTerms());
                           // System.out.println(previousBindingsSet);
    
                            // Recursively evaluate the alternative predicate
                           // System.out.println(bindings);
                            previousBindingsSet.add(new HashMap<>(bindings));
                            if(evaluateVariableQuery(alternativePredicate, arguments)){
                                return true;
                            }
                            
                            
                             // Setting foundSolution to true so we know we entered the recursive call
                        }
                    }
                }
            }
        }
         
        if (!foundSolution) {
           // System.out.println("No solutions found.");
        }
        return false;
    }
    private boolean allArgumentsHaveAssignments(List<String> arguments, Map<String, String> bindings) {
        for (String arg : arguments) {
            // Check if the argument is a variable and requires a binding.
            if (isVariable(arg) && !bindings.containsKey(arg)) {
                return false; // Variable found without an assignment.
            }
        }
        return true; // All variables have bindings.
    }
    
    private List<String> convertTermsToStrings2(List<FPTerm> terms, Map<String, String> bindings) {
        List<String> result = new ArrayList<>();
        for (FPTerm term : terms) {
            String value = term.getName();
    
            // If the term is a variable and has a binding, use the binding value.
            if (isVariable(value) && bindings.containsKey(value)) {
                result.add(bindings.get(value));
            } else {
                // Otherwise, use the term's original value (constant or unbound variable).
                result.add(value);
            }
        }
        return result;
    }
    
    private void printBindings(List<String> arguments, Map<String, String> bindings) {
        System.out.print("Solution: ");
        for (String arg : arguments) {
            if (isVariable(arg)) {
                System.out.print(arg + " = " + bindings.get(arg) + " ");
            }
        }
        System.out.println();
    }
    private boolean evaluateBodyWithBacktracking(FPBody body, Map<String, String> bindings) {
        return evaluateTermsWithBacktracking(body.getTerms(), bindings, 0);
    }
    
    private boolean evaluateTermsWithBacktracking(List<FPTerm> terms, Map<String, String> bindings, int index) {
        // Base case: all terms in the body have been successfully evaluated
        if (index >= terms.size()) return true;
    
        FPTerm term = terms.get(index);
        String predicate = term.getName();
        List<String> arguments = convertTermsToStrings(term.getTerms(), bindings);
    
        // Store the current state of bindings before attempting this term
        Map<String, String> originalBindings = new HashMap<>(bindings);
    
        // Try to solve the current term
        if (solve(predicate, arguments, bindings)) {
            // If it succeeds, move to the next term
            if (evaluateTermsWithBacktracking(terms, bindings, index + 1)) {
                return true; // Continue to next term if successful
            }
            // Restore bindings if further terms failed
            restoreBindings(bindings, originalBindings);
        }
    
        // If solving the current term fails, restore the bindings and backtrack
        restoreBindings(bindings, originalBindings);
        return false;
    }
    
    // Restores the previous bindings state
    private void restoreBindings(Map<String, String> bindings, Map<String, String> originalBindings) {
        bindings.clear();
        bindings.putAll(originalBindings);
    }
    private boolean solve(String predicate, List<String> arguments, Map<String, String> bindings) {
        List<FPClause> clauses = knowledgeBase.get(predicate);

        if (clauses == null) return false;

        for (FPClause clause : clauses) {
            Map<String, String> newBindings = new HashMap<>(bindings);

            if (match(clause.getHead().getTerms(), arguments, newBindings)) {
                if (clause.getBody() == null) {
                    // Fact: Succeeds if the head matches.
                    bindings.putAll(newBindings);
                    return true;
                } else {
                    // Rule: Evaluate the body recursively.
                    if (evaluateBody(clause.getBody(), newBindings)) {
                        bindings.putAll(newBindings);
                        return true;
                    }
                }
            }
        }

        return false;
    }
    private boolean evaluateAlternativePredicates(FPBody body, Map<String, String> bindings) {
        for (FPTerm term : body.getTerms()) {
            String alternativePredicate = term.getName();
            List<String> altArguments = convertTermsToStrings(term.getTerms(), bindings);
    
            // Attempt to solve this alternative predicate directly in the knowledge base.
            if (solve(alternativePredicate, altArguments, bindings)) {
                return true;
            }
        }
        return false;
    }
    private boolean evaluateBody(FPBody body, Map<String, String> bindings) {
        for (FPTerm term : body.getTerms()) {
            String predicate = term.getName();
            List<String> arguments = convertTermsToStrings(term.getTerms(), bindings);
    
            // Check if this is a write/1 predicate
            if (predicate.equals("write") && arguments.size() == 1) {
                // Print the argument of write/1, using the current bindings for variables
                String toPrint = isVariable(arguments.get(0)) ? bindings.getOrDefault(arguments.get(0), arguments.get(0)) : arguments.get(0);
                System.out.println(toPrint);
                // Continue evaluating the rest of the body, write/1 always succeeds
                continue;
            }
    
            // For other predicates, attempt to solve
            if (!solve(predicate, arguments, bindings)) {
                return false;  // If any predicate in the body fails, the whole body fails
            }
        }
        return true;
    }
    private boolean match(List<FPTerm> clauseTerms, List<String> arguments, Map<String, String> bindings) {
        if (clauseTerms.size() != arguments.size()) return false;

        for (int i = 0; i < clauseTerms.size(); i++) {
            String term = clauseTerms.get(i).getName();
         //   System.out.println(term);
            String arg = arguments.get(i);

            if (isVariable(term)) {
                if (bindings.containsKey(term)) {
                    if (!bindings.get(term).equals(arg)) return false;
                } else {
                    bindings.put(term, arg);
                }
            } else if (!term.equals(arg)) {
                return false;
            }
        }

        return true;
    }
    private boolean matchWithBinding(List<FPTerm> clauseTerms, List<String> arguments, Map<String, String> bindings) {
        if (clauseTerms.size() != arguments.size()) return false;
    
        // Iterate over each term in the clause and the corresponding argument.
        for (int i = 0; i < clauseTerms.size(); i++) {
            String term = clauseTerms.get(i).getName();
            String arg = arguments.get(i);
    
            // Check if term is a variable
            if (isVariable(term)) {
                if (bindings.containsKey(term)) {
                    // If term variable is already bound, it must match the current argument or its binding
                    if (!bindings.get(term).equals(arg) && !bindings.get(term).equals(bindings.get(arg))) {
                        return false;
                    }
                } else {
                    // Bind term variable to the argument or its binding if it is a variable
                    bindings.put(term, bindings.getOrDefault(arg, arg));
                }
            }
            // Check if argument is a variable
            else if (isVariable(arg)) {
                if (bindings.containsKey(arg)) {
                    // If argument variable is already bound, it must match the term
                    if (!bindings.get(arg).equals(term)) {
                        return false;
                    }
                } else {
                    // Bind argument variable to the term
                    bindings.put(arg, term);
                }
            } else {
                // Both term and argument are constants; they must match exactly
                if (!term.equals(arg)) {
                    return false;
                }
            }
        }
    
        // All terms and arguments matched with the current bindings
        return true;
    }
    
    

    private boolean isVariable(String term) {
        return Character.isUpperCase(term.charAt(0));
    }

    private List<String> convertTermsToStrings(List<FPTerm> terms) {
        List<String> result = new ArrayList<>();
        for (FPTerm term : terms) {
            result.add(term.getName());
        }
        return result;
    }

    private List<String> convertTermsToStrings(List<FPTerm> terms, Map<String, String> bindings) {
        List<String> result = new ArrayList<>();
        for (FPTerm term : terms) {
            String value = term.getName();
            if (isVariable(value) && bindings.containsKey(value)) {
                result.add(bindings.get(value));
            } else {
                result.add(value);
            }
        }
        return result;
    }
}

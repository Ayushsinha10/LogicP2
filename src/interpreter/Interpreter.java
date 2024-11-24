package interpreter;
import java.util.*;
import java.util.stream.Collectors;

import parser.ast.*;

public class Interpreter {
    private final Map<String, List<FPClause>> knowledgeBase = new HashMap<>();
    private final Map<String, List<FPClause>> knowledgeBase2 = new HashMap<>();
    private FPClause firstClause = null;
    private boolean trace;
    private boolean fail = false;
    private boolean redo = false;
    private boolean check = false;
    private boolean rule = false;

    private   Map<String, FPTerm> previousBinding = new HashMap<>();
    private List<FPClause> btClause = new ArrayList<>();
    private List<FPClause> removedbtClause = new ArrayList<>();
    private List<String> writings = new ArrayList<>();
    
    private FPClause failure;
    private FPClause current;
    private FPTerm arg1;


    private String lastPredicate;
    private ArrayList<FPTerm> lastArguments;

    public Interpreter(boolean trace){
        this.trace = trace;

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
               if(!clause.getBody().getTerms().isEmpty()){
                arg1 = clause.getBody().getTerms().get(0);
               }
              // arg1 = clause.getBody().getTerms().get(0);
                evaluateQuery(clause.getBody());
              //  System.out.println( clause.getBody().getTerms().getFirst());
                
                
            }
        }
    }

    private void addFact(FPClause clause) {
        knowledgeBase
            .computeIfAbsent(clause.getHead().getName(), k -> new ArrayList<>())
            .add(clause);
            knowledgeBase2
            .computeIfAbsent(clause.getHead().getName(), k -> new ArrayList<>())
            .add(clause);
    }

    private void addRule(FPClause clause) {
        knowledgeBase
            .computeIfAbsent(clause.getHead().getName(), k -> new ArrayList<>())
            .add(clause);
            knowledgeBase2
            .computeIfAbsent(clause.getHead().getName(), k -> new ArrayList<>())
            .add(clause);
    }

    private void evaluateQuery(FPBody query) {
       
        if (query.getTerms().isEmpty()){

            if (!previousBinding.isEmpty()){
             //  removeClause(firstClause.getHead().getName(), firstClause);
               Map<String, FPTerm> bindings = new HashMap<>();
              // System.out.println(lastPredicate);
              // System.out.println(firstClause);
              // redo = true;
               check = true;
               boolean result = btCheck(lastPredicate, lastArguments);

             
               if (result) {
              //  firstClause = null;
                knowledgeBase2.clear();
                knowledgeBase2.putAll(knowledgeBase);
                bindings.clear();
                bindings = Unifyer.getBinding();
                if(trace){
                    System.out.println();
                }
                
                outWritings();
              //  System.out.println();
             //   System.out.println("Result: Yes");
                if (!bindings.isEmpty()) {
                   previousBinding = bindings;

                    printBindings(bindings);
                   
                    return;
                }
            } else {
                if(trace){
                    System.out.println();
                }
                outWritings();
              //  System.out.println();
                removedbtClause.clear();
                firstClause = null;
                btClause.clear();
               knowledgeBase2.clear();
               knowledgeBase2.putAll(knowledgeBase);
               outWritings();
               System.out.println("no");
               return;
            }

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
               
                boolean result = solvePredicate(predicate, arguments, bindings, null);

   
               if (result) {
              //  firstClause = null;
                knowledgeBase2.clear();
                knowledgeBase2.putAll(knowledgeBase);
                   bindings.clear();
                   bindings = Unifyer.getBinding();
                   
                   if(trace){
                    System.out.println();
                }
                   outWritings();
               //    System.out.println();
                   
                //   System.out.println("Result: Yes");
                   if (!bindings.isEmpty()) {
                      previousBinding = bindings;
                      printBindings(bindings);
                      
                       return;
                   }
               } else {
                if(trace){
                    System.out.println();
                }
                  outWritings();

                  removedbtClause.clear();
                  firstClause = null;
                  knowledgeBase2.clear();
                  knowledgeBase2.putAll(knowledgeBase);
                  outWritings();
                  System.out.println("no");
                  return;
               }

            }
   

      //      System.out.println("Query: " + predicate + "(" + String.join(", ", arguments) + ")");
            Map<String, FPTerm> bindings = new HashMap<>();
            // List<String> arguments2 = convertTermsToStrings(term.getTerms());
             boolean result = solvePredicate(predicate, arguments, bindings, null);

            if (result) {
                if(trace){
                System.out.println();

                }

                outWritings();

                System.out.println("yes");
                //System.out.println(current);

                if (!bindings.isEmpty()) {
                  //  bindings.forEach((var, value) -> System.out.println(var + " = " + value));
                }
            } 
            else {
                if(trace){
                   System.out.println("Fail: "+arg1);
                    System.out.println();

                }
                outWritings();
              //  System.out.println();
                knowledgeBase2.clear();
                knowledgeBase2.putAll(knowledgeBase);
      
                System.out.println("no");
            }
        
        }
    }
    private boolean solvePredicate(String predicate, ArrayList<FPTerm> arguments, Map<String, FPTerm> bindings, Map<String, List<FPClause>> kb) {
        if (kb == null){
            
            
            return solvePredicate2(predicate, arguments, bindings, knowledgeBase);

        }
        else{
            return solvePredicate2(predicate, arguments, bindings, knowledgeBase2);
        }
    }

    
    private boolean solvePredicate2(String predicate, ArrayList<FPTerm> arguments, Map<String, FPTerm> bindings, Map<String, List<FPClause>> kb) {
        // Retrieve the clauses for the given predicate from the knowledge base
        List<FPClause> clauses = kb.get(predicate);
      //  System.out.println(knowledgeBase);
    
        if (clauses == null) return false; // No clauses for this predicate
        
        boolean resultFound = false;
        
        // Track previously attempted bindings to avoid revisiting the same state

  
        // Iterate over the clauses for the predicate
        for (FPClause clause : clauses) {
//System.out.println(Unifyer.getAttempted());
           // System.out.println(clause);
           current = clause;
           if(current.isRule()){
            rule = true;

           }

            btClause.add(clause);
            if (firstClause == null && !clause.isRule()){
                firstClause = clause;
            }
            
            Map<String, FPTerm> newBindings = new HashMap<>(bindings);

    
            // Convert the head of the clause to an FPTerm (used for unification)
            FPTerm headTerm = clause.getHead().convertToTerm();
            
            // Create the query term using the provided arguments (already in FPTerm form)
            FPTerm queryTerm = new FPTerm(TKind.CTERM, predicate, arguments);
      
    
            // Check if this set of bindings has already been attempted
    
    
            // Mark this set of bindings as attempted
            Unifyer.markAttempted(newBindings);
      
            failure = clause;
            // Attempt unification between the head of the clause and the query term
            if (Unifyer.unify(headTerm, queryTerm, newBindings)) {
                // If the body is null, it's a fact (success)
                if (clause.getBody() == null) {
                    if(trace && !rule){
                        String curr = current.toString();
                    
                        curr = curr.replaceAll("\\s", "");
                        curr = curr.substring(0, curr.length() - 1);
                        
                       
    
                       System.out.println("Call: "+curr);
                    }

                    bindings.putAll(newBindings);  // Add new bindings
                    resultFound = true;
                    return true;
                } else {
   
                    // Otherwise, recursively evaluate the body (rule)

                   
                    if (evaluateBody(clause.getBody(), newBindings, clause)) {
                        bindings.putAll(newBindings);  // Add new bindings
                        resultFound = true;
                        return true;
                    }
                    else{
                        failure = clause;
                        if(!btClause.isEmpty() && !areAllRules(btClause)){
                            for(int i = 0; i < btClause.size();){
                                if(!isRule(btClause.get(i))){
                                    removeClause(btClause.get(i).getHead().getName(), btClause.get(i));
                                    btClause.remove(i);
                                    return solvePredicate(predicate, arguments, bindings, kb);

                                    

                                }
                                else{
                                    i++;
                                }



                            }
                        }
                       
                       
                    }

                }
            }

            //System.out.println(bindings);
            // If no solution was found, backtrack by restoring the bindings
            
        }
        

        return resultFound;
        }

        


  
    
    private void outWritings(){
        if(!writings.isEmpty()){

        for(int i = 0; i < writings.size(); i++){
            System.out.println(writings.get(i));
        }
        System.out.println();

        writings.clear();
    }

    }

    private boolean evaluateBody(FPBody body, Map<String, FPTerm> bindings, FPClause clause) {
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
                if(trace){
                System.out.println("Call: write("+output+")");
                }
                //System.out.println(output);
                writings.add(output);
    
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
            if (!solvePredicate(goal.getName(), goalArgs, bindings, knowledgeBase2)) {
                
              //  System.out.println(failure);
                if(trace){
                System.out.print("Fail: "+goal.getName() + "(");
                if(!bindings.isEmpty()){
                    for (int i = 0; i < goalArgs.size(); i++){
                        if (i == goalArgs.size() - 1){
                            System.out.print(bindings.get(goalArgs.get(i).getName()));
                            
                        }
                        else{
                        System.out.print(bindings.get(goalArgs.get(i).getName()));
                        System.out.print(","); 
                        }

                    }
                    
                }
                System.out.print(")");
                System.out.println();
            }
                redo = true;
                return false; // If any subgoal fails, return false
            }
            else{
                if(trace && !isRule(current)){

                    if(redo && !check){
                    String curr = current.toString();
                    
                    curr = curr.replaceAll("\\s", "");
                    curr = curr.substring(0, curr.length() - 1);
                        

                    System.out.println("Redo: "+curr);
                    redo = false;
                   }
                   else if(!check){

                    String curr = current.toString();
                    
                    curr = curr.replaceAll("\\s", "");
                    curr = curr.substring(0, curr.length() - 1);
                    
                   

                   System.out.println("Call: "+curr);
                   }
        
                   
               } 
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
    private boolean areAllRules(List<FPClause> clauses) {
        for (FPClause clause : clauses) {
            // A clause is a rule if it has both a head and a non-null body
            if (clause.getHead() == null || clause.getBody() == null) {
                return false; // Found a clause that is not a rule
            }
        }
        return true; // All clauses are rules
    }
    private boolean isRule(FPClause clause) {
        // A rule must have both a head and a non-null body
        return clause.getHead() != null && clause.getBody() != null;
    }

    private boolean removeClause(String key, FPClause clause) {
        // Get the list of clauses for the given key
        List<FPClause> clauseList = knowledgeBase2.get(key);
    
        if (clauseList != null) {
            // Remove the clause from the list
            removedbtClause.add(clause);
            boolean removed = clauseList.remove(clause);
    
            // If the list is now empty, remove the key from the map entirely
            if (clauseList.isEmpty()) {
                knowledgeBase2.remove(key);
            }
    
            return removed;
        }
    
        return false; // Key not found or clause not in list
    }
    public  void restoreKnowledgeBase() {
        knowledgeBase2.clear();
        for (String key : knowledgeBase.keySet()) {
            knowledgeBase2.put(key, new ArrayList<>(knowledgeBase.get(key)));
        }
    }
    public boolean btCheck(String lp, ArrayList<FPTerm> arguments){
         redo = true;

        Map<String, FPTerm> bindings = new HashMap<>();
        FPClause clause = firstClause;
     //   System.out.println(removedbtClause.size());
      //  System.out.println(removedbtClause.get(0));

        for(int i = 0; i < removedbtClause.size(); i++){
            //System.out.print("test");
             //   System.out.println(removedbtClause);
                removeClause(removedbtClause.get(i).getHead().getName(), removedbtClause.get(i));
                removedbtClause.remove(i);
             //   System.out.println(removedbtClause);
                
                
            
              
              
                
        }
       this.firstClause = null;

        boolean result = solvePredicate(lp, arguments, bindings, knowledgeBase2);
      //  System.out.println(previousBinding);
      //  System.out.println(bindings);
        check = false;

        if (previousBinding.equals(bindings)){

        
        //    System.out.println("test");
        //    System.out.println(clause);
            removeClause(firstClause.getHead().getName(), firstClause);
       //     System.out.println(knowledgeBase2);
            bindings.clear();
            result = solvePredicate(lp, arguments, bindings, knowledgeBase2);
            return result;


          
        

        }
        return result;

    }
    private void printBindings(Map<String, FPTerm> bindings){
        int size = bindings.size();
        int count = 0;
       // System.out.println();

        for (Map.Entry<String, FPTerm> entry : bindings.entrySet()) {
          count++;
          String key = entry.getKey();
          FPTerm value = entry.getValue();
          if (count == size) {
              System.out.print(key + " = " + value);
              System.out.println();
          } else {
              System.out.print(key + " = " + value +", ");
          }
      }
         }
    }



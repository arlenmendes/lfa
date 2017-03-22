import java.util.Scanner;
import java.util.ArrayList;
import java.util.Stack;

public class Thompson{
    
    public static class Trans{
        public int estadoInicial, estadoFinal;
        public char simbolo_transicao;

        public Trans(int e1, int e2, char simb){
            this.estadoInicial = e1;
            this.estadoFinal = e2;
            this.simbolo_transicao = simb;
        }
    }

    /*
        AFND - serves as the graph that represents the Non-Deterministic
            Finite Automata. Will use this to better combine the states.
    */
    public static class AFND{
        public ArrayList <Integer> states;
        public ArrayList <Trans> transitions;
        public int final_state;
        
        public AFND(){
            this.states = new ArrayList <Integer> ();
            this.transitions = new ArrayList <Trans> ();
            this.final_state = 0;
        }
        public AFND(int size){
            this.states = new ArrayList <Integer> ();
            this.transitions = new ArrayList <Trans> ();
            this.final_state = 0;
            this.setStateSize(size);
        }
        public AFND(char c){
            this.states = new ArrayList<Integer> ();
            this.transitions = new ArrayList <Trans> ();
            this.setStateSize(2);
            this.final_state = 1;
            this.transitions.add(new Trans(0, 1, c));
        }

        public void setStateSize(int size){
            for (int i = 0; i < size; i++)
                this.states.add(i);
        }

        public void display(){
            for (Trans t: transitions){
                System.out.println("("+ t.estadoInicial +", "+ t.simbolo_transicao +
                    ", "+ t.estadoFinal +")");
            }    
        }
    }

    /*
        kleene() - Highest Precedence regular expression operator. Thompson
            algoritm for kleene star.
    */
    public static AFND kleene(AFND n){
        AFND result = new AFND(n.states.size()+2);
        result.transitions.add(new Trans(0, 1, 'E')); // new trans for q0

        // copy existing transisitons
        for (Trans t: n.transitions){
            result.transitions.add(new Trans(t.estadoInicial + 1,
            t.estadoFinal + 1, t.simbolo_transicao));
        }
        
        // add empty transition from final n state to new final state.
        result.transitions.add(new Trans(n.states.size(), 
            n.states.size() + 1, 'E'));
        
        // Loop back from last state of n to initial state of n.
        result.transitions.add(new Trans(n.states.size(), 1, 'E'));

        // Add empty transition from new initial state to new final state.
        result.transitions.add(new Trans(0, n.states.size() + 1, 'E'));

        result.final_state = n.states.size() + 1;
        return result;
    }

    /*
        concat() - Thompson algorithm for concatenation. Middle Precedence.
    */
    public static AFND concat(AFND n, AFND m){
        ///*
        m.states.remove(0); // delete m's initial state

        // copy AFND m's transitions to n, and handles connecting n & m
        for (Trans t: m.transitions){
            n.transitions.add(new Trans(t.estadoInicial + n.states.size()-1,
                t.estadoFinal + n.states.size() - 1, t.simbolo_transicao));
        }

        // take m and combine to n after erasing inital m state
        for (Integer s: m.states){
            n.states.add(s + n.states.size() + 1);
        }
        
        n.final_state = n.states.size() + m.states.size() - 2;
        return n;
        //*/
        /* ~~~ Makes new AFND, rather than mod n. I believe my above
            sacrifice trades non-changed original for speed. Not much gain
            though. And could be implemented in the other functions.
        
        AFND result = new AFND(n.states.size() + m.states.size());

        // copy AFND n's transitions to result
        for (Trans t: n.transitions){
            result.transitions.add(new Trans(t.estadoInicial, t.estadoFinal,
                t.simbolo_transicao));
        }

        // empty transition from final state of n to beginning state of m
        result.transitions.add(new Trans(n.final_state, n.states.size(), 
            'E'));

        // copy AFND m's transitions to result
        for (Trans t: m.transitions){
            result.transitions.add(new Trans(t.estadoInicial + n.states.size(),
                t.estadoFinal + n.states.size(), t.simbolo_transicao));
        }
        
        result.final_state = n.final_state + m.final_state - 1;
        return result;
        */
    }
    
    /*
        union() - Lowest Precedence regular expression operator. Thompson
            algorithm for union (or). 
    */
    public static AFND union(AFND n, AFND m){
        AFND result = new AFND(n.states.size() + m.states.size() + 2);

        // the branching of q0 to beginning of n
        result.transitions.add(new Trans(0, 1, 'E'));
        
        // copy existing transisitons of n
        for (Trans t: n.transitions){
            result.transitions.add(new Trans(t.estadoInicial + 1,
            t.estadoFinal + 1, t.simbolo_transicao));
        }
        
        // transition from last n to final state
        result.transitions.add(new Trans(n.states.size(),
            n.states.size() + m.states.size() + 1, 'E'));

        // the branching of q0 to beginning of m
        result.transitions.add(new Trans(0, n.states.size() + 1, 'E'));

        // copy existing transisitons of m
        for (Trans t: m.transitions){
            result.transitions.add(new Trans(t.estadoInicial + n.states.size()
                + 1, t.estadoFinal + n.states.size() + 1, t.simbolo_transicao));
        }
        
        // transition from last m to final state
        result.transitions.add(new Trans(m.states.size() + n.states.size(),
            n.states.size() + m.states.size() + 1, 'E'));
       
        // 2 new states and shifted m to avoid repetition of last n & 1st m
        result.final_state = n.states.size() + m.states.size() + 1;
        return result;
    }

    /*
        Recursive Descent Parser: Recursion To Parse the String.
            I have already written a Recursive Descent Parser, and so I am 
            giving stacks a go instead. This code snippet is the basic 
            structure of my functions if I were to do RDP.
    
    // <uni> := <concat> { |<concat> }
    public static AFND uni(String regex, AFND n){
        
    }
    // <conact> := <kleene> { .<kleene> }
    public static AFND concatenation(String regex, AFND n){
        
    }
    // <kleene> := <element> | <element>*
    public static AFND kleeneStar(String regex, AFND n){
        
    }
    // <element> := letter | E | ( <uni> )
    public static AFND element(String regex, AFND n){
        if (regex.charAt(0) == '('){
            uni(regex.substring(1),n);
            if(!regex.charAt(0) == ')'){
                System.out.println("Missing End Paranthesis.");
                System.exit(1);
            }

        }
    }
    */

    // simplify the repeated boolean condition checks
    public static boolean alpha(char c){ return c >= 'a' && c <= 'z';}
    public static boolean alphabet(char c){ return alpha(c) || c == 'E';}
    public static boolean regexOperator(char c){
        return c == '(' || c == ')' || c == '*' || c == '|';
    }
    public static boolean validRegExChar(char c){
        return alphabet(c) || regexOperator(c);
    }
    // validRegEx() - checks if given string is a valid regular expression.
    public static boolean validRegEx(String regex){
        if (regex.isEmpty())
            return false;
        for (char c: regex.toCharArray())
            if (!validRegExChar(c))
                return false;
        return true;
    }

    /*
        compile() - compile given regualr expression into a AFND using 
            Thompson Construction Algorithm. Will implement typical compiler
            stack model to simplify processing the string. This gives 
            descending precedence to characters on the right.
    */
    public static AFND compile(String regex){
        if (!validRegEx(regex)){
            System.out.println("Invalid Regular Expression Input.");
            return new AFND(); // empty AFND if invalid regex
        }
        
        Stack <Character> operators = new Stack <Character> ();
        Stack <AFND> operands = new Stack <AFND> ();
        Stack <AFND> concat_stack = new Stack <AFND> ();
        boolean ccflag = false; // concat flag
        char op, c; // current character of string
        int para_count = 0;
        AFND nfaD1, nfa2;

        for (int i = 0; i < regex.length(); i++){
            c = regex.charAt(i);
            if (alphabet(c)){
                operands.push(new AFND(c));
                if (ccflag){ // concat this w/ previous
                    operators.push('.'); // '.' used to represent concat.
                }
                else
                    ccflag = true;
            }
            else{
                if (c == ')'){
                    ccflag = false;
                    if (para_count == 0){
                        System.out.println("Error: More end paranthesis "+
                            "than beginning paranthesis");
                        System.exit(1);
                    }
                    else{ para_count--;}
                    // process stuff on stack till '('
                    while (!operators.empty() && operators.peek() != '('){
                        op = operators.pop();
                        if (op == '.'){
                            nfa2 = operands.pop();
                            nfa1 = operands.pop();
                            operands.push(concat(nfa1, nfa2));
                        }
                        else if (op == '|'){
                            nfa2 = operands.pop();
                            
                            if(!operators.empty() && 
                                operators.peek() == '.'){
                                
                                concat_stack.push(operands.pop());
                                while (!operators.empty() && 
                                    operators.peek() == '.'){
                                    
                                    concat_stack.push(operands.pop());
                                    operators.pop();
                                }
                                nfa1 = concat(concat_stack.pop(),
                                    concat_stack.pop());
                                while (concat_stack.size() > 0){
                                   nfa1 =  concat(nfa1, concat_stack.pop());
                                }
                            }
                            else{
                                nfa1 = operands.pop();
                            }
                            operands.push(union(nfa1, nfa2));
                        }
                    }
                }
                else if (c == '*'){
                    operands.push(kleene(operands.pop()));
                    ccflag = true;
                }
                else if (c == '('){ // if any other operator: push
                    operators.push(c);
                    para_count++;
                }
                else if (c == '|'){
                    operators.push(c);
                    ccflag = false;
                }
            }
        }
        while (operators.size() > 0){
            if (operands.empty()){
                System.out.println("Error: imbalanace in operands and "
                + "operators");
                System.exit(1);
            }
            op = operators.pop();
            if (op == '.'){
                nfa2 = operands.pop();
                nfa1 = operands.pop();
                operands.push(concat(nfa1, nfa2));
            }
            else if (op == '|'){
                nfa2 = operands.pop();
                if( !operators.empty() && operators.peek() == '.'){
                    concat_stack.push(operands.pop());
                    while (!operators.empty() && operators.peek() == '.'){
                        concat_stack.push(operands.pop());
                        operators.pop();
                    }
                    nfa1 = concat(concat_stack.pop(),
                        concat_stack.pop());
                    while (concat_stack.size() > 0){
                       nfa1 =  concat(nfa1, concat_stack.pop());
                    }
                }
                else{
                    nfa1 = operands.pop();
                }
                operands.push(union(nfa1, nfa2));
            }
        }
        return operands.pop();
    }

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        String line;
        System.out.println("\nEnter a regular expression with the " +
            "alphabet ['a','z'] & E for empty "+"\n* for Kleene Star" + 
            "\nelements with nothing between them indicates " +
            "concatenation "+ "\n| for Union \n\":q\" to quit");
        while(sc.hasNextLine()){
            System.out.println("Enter a regular expression with the " +
                "alphabet ['a','z'] & E for empty "+"\n* for Kleene Star" + 
                "\nelements with nothing between them indicates " +
                "concatenation "+ "\n| for Union \n\":q\" to quit");
            line = sc.nextLine();
            if (line.equals(":q") || line.equals("QUIT"))
                break;
            AFND nfa_of_input = compile(line);
            System.out.println("\nNFA:");
            nfa_of_input.display();
        }
    }
}

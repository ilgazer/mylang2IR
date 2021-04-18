package com.team.mylang2IR;

public class ArithmeticExpression extends Expression {
    public enum Operation {
        ADD('+', 1), SUBTRACT('-', 1),
        MULTIPLY('*', 2), DIVIDE('/', 2),
        DEFAULT('\n', -1);

        public final char sign;

        public final int precedence;

        Operation(char sign, int precedence) {
            this.sign = sign;
            this.precedence = precedence;
        }

        public int comparePrecedence(Operation other) {
            return Integer.compare(precedence, other.precedence);
        }

        public static int comparePrecedence(char sign1, char sign2) {
            return ArithmeticExpression.Operation.getOperationFrom(sign1)
                    .comparePrecedence(ArithmeticExpression.Operation.getOperationFrom(sign2));
        }

        public static Operation getOperationFrom(char sign) {
            for (Operation value : Operation.values()) {
                if (value.sign == sign) {
                    return value;
                }
            }
            return DEFAULT;
        }

    }

    private final Operation operation;
    private final Expression leftTerm;
    private final Expression rightTerm;

    public ArithmeticExpression(char operation, Expression leftTerm, Expression rightTerm) {
        this.operation = Operation.getOperationFrom(operation);
        this.leftTerm = leftTerm;
        this.rightTerm = rightTerm;
    }
    
    private String resultVar;

    @Override
    public String getResult() {
        return resultVar;
    }

    @Override
    public String getLLVM() {
    	String ans = "";
        ans += leftTerm.getLLVM();
        String lans = leftTerm.getResult();
        ans += rightTerm.getLLVM();
        String rans = rightTerm.getResult();
        resultVar = Expression.getNewVariable();
        
        String nameOfOperation = "";
        if(operation.sign == '*')nameOfOperation = "mul";
        else if(operation.sign == '+')nameOfOperation = "add";
        else if(operation.sign == '-')nameOfOperation = "sub";
        else if(operation.sign == '/')nameOfOperation = "sdiv";
        ans += resultVar + " = " + nameOfOperation + " i32 " + lans +  ", " + rans + "\n";
        return ans;
    }
}

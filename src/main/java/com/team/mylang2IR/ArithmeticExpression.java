package com.team.mylang2IR;

import java.util.HashMap;
import java.util.Map;

public class ArithmeticExpression extends Expression {
    public enum Operation {
        ADD('+', "add", 1), SUBTRACT('-', "sub", 1),
        MULTIPLY('*', "mul", 2), DIVIDE('/', "sdiv", 2),
        DEFAULT('\n', "ERROR", -1);

        private static final Map<Character, Operation> CHARACTER_OPERATION_MAP = new HashMap<>();

        static {
            for (Operation operation : Operation.values()) {
                CHARACTER_OPERATION_MAP.put(operation.sign, operation);
            }
        }

        public final char sign;
        public final String llvmName;
        public final int precedence;

        Operation(char sign, String llvmName, int precedence) {
            this.sign = sign;
            this.llvmName = llvmName;
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
            return CHARACTER_OPERATION_MAP.getOrDefault(sign, DEFAULT);
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

    @Override
    public String getLLVM() {
        String ans = "";
        ans += leftTerm.getLLVM();
        String lans = leftTerm.getResult();
        ans += rightTerm.getLLVM();
        String rans = rightTerm.getResult();

        ans += resultVar + " = " + operation.llvmName + " i32 " + lans + ", " + rans + "\n";
        return ans;
    }
}

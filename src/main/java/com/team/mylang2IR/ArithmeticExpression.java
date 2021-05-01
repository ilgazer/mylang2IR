package com.team.mylang2IR;

import java.util.HashMap;
import java.util.Map;

public class ArithmeticExpression extends Expression {
    /**
     * Defines the four arithmetic operations defined in mylang along with the corresponding opcodes and priorities
     */
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

        /**
         * Compares the precedence of two {@code Operation} objects.
         *
         * @param other the {@code Operation} to be compared.
         * @return the value {@code 0} if this {@code Operation} has the same precedence;
         * a value less than {@code 0} if this {@code Operation} has lower precedence than the argument {@code Operation};
         * a value greater than {@code 0} if this {@code Operation} has higher precedence than the argument {@code Operation}
         */
        public int comparePrecedence(Operation other) {
            return Integer.compare(precedence, other.precedence);
        }

        /**
         * Compares the precedence of two {@code Operation} objects.
         * The value returned is identical to what would be returned by:
         * <pre>
         *    Integer.valueOf(x).compareTo(Integer.valueOf(y))
         * </pre>
         *
         * @param sign1 the first {@code Operation} to compare
         * @param sign2 the second {@code Operation} to compare
         * @return the value {@code 0} if {@code sign1} and {@code sign2} have the same precedence;
         * a value less than {@code 0} if {@code sign1} has lower precedence than the {@code sign2};
         * a value greater than {@code 0} if {@code sign1} has higher precedence than the {@code sign2}
         */
        public static int comparePrecedence(char sign1, char sign2) {
            return ArithmeticExpression.Operation.getOperationFrom(sign1)
                    .comparePrecedence(ArithmeticExpression.Operation.getOperationFrom(sign2));
        }

        /**
         * @param sign the sign corresponding to the arithmetic operation
         * @return the {@code Operation} corresponding to the {@code sign}, or {@code Operation.DEFAULT} if none match
         */
        public static Operation getOperationFrom(char sign) {
            return CHARACTER_OPERATION_MAP.getOrDefault(sign, DEFAULT);
        }
    }

    private final Operation operation;
    private final Expression leftTerm;
    private final Expression rightTerm;

    /**
     * @param operator the operator character
     * @param leftTerm the expression corresponding to the left term
     * @param rightTerm the expression corresponding to the right term
     */
    public ArithmeticExpression(char operator, Expression leftTerm, Expression rightTerm) {
        this.operation = Operation.getOperationFrom(operator);
        this.leftTerm = leftTerm;
        this.rightTerm = rightTerm;
    }
    
    /**
     * This method returns LLVM code of ArithmeticExpression instance
     * If result of this expression is related to another sub expressions it first recursively finds LLVM codes of that sub expressions
     * @return LLVM code of this expression in String format, returning String may have multiple lines.
     */
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

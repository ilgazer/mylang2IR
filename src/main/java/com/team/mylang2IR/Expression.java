package com.team.mylang2IR;

import java.util.Stack;

public abstract class Expression {

    public abstract String getResult();

    public abstract String getLLVM();

    public static Expression getExpressionFrom(String inputStr) {
        Stack<Character> tempStack = new Stack<>();
        Stack<Expression> resultStack = new Stack<>();
        char[] chars = inputStr.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];

            // Read next value token
            if (Character.isLetterOrDigit(c)) {
                StringBuilder s = new StringBuilder();
                for (; i < chars.length && Character.isLetterOrDigit(chars[i]); ++i) {
                    s.append(chars[i]);
                }
                //Check for reserved keyword
                if ("choose".equals(s.toString())) {
                    if (chars[i] != '(') {
                        throw new IllegalStateException();
                    }
                    int numParantheses = 1;
                    int currentTermIndex = i;
                    int currentNumTerms = 0;
                    String[] terms = new String[4];
                    while (numParantheses != 0) {
                        i++;
                        if (chars[i] == '(') {
                            numParantheses++;
                        } else if (chars[i] == ')') {
                            numParantheses--;
                        }
                        if (chars[i] == ',' || numParantheses == 0) {
                            terms[currentNumTerms] = inputStr.substring(currentTermIndex + 1, i);
                            currentNumTerms++;
                            currentTermIndex = i;
                        }
                    }
                    resultStack.push(new Choose(terms));
                } else {
                    resultStack.push(Value.getValueFrom(s.toString()));
                    i--;
                }
            }
            // If next token is '(', push to the tempStack.
            else if (c == '(') {
                tempStack.push(c);
            }

            //  If next token is ')', create Expression from last 2 operands and the last operator
            else if (c == ')') {
                while (!tempStack.isEmpty() && tempStack.peek() != '(') {
                    Expression right = resultStack.pop();
                    Expression left = resultStack.pop();
                    resultStack.push(new ArithmeticExpression(tempStack.pop(), left, right));
                }
                tempStack.pop();
                //Next token is an operator, hopefully.
            } else {
                while (!tempStack.isEmpty() && ArithmeticExpression.Operation.comparePrecedence(c, tempStack.peek()) <= 0) {
                    Expression right = resultStack.pop();
                    Expression left = resultStack.pop();
                    resultStack.push(new ArithmeticExpression(tempStack.pop(), left, right));
                }
                tempStack.push(c);
            }

        }

        // pop all the operators from the tempStack
        while (!tempStack.isEmpty()) {
            if (tempStack.peek() == '(') {
                throw new IllegalStateException();
            }
            Expression right = resultStack.pop();
            Expression left = resultStack.pop();
            resultStack.push(new ArithmeticExpression(tempStack.pop(), left, right));
        }
        if (resultStack.size() != 1) {
            throw new IllegalStateException();
        }
        return resultStack.pop();
    }

    public static class Choose extends Expression {
        private final Expression condition;
        private final Expression positive;
        private final Expression zero;
        private final Expression negative;

        public Choose(Expression condition, Expression positive, Expression zero, Expression negative) {
            this.condition = condition;
            this.positive = positive;
            this.zero = zero;
            this.negative = negative;
        }

        public Choose(String[] terms) {
            this(Expression.getExpressionFrom(terms[0]), Expression.getExpressionFrom(terms[1]),
                    Expression.getExpressionFrom(terms[2]), Expression.getExpressionFrom(terms[3]));
        }

        @Override
        public String getResult() {
            return null;
        }

        @Override
        public String getLLVM() {
            return null;
        }
    }
}

package com.team.mylang2IR;

import java.util.Stack;

public abstract class Expression {

    protected final String resultVar = Program.getNewTempVariable();

    public String getResult() {
        return resultVar;
    }

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
                        } else if (numParantheses == 1 && (chars[i] == ',' || chars[i] == ')')) {
                            terms[currentNumTerms] = inputStr.substring(currentTermIndex + 1, i);
                            currentNumTerms++;
                            currentTermIndex = i;
                        }
                        if (chars[i] == ')') {
                            numParantheses--;
                        }
                    }
                    if (currentNumTerms != 4) {
                        throw new IllegalStateException();
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
            } else if (!Character.isWhitespace(c)) {
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

        private final int id;
        static int chooseCnt = 0;

        public Choose(Expression condition, Expression positive, Expression zero, Expression negative) {
            this.condition = condition;
            this.positive = positive;
            this.zero = zero;
            this.negative = negative;

            chooseCnt++;
            this.id = chooseCnt;
        }

        public Choose(String[] terms) {
            this(Expression.getExpressionFrom(terms[0]), Expression.getExpressionFrom(terms[1]),
                    Expression.getExpressionFrom(terms[2]), Expression.getExpressionFrom(terms[3]));
        }

        @Override
        public String getLLVM() {
            String eq0CondName = "choose" + this.id + "cond1";
            String gt0CondName = "choose" + this.id + "cond2";
            String zeroName = "choose" + this.id + "zero";
            String posName = "choose" + this.id + "pos";
            String negName = "choose" + this.id + "neg";
            String endName = "choose" + this.id + "end";

            String resultPtr = "%choose" + this.id + "resultPtr";

            String ans = "";

            ans += resultPtr + " = alloca i32\n";

            //Equals 0 condition
            ans += "br label %" + eq0CondName + "\n";
            ans += eq0CondName + ":\n";
            ans += condition.getLLVM();
            String eq0CondResult = Program.getNewTempVariable();
            ans += eq0CondResult + " = icmp eq i32 0, " + condition.getResult() + "\n";
            ans += "br i1 " + eq0CondResult + ", label %" + zeroName + ", label %" + gt0CondName + "\n";

            //Greater than 0 condition
            ans += gt0CondName + ":\n";
            String gt0CondResult = Program.getNewTempVariable();
            ans += gt0CondResult + " = icmp sgt i32 " + condition.getResult() + ", 0\n";
            ans += "br i1 " + gt0CondResult + ", label %" + posName + ", label %" + negName + "\n";

            ans += zeroName + ":\n";
            ans += zero.getLLVM();
            ans += "store i32 " + zero.getResult() + ", i32* " + resultPtr + "\n";
            ans += "br label %" + endName + "\n";

            ans += posName + ":\n";
            ans += positive.getLLVM();
            ans += "store i32 " + positive.getResult() + ",i32* " + resultPtr + "\n";
            ans += "br label %" + endName + "\n";

            ans += negName + ":\n";
            ans += negative.getLLVM();
            ans += "store i32 " + negative.getResult() + ", i32* " + resultPtr + "\n";
            ans += "br label %" + endName + "\n";

            ans += endName + ":\n";

            //Burada pointerlı variable kullanmanın bir avantajı var mı? Sonucumuz da temporary değişken olsa?
            ans += resultVar + " = load i32* " + resultPtr + "\n";

            return ans;
        }
    }
}

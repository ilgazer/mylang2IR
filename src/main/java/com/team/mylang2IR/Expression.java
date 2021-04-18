package com.team.mylang2IR;

import java.util.Stack;

public abstract class Expression {
	
	public static int cnt = 0;
	public static String getNewVariable()
	{
		cnt++;
		return "%t" + cnt;
	}

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
        
        private int id;
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
        
        private String resultVar;
        @Override
        public String getResult() {
            return resultVar;
        }

        @Override
        public String getLLVM() {
        	String cond1Name = "choose" + this.id + "cond1";
        	String cond2Name = "choose" + this.id + "cond2";
        	String zeroName = "choose" + this.id + "zero";
        	String posName = "choose" + this.id + "pos";
        	String negName = "choose" + this.id + "neg";
        	String endName = "choose" + this.id + "end";
        	
        	String resultPtr = "%choose" + this.id + "resultPtr";
        	
        	String ans = "";
        	
        	ans += "alloca i32* " + resultPtr + "\n";
        	ans += "br label %" + cond1Name + "\n";
        	ans += cond1Name + ":\n";
        	ans += condition.getLLVM();
            String condSubResult = condition.getResult();
            String cond1Result = Expression.getNewVariable();
            ans += cond1Result + " = icmp eq i32 0, " + condSubResult + "\n";
            ans += "br i1 label " + cond1Result + ", label %" + zeroName + ", label %" + cond2Name + "\n";
            ans += cond2Name + ":\n";
            String cond2Result = Expression.getNewVariable();
            ans += cond2Result + " icmp sgt i32 0, " + condSubResult + "\n";
            ans += "br i1 label " + cond2Result + ", label %" + posName + ", label %" + negName + "\n";
            
            ans += zeroName + ":\n";
            ans += zero.getLLVM();
            ans += "store i32 " + zero.getResult() + ", i32* " + resultPtr + "\n";
            ans += "br label %" + endName + "\n";
            
            ans += posName + ":\n";
            ans += positive.getLLVM();
            ans += "store i32 " + positive.getResult() + ",i32* "+ resultPtr + "\n";
            ans += "br label %" + endName + "\n";
            
            ans += negName + ":\n";
            ans += negative.getLLVM();
            ans += "store i32 " + negative.getResult() + ", i32* " + resultPtr + "\n";
            ans += "br label %" + endName + "\n";
            
            ans += endName + ":\n";
            
            return ans;
        }
    }
}

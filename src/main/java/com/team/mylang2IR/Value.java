package com.team.mylang2IR;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Value extends Expression {
    public static Value getValueFrom(String s) {
        Matcher matcher;
        if ((matcher = Number.PATTERN.matcher(s)).matches()) {
            return Number.getNextNumber(matcher);
        } else if ((matcher = Variable.PATTERN.matcher(s)).matches()) {
            return Variable.getNextVariable(matcher);
        }
        throw new IllegalStateException();
    }

    public static class Number extends Value {
        public static final Pattern PATTERN = Pattern.compile("[0-9]+");

        private final String num;

        public Number(String num) {
            this.num = num;
        }

        @Override
        public String getResult() {
            return this.num;
        }

        @Override
        public String getLLVM() {
            return "";
        }

        public static Number getNextNumber(Matcher matcher) {
            return new Number(matcher.group(0));
        }
    }

    public static class Variable extends Value {
        public static final Pattern PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");

        private final String var;

        public Variable(String var) {
            this.var = var;
        }
        
        private String resultVar;
        @Override
        public String getResult() {
            return resultVar;
        }

        @Override
        public String getLLVM() {
        	resultVar = Expression.getNewVariable();
        	Statement.StatementList.addVar(this.var);
            String ans = resultVar + " = " + "load i32* %" + this.var + "\n";
            return ans;
        }

        public static Variable getNextVariable(Matcher matcher) {
            return new Variable(matcher.group(0));
        }
    }
}

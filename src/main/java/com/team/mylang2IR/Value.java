package com.team.mylang2IR;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The parse tree representation of a value, i.e. a numerical constant or a variable
 */
public abstract class Value extends Expression {
    /**
     * Parses the string and returns an appropriate subclass of {@link Value}
     *
     * @param inputStr the value as a string
     * @return the parse tree node that corresponds to the value
     */
    public static Value getValueFrom(String inputStr) {
        Matcher matcher;
        if ((matcher = Number.PATTERN.matcher(inputStr)).matches()) {
            return Number.getNumberFrom(matcher);
        } else if ((matcher = Variable.PATTERN.matcher(inputStr)).matches()) {
            return Variable.getVariableFrom(matcher);
        }
        throw new IllegalStateException();
    }

    /**
     * The parse tree representation of a numerical constant
     */
    public static class Number extends Value {
        public static final Pattern PATTERN = Pattern.compile("[0-9]+");

        private final String num;

        public Number(String num) {
            this.num = num;
        }
        /*
         * @return this number as a string
         */
        @Override
        public String getResult() {
            return this.num;
        }
        
        /**
         * @return an empty string
         */
        @Override
        public String getLLVM() {
            return "";
        }

        /**
         * Parses the next line and returns a parse tree node for the number
         * @param matcher the Regex matcher corresponding to the number
         * @return the parse tree node that corresponds to the number
         */
        public static Number getNumberFrom(Matcher matcher) {
            return new Number(matcher.group(0));
        }
    }

    /**
     * The parse tree representation of a variable
     */
    public static class Variable extends Value {
        public static final Pattern PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");

        private final String var;

        public Variable(String var) {
            this.var = var;
            Program.addVariable(var);
        }
        /**
         * @return LLVM code that loads this variable to a temporary variable
         */
        @Override
        public String getLLVM() {
            return resultVar + " = " + "load i32* %" + var + "\n";
        }

        /**
         * Parses the next line and returns a parse tree node for the variable
         * @param matcher the Regex matcher corresponding to the variable
         * @return the parse tree node that corresponds to the variable
         */
        public static Variable getVariableFrom(Matcher matcher) {
            return new Variable(matcher.group(0));
        }
    }
}

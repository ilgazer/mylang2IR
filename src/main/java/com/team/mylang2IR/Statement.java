package com.team.mylang2IR;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Statement {

    public static Statement getNextStatement(Scanner s) {
        if (s.hasNext(WhileStatement.PATTERN)) {
            return WhileStatement.getNextWhileStatement(s);
        } else if (s.hasNext(IfStatement.PATTERN)) {
            return IfStatement.getNextIfStatement(s);
        } else if (s.hasNext(PrintStatement.PATTERN)) {
            return PrintStatement.getNextPrintStatement(s);
        } else if (s.hasNext(AssignStatement.PATTERN)) {
            return AssignStatement.getNextAssignStatement(s);
        }
        throw new IllegalStateException();
    }

    public abstract String getLLVM();

    public static class WhileStatement extends Statement {
        public static final Pattern PATTERN = Pattern.compile("while\\((.+)\\)\\{");
        private final Expression conditional;
        private final StatementList statementList;

        public WhileStatement(Expression conditional, StatementList statementList) {
            this.conditional = conditional;
            this.statementList = statementList;
        }

        @Override
        public String getLLVM() {
            return null;
        }

        public static WhileStatement getNextWhileStatement(Scanner s) {
            Matcher matcher = PATTERN.matcher(s.nextLine());
            if (!matcher.matches()) {
                throw new IllegalStateException();
            }
            Expression condition = Expression.getExpressionFrom(matcher.group(1));
            StatementList statementList = StatementList.getNextStatementList(s);
            s.next("}");
            return new WhileStatement(condition, statementList);
        }
    }

    public static class IfStatement extends Statement {
        public static final Pattern PATTERN = Pattern.compile("if\\((.+)\\)\\{");
        private final Expression conditional;
        private final StatementList statementList;

        public IfStatement(Expression conditional, StatementList statementList) {
            this.conditional = conditional;
            this.statementList = statementList;
        }

        @Override
        public String getLLVM() {
            return null;
        }

        public static IfStatement getNextIfStatement(Scanner s) {
            Matcher matcher = PATTERN.matcher(s.nextLine());
            if (!matcher.matches()) {
                throw new IllegalStateException();
            }
            Expression condition = Expression.getExpressionFrom(matcher.group(1));
            StatementList statementList = StatementList.getNextStatementList(s);
            s.next("}");

            return new IfStatement(condition, statementList);
        }
    }

    public static class PrintStatement extends Statement {
        public static final Pattern PATTERN = Pattern.compile("print\\((.+)\\)");
        private final Expression expression;

        public PrintStatement(Expression expression) {
            this.expression = expression;
        }

        @Override
        public String getLLVM() {
            return null;
        }

        public static PrintStatement getNextPrintStatement(Scanner s) {
            Matcher matcher = PATTERN.matcher(s.nextLine());
            if (!matcher.matches()) {
                throw new IllegalStateException();
            }
            return new PrintStatement(Expression.getExpressionFrom(matcher.group(1)));
        }
    }

    public static class AssignStatement extends Statement {
        public static final Pattern PATTERN = Pattern.compile("([a-zA-Z][a-zA-Z0-9]*)=(.+)");

        private final String variable;
        private final Expression expression;

        public AssignStatement(String variable, Expression expression) {
            this.variable = variable;
            this.expression = expression;
        }

        @Override
        public String getLLVM() {
            return null;
        }

        public static AssignStatement getNextAssignStatement(Scanner s) {
            String line = s.nextLine();
            Matcher matcher = PATTERN.matcher(line);
            if (!matcher.matches()) {
                throw new IllegalStateException();
            }
            return new AssignStatement(matcher.group(1), Expression.getExpressionFrom(matcher.group(2)));
        }
    }

    public static class StatementList {
        private final ArrayList<Statement> statements;

        public StatementList(ArrayList<Statement> statements) {
            this.statements = statements;
        }

        public static StatementList getNextStatementList(Scanner s) {
            ArrayList<Statement> statements = new ArrayList<>();
            while (s.hasNextLine() && (!s.hasNext("}"))) {
                statements.add(Statement.getNextStatement(s));
            }
            return new StatementList(statements);
        }

        public String getLLVM() {
            return null;
        }
    }
}

package com.team.mylang2IR;

import java.util.ArrayList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Statement {
    public static final Pattern CURLY_BRACE_PATTERN = Pattern.compile("\\h*}\\h*");

    public static Statement getNextStatement(Queue<String> lines, boolean canHaveFlow) {
        String nextLine = lines.remove();
        Matcher matcher;
        if (canHaveFlow && (matcher = WhileStatement.PATTERN.matcher(nextLine)).matches()) {
            return WhileStatement.getNextWhileStatement(matcher, lines);
        } else if (canHaveFlow && (matcher = IfStatement.PATTERN.matcher(nextLine)).matches()) {
            return IfStatement.getNextIfStatement(matcher, lines);
        } else if ((matcher = PrintStatement.PATTERN.matcher(nextLine)).matches()) {
            return PrintStatement.getNextPrintStatement(matcher);
        } else if ((matcher = AssignStatement.PATTERN.matcher(nextLine)).matches()) {
            return AssignStatement.getNextAssignStatement(matcher);
        }
        throw new IllegalStateException();
    }

    public abstract String getLLVM();

    public static class WhileStatement extends Statement {
        public static final Pattern PATTERN = Pattern.compile("\\h*while\\h*\\((.+)\\)\\h*\\{\\h*");

        private static int whileCnt = 0;

        private final int id;
        private final Expression conditional;
        private final StatementList statementList;

        public WhileStatement(Expression conditional, StatementList statementList) {
            this.conditional = conditional;
            this.statementList = statementList;
            whileCnt++;
            id = whileCnt;
        }

        @Override
        public String getLLVM() {

            String condName = "while" + id + "condition";
            String thenName = "while" + id + "then";
            String endName = "while" + id + "end";

            String ans = "";
            ans += "br label %" + condName + "\n";
            ans += condName + ":\n";
            ans += conditional.getLLVM();

            String realConditional = Program.getNewTempVariable();
            ans += realConditional + " = icmp ne i32 0, " + conditional.getResult() + "\n";
            ans += "br i1 " + realConditional + ", label %" + thenName + ", label %" + endName + "\n";
            ans += thenName + ":\n";
            ans += statementList.getLLVM();
            ans += "br label %" + condName + "\n";
            ans += endName + ":\n";

            return ans;
        }

        public static WhileStatement getNextWhileStatement(Matcher matcher, Queue<String> lines) {
            Expression condition = Expression.getExpressionFrom(matcher.group(1));
            StatementList statementList = StatementList.getNextStatementList(lines, false);
            if (!CURLY_BRACE_PATTERN.matcher(lines.remove()).matches()) {
                throw new IllegalStateException();
            }
            return new WhileStatement(condition, statementList);
        }
    }

    public static class IfStatement extends Statement {
        public static final Pattern PATTERN = Pattern.compile("\\h*if\\h*\\((.+)\\)\\h*\\{\\h*");
        private static int ifCount = 0;

        private final int id;
        private final Expression conditional;
        private final StatementList statementList;

        public IfStatement(Expression conditional, StatementList statementList) {
            this.conditional = conditional;
            this.statementList = statementList;
            ifCount++;
            this.id = ifCount;
        }

        @Override
        public String getLLVM() {
            String condName = "if" + this.id + "condition";
            String thenName = "if" + this.id + "then";
            String endName = "if" + this.id + "end";

            String ans = "";
            ans += "br label %" + condName + "\n";
            ans += condName + ":\n";
            ans += conditional.getLLVM();

            String realCondName = Program.getNewTempVariable();
            ans += realCondName + " = " + "icmp ne i32 0, " + conditional.getResult() + "\n";

            ans += "br i1 " + realCondName + ", label %" + thenName + ", label %" + endName + "\n";

            ans += thenName + ":\n";
            ans += statementList.getLLVM();
            ans += "br label %" + endName + "\n";

            ans += endName + ":\n";

            return ans;
        }

        public static IfStatement getNextIfStatement(Matcher matcher, Queue<String> lines) {
            if (!matcher.matches()) {
                throw new IllegalStateException();
            }
            Expression condition = Expression.getExpressionFrom(matcher.group(1));
            StatementList statementList = StatementList.getNextStatementList(lines, false);

            if (!CURLY_BRACE_PATTERN.matcher(lines.remove()).matches()) {
                throw new IllegalStateException();
            }

            return new IfStatement(condition, statementList);
        }
    }

    public static class PrintStatement extends Statement {
        public static final Pattern PATTERN = Pattern.compile("\\h*print\\h*\\((.+)\\)\\h*");
        private final Expression expression;

        public PrintStatement(Expression expression) {
            this.expression = expression;
        }

        @Override
        public String getLLVM() {
            String ans = "";
            ans += expression.getLLVM();
            String resultName = expression.getResult();
            ans += "call i32 (i8*, ...)* @printf(i8* getelementptr ([4 x i8]* @print.str, i32 0, i32 0), i32 " + resultName + " )\n";
            return ans;
        }

        public static PrintStatement getNextPrintStatement(Matcher matcher) {
            if (!matcher.matches()) {
                throw new IllegalStateException();
            }
            return new PrintStatement(Expression.getExpressionFrom(matcher.group(1)));
        }
    }

    public static class AssignStatement extends Statement {
        public static final Pattern PATTERN = Pattern.compile("\\h*([a-zA-Z][a-zA-Z0-9]*)\\h*=(.+?)\\h*");

        private final String variable;
        private final Expression expression;

        public AssignStatement(String variable, Expression expression) {
            this.variable = variable;
            this.expression = expression;
        }

        @Override
        public String getLLVM() {
            Program.addVariable(variable);
            String ans = "";
            ans += expression.getLLVM();

            ans += "store i32 " + expression.getResult() + ", i32* %" + variable + "\n";
            return ans;
        }

        public static AssignStatement getNextAssignStatement(Matcher matcher) {
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

        public static StatementList getNextStatementList(Queue<String> lines, boolean canHaveFlow) {
            ArrayList<Statement> statements = new ArrayList<>();
            while (!lines.isEmpty() && !(CURLY_BRACE_PATTERN.matcher(lines.peek()).matches())) {
                statements.add(Statement.getNextStatement(lines, canHaveFlow));
            }
            return new StatementList(statements);
        }

        public String getLLVM() {

            StringBuilder ans = new StringBuilder();
            for (Statement statement : statements) {
                ans.append(statement.getLLVM());
            }
            return ans.toString();
        }
    }
}

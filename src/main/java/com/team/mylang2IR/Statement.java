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
        
        private int id;
        private static int whileCnt = 0;
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
        	
        	String realConditional = Expression.getNewVariable();
			ans += realConditional + " = icmp ne i32 0, " + conditional.getResult() + "\n";
        	ans += "br i1 " + realConditional + ", label %" + thenName + ", label %" + endName + "\n";
        	ans += thenName + ":\n";
        	ans += statementList.getLLVM();
        	ans += "br label %" + condName + "\n";
        	ans += endName + ":\n";
        	
        	return ans;
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
        private int id;
        static int ifCnt = 0;
        public IfStatement(Expression conditional, StatementList statementList) {
            this.conditional = conditional;
            this.statementList = statementList;
            ifCnt++;
            this.id = ifCnt;
        }

        @Override
        public String getLLVM() {
        	String condName = "if" + this.id + "condition";
        	String thenName = "if" + this.id + "then";
        	String endName = "if" + this.id + "end";
        	
        	String ans = "";
        	ans += "br label %" + condName + "\n";
        	ans += condName +":\n";
        	ans += conditional.getLLVM();
        	
        	String realCondName = Expression.getNewVariable();
        	ans += realCondName + " = " + "icmp ne i32 0, " + conditional.getResult() + "\n";
        	
        	ans += "br i1 " + realCondName + ", label %" + thenName + ", label %" + endName + "\n";
        	
        	ans += thenName +":\n";
        	ans += statementList.getLLVM();
        	ans += "br label %" + endName + "\n";
        	
        	ans += endName + ":\n";
        	
        	
            return ans;
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
            String ans = "";
            ans += expression.getLLVM();
            String resultName = expression.getResult();
            ans += "call i32 (i8*, ...)* @printf(i8* getelementptr ([4 x i8]* @print.str, i32 0, i32 0), i32 " + resultName + " )\n";
            return ans;
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
        	String ans = "";
        	ans += expression.getLLVM();
        	String lhs = expression.getResult();
        	
            ans += "store i32 " + lhs + ", i32* %" + variable + "\n";
            return ans;
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
        private static ArrayList<String> varNames;
        
        public static void addVar(String s)
        {
        	for(int i = 0; i < varNames.size(); i++)
        	{
        		if(varNames.get(i).equals(s))return ;
        	}
        	varNames.add(s);
        }

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
        
        
        private static boolean didInit = false;
        private String initialize()
        {
        	
        	String ans = "";
        	if(didInit)return ans;
        	didInit = true;
        	ans += "; ModuleID = 'mylang2ir'\n";
            ans += "declare i32 @printf(i8*, ...)\n";
            ans += "@print.str = constant [4 x i8] c\"%d\\0A\\0\n";
            ans += "define i32 @main() {\n";
            
            for(String s: varNames)
            {
            	ans += "%" + s + " = alloca i32\n";
            	ans +=  "store i32 0, i32* %" + s + "\n";
            }
            
            ans += getLLVM();
            ans += "ret i32 0\n";
            ans += "}\n";
            return ans;
        }

        public String getLLVM() {
        	
        	if(!didInit)return initialize();
        	
            int n = statements.size();
            String ans = "";
            
            
            for(int i = 0;i<n;i++)
            {
            	ans += statements.get(i).getLLVM();
            }
            return ans;
        }
    }
}

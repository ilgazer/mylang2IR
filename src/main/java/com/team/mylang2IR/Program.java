package com.team.mylang2IR;

import java.util.*;
import java.util.stream.Collectors;

public class Program {

    private static final HashSet<String> varNames = new HashSet<>();
    public static int tempVariableCount = 0;
    private final Statement.StatementList statementList;

    public Program(Statement.StatementList statementList) {
        this.statementList = statementList;
    }

    public static String getNewTempVariable() {
        tempVariableCount++;
        return "%t" + tempVariableCount;
    }

    public static void addVariable(String s) {
        //System.out.println("NEW VARIABLE " + s + "ADDED");
        varNames.add(s); //HashSet only contains one of each value
    }

    public String getLLVM() {

        StringBuilder result = new StringBuilder("; ModuleID = 'mylang2ir'\n" +
                "declare i32 @printf(i8*, ...)\n" +
                "@print.str = constant [4 x i8] c\"%d\\0A\\00\"\n" +
                "define i32 @main() {\n");
        for (String s : Program.varNames) {
            result.append("%").append(s).append(" = alloca i32\n")
                    .append("store i32 0, i32* %").append(s).append("\n");
        }

        result.append(statementList.getLLVM())
                .append("ret i32 0\n")
                .append("}\n");

        return result.toString();
    }

    public static Program getProgram(List<String> rawLines) throws InvalidSyntaxException {

        Queue<String> lines = rawLines
                .stream()
                .map(s -> s.replaceAll("#.*", ""))
                .collect(Collectors.toCollection(ArrayDeque::new));

        int numLines = lines.size();

        try {
            return new Program(Statement.StatementList.getNextStatementList(lines, true));
        } catch (IllegalStateException | NoSuchElementException | ArrayIndexOutOfBoundsException | EmptyStackException exception) {
            throw new InvalidSyntaxException(numLines - lines.size());
        }
    }

}

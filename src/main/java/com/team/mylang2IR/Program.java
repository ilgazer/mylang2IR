package com.team.mylang2IR;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents the program as a whole and also the root node of the parse tree. There is only one instance of
 * this generated throughout the lifetime of the program.
 */
public class Program {

    private static final HashSet<String> varNames = new HashSet<>();
    public static int tempVariableCount = 0;
    private final Statement.StatementList statementList;

    /**
     * @return the string identifier for a temporary variable in the form of %t1 or %t2
     */
    public static String getNewTempVariable() {
        tempVariableCount++;
        return "%t" + tempVariableCount;
    }

    /**
     * Adds the variable to the list of all variables used in the program.
     *
     * @param s the name of the variable
     */
    public static void addVariable(String s) {
        //System.out.println("NEW VARIABLE " + s + "ADDED");
        varNames.add(s); //HashSet only contains one of each value
    }

    /**
     * @param statementList Parse tree of the list of statements that make up the program
     */
    public Program(Statement.StatementList statementList) {
        this.statementList = statementList;
    }

    /**
     * Parses the program line by line adn generates a parse tree
     *
     * @param rawLines the lines from the file as a List
     * @return the parse tree of the program
     * @throws InvalidSyntaxException when there is invlid syntax
     */
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

}

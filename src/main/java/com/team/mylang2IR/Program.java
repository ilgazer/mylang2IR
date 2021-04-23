package com.team.mylang2IR;

import java.util.*;
import java.util.stream.Collectors;

public class Program {

    private static final HashSet<String> varNames = new HashSet<>();
    public static int cnt = 0;
    private final Statement.StatementList statementList;

    public Program(Statement.StatementList statementList) {
        this.statementList = statementList;
    }

    public static String getNewTempVariable()
    {
        cnt++;
        return "%t" + cnt;
    }

    public static void addVariable(String s) {
        //System.out.println("NEW VARIABLE " + s + "ADDED");
        varNames.add(s); //HashSet only contains one of each value
    }

    public String getLLVM() {

        String ans = "";
        StringBuilder bas = new StringBuilder();
        bas.append("; ModuleID = 'mylang2ir'\n");
        bas.append("declare i32 @printf(i8*, ...)\n");
        bas.append("@print.str = constant [4 x i8] c\"%d\\0A\\00\"\n");
        bas.append("define i32 @main() {\n");


        ans += statementList.getLLVM();
        ans += "ret i32 0\n";
        ans += "}\n";

        for (String s : Program.varNames) {
            bas.append("%").append(s).append(" = alloca i32\n");
            bas.append("store i32 0, i32* %").append(s).append("\n");
        }

        return bas + ans;
    }

    public static Program getProgram(List<String> lines) throws InvalidSyntaxException {

        String rawStr = lines.stream().map(s -> s.replaceAll("#.*", "")).collect(Collectors.joining("\n"));
        Scanner s = new Scanner(rawStr);
        try {
            return new Program(Statement.StatementList.getNextStatementList(s));
        } catch (IllegalStateException | NoSuchElementException | ArrayIndexOutOfBoundsException | EmptyStackException exception) {
            int i = lines.size();
            while (s.hasNextLine()) {
                s.nextLine();
                i--;
            }
            throw new InvalidSyntaxException(i);
        }
    }

}

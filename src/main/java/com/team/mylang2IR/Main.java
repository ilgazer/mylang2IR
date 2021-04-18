package com.team.mylang2IR;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        //TODO this accidentally combines "f1 f2" when it should instead create errors.
        String rawStr = new String(new FileInputStream("test.txt").readAllBytes()).trim().replaceAll("\\h*", "");
        Scanner s = new Scanner(rawStr);
        try {
            Statement.StatementList statementList = Statement.StatementList.getNextStatementList(s);
            System.out.println(statementList.getLLVM());
        } catch (IllegalStateException | NoSuchElementException exception) {
            int i = rawStr.split("\n").length;
            while (s.hasNextLine()) {
                s.nextLine();
                i--;
            }
            System.out.println("Line " + i + ": Syntax error");
        }
    }
}

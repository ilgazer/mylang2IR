package com.team.mylang2IR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        //TODO bir bug vardi ama ne oldugunu unuttum
        List<String> lines = Files.readAllLines(Path.of("test.txt"));
        String rawStr = lines.stream().map(s -> s.replaceAll("#.*", "")).collect(Collectors.joining("\n"));
        Scanner s = new Scanner(rawStr);
        try {
            Statement.StatementList statementList = Statement.StatementList.getNextStatementList(s);
            System.out.println(statementList.getLLVM());
        } catch (IllegalStateException | NoSuchElementException exception) {
            int i = lines.size();
            while (s.hasNextLine()) {
                s.nextLine();
                i--;
            }
            System.out.println("Line " + i + ": Syntax error");
        }
    }
}

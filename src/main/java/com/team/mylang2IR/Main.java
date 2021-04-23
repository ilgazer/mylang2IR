package com.team.mylang2IR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        //TODO bir bug vardi ama ne oldugunu unuttum
        List<String> lines = Files.readAllLines(new File("test.txt").toPath());
        try {
            Program program = Program.getProgram(lines);
            System.out.println(program.getLLVM());
        } catch (InvalidSyntaxException e) {
            System.out.println("Line " + e.line + ": Syntax error");
        }

    }
}

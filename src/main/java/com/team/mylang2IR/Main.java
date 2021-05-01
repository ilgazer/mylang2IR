package com.team.mylang2IR;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;

public class Main {
	/**
	 * LLVM code should be printed in case there is a syntax error in input mylang code
	 * @param syntax error message
	 * @return LLVM code that prints syntax error message as a String (contains multiple lines)
	 */
	public static String printSyntaxError(String s)
	{
		String ans = "";
		ans += "; ModuleID = 'mylang2ir'\n" +
                "declare i32 @printf(i8*, ...)\n" +
                "@print.str = constant [2 x i8] c\"%c\"\n" +
                "define i32 @main() {\n";
		for(int i = 0;i<s.length();i++)
		{
			char ch = s.charAt(i);
			ans += "call i32 (i8*, ...)* @printf(i8* getelementptr ([2 x i8]* @print.str, i32 0, i32 0), i32 " + (+ch) + " )\n";
		}
		
		// regular ending of LLVm code
		ans += "ret i32 0\n" + "}\n";
		
		return ans;
	}
    public static void main(String[] args) throws IOException {
    	
    	String fileName = args[0].substring(0,args[0].length()-2);
    	fileName += "ll";
    	PrintStream p = new PrintStream(new File(fileName));

        //TODO bir bug vardi ama ne oldugunu unuttum
        List<String> lines = Files.readAllLines(new File(args[0]).toPath());
        try {
            Program program = Program.getProgram(lines);
            p.println(program.getLLVM());
        } catch (InvalidSyntaxException e) {
        	p.println(printSyntaxError("Line " + e.line + ": Syntax error\n"));
        }

    }
}

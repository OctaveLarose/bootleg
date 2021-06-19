package com.github.octavelarose.codegenerator;

import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import com.github.octavelarose.codegenerator.builders.programs.CTParserProgramBuilder;
import com.github.octavelarose.codegenerator.builders.programs.TestProgramBuilder;
import com.github.octavelarose.codegenerator.export.ProgramExporter;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Main class for the code generator program.
 */
public class CodeGenerator {
    /**
     * Main function to generate a codebase.
     * @param args Unused args for now.
     */
    public static void main(String[] args) {
        if (args.length > 1 && args[0].equals("--help")) {
            printUsage();
            return;
        }

        generateProgram(args);
    }

    private static void printUsage() {
        System.out.println("--help: print this message.");
        System.out.println("--test: to generate a proof of concept program.");
        System.out.println("--ct-file FILENAME: to generate a proof of concept program.");
    }

    private static void generateProgram(String[] args) {
        HashMap<String, ClassBuilder> builders;

        try {
            if (args.length < 1 || args[0].equals("--test"))
                builders = new TestProgramBuilder().build();
            else if (args[0].equals("--ct-file") && args.length > 1) {
                builders = new CTParserProgramBuilder(args[1]).build();
            } else {
                System.out.println(Arrays.toString(args));
                printUsage();
                return;
            }
        } catch (BuildFailedException e) {
            e.printStackTrace();
            return;
        }

        new ProgramExporter().export(builders);
    }
}

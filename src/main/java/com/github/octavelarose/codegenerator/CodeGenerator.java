package com.github.octavelarose.codegenerator;

import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import com.github.octavelarose.codegenerator.builders.programs.CTParserProgramBuilder;
import com.github.octavelarose.codegenerator.builders.programs.ProgramBuilder;
import com.github.octavelarose.codegenerator.builders.programs.TestProgramBuilder;
import com.github.octavelarose.codegenerator.export.ProgramExporter;
import org.apache.commons.cli.*;

import java.util.HashMap;


/**
 * Main class for the code generator program.
 */
public class CodeGenerator {
    /**
     * Main function to generate a codebase.
     * @param args Unused args for now.
     */
    public static void main(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(CodeGenerator.getOptions(), args);

        generateProgram(cmd);
    }

    private static Options getOptions() {
        Options options = new Options();

        options.addOption("ct", "ct-file", true, "generates a program from a calltrace file");
        options.addOption("op", "op-file", true, "if a calltrace file has been provided, you can also provide a file detailing method operations");
        options.addOption("t", "test", false, "generates a very basic proof of concept program");
        options.addOption("h", "help", false, "displays this message.");

        return options;
    }

    private static void generateProgram(CommandLine cmd) {
        HashMap<String, ClassBuilder> builders;
        ProgramBuilder pb;

        if (cmd.hasOption("help")) {
            new HelpFormatter().printHelp("bootleg", CodeGenerator.getOptions());
            return;
        }

        try {
            if (cmd.hasOption("test"))
                pb = new TestProgramBuilder();
            else if (cmd.hasOption("ct-file")) {
                pb = new CTParserProgramBuilder(cmd.getOptionValue("ct-file"));
                if (cmd.hasOption("op-file"))
                    ((CTParserProgramBuilder)pb).setOperationsFileName(cmd.getOptionValue("op-file"));
            } else {
                new HelpFormatter().printHelp("bootleg", CodeGenerator.getOptions());
                return;
            }

            builders = pb.build();
        } catch (BuildFailedException e) {
            e.printStackTrace();
            return;
        }

        new ProgramExporter().export(builders);
    }
}

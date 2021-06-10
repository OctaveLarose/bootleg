package com.github.octavelarose.codegenerator;

/**
 * Main class for the code generator program.
 */
public class CodeGenerator {
    /**
     * Main function to generate a codebase.
     * @param args Unused args.
     */
    public static void main(String[] args) {
//        HashMap<String, ClassBuilder> builders = TestProgramBuilder.build();
//        ProgramExporter.export(builders);

        CTParserProgramBuilder.build();
    }
}
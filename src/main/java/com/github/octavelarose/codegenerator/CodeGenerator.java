package com.github.octavelarose.codegenerator;

import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.BasicClassBuilder;
import com.github.octavelarose.codegenerator.export.ClassExporter;
import com.github.octavelarose.codegenerator.export.ExportFailedException;


/**
 * Main class for the code generator program.
 */
public class CodeGenerator {
    /**
     * Main function to generate a codebase.
     *
     * @param args Unused args.
     */
    public static void main(String[] args) {
        BasicClassBuilder testClassBuilder = new BasicClassBuilder("TestClass", 5);
        testClassBuilder.setPackageDeclaration("com.abc.random");

        BasicClassBuilder helperClassBuilder = new BasicClassBuilder("HelperClass", 5);
        helperClassBuilder.setPackageDeclaration("com.abc.random");

        try {
            testClassBuilder.addBasicLinkedMethod("linkedMethodTest", helperClassBuilder);
        } catch (BuildFailedException e) {
            System.err.println("Creating a linked method failed: " + e.getMessage());
        }

        ClassExporter testClassExporter = new ClassExporter(testClassBuilder);
        ClassExporter helperClassExporter = new ClassExporter(helperClassBuilder);

        try {
            testClassExporter.exportToFile();
            helperClassExporter.exportToFile();
            // testClassExporter.exportToStdout();
        } catch (ExportFailedException e) {
            System.err.println("Export failed: " + e.getMessage());
        }
    }
}
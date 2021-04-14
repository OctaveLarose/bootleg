package com.github.octavelarose.codegenerator;

import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.BasicClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.EntryPointBuilder;
import com.github.octavelarose.codegenerator.export.ClassExporter;
import com.github.octavelarose.codegenerator.export.ExportFailedException;

import java.util.ArrayList;
import java.util.HashMap;


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
        HashMap<String, ClassBuilder> classBuilders = new HashMap<>();

        classBuilders.put("TestClass", new BasicClassBuilder(
                "TestClass",
                5,
                "com.abc.random")
        );

        classBuilders.put("HelperClass", new BasicClassBuilder(
                "HelperClass",
                5,
                "com.abc.random")
        );

        classBuilders.put("Main", new EntryPointBuilder("Main", "com.abc"));

        try {
            BasicClassBuilder testClass = (BasicClassBuilder) classBuilders.get("TestClass");
            testClass.addBasicLinkedMethod("linkedMethodTest", classBuilders.get("HelperClass"));
        } catch (BuildFailedException e) {
            System.err.println("Creating a linked method failed: " + e.getMessage());
        }

        ArrayList<ClassExporter> classExporters = new ArrayList<>();
        for (ClassBuilder builder : classBuilders.values()) {
            classExporters.add(new ClassExporter(builder));
        }

        try {
            for (ClassExporter exporter : classExporters) {
                exporter.exportToFile();
                // testClassExporter.exportToStdout();
            }
        } catch (ExportFailedException e) {
            System.err.println("Export failed: " + e.getMessage());
        }
    }
}
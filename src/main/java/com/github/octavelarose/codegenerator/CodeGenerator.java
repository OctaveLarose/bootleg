package com.github.octavelarose.codegenerator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.octavelarose.codegenerator.build.ClassBuilder;
import com.github.octavelarose.codegenerator.export.ClassExporter;
import com.github.octavelarose.codegenerator.export.ExportFailedException;


public class CodeGenerator {
    public static void main(String[] args) {
        // CompilationUnit cu = StaticJavaParser.parse(new File("src/main/java/com/github/octavelarose/codegenerator/samples/ReversePolishNotation.java"));
        CompilationUnit cuTestClass = new CompilationUnit();
        ClassBuilder testClassBuilder = new ClassBuilder("TestClass", 5, cuTestClass);
        testClassBuilder.setPackageDeclaration("com.test.random");

        CompilationUnit cuHelperClass = new CompilationUnit();
        ClassBuilder helperClassBuilder = new ClassBuilder("HelperClass", 5, cuHelperClass);
        helperClassBuilder.setPackageDeclaration("com.test.random");
        ClassOrInterfaceDeclaration HelperClass = helperClassBuilder.build();

        testClassBuilder.addBasicLinkedMethod("linkedMethodTest", HelperClass);

        ClassExporter testClassExporter = new ClassExporter(cuTestClass);
        ClassExporter helperClassExporter = new ClassExporter(cuHelperClass);
//        classExporter.exportToStdout();

        try {
            testClassExporter.exportToFile();
            helperClassExporter.exportToFile();
        } catch (ExportFailedException e) {
            System.err.println("Export failed: " + e.getMessage());
        }
    }
}
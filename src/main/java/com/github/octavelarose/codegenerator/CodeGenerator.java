package com.github.octavelarose.codegenerator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.io.File;
import java.io.FileNotFoundException;

import static com.github.javaparser.ast.Modifier.Keyword.*;


public class CodeGenerator {
    public static void main(String[] args) {
        // CompilationUnit cu = StaticJavaParser.parse(new File("src/main/java/com/github/octavelarose/codegenerator/samples/ReversePolishNotation.java"));
        CompilationUnit cuTestClass = new CompilationUnit();
        ClassBuilder testClassBuilder = new ClassBuilder("TestClass", 5, cuTestClass);
        testClassBuilder.setPackageDeclaration("com.test.testClass");


        CompilationUnit cuHelperClass = new CompilationUnit();
        ClassBuilder helperClassBuilder = new ClassBuilder("HelperClass", 5, cuHelperClass);
        ClassOrInterfaceDeclaration HelperClass = helperClassBuilder.build();
//
        testClassBuilder.addBasicLinkedMethod("linkedMethodTest", HelperClass);

        ClassExporter classExporter = new ClassExporter(cuTestClass, ".");
        classExporter.exportToStdout();
    }
}
package com.github.octavelarose.codegenerator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.io.File;
import java.io.FileNotFoundException;

import static com.github.javaparser.ast.Modifier.Keyword.*;


public class CodeGenerator {
    public static void main(String[] args) {
        // CompilationUnit cu = StaticJavaParser.parse(new File("src/main/java/com/github/octavelarose/codegenerator/samples/ReversePolishNotation.java"));
        CompilationUnit cu = new CompilationUnit();
        ClassBuilder testClassBuilder = new ClassBuilder("TestClass", 5, cu);
        ClassOrInterfaceDeclaration testClass = testClassBuilder.build();

        ClassBuilder helperClassBuilder = new ClassBuilder("HelperClass", 5, cu);
        ClassOrInterfaceDeclaration HelperClass = helperClassBuilder.build();
//
//        testClassBuilder.addLinkedMethod(HelperClass);
//        ClassOrInterfaceDeclaration testClass = testClassBuilder.build();

        ClassExporter classExporter = new ClassExporter("com.test.testClass", testClass);
        classExporter.exportToStdout();
    }
}
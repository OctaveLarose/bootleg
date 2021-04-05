package com.github.octavelarose.codegenerator.javapoet;

import com.squareup.javapoet.TypeSpec;

public class CodeGenerator {
    public static void main(String[] args) {
        ClassBuilder testClassBuilder = new ClassBuilder("TestClass", 5);

        ClassBuilder helperClassBuilder = new ClassBuilder("HelperClass", 5);
        TypeSpec HelperClass = helperClassBuilder.build();

        testClassBuilder.addLinkedMethod(HelperClass);
        TypeSpec testClass = testClassBuilder.build();

        ClassExporter classExporter = new ClassExporter("com.test.testClass", testClass);
        classExporter.exportToStdout();
//        classExporter.exportToFile();
    }
}
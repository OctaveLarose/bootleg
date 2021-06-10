package com.github.octavelarose.codegenerator.builders;

import com.github.octavelarose.codegenerator.builders.classes.BasicClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.EntryPointBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class TestProgramBuilder implements ProgramBuilder {
    public static HashMap<String, ClassBuilder> build() {
        HashMap<String, ClassBuilder> classBuilders = new HashMap<>();

        classBuilders.put("TestClass", new BasicClassBuilder(
                "TestClass",
                5,
                5,
                "com.abc.random")
        );

        classBuilders.put("HelperClass", new BasicClassBuilder(
                "HelperClass",
                5,
                0,
                "com.abc.random")
        );

        try {
            BasicClassBuilder testClass = (BasicClassBuilder) classBuilders.get("TestClass");
            testClass.addBasicLinkedMethod("linkedMethodTest", classBuilders.get("HelperClass"));
        } catch (BuildFailedException e) {
            System.err.println("Creating a linked method failed: " + e.getMessage());
        }

        classBuilders.put("Main", new EntryPointBuilder(
                "Main",
                "com.abc",
                new ArrayList<>(Arrays.asList(classBuilders.get("TestClass"), classBuilders.get("HelperClass")))
        ));

        return classBuilders;
    }
}

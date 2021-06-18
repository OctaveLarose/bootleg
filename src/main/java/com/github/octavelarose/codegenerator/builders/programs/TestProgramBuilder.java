package com.github.octavelarose.codegenerator.builders.programs;

import com.github.octavelarose.codegenerator.builders.BuildFailedException;
import com.github.octavelarose.codegenerator.builders.classes.BasicClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import com.github.octavelarose.codegenerator.builders.classes.EntryPointBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Builds a test program, a PoC for my system.
 */
public class TestProgramBuilder implements ProgramBuilder {
    public HashMap<String, ClassBuilder> build() throws BuildFailedException {
        System.out.println("Generating a test, proof of concept program.");

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
            throw new BuildFailedException("Creating a linked method failed: " + e.getMessage());
        }

        classBuilders.put("Main", new EntryPointBuilder(
                "Main",
                "com.abc",
                new ArrayList<>(Arrays.asList(classBuilders.get("TestClass"), classBuilders.get("HelperClass")))
        ));

        return classBuilders;
    }
}

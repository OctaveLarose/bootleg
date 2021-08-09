package com.github.octavelarose.bootleg.builders.programs;

import com.github.octavelarose.bootleg.builders.BuildFailedException;
import com.github.octavelarose.bootleg.builders.programs.classes.ClassBuilder;

import java.util.HashMap;

/**
 * Generates a program, in the form of classes definition that can then be exported.
 */
public interface ProgramBuilder {
    /**
     * Build a program.
     * @return A map made up of class definitions as well as their names.
     * @throws BuildFailedException If building the program fails.
     */
    HashMap<String, ClassBuilder> build() throws BuildFailedException;
}

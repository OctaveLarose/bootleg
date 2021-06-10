package com.github.octavelarose.codegenerator.builders;

import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;

import java.util.HashMap;

public interface ProgramBuilder {
    HashMap<String, ClassBuilder> build() throws BuildFailedException;
}

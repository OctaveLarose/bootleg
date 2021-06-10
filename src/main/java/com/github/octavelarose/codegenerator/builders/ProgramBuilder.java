package com.github.octavelarose.codegenerator.builders;

import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;

public interface ProgramBuilder {
    static HashMap<String, ClassBuilder> build() {
        throw new NotImplementedException();
    }
}

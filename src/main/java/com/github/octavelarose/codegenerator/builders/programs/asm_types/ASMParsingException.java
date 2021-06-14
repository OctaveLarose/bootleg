package com.github.octavelarose.codegenerator.builders.programs.asm_types;

import com.github.octavelarose.codegenerator.builders.BuildFailedException;

public class ASMParsingException extends BuildFailedException {
    ASMParsingException(String errMsg) {
        super(errMsg);
    }
}

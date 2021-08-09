package com.github.octavelarose.bootleg.builders.programs.program_builder_helpers.asm_types;

import com.github.octavelarose.bootleg.builders.BuildFailedException;

public class ASMParsingException extends BuildFailedException {
    ASMParsingException(String errMsg) {
        super(errMsg);
    }
}

package com.github.octavelarose.bootleg.builders.programs.asm_types;

import com.github.octavelarose.bootleg.builders.BuildFailedException;

public class ASMParsingException extends BuildFailedException {
    ASMParsingException(String errMsg) {
        super(errMsg);
    }
}

package com.github.octavelarose.codegenerator.builders;

public class BuildFailedException extends Exception {
    public BuildFailedException(String errMsg) {
        super(errMsg);
    }
}

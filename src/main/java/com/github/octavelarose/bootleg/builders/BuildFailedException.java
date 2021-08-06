package com.github.octavelarose.bootleg.builders;

/**
 * Exception thrown when building a code module goes wrong.
 */
public class BuildFailedException extends Exception {
    public BuildFailedException(String errMsg) {
        super(errMsg);
    }
}

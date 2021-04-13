package com.github.octavelarose.codegenerator.export;

/**
 * Exception thrown when exporting a code module fails.
 */
public class ExportFailedException extends Exception {
    public ExportFailedException(String errMsg) {
        super(errMsg);
    }
}

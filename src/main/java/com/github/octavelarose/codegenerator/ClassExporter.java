package com.github.octavelarose.codegenerator;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClassExporter {
    JavaFile javaFile;

    ClassExporter(String pkgName, TypeSpec classToExport) {
        this.javaFile = JavaFile.builder(pkgName, classToExport)
                .build();
    }

    public void exportToStdout() {
        try {
            this.javaFile.writeTo(System.out);
        } catch (IOException e) {
            System.err.println("Couldn't print class to stdout.");
        }
    }

    public void exportToFile() {
        try {
            this.javaFile.writeToPath(Paths.get("./OutputClass.java"));
        } catch (IOException e) {
            System.err.println("Couldn't write class to file.");
        }
    }
}

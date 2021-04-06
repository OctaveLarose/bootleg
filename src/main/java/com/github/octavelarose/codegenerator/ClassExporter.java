package com.github.octavelarose.codegenerator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;

import java.nio.file.Path;

public class ClassExporter {
    CompilationUnit cuToExport;
    Path outputPath = null;

    ClassExporter(CompilationUnit cuToExport) {
        this.cuToExport = cuToExport;
    }

    ClassExporter(CompilationUnit cuToExport, String outputPath) {
        this.cuToExport = cuToExport;
    }

    public void exportToStdout() {
        System.out.println(cuToExport.toString());
    }

    public void exportToFile() {
        System.out.println(cuToExport.toString());
    }
}

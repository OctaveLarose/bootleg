package com.github.octavelarose.codegenerator;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

public class ClassExporter {
    ClassOrInterfaceDeclaration classToExport;

    ClassExporter(String pkgName, ClassOrInterfaceDeclaration classToExport) {
        this.classToExport = classToExport;
    }

    public void exportToStdout() {
        System.out.println(classToExport.toString());
    }

    public void exportToFile() {
        // TODO.
    }
}

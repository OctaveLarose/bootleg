package com.github.octavelarose.codegenerator;

import com.github.javaparser.ast.CompilationUnit;

import java.io.*;

public class ClassExporter {
    CompilationUnit cuToExport;
    String outputPath = ClassExporter.DEFAULT_OUTPUT_PATH;

    static String DEFAULT_OUTPUT_PATH = ".";

    ClassExporter(CompilationUnit cuToExport) {
        this.cuToExport = cuToExport;
    }

    ClassExporter(CompilationUnit cuToExport, String outputPath) {
        this.cuToExport = cuToExport;
        this.outputPath = outputPath;
    }

    public void exportToStdout() {
        System.out.println(cuToExport.toString());
    }

    public void exportToFile() {
        // TODO make it use the package name
        // TODO generate the package dirs
        File newTextFile = new File(DEFAULT_OUTPUT_PATH, cuToExport.getClass().getName() + ".java");

        FileWriter fw;
        try {
            fw = new FileWriter(newTextFile);
            fw.write(cuToExport.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

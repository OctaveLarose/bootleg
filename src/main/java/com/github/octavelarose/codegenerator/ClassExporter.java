package com.github.octavelarose.codegenerator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;

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
        // TODO generate the package dirs
        Optional<PackageDeclaration> pkgDeclarationOptional = cuToExport.getPackageDeclaration();

        if (pkgDeclarationOptional.isEmpty()) {
            System.err.println("No package declaration, cannot export file");
            return;
        }

        String pkgDeclarationStr = cuToExport.getPackageDeclaration().get().toString();
        // 8 is the length of "package " ; and we remove the newlines and ; at the end
        pkgDeclarationStr = pkgDeclarationStr.substring(8, pkgDeclarationStr.length() - 3);
        String[] pkgDeclarationSplit = pkgDeclarationStr.split("\\.");
        System.out.println(Arrays.toString(pkgDeclarationSplit));

        File newTextFile = new File(DEFAULT_OUTPUT_PATH, pkgDeclarationSplit[pkgDeclarationSplit.length - 1] + ".java");

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

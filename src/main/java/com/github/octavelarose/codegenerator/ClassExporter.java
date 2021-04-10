package com.github.octavelarose.codegenerator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class ClassExporter {
    CompilationUnit cuToExport;
    String outputPath = ClassExporter.DEFAULT_PKG_OUTPUT_PATH;

    static String DEFAULT_PKG_OUTPUT_PATH = "./code_output";

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
        Optional<PackageDeclaration> pkgDeclarationOptional = cuToExport.getPackageDeclaration();

        String pkgDeclarationStr;
        if (pkgDeclarationOptional.isPresent()) {
            pkgDeclarationStr = pkgDeclarationOptional.get().toString();
        } else {
            System.err.println("No package declaration, cannot export file");
            return;
        }

        // 8 is the length of "package " ; and we remove the newlines and ; at the end
        pkgDeclarationStr = pkgDeclarationStr.substring(8, pkgDeclarationStr.length() - 3);
        ArrayList<String> pkgDeclarationSplit = new ArrayList<>(Arrays.asList(pkgDeclarationStr.split("\\.")));
        String className = pkgDeclarationSplit.remove(pkgDeclarationSplit.size() - 1);

        Path dirsPath = Paths.get(
                DEFAULT_PKG_OUTPUT_PATH,
                String.join("/", pkgDeclarationSplit));

        File dirs = new File(String.valueOf(dirsPath));
        if (!dirs.exists()) {
            boolean dirCreationResult = dirs.mkdirs();
            if (!dirCreationResult)
                System.out.println("Directory creation failed.");
        }

        File newTextFile = new File(
                String.valueOf(dirsPath),
                className.substring(0, 1).toUpperCase() + className.substring(1) + ".java"
        );

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

package com.github.octavelarose.codegenerator.export;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.octavelarose.codegenerator.builders.ClassBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class ClassExporter {
    CompilationUnit cuToExport;
    String outputPath = ClassExporter.DEFAULT_PKG_OUTPUT_PATH;

    static String DEFAULT_PKG_OUTPUT_PATH = "./code_output/main/src/main/java";

    public ClassExporter(ClassBuilder classBuilderToExport) {
        this.cuToExport = classBuilderToExport.getCompilationUnit();
    }

    public ClassExporter(CompilationUnit cuToExport, String outputPath) {
        this.cuToExport = cuToExport;
        this.outputPath = outputPath;
    }

    public void exportToStdout() {
        System.out.println(cuToExport.toString());
    }

    private ArrayList<String> getPkgDeclarationSplit() throws ExportFailedException {
        Optional<PackageDeclaration> pkgDeclarationOptional = cuToExport.getPackageDeclaration();

        String pkgDeclarationStr;
        if (pkgDeclarationOptional.isPresent()) {
            pkgDeclarationStr = pkgDeclarationOptional.get().toString();
        } else {
            throw new ExportFailedException("No package declaration, cannot export file");
        }

        // 8 is the length of "package " ; and we remove the 2 newlines and 1 ; at the end
        pkgDeclarationStr = pkgDeclarationStr.substring(8, pkgDeclarationStr.length() - 3);
        return new ArrayList<>(Arrays.asList(pkgDeclarationStr.split("\\.")));
    }

    public void exportToFile() throws ExportFailedException {
        ArrayList<String> pkgDeclarationSplit = this.getPkgDeclarationSplit();
        String className = this.getClassName();
        Path dirsPath = this.createPkgDirs(pkgDeclarationSplit);
        this.exportClassFile(dirsPath, className);
    }

    private String getClassName() {
        return cuToExport.getType(0).getName().toString();
    }

    private void exportClassFile(Path dirsPath, String className) throws ExportFailedException {
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
            throw new ExportFailedException("IOException: " + Arrays.toString(e.getStackTrace()));
        }
    }

    private Path createPkgDirs(ArrayList<String> pkgDeclarationSplit) throws ExportFailedException {
        Path dirsPath = Paths.get(
                outputPath,
                String.join("/", pkgDeclarationSplit));

        File dirs = new File(String.valueOf(dirsPath));
        if (!dirs.exists()) {
            boolean dirCreationResult = dirs.mkdirs();
            if (!dirCreationResult)
                throw new ExportFailedException("Directory creation failed.");
        }

        return dirsPath;
    }
}

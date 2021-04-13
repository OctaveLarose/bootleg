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

/**
 * Takes in a class we generated and exports it, either to a file or standard output.
 */
public class ClassExporter {
    CompilationUnit cuToExport;
    String outputPath = ClassExporter.DEFAULT_PKG_OUTPUT_PATH;

    static String DEFAULT_PKG_OUTPUT_PATH = "./code_output/main/src/main/java";

    /**
     * The class constructor.
     *
     * @param classBuilderToExport The class builder that contains all the class info.
     */
    public ClassExporter(ClassBuilder classBuilderToExport) {
        this.cuToExport = classBuilderToExport.getCompilationUnit();
    }

    /**
     * The class constructor.
     *
     * @param classBuilderToExport The class builder that contains all the class info.
     * @param outputPath           The file output path when building the package.
     */
    public ClassExporter(ClassBuilder classBuilderToExport, String outputPath) {
        this.cuToExport = classBuilderToExport.getCompilationUnit();
        this.outputPath = outputPath;
    }

    /**
     * Exports the class to stdout, printing its contents.
     */
    public void exportToStdout() {
        System.out.println(cuToExport.toString());
    }

    /**
     * Export the class to a file. Takes the package of the class into account and generates these dirs as well.
     *
     * @throws ExportFailedException Thrown if the export goes wrong.
     */
    public void exportToFile() throws ExportFailedException {
        ArrayList<String> pkgDeclarationSplit = this.getPkgDeclarationSplit();
        String className = this.getClassName();
        Path dirsPath = this.createPkgDirs(pkgDeclarationSplit);
        this.exportClassFile(dirsPath, className);
    }

    /**
     * Returns the split package declaration. Used for generating the package directories.
     *
     * @return An array containing the package declaration separated into all its subelements.
     * @throws ExportFailedException Thrown if there's no package declaration.
     */
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

    /**
     * @return The class' name.
     */
    private String getClassName() {
        return cuToExport.getType(0).getName().toString();
    }

    /**
     * Exports the actual class (and only the class file, no pkg dirs) to a file.
     *
     * @param dirsPath  The path where the class should be exported, taking into account the package subdirs.
     * @param className The name of the class, and so name of the file.
     * @throws ExportFailedException Thrown if the class can't be written to a file.
     */
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

    /**
     * Generates the class' package directories.
     *
     * @param pkgDeclarationSplit The list of directory names.
     * @return A path to the deepest part of the directories.
     * @throws ExportFailedException Thrown if creating directories goes wrong.
     */
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

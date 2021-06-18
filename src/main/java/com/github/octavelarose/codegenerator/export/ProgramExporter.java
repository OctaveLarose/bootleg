package com.github.octavelarose.codegenerator.export;

import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ProgramExporter {
    static String DEFAULT_PKG_OUTPUT_PATH = "./code_output/src/main/java";

    public void export(HashMap<String, ClassBuilder> classBuilders) {
        ArrayList<ClassExporter> classExporters = new ArrayList<>();
        int nbrClassesExported = 0;

        try {
            FileUtils.deleteDirectory(new File(DEFAULT_PKG_OUTPUT_PATH));
        } catch (IOException e) {
            System.err.println("Emptying the output directory failed. Exporting anyway.");
        }

        for (ClassBuilder builder : classBuilders.values())
            classExporters.add(new ClassExporter(builder, DEFAULT_PKG_OUTPUT_PATH));

        try {
            for (ClassExporter exporter : classExporters) {
                exporter.exportToFile();
                // exporter.exportToStdout();
                nbrClassesExported++;
            }
        } catch (ExportFailedException e) {
            System.err.println("Export failed: " + e.getMessage());
        }

        System.out.println("Successfully exported " + nbrClassesExported + " classes.");
    }
}

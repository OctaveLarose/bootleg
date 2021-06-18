package com.github.octavelarose.codegenerator.export;

import com.github.octavelarose.codegenerator.builders.classes.ClassBuilder;

import java.util.ArrayList;
import java.util.HashMap;

public class ProgramExporter {
    public void export(HashMap<String, ClassBuilder> classBuilders) {
        ArrayList<ClassExporter> classExporters = new ArrayList<>();
        int nbrClassesExported = 0;

        for (ClassBuilder builder : classBuilders.values())
            classExporters.add(new ClassExporter(builder));

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

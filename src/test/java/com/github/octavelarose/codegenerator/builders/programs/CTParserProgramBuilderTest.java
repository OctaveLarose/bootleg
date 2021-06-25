package com.github.octavelarose.codegenerator.builders.programs;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class CTParserProgramBuilderTest {

    private List<String> getCTFiles() {
        String CT_FOLDER = "./input_data/disl_awfy_calltraces";
        List<String> ctFiles = new ArrayList<>();

//        var a = new CTParserProgramBuilder("");
        File dir = new File(CT_FOLDER);
        File[] directoryListing = dir.listFiles();

        assertNotNull(directoryListing);

        for (File child : directoryListing)
            ctFiles.add(child.getAbsolutePath());
        return ctFiles;
    }

    @Test
    public void buildAllAwfyCalltraces() {
        List<String> ctFiles = getCTFiles();
        int nbrSuccessful = 0;

        assertNotNull(ctFiles);

        for (String fn: ctFiles) {
            try {
                new CTParserProgramBuilder(fn).build();
                nbrSuccessful++;
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        System.out.println(nbrSuccessful + "/" + ctFiles.size() + " built successfully.");
    }
}

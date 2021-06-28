package com.github.octavelarose.codegenerator.builders.classes.instructions;

public class ImportStatementManager {
    static public boolean areBothImportsFromSamePkg(String importStmt1, String importStmt2) {
        String[] splitImportStmt1 = importStmt1.split("\\.");
        String[] splitImportStmt2 = importStmt2.split("\\.");
        int i = splitImportStmt1.length - 1 - 1;
        int j = splitImportStmt2.length - 1 - 1;

        if (importStmt1.equals(importStmt2))
            return true;

        while (i >= 0 && j >= 0) {
            if (!splitImportStmt1[i].equals(splitImportStmt2[j]))
                return false;
            i--;
            j--;
        }

        return true;
    }
}

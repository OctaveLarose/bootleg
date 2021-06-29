package com.github.octavelarose.codegenerator.builders.classes.instructions;

public class ImportStatementManager {
    /**
     * Returns an import string, i.e a class' path separated with dots.
     * @param classTypeStr A string defining an class type under the ASM format. (https://asm.ow2.io/faq.html)
     * @return A string definining an import statement.
     */
    public static String getImportStrFromClassTypeStr(String classTypeStr) {
        String importStr = classTypeStr;

        importStr = importStr.replace("/", ".");

        if (importStr.charAt(0) == '[')
            importStr = importStr.substring(2, importStr.length() - 1);
        else
            importStr = importStr.substring(1, importStr.length() - 1);

        return importStr;
    }

    /**
     * Since we strap on a base package path to every class, "som.Random" can be the equivalent of "com.abc.(...).som.Random"
     * Pretty sure this method should be deleted and we should stop adding "com.random.abc...whatever" to every class
     * @param importStmt1 The first import statement.
     * @param importStmt2 The second import statement.
     * @return Checks if both import statements point to classes in the same package.
     */
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

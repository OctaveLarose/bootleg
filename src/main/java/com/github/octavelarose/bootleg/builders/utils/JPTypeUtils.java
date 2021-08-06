package com.github.octavelarose.bootleg.builders.utils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.util.Optional;

/**
 * Utils related to JavaParser and its type handling. Frankly only exists for one specific bit of code, originally.
 */
public class JPTypeUtils {
    /**
     * Returns a ClassOrInterfaceType from a string.
     * Needed to be wrapped in its own method as it's kind of a mouthful and got duplicated among the codebase fast.
     * @param className The name of the class.
     * @return The type of the class, from its name.
     * @throws ParseException If JP failed to parse the class. I believe this only happens if the string has a bad format.
     */
    static public ClassOrInterfaceType getClassTypeFromName(String className) throws ParseException {
        Optional<ClassOrInterfaceType> classWithName = new JavaParser()
                .parseClassOrInterfaceType(className)
                .getResult();

        if (classWithName.isEmpty())
            throw new ParseException("Couldn't parse class " + classWithName);

        return classWithName.get();
    }
}

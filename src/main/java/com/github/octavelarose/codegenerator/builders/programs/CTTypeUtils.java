package com.github.octavelarose.codegenerator.builders.programs;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.type.*;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;

import java.util.Optional;

public class CTTypeUtils {
    /**
     * Returns a JavaParser Type object from an input string with an ASM format.
     * https://asm.ow2.io/faq.html#Q7
     * @param typeStr The string defining the type.
     * @return A corresponding Type object.
     * @throws BuildFailedException If the input string isn't correct.
     */
    static Type getTypeFromStr(String typeStr) throws BuildFailedException {
        Type returnType;
        char typeChar = typeStr.charAt(0);
        boolean isArrayType = false;

        // System.out.println(PrimitiveType.Primitive.valueOf(typeStr) + ", " + typeStr);

        if (typeChar == '[') {
            typeChar = typeStr.charAt(1);
            isArrayType = true;
        }

        switch (typeChar) {
            case 'B':
                returnType = PrimitiveType.byteType();
                break;
            case 'C':
                returnType = PrimitiveType.charType();
                break;
            case 'D':
                returnType = PrimitiveType.doubleType();
                break;
            case 'F':
                returnType = PrimitiveType.floatType();
                break;
            case 'I':
                returnType = PrimitiveType.intType();
                break;
            case 'J':
                returnType = PrimitiveType.longType();
                break;
            case 'S':
                returnType = PrimitiveType.shortType();
                break;
            case 'V':
                returnType = new VoidType();
                break;
            case 'Z':
                returnType = new PrimitiveType(PrimitiveType.Primitive.BOOLEAN);
                break;
            case 'L':
                returnType = getClassTypeFromStr(typeStr);
                break;
            default:
                throw new BuildFailedException("Unknown type: " + typeStr);
        }

        if (isArrayType)
            returnType = new ArrayType(returnType);

        return returnType;
    }

    static private Type getClassTypeFromStr(String typeStr) throws BuildFailedException {
        String className = typeStr.substring(1, typeStr.length() - 1); // Removing the L and the final ;

        // Cheating for now since I don't get the info in the form <Class extends XXX>
        if (className.equals("java/lang/Class"))
            className = "Object";

        Optional<ClassOrInterfaceType> classWithName = new JavaParser()
                .parseClassOrInterfaceType(className)
                .getResult();

        if (classWithName.isEmpty())
            throw new BuildFailedException("Unknown class: " + typeStr.substring(1));

        return classWithName.get();
    }
}

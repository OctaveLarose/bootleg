package com.github.octavelarose.codegenerator.builders.programs;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.type.*;
import com.github.octavelarose.codegenerator.builders.BuildFailedException;

import java.util.ArrayList;
import java.util.List;
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

        if (typeChar == 'L') {
            returnType = getClassTypeFromStr(typeStr);
        } else {
            returnType = getPrimitiveTypeFromChar(typeChar);
        }

        if (isArrayType)
            returnType = new ArrayType(returnType);

        return returnType;
    }

    static Type getPrimitiveTypeFromChar(char typeChar) throws BuildFailedException {
        switch (typeChar) {
            case 'B':
                return PrimitiveType.byteType();
            case 'C':
                return PrimitiveType.charType();
            case 'D':
                return PrimitiveType.doubleType();
            case 'F':
                return PrimitiveType.floatType();
            case 'I':
                return PrimitiveType.intType();
            case 'J':
                return PrimitiveType.longType();
            case 'S':
                return PrimitiveType.shortType();
            case 'V':
                return new VoidType();
            case 'Z':
                return new PrimitiveType(PrimitiveType.Primitive.BOOLEAN);
            default:
                throw new BuildFailedException("Unknown type: " + typeChar);
        }
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

    // (ILjava/lang/String;I[DF)
    static public List<Type> getTypesFromParametersStr(String paramsDescriptor) throws BuildFailedException {
        List<Type> typeArr = new ArrayList<>();
        char[] argsBuf = paramsDescriptor.toCharArray();
        boolean isArrayType;
        String PRIMITIVE_REPRES = "VZCBSIFJD";

        for (int i = 0; i < paramsDescriptor.length(); i++) {
            Type newParam;

            if (argsBuf[i] == '[') {
                isArrayType = true;
                i++;
            } else {
                isArrayType = false;
            }

            if (PRIMITIVE_REPRES.indexOf(argsBuf[i]) != -1) {
                newParam = getPrimitiveTypeFromChar(argsBuf[i]);
            } else if (argsBuf[i] == 'L') {
                String objectSubStr = paramsDescriptor.substring(i, paramsDescriptor.indexOf(";", i));
                newParam = getTypeFromStr(objectSubStr);
                i += objectSubStr.length();
            } else {
                System.out.println(argsBuf[i]);
                throw new BuildFailedException("Parsing of parameters data failed.");
            }

            if (isArrayType)
                newParam = new ArrayType(newParam);

            typeArr.add(newParam);
        }
        return typeArr;
    }

    // --- Some example from the ASM lib ---
/*    public static Type[] getArgumentTypes(final String methodDescriptor) throws BuildFailedException {
        char[] buf = methodDescriptor.toCharArray();
        int off = 1;
        int size = 0;
        while (true) {
            char car = buf[off++];
            if (car == ')') {
                break;
            } else if (car == 'L') {
                while (buf[off++] != ';') {
                }
                ++size;
            } else if (car != '[') {
                ++size;
            }
        }
        Type[] args = new Type[size];
        off = 1;
        size = 0;
        while (buf[off] != ')') {
            args[size] = getTypeFromStr(methodDescriptor.substring(off));
            off += 1;
            size += 1;
        }
        return args;
    }*/
}

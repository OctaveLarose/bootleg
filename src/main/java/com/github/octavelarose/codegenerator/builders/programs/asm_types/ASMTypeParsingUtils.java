package com.github.octavelarose.codegenerator.builders.programs.asm_types;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import com.github.octavelarose.codegenerator.builders.utils.JPTypeUtils;

import java.util.ArrayList;
import java.util.List;

public class ASMTypeParsingUtils {
    /**
     * Returns a JavaParser Type object from an input string with an ASM format.
     * https://asm.ow2.io/faq.html#Q7
     * @param typeStr The string defining the type.
     * @return A corresponding Type object.
     * @throws ASMParsingException If the input string isn't correct.
     */
    public static Type getTypeFromStr(String typeStr) throws ASMParsingException {
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

    /**
     * Returns a type from a char that defines a primitive type in the ASM specification.
     * @param typeChar The char corresponding to a primitive type.
     * @return The type associated with the input char.
     * @throws ASMParsingException If the char doesn't correspond to any primitive type.
     */
    static Type getPrimitiveTypeFromChar(char typeChar) throws ASMParsingException {
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
            case 'Z':
                return PrimitiveType.booleanType();
            case 'V': // Technically not a primitive type, but the ASM specification calls it one, so I win
                return new VoidType();
            default:
                throw new ASMParsingException("Unknown type: " + typeChar);
        }
    }

    /**
     * Parses a string that defines a class/object, like "Ljava/lang/String".
     * @param typeStr The string that defines the class.
     * @return The type of the class.
     * @throws ASMParsingException If the class is unknown
     */
    static private Type getClassTypeFromStr(String typeStr) throws ASMParsingException {
        String className = typeStr.substring(1, typeStr.length() - 1); // Removing the L and the final ;

        // Note: for <Class extends XXX>, I don't get the info about the XXX class, so it's just Class for now...

        // If it's a class definition, it contains slashes, but JavaParser prefers dots.
        if (className.contains("/"))
            className = className.replace("/", ".");

        try {
            return JPTypeUtils.getClassTypeFromName(className);
        } catch (ParseException e) {
            throw new ASMParsingException(e.getMessage());
        }
    }

    /**
     * Parses the parameters descriptor part of a method descriptor, like "ILjava/lang/String;I[DF"
     * @param paramsDescriptor The parameters descriptor string.
     * @return A list of all the types.
     * @throws ASMParsingException If the parsing fails.
     */
    static public List<Type> getTypesFromParametersStr(String paramsDescriptor) throws ASMParsingException {
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
                String objectSubStr = paramsDescriptor.substring(i, paramsDescriptor.indexOf(";", i) + 1);
                newParam = getTypeFromStr(objectSubStr);
                i += (objectSubStr.length() - 1);
            } else {
                throw new ASMParsingException("Parsing of parameters data failed for character " + argsBuf[i]);
            }

            if (isArrayType)
                newParam = new ArrayType(newParam);

            typeArr.add(newParam);
        }
        return typeArr;
    }

    // --- Some example from the ASM lib ---
/*    public static Type[] getArgumentTypes(final String methodDescriptor) throws ASMParsingException {
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

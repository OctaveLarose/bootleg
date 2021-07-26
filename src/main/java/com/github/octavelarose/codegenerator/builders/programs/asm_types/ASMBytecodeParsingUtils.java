package com.github.octavelarose.codegenerator.builders.programs.asm_types;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

/**
 * Utils for parsing bytecode strings, like instruction names.
 */
public class ASMBytecodeParsingUtils {
    /**
     * Returns a type given a char describing a type in Java bytecode instructions. (ex: i for integer, l for long)
     * https://en.wikipedia.org/wiki/Java_bytecode
     * @param typeChar The char describing the type
     * @return A type corresponding to the input char
     * @throws ASMParsingException If the input doesn't correspond to any known type
     */
    public static Type getTypeFromBytecodePrefix(char typeChar) throws ASMParsingException {
        // Could handle lowercase as well
        // Not counting short/byte/char/a few others, so far
        switch (typeChar) {
            case 'D':
                return PrimitiveType.doubleType();
            case 'F':
                return PrimitiveType.floatType();
            case 'I':
                return PrimitiveType.intType();
            case 'L':
                return PrimitiveType.longType();
            default:
                throw new ASMParsingException("Unknown type: " + typeChar);
        }
    }

    /**
     * Returns an operator given an input corresponding to a bytecode operation (ADD, SUB, MUL, DIV)
     * @param bcStr The string describing the bytecode operation
     * @return An operator object corresponding to the input
     * @throws ASMParsingException If the input string is invalid
     */
    public static BinaryExpr.Operator getOperatorFromBytecodeStr(String bcStr) throws ASMParsingException {
        // Only arithmetic operations so far
        switch (bcStr) {
            case "ADD":
                return BinaryExpr.Operator.PLUS;
            case "SUB":
                return BinaryExpr.Operator.MINUS;
            case "MUL":
                return BinaryExpr.Operator.MULTIPLY;
            case "DIV":
                return BinaryExpr.Operator.DIVIDE;
            default:
                throw new ASMParsingException("Invalid operator str");
        }
    }

    /**
     * Returns an operator given an input corresponding to a bytecode operation (ADD, SUB, MUL, DIV)
     * The operator is an assignment operator, so "+=", "/=" and others.
     * Too similar to getOperatorFromBytecodeStr() for my taste.
     * @param bcStr The string describing the bytecode operation
     * @return An operator object corresponding to the input
     * @throws ASMParsingException If the input string is invalid
     */
    public static AssignExpr.Operator getAssignOperatorFromBytecodeStr(String bcStr) throws ASMParsingException {
        // Only arithmetic operations so far
        switch (bcStr) {
            case "ADD":
                return AssignExpr.Operator.PLUS;
            case "SUB":
                return AssignExpr.Operator.MINUS;
            case "MUL":
                return AssignExpr.Operator.MULTIPLY;
            case "DIV":
                return AssignExpr.Operator.DIVIDE;
            default:
                throw new ASMParsingException("Invalid operator str");
        }
    }
}

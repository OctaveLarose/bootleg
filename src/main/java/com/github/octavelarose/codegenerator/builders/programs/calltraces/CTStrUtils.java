package com.github.octavelarose.codegenerator.builders.programs.calltraces;

import java.util.List;

/**
 * Useful functions for parsing the call definition, given it as a string / list of strings.
 * Format example: "< pub/con (Lcd/CallSign;Lcd/Vector3D;)V cd/Aircraft.<init> (54562ns)"
 * Should probably be implemented as a wrapper around a list of strings corresponding to the full call definition, ...
 * ... but I'm not sure how memory inefficient / overkill / impractical that would be.
 */
public class CTStrUtils {
    public static int DIRECTION = 0;
    public static int SCOPE = 1;
    public static int DESCRIPTOR = 2;
    public static int FULLNAME = 3;
    public static int TIME = 4;

    public static String getClassName(List<String> methodArr) {
        String[] splitFullName = methodArr.get(FULLNAME).split("\\.");
        return splitFullName[0];
    }

    public static String getMethodName(List<String> methodArr) {
        String[] splitFullName = methodArr.get(FULLNAME).split("\\.");
        return splitFullName[1];
    }

    public static String getParamsStr(List<String> methodArr) {
        String descriptor = methodArr.get(DESCRIPTOR);
        String[] splitDescriptor = descriptor.split("\\)");
        return splitDescriptor[0].substring(1);
    }

    public static String getReturnTypeStr(List<String> methodArr) {
        String descriptor = methodArr.get(DESCRIPTOR);
        String[] splitDescriptor = descriptor.split("\\)");
        return splitDescriptor[1];
    }
}

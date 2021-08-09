package com.github.octavelarose.bootleg.builders;

/**
 * Constants related to code generation.
 */
public class BuildConstants {
    // The name given to constructors, by the ASM library.
    public static String CONSTRUCTOR_NAME = "<init>";

    // The name given to static init blocks, by the ASM library.
    public static String STATIC_INIT_NAME = "<clinit>";

    // How a function entry is defined in our data
    public static String ENTRY_STR = ">";

    // The length of method parameter names.
    public static int PARAM_NAME_LENGTH = 5;

    // The length of local variable names.
    public static final int LOCAL_VAR_NAME_LENGTH = 5;
}

package com.github.octavelarose.codegenerator.builders.programs.calltraces;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.octavelarose.codegenerator.builders.BuildConstants;

import java.util.List;

import static com.github.octavelarose.codegenerator.builders.BuildConstants.STATIC_INIT_NAME;

/**
 * Used to wrap a call definition, which is given as a list of strings describing the method.
 * Format example: "< pub/con (Lcd/CallSign;Lcd/Vector3D;)V cd/Aircraft.<init> (54562ns)"
 */
public class CTMethodInfo {
    public static int DIRECTION = 0;
    public static int SCOPE = 1;
    public static int DESCRIPTOR = 2;
    public static int FULLNAME = 3;
    public static int TIME = 4;

    private final List<String> methodArr;

    public CTMethodInfo(List<String> methodArr) {
        this.methodArr = methodArr;
    }

    /**
     * @param idx The method array index. Needs to be one of the public static index constants in this class.
     * @return The element of the method array corresponding to the index.
     */
     public String get(int idx) {
         return this.methodArr.get(idx);
     }

    public String getClassName() {
        String[] splitFullName = methodArr.get(FULLNAME).split("\\.");
        return splitFullName[0];
    }

    public String getMethodName() {
        String[] splitFullName = methodArr.get(FULLNAME).split("\\.");
        return splitFullName[1];
    }

    public String getParamsStr() {
        String descriptor = methodArr.get(DESCRIPTOR);
        String[] splitDescriptor = descriptor.split("\\)");
        return splitDescriptor[0].substring(1);
    }

    public String getReturnTypeStr() {
        String descriptor = methodArr.get(DESCRIPTOR);
        String[] splitDescriptor = descriptor.split("\\)");
        return splitDescriptor[1];
    }

    /**
     * @return true if it represents a function entry, false otherwise.
     */
    public boolean isFunctionEntry() {
        return this.methodArr.get(DIRECTION).equals(BuildConstants.ENTRY_STR);
    }

    /**
     * @return true if the method is a lambda, false otherwise.
     */
    public boolean isLambda() {
        return methodArr.get(FULLNAME).contains("Lambda") || methodArr.get(FULLNAME).contains("lambda");
    }

    /**
     * Static initializers, defined by <clinit>, are NOT handled so we pretend it's a regular public method.
     * TODO remove and handle static initializers instead.
     */
    public void modifyIfStaticInit() {
        if (this.getMethodName().equals(STATIC_INIT_NAME)) {
            methodArr.set(FULLNAME, methodArr.get(FULLNAME).replace(this.getMethodName(), "staticInit"));
            methodArr.set(SCOPE, methodArr.get(SCOPE).concat("/pub"));
        }
    }

    /**
     * Returns modifiers using the method's scope.
     * Needs a definition of the syntax I use somewhere, since it's my own standard.
     * @return a NodeList of Modifier objects, corresponding to the input scope
     */
    public NodeList<Modifier> getScopeModifiersList() {
        NodeList<Modifier> modifiers = new NodeList<>();
        String[] splitScope = methodArr.get(SCOPE).split("/");

        for (String modStr: splitScope) {
            if (modStr.equals("pub"))
                modifiers.add(Modifier.publicModifier());
            if (modStr.equals("pri"))
                modifiers.add(Modifier.privateModifier());
            if (modStr.equals("pro"))
                modifiers.add(Modifier.protectedModifier());
            if (modStr.equals("sta"))
                modifiers.add(Modifier.staticModifier());
        }

        return modifiers;
    }
}

package com.github.octavelarose.codegenerator.builders.classes.instructions;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.Type;
import com.github.octavelarose.codegenerator.builders.utils.RandomUtils;

/**
 * Used to generate dummy values.
 * My definition of a dummy value is an arbitrary value for a given type, not necessarily fully random.
 */
public class DummyValueCreator {
    /**
     * Remark: Expression objects are used since this is how JavaParser prefers it when setting parameter values.
     * @param parameters The parameters to get their types from.
     * @return A list of Expression objects containing dummy values, like random integers as input.
     */
    public static NodeList<Expression> getDummyParameterValuesAsExprs(NodeList<Parameter> parameters) {
        NodeList<Expression> dummyParamVals = new NodeList<>();

        for (Parameter param: parameters)
            dummyParamVals.add(new NameExpr(getDummyParamValueFromType(param.getType())));

        return dummyParamVals;
    }

    /**
     * @param t The type to get a dummy value from
     * @return A string representing a dummy value.
     */
    public static String getDummyParamValueFromType(Type t) {
        return getDummyParamValueFromTypeStr(t.toString());
    }

    /**
     * Remark: the format of the type is defined by JavaParser. This is the string representation of a JP Type object.
     * @param typeStr The name of the type to get a dummy type from.
     * @return A string representing a dummy value.
     */
    private static String getDummyParamValueFromTypeStr(String typeStr) {
        // JP can parse a NameExpr which is fed this, but it'd be better if we made it return a VariableDeclarationExpr
        if (typeStr.endsWith("[]"))
            return "new " + typeStr.substring(0, typeStr.length() - 2) + "[]{}";

        switch (typeStr) {
            case "":
                return "";
            case "boolean":
                return String.valueOf(RandomUtils.generateRandomBool());
            case "byte":
                return String.valueOf(RandomUtils.generateRandomInt(127));
            case "char":
            case "int":
            case "long":
            case "short":
                return String.valueOf(RandomUtils.generateRandomInt(10000));
            case "float":
            case "double":
                return String.valueOf(RandomUtils.generateRandomFloat());
            default: // We assume it's an object.
                return "null";
        }
    }
}

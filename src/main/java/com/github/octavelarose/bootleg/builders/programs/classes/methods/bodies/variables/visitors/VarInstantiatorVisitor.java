package com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables.visitors;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.octavelarose.bootleg.builders.BuildConstants;
import com.github.octavelarose.bootleg.builders.BuildFailedException;
import com.github.octavelarose.bootleg.builders.programs.classes.ClassBuilder;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.MethodBodyEditor;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables.DummyValueCreator;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables.LocalVariableFetcher;
import com.github.octavelarose.bootleg.builders.programs.utils.JPTypeUtils;
import com.github.octavelarose.bootleg.builders.programs.utils.RandomUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public abstract class VarInstantiatorVisitor {
    protected MethodBodyEditor methodBodyEditor;
    protected LocalVariableFetcher localVariableFetcher;

    /**
     * Main method of the visitor. To be extended by subclasses for them to call their logic.
     * @param methodBodyEditor The MethodBodyEditor object used to manipulate the method's instructions.
     * @param localVariableFetcher LocalVariableFetcher the MBE relies on to fetch local variables.
     * @throws BuildFailedException If instantiating the method goes wrong in one of the children visitors.
     */
    public void visit(MethodBodyEditor methodBodyEditor, LocalVariableFetcher localVariableFetcher) throws BuildFailedException {
        this.methodBodyEditor = methodBodyEditor;
        this.localVariableFetcher = localVariableFetcher;
    }

    /**
     * Creates a new variable of a given class type, being given a class.
     * @param inputClass The class which needs a new instance
     * @return The variable in question.
     * @throws BuildFailedException If the class can't be instantiated because it has no constructors,
     */
    protected VariableDeclarator createNewVarOfTypeObj(ClassBuilder inputClass) throws BuildFailedException {
        List<ConstructorDeclaration> constructors = inputClass.getConstructors();

        if (constructors.size() == 0)
            throw new BuildFailedException("Can't instantiate a new instance of class "
                    + inputClass.getName()
                    + ", as it has no constructors");

        var dummyParamVals = DummyValueCreator.getDummyParameterValuesAsExprs(constructors.get(0).getParameters());

        try {
            ClassOrInterfaceType classType = JPTypeUtils.getClassTypeFromName(inputClass.getImportStr());
            var varDeclarator = new VariableDeclarator(classType,
                    RandomUtils.generateRandomName(BuildConstants.LOCAL_VAR_NAME_LENGTH),
                    new ObjectCreationExpr().setType(classType).setArguments(dummyParamVals));

            methodBodyEditor.addStatement(new ExpressionStmt(new VariableDeclarationExpr(varDeclarator)));

            return varDeclarator;
        } catch (ParseException e) {
            throw new BuildFailedException("ParseException: " + e.getMessage());
        }
    }

    /**
     * Returns a list of filled parameter values inferred from context, i.e local variable/parameter names where possible.
     * @param parameters The input parameters
     * @param otherClasses The other classes we created so far.
     * @return A list of Expression objects containing values, like local variable names.
     */
    protected NodeList<Expression> getParamValuesFromContext(NodeList<Parameter> parameters, HashMap<String, ClassBuilder> otherClasses) {
        NodeList<Expression> paramValues = new NodeList<>();

        for (Parameter param: parameters) {
            Optional<VariableDeclarator> localVar = this.localVariableFetcher.getLocalVarOrParamOfType(param.getType());

            if (localVar.isPresent()) {
                paramValues.add(new NameExpr(localVar.get().getName()));
            } else {
                String varTypeStr = DummyValueCreator.getDummyParamValueFromType(param.getType());

                // TODO clean up this
                if (!varTypeStr.equals("null"))
                    paramValues.add(new NameExpr(DummyValueCreator.getDummyParamValueFromType(param.getType())));
                else if (param.getType().asString().startsWith("java.")) {
                    paramValues.add(new NullLiteralExpr());
                } else {
                    ClassOrInterfaceType classType = param.getType().asClassOrInterfaceType();
                    ClassBuilder cb = otherClasses.get(classType.getNameWithScope().replace(".", "/"));

                    // TODO check if there are constructors
                    var dummyParamVals = DummyValueCreator.getDummyParameterValuesAsExprs(cb.getConstructors().get(0).getParameters());

                    paramValues.add(new ObjectCreationExpr().setType(classType).setArguments(dummyParamVals));
                }
            }
        }

        return paramValues;
    }
}

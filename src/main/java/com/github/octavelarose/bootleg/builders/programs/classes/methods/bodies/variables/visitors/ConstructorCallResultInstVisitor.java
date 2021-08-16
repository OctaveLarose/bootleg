package com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables.visitors;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.octavelarose.bootleg.builders.BuildConstants;
import com.github.octavelarose.bootleg.builders.BuildFailedException;
import com.github.octavelarose.bootleg.builders.programs.classes.ClassBuilder;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.MethodBodyEditor;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.variables.LocalVariableFetcher;
import com.github.octavelarose.bootleg.builders.programs.utils.JPTypeUtils;
import com.github.octavelarose.bootleg.builders.programs.utils.RandomUtils;

import java.util.HashMap;

/**
 * Puts the result of a constructor call into a local variable.
 */
public class ConstructorCallResultInstVisitor extends VarInstantiatorVisitor {
    // The callee class to which the constructor belongs.
    ClassBuilder calleeClass;

    // The constructor's parameters.
    NodeList<Parameter> parameters;

    // The other classes in our system.
    HashMap<String, ClassBuilder> classesContext;

    public ConstructorCallResultInstVisitor setCallerClass(ClassBuilder calleeClass) {
        this.calleeClass = calleeClass;
        return this;
    }

    public ConstructorCallResultInstVisitor setParameters(NodeList<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public ConstructorCallResultInstVisitor setClassesContext(HashMap<String, ClassBuilder> classesContext) {
        this.classesContext = classesContext;
        return this;
    }

    @Override
    public void visit(MethodBodyEditor methodBodyEditor, LocalVariableFetcher localVariableFetcher) throws BuildFailedException {
        super.visit(methodBodyEditor, localVariableFetcher);
        this.addConstructorCallToLocalVar(calleeClass, parameters, classesContext);
    }

    /**
     * Adds a call to a constructor, i.e instantiates a class and puts it in a new local variable.
     * @param calleeClass The class to be instantiated
     * @param constructorParameters Parameters of the class constructor.
     * @param classesContext The other classes we created so far.
     * @throws BuildFailedException If the class with the given name couldn't be accessed.
     */
    private void addConstructorCallToLocalVar(ClassBuilder calleeClass,
                                             NodeList<Parameter> constructorParameters,
                                             HashMap<String, ClassBuilder> classesContext) throws BuildFailedException {
        var dummyParamVals = this.getParamValuesFromContext(constructorParameters, classesContext);

        try {
            ClassOrInterfaceType classWithName = JPTypeUtils.getClassTypeFromName(calleeClass.getImportStr());

            methodBodyEditor.addStatement(new ExpressionStmt(new VariableDeclarationExpr(
                            new VariableDeclarator(classWithName, RandomUtils.generateRandomName(BuildConstants.LOCAL_VAR_NAME_LENGTH),
                                    new ObjectCreationExpr().setType(classWithName).setArguments(dummyParamVals))
                    ))
            );
        } catch (ParseException e) {
            throw new BuildFailedException(e.getMessage());
        }
    }
}

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

public class ConstructorCallResultInstVisitor extends VarInstantiatorVisitor {
    ClassBuilder calleeClass;
    NodeList<Parameter> parameters;
    HashMap<String, ClassBuilder> otherClasses;

    public ConstructorCallResultInstVisitor(ClassBuilder calleeClass, NodeList<Parameter> parameters, HashMap<String, ClassBuilder> otherClasses) {
        this.calleeClass = calleeClass;
        this.parameters = parameters;
        this.otherClasses = otherClasses;
    }

    @Override
    public void visit(MethodBodyEditor methodBodyEditor, LocalVariableFetcher localVariableFetcher) throws BuildFailedException {
        super.visit(methodBodyEditor, localVariableFetcher);
        this.addConstructorCallToLocalVar(calleeClass, parameters, otherClasses);
    }

    /**
     * Adds a call to a constructor, i.e instantiates a class and puts it in a new local variable.
     * @param calleeClass The class to be instantiated
     * @param constructorParameters Parameters of the class constructor.
     * @param otherClasses The other classes we created so far.
     * @throws BuildFailedException If the class with the given name couldn't be accessed.
     */
    public void addConstructorCallToLocalVar(ClassBuilder calleeClass,
                                             NodeList<Parameter> constructorParameters,
                                             HashMap<String, ClassBuilder> otherClasses) throws BuildFailedException {
        var dummyParamVals = this.getParamValuesFromContext(constructorParameters, otherClasses);

        try {
            ClassOrInterfaceType classWithName = JPTypeUtils.getClassTypeFromName(calleeClass.getImportStr());

            // Added to the start to make sure it's instantiated before the operations that need it, since those operations may be in variable instantiations themselves
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

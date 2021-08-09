package com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.octavelarose.bootleg.builders.BuildConstants;
import com.github.octavelarose.bootleg.builders.BuildFailedException;
import com.github.octavelarose.bootleg.builders.programs.classes.ClassBuilder;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.MethodCallInstructionWriter;
import com.github.octavelarose.bootleg.builders.programs.classes.methods.bodies.values.DummyValueCreator;
import com.github.octavelarose.bootleg.builders.programs.utils.JPTypeUtils;
import com.github.octavelarose.bootleg.builders.programs.utils.RandomUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class CallableMethodBodyEditor extends MethodBodyEditor {
    private final CallableDeclaration<?> method;
    private final ClassBuilder parentClass;

    /**
     * A constructor that takes in a method object.
     * @param method The method object to get the existing method body from.
     */
    public CallableMethodBodyEditor(CallableDeclaration<?> method, ClassBuilder parentClass) throws BuildFailedException {
        BlockStmt methodBody = this.getMethodBodyOfCallable(method);

        this.method = method;
        this.parentClass = parentClass;

        // Not very good, what if there's a return statement in the middle of the function?
        // Will do the job fine so far since we're assuming all methods will have this distinct var insn/calculations/return statement structure
        for (Statement stmt: methodBody.getStatements()) {
            if (stmt instanceof ExpressionStmt && ((ExpressionStmt)stmt).getExpression().isVariableDeclarationExpr())
                this.varsInsnBlock.addStatement(stmt);
            else if (stmt instanceof ReturnStmt)
                this.returnStmt = (ReturnStmt) stmt;
            else
                this.regularInstrsBlock.addStatement(stmt);
        }

        this.setMethodParameters(method.getParameters());
    }

    /**
     * @return The caller method's body, containing the method instructions.
     * @throws BuildFailedException If the method's type can't be inferred (i.e it isn't a method/constructor)
     */
    private BlockStmt getMethodBodyOfCallable(CallableDeclaration<?> method) throws BuildFailedException {
        BlockStmt methodBody;

        if (method instanceof MethodDeclaration) {
            MethodDeclaration md = ((MethodDeclaration) method);
            if (md.getBody().isEmpty()) { // Should never happen since methods are always instantiated with an empty block
                methodBody = new BlockStmt();
                md.setBody(methodBody);
            } else {
                methodBody = md.getBody().get();
            }
        } else if (method instanceof ConstructorDeclaration)
            methodBody = ((ConstructorDeclaration) method).getBody();
        else
            throw new BuildFailedException("Couldn't find method body, as this is neither a classic method nor a constructor");

        return methodBody;
    }

    /**
     * Sets the fabricated body to the wrapped callable.
     */
    public void setBodyToCallable() throws BuildFailedException {
        if (this.method instanceof ConstructorDeclaration) {
            ((ConstructorDeclaration) this.method).setBody(this.generateMethodBody());
        }
        else if (this.method instanceof MethodDeclaration)
            ((MethodDeclaration) this.method).setBody(this.generateMethodBody());
        else
            throw new BuildFailedException("Couldn't set method body, as this is neither a classic method nor a constructor");
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
            this.addVarInsnStatement(new ExpressionStmt(new VariableDeclarationExpr(
                            new VariableDeclarator(classWithName, RandomUtils.generateRandomName(BuildConstants.LOCAL_VAR_NAME_LENGTH),
                                    new ObjectCreationExpr().setType(classWithName).setArguments(dummyParamVals))
                    ))
            );
        } catch (ParseException e) {
            throw new BuildFailedException(e.getMessage());
        }
    }

    /**
     * Generates a new statement from a method call, a var. instantiation statement or a regular statement if void is returned
     * @param method A method instance
     * @param calleeClass The class the method belongs to
     * @param isCalleeMethodStatic Whether or not the method is static
     * @param otherClasses The other classes we created so far.
     */
    public void addMethodCallToLocalVar(MethodDeclaration method,
                                        ClassBuilder calleeClass,
                                        MethodCallInstructionWriter.IsCalleeMethodStatic isCalleeMethodStatic,
                                        HashMap<String, ClassBuilder> otherClasses) throws BuildFailedException {
        String calleeClassName = calleeClass.getName();
        NodeList<Expression> dummyParamVals = this.getParamValuesFromContext(method.getParameters(), otherClasses);
        MethodCallExpr methodCallExpr = new MethodCallExpr()
                .setName(method.getName())
                .setArguments(dummyParamVals);

        if (isCalleeMethodStatic == MethodCallInstructionWriter.IsCalleeMethodStatic.YES) {
            methodCallExpr.setScope(new NameExpr(calleeClassName));
        } else {
            if (calleeClassName.equals(this.parentClass.getName()))
                methodCallExpr.setScope(new ThisExpr());
            else {
                Optional<VariableDeclarator> localVarOfType = this.getLocalVarOrParamOfTypeObjFromStr(calleeClass.getImportStr());

                if (localVarOfType.isPresent())
                    methodCallExpr.setScope(new NameExpr(localVarOfType.get().getName()));
                else {
                    // We instantiate a new class of the given type if none is present to access the method from.
                    // This safeguard shouldn't exist, since there should always be an option to find an instance of one in the input real program (else it wouldn't run).
                    // This is needed (as of 05/08/21) since for instance, a class instance could only be present in a field, and those aren't implemented yet.
                    VariableDeclarator newVar = this.createNewVarOfTypeObj(calleeClass);
                    methodCallExpr.setScope(new NameExpr(newVar.getName()));
                }
            }
        }

        if (method.getType().isVoidType())
            this.addRegularStatement(new ExpressionStmt(methodCallExpr));
        else
            this.addVarInsnStatement(new ExpressionStmt(new VariableDeclarationExpr(
                    new VariableDeclarator(method.getType(),
                            RandomUtils.generateRandomName(BuildConstants.LOCAL_VAR_NAME_LENGTH),
                            methodCallExpr))
            ));
    }

    /**
     * Returns a list of filled parameter values inferred from context, i.e local variable/parameter names where possible.
     * @param parameters The input parameters
     * @param otherClasses The other classes we created so far.
     * @return A list of Expression objects containing values, like local variable names.
     */
    private NodeList<Expression> getParamValuesFromContext(NodeList<Parameter> parameters, HashMap<String, ClassBuilder> otherClasses) {
        NodeList<Expression> paramValues = new NodeList<>();

        for (Parameter param: parameters) {
            Optional<VariableDeclarator> localVar = this.getLocalVarOrParamOfType(param.getType());

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

    /**
     * Creates a new variable of a given class type, being given a class.
     * @param inputClass The class which needs a new instance
     * @return The variable in question.
     * @throws BuildFailedException If the class can't be instantiated because it has no constructors,
     */
    private VariableDeclarator createNewVarOfTypeObj(ClassBuilder inputClass) throws BuildFailedException {
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

            // Added to the start to make sure it's instantiated before the operations that need it, since those operations may be in variable instantiations themselves
            this.addVarInsnStatementToStart(new ExpressionStmt(new VariableDeclarationExpr(varDeclarator)));

            return varDeclarator;
        } catch (ParseException e) {
            throw new BuildFailedException("ParseException: " + e.getMessage());
        }
    }
}

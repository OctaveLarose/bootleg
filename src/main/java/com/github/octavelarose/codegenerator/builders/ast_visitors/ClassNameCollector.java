package com.github.octavelarose.codegenerator.builders.ast_visitors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;

/**
 * A JavaParser visitor to get the name of the classes in a CompilationUnit.
 * Shamelessly stolen off SO, and could probably be simplified if not removed altogether.
 */
public class ClassNameCollector extends VoidVisitorAdapter<List<String>>
{
    @Override
    public void visit(ClassOrInterfaceDeclaration n, List<String> collector) {
        super.visit(n, collector);
        collector.add(n.getNameAsString());
    }
}
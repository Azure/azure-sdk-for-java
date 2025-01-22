package com.azure.recipes;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeTree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Recipe to convert TypeReference to ParameterizedType and remove TypeReference import statements.
 * --------------------------------------------------
 * Before applying this recipe:
 * import com.azure.core.util.serializer.TypeReference;
 * ...
 *  result = binaryDataResponse.getValue().toObject(new TypeReference<List<TranslatedTextItem>>() { });
 * --------------------------------------------------
 * After applying this recipe:
 * import java.lang.reflect.ParameterizedType;
 * import java.lang.reflect.Type;
 * ...
    result = binaryDataResponse.getValue().toObject(new ParameterizedType() {
    @Override
    public Type getRawType() {
    return List.class;
    }

    @Override
    public Type[] getActualTypeArguments() {
    return new Type[]{TranslatedTextItem.class};
    }

    @Override
    public Type getOwnerType() {
    return null;
    }
    });
 * --------------------------------------------------
 * @author Ali Soltanian Fard Jahromi
 */
public class TypeReferenceRecipe extends Recipe {

    @Override
    public @NotNull String getDisplayName() {
        return "Convert TypeReference to ParameterizedType and remove imports";
    }

    @Override
    public @NotNull String getDescription() {
        return "This recipe converts TypeReference<> to ParameterizedType and removes the import statement for TypeReference.";
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ConvertTypeReferenceVisitor();
    }

    private static class ConvertTypeReferenceVisitor extends JavaIsoVisitor<ExecutionContext> {
        /**
         * Method to visit instantiation of TypeReference and replace it with ParameterizedType
         * instantiation including override methods.
         * */
        @Override
        public J.@NotNull NewClass visitNewClass(J.@NotNull NewClass newClass, @NotNull ExecutionContext ctx) {
            J.NewClass visitedNewClass = super.visitNewClass(newClass, ctx);

            // Check if the TypeReference reference has already been transformed
            if (visitedNewClass.getBody() == null){return visitedNewClass;}
            boolean alreadyTransformed = visitedNewClass.getBody().getStatements().stream()
                    .filter(statement -> statement instanceof J.MethodDeclaration)
                    .map(J.MethodDeclaration.class::cast)
                    .anyMatch(methodDeclaration -> methodDeclaration.getName().getSimpleName().equals("getRawType"));
            if (visitedNewClass.getClazz() == null){return visitedNewClass;}
            if (!alreadyTransformed && visitedNewClass.getClazz().toString().contains("TypeReference")) {
                // Extract type from generic type in TypeReference declaration
                String genType = null;
                String type = null;
                String type2 = null;
                List<JavaType> fullType = null;


                if(visitedNewClass.getClazz().getType() != null){
                    try {
                        fullType = ((JavaType.Parameterized) visitedNewClass.getClazz().getType()).getTypeParameters();
                        if (fullType.get(0) instanceof JavaType.Parameterized) { // Check if using parameterized type
                            type = ((JavaType.Class)((JavaType.Parameterized) fullType.get(0)).getTypeParameters().get(0)).getClassName();
                            if (((JavaType.Parameterized) fullType.get(0)).getTypeParameters().size() > 1) {
                                type2 = ((JavaType.Class)((JavaType.Parameterized) fullType.get(0)).getTypeParameters().get(1)).getClassName();
                            }
                            genType = ((JavaType.Parameterized) fullType.get(0)).getClassName();
                        } else {
                            genType = ((JavaType.Class)fullType.get(0)).getClassName();
                        }
                    } catch (ClassCastException e) { // OpenRewrite has a bug where parameterized new-classes contained in method arguments can't be parsed, so extract type information using String manipulation instead
                        genType = visitedNewClass.getClazz().toString().split("<")[1];
                        if (visitedNewClass.getClazz().toString().contains(",")) {
                            type = visitedNewClass.getClazz().toString().split("<")[2].replace(">", "").split(",")[0];
                            type2 = visitedNewClass.getClazz().toString().split("<")[2].replace(">", "").split(",")[1];
                        }else {
                            type = visitedNewClass.getClazz().toString().split("<")[2].replace(">", "");
                        }
                    }
                }
                JavaTemplate methodRawTypeTemplate = JavaTemplate.builder("@Override public Type getRawType() { return " + extractTypeArgument(visitedNewClass.toString()) + ".class; }").build();
                if (genType!=null){
                    methodRawTypeTemplate = JavaTemplate.builder("@Override public Type getRawType() { return " + genType + ".class; }").build();
                }
                JavaTemplate methodActualTypeTemplate = JavaTemplate.builder("@Override public Type[] getActualTypeArguments() { return new Type[] {}; }").build();
                if (type != null){
                    methodActualTypeTemplate = JavaTemplate.builder("@Override public Type[] getActualTypeArguments() { return new Type[] {  " + type + ".class  }; }").build();
                }
                JavaTemplate methodOwnerTypeTemplate = JavaTemplate.builder("@Override public Type getOwnerType() { return null; }").build();
                if (type2 != null){ // If TypeReference uses Map<T,V> or any other class with the same structure like Foo<T,V>
                    methodActualTypeTemplate = JavaTemplate.builder("@Override public Type[] getActualTypeArguments() { return new Type[] {  " + type + ".class," + type2 + ".class  }; }").build();
                }
                // Apply Templates (add methods to body)

                visitedNewClass = visitedNewClass.withBody(methodRawTypeTemplate.apply(new Cursor(getCursor(), visitedNewClass.getBody()),
                        visitedNewClass.getBody().getCoordinates().lastStatement()));
                visitedNewClass = visitedNewClass.withBody(methodActualTypeTemplate.apply(new Cursor(getCursor(), visitedNewClass.getBody()),
                        visitedNewClass.getBody().getCoordinates().lastStatement()));
                visitedNewClass = visitedNewClass.withBody(methodOwnerTypeTemplate.apply(new Cursor(getCursor(), visitedNewClass.getBody()),
                        visitedNewClass.getBody().getCoordinates().lastStatement()));

                visitedNewClass = visitedNewClass.withClazz(TypeTree.build(" ParameterizedType")); // Replace TypeReference with Type

            }
            return visitedNewClass;
        }

        private final Set<String> importSet = new HashSet<>();

        @Override
        public J.@NotNull Import visitImport(J.@NotNull Import importStmt, @NotNull ExecutionContext ctx) {
            String importQualid = importStmt.getQualid().toString();

            // Add the import to the set and check if it already exists
            boolean isNewImport = importSet.add(importQualid);

            // If the import is for ParameterizedType, and it's already in the set, skip it
            if (importQualid.trim().equals("java.lang.reflect.ParameterizedType") && !isNewImport) {
                return null;
            }

            // Remove the import statement for TypeReference and add import for ParameterizedType
            if (importQualid.equals("com.azure.core.util.serializer.TypeReference")) {
                importSet.add("java.lang.reflect.ParameterizedType");
                return importStmt.withQualid(TypeTree.build(" java.lang.reflect.ParameterizedType"));
            }

            // Change BinaryData import to a new package
            if (importQualid.equals("com.azure.core.util.BinaryData")) {
                importSet.add("io.clientcore.core.util.binarydata.BinaryData");
                return importStmt.withQualid(TypeTree.build(" io.clientcore.core.util.binarydata.BinaryData"));
            }

            // Return other imports normally
            return importStmt;
        }
        /**
         * Method to visit variable declaration for TypeReference and make sure it is converted to java.lang.reflect.Type
         */
        @Override
        public J.@NotNull VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable, ExecutionContext executionContext) {
            J.VariableDeclarations visitedDeclarations = super.visitVariableDeclarations(multiVariable, executionContext);
            if (visitedDeclarations.toString().contains("TypeReference")
                    && visitedDeclarations.toString().contains("ParameterizedType")) {
                visitedDeclarations = visitedDeclarations.withTypeExpression(TypeTree.build(" Type"));
                return visitedDeclarations;
            }
            return visitedDeclarations;
        }

        /**
         * Method to visit BinaryData type and change it to the new version
         */
        @Override
        public J.@NotNull FieldAccess visitFieldAccess(J.@NotNull FieldAccess fieldAccess, @NotNull ExecutionContext ctx) {
            J.FieldAccess fa = super.visitFieldAccess(fieldAccess, ctx);
            String fullyQualified = fa.getTarget() + "." + fa.getSimpleName();
            if (fullyQualified.equals("com.azure.core.util.BinaryData")) {
                return TypeTree.build(" io.clientcore.core.util.binarydata.BinaryData");
            }
            return fa;
        }

        /**
         * Method to add import for java.lang.reflect.Type if needed
         */
        @Override
        public J.CompilationUnit visitCompilationUnit(J.CompilationUnit cu, ExecutionContext executionContext) {
            J.CompilationUnit visitedCompilationUnit = super.visitCompilationUnit(cu, executionContext);
            J.Import newImport = null;
            boolean addTypeImport = false;
            if (visitedCompilationUnit.getImports().isEmpty()){return visitedCompilationUnit;}
            for (J.Import i : visitedCompilationUnit.getImports()) {
                newImport = i;
                if (i.toString().contains("java.lang.reflect.Type")){
                    return visitedCompilationUnit;
                }
                if (i.toString().contains("java.lang.reflect.ParameterizedType")){
                    addTypeImport = true;
                }
            }
            if (addTypeImport) {
                newImport = newImport.withQualid(TypeTree.build(" java.lang.reflect.Type"));
                List<J.Import> imports = visitedCompilationUnit.getImports();
                imports.add(newImport);
                return visitedCompilationUnit.withImports(imports);
            }
            return visitedCompilationUnit;
        }

        public String extractTypeArgument(String text) {
            // Find the start and end of the type argument
            int startIndex = text.indexOf('<');
            int endIndex = text.indexOf('>', startIndex);

            if (startIndex == -1 || endIndex == -1) {
                return null; // If '<' or '>' not found, return null
            }

            // Extract the substring between the angle brackets
            return text.substring(startIndex + 1, endIndex).trim();
        }
    }

}

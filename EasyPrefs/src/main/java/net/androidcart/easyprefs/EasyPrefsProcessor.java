package net.androidcart.easyprefs;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

public class EasyPrefsProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elements;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elements = processingEnv.getElementUtils();
    }

    private ClassName context(){
        return ClassName.get("android.content", "Context");
    }
    private ClassName prefs(){
        return ClassName.get("android.content", "SharedPreferences");
    }


    private boolean isAbstract(Element e){
        for ( Modifier mod : e.getModifiers()){
            if (mod.equals(Modifier.ABSTRACT)){
                return true;
            }
        }
        return false;
    }

    private TypeName returnType(ExecutableElement method){
        return TypeName.get(method.getReturnType()).withoutAnnotations();
    }
    private DeclaredType returnDeclared(ExecutableElement method){
        return (DeclaredType) method.getReturnType();
    }

    private String getCamel(String in){
        String typeCamel = in;
        if ( typeCamel.length()>1 ) {
            typeCamel = typeCamel.substring(0, 1).toUpperCase() + typeCamel.substring(1);
        } else {
            typeCamel = typeCamel.toUpperCase();
        }
        return typeCamel;
    }

    private String getDefaultStatement(ExecutableElement method){
        TypeName retTN = returnType(method);

        if ( retTN.isPrimitive() ){
            if (retTN.equals(TypeName.BOOLEAN) ){
                return "return false";
            }
            if (retTN.equals(TypeName.BYTE) ){
                return "return 0";
            }
            if (retTN.equals(TypeName.SHORT) ){
                return "return 0";
            }
            if (retTN.equals(TypeName.INT) ){
                return "return 0";
            }
            if (retTN.equals(TypeName.LONG) ){
                return "return 0";
            }
            if (retTN.equals(TypeName.CHAR) ){
                return "return ' '";
            }
            if (retTN.equals(TypeName.FLOAT) ){
                return "return 0";
            }
            if (retTN.equals(TypeName.DOUBLE) ){
                return "return 0";
            }
        }
        return "return null";
    }

    private MethodSpec getMethod(ExecutableElement method){
        TypeName retTN = returnType(method);
        String itemName = method.getSimpleName().toString();
        String defaultCall = itemName + "()";
        String itemNameQuoted = "\"" + itemName + "\"";
        String getterName = (retTN.equals(TypeName.BOOLEAN) ? "is" : "get") +  getCamel(itemName);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(getterName)
                .addModifiers(Modifier.PUBLIC)
                .returns(retTN)
                .beginControlFlow("if( doesKeyExistsInternal("+itemNameQuoted+") )")
                ;

        String fullTypeName = retTN.withoutAnnotations().toString();

        if ( retTN.isPrimitive() ){
            if (retTN.equals(TypeName.BOOLEAN) ){
                builder.addStatement("return mSharedPreferences.getBoolean("+itemNameQuoted+","+defaultCall+")");
            }
            if (retTN.equals(TypeName.BYTE) ){
                builder.addStatement("return mSharedPreferences.getInt("+itemNameQuoted+","+defaultCall+")");
            }
            if (retTN.equals(TypeName.SHORT) ){
                builder.addStatement("return (short)mSharedPreferences.getInt("+itemNameQuoted+","+defaultCall+")");
            }
            if (retTN.equals(TypeName.INT) ){
                builder.addStatement("return mSharedPreferences.getInt("+itemNameQuoted+","+defaultCall+")");
            }
            if (retTN.equals(TypeName.LONG) ){
                builder.addStatement("return mSharedPreferences.getLong("+itemNameQuoted+","+defaultCall+")");
            }
            if (retTN.equals(TypeName.FLOAT) ){
                builder.addStatement("return mSharedPreferences.getFloat("+itemNameQuoted+","+defaultCall+")");
            }
            if (retTN.equals(TypeName.DOUBLE) ){
                builder.beginControlFlow("try");
                builder.addStatement("String serializedStr = mSharedPreferences.getString(" + itemNameQuoted + ",null)");
                builder.addStatement("return gson.fromJson(serializedStr, java.lang.Double.class ).doubleValue()");
                builder.endControlFlow();
                builder.beginControlFlow("catch(java.lang.Throwable ignored)");
                builder.addStatement("return (double) mSharedPreferences.getFloat("+itemNameQuoted+",0)");
                builder.endControlFlow();
            }
        } else if (fullTypeName.equals("java.lang.String")) {
            builder.addStatement("return mSharedPreferences.getString("+itemNameQuoted+",\"\")");
        } else {
            builder.beginControlFlow("try");
            builder.addStatement("String serializedStr = mSharedPreferences.getString(" + itemNameQuoted + ",null)");

            if (returnDeclared(method).getTypeArguments().size() > 0) {
                builder.addStatement(String.format("return gson.fromJson(serializedStr, new com.google.gson.reflect.TypeToken< %s >(){}.getType() )", fullTypeName));
            } else {
                builder.addStatement(String.format("return gson.fromJson(serializedStr, %s.class )", fullTypeName));
            }
            builder.endControlFlow();
            builder.beginControlFlow("catch(java.lang.Throwable ignored)");
            builder.endControlFlow();
        }

        builder.endControlFlow();
        builder.addStatement("return " + defaultCall  );
        return builder.build();
    }

    private MethodSpec setMethod(ExecutableElement method){
        TypeName retTN = returnType(method);
        String itemName = method.getSimpleName().toString();
        String itemNameQuoted = "\"" + itemName + "\"";
        String getterName = "set" +  getCamel(itemName);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(getterName)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.SYNCHRONIZED)
                .addParameter(retTN, itemName)
                .addStatement("SharedPreferences.Editor editor = mSharedPreferences.edit()")
                ;

        String fullTypeName = retTN.withoutAnnotations().toString();

        if ( retTN.isPrimitive() ){
            if (retTN.equals(TypeName.BOOLEAN) ){
                builder.addStatement(String.format("editor.putBoolean(%s,%s)",itemNameQuoted,itemName));
            }
            if (retTN.equals(TypeName.BYTE) ){
                builder.addStatement(String.format("editor.putInt(%s,%s)",itemNameQuoted,itemName));
            }
            if (retTN.equals(TypeName.SHORT) ){
                builder.addStatement(String.format("editor.putInt(%s,%s)",itemNameQuoted,itemName));
            }
            if (retTN.equals(TypeName.INT) ){
                builder.addStatement(String.format("editor.putInt(%s,%s)",itemNameQuoted,itemName));
            }
            if (retTN.equals(TypeName.LONG) ){
                builder.addStatement(String.format("editor.putLong(%s,%s)",itemNameQuoted,itemName));
            }
            if (retTN.equals(TypeName.FLOAT) ){
                builder.addStatement(String.format("editor.putFloat(%s,%s)",itemNameQuoted,itemName));
            }
            if (retTN.equals(TypeName.DOUBLE) ){
                builder.beginControlFlow("try");
                builder.addStatement(String.format("String serializedStr = gson.toJson(%s)", itemName));
                builder.addStatement(String.format("editor.putString(%s,serializedStr)",itemNameQuoted));
                builder.endControlFlow();
                builder.beginControlFlow("catch(java.lang.Throwable ignored)");
                builder.addStatement(String.format("editor.putFloat(%s,(float)%s)",itemNameQuoted,itemName));
                builder.endControlFlow();

            }
        } else if (fullTypeName.equals("java.lang.String")) {
            builder.addStatement(String.format("editor.putString(%s,%s)",itemNameQuoted,itemName));
        } else {
            builder.beginControlFlow("try");
            builder.addStatement(String.format("String serializedStr = gson.toJson(%s)", itemName));
            builder.addStatement(String.format("editor.putString(%s,serializedStr)",itemNameQuoted));
            builder.endControlFlow();
            builder.beginControlFlow("catch(java.lang.Throwable ignored)");
            builder.endControlFlow();
        }

        builder.addStatement("editor.apply()");
        return builder.build();
    }


    private MethodSpec deleteMethod(ExecutableElement method){
        String itemName = method.getSimpleName().toString();
        String itemNameQuoted = "\"" + itemName + "\"";
        String getterName = "delete" +  getCamel(itemName);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(getterName)
                .returns(TypeName.BOOLEAN)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.SYNCHRONIZED)
                .beginControlFlow("if( doesKeyExistsInternal("+itemNameQuoted+") )")
                    .addStatement("SharedPreferences.Editor editor = mSharedPreferences.edit()")
                    .addStatement("editor.remove("+itemNameQuoted+")")
                    .addStatement("editor.apply()")
                    .addStatement("return true")
                .endControlFlow()
                .addStatement("return false")
                ;

        return builder.build();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        ArrayList<String> schemaNames = new ArrayList<>();
        for (Element element : roundEnvironment.getElementsAnnotatedWith(EasyPrefsSchema.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }

            TypeElement schemaTE = (TypeElement) element;
            TypeName schema = TypeName.get(schemaTE.asType());
            String packageName = elements.getPackageOf(schemaTE).getQualifiedName().toString();
            EasyPrefsSchema annot = schemaTE.getAnnotation(EasyPrefsSchema.class);
            String prefsClassName = annot.value();
            boolean useStatic = annot.useStaticReferences();
            if (prefsClassName.length()==0){
                messager.printMessage(Diagnostic.Kind.ERROR, "Your EasyPrefs schema must provide preferences class name!" );
                return false;
            }
            if (schemaNames.contains(prefsClassName)){
                messager.printMessage(Diagnostic.Kind.ERROR, String.format("You have used same names (%s) for different preferences!" , prefsClassName) );
                return false;
            }
            schemaNames.add(prefsClassName);

            ClassName prefsCN = ClassName.get(packageName, prefsClassName);


            TypeSpec.Builder prefsClass = TypeSpec
                    .classBuilder( prefsCN )
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(schema);

            if(useStatic) {
                prefsClass.addField(FieldSpec.builder(
                        prefsCN,
                        "instance",
                        Modifier.STATIC,
                        Modifier.PRIVATE).build());
            }

            prefsClass.addField(FieldSpec.builder(
                    prefs(),
                    "mSharedPreferences",
                    Modifier.PRIVATE).build());

            prefsClass.addField(FieldSpec.builder(
                    Gson.class,
                    "gson",
                    Modifier.PRIVATE).build());

            prefsClass.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers( useStatic ? Modifier.PRIVATE : Modifier.PUBLIC)
                    .addParameter(context(), "context")
                    .addStatement(String.format("mSharedPreferences = context.getSharedPreferences(\"%s\", android.content.Context.MODE_PRIVATE)", prefsClassName))
                    .addStatement("gson = new Gson()")
                    .build());

            if(useStatic) {
                prefsClass.addMethod(MethodSpec.methodBuilder("init")
                        .addModifiers(Modifier.PUBLIC)
                        .addModifiers(Modifier.STATIC)
                        .returns(prefsCN)
                        .addParameter(context(), "context")
                        .addStatement(String.format("if (instance==null){ instance = new %s (context); } return instance", prefsClassName))
                        .build());


                prefsClass.addMethod(MethodSpec.methodBuilder("i")
                        .addModifiers(Modifier.PUBLIC)
                        .addModifiers(Modifier.STATIC)
                        .returns(prefsCN)
                        .addStatement("return instance")
                        .build());
            }

            prefsClass.addMethod(MethodSpec.methodBuilder("doesKeyExistsInternal")
                    .returns(TypeName.BOOLEAN)
                    .addParameter(String.class, "key")
                    .addStatement("return mSharedPreferences.getAll().containsKey(key)")
                    .build());


            for ( Element item : schemaTE.getEnclosedElements() ) {
                if (item.getKind() != ElementKind.METHOD){
                    continue;
                }
                String itemName = item.getSimpleName().toString();
                ExecutableElement eMethod = (ExecutableElement) item;
                if( eMethod.getParameters().size() > 0 ){
                    messager.printMessage(Diagnostic.Kind.ERROR, "Your EasyPrefs schema methods cannot have parameters!" );
                    return false;
                }

                if( returnType(eMethod).equals(TypeName.CHAR) ){
                    messager.printMessage(Diagnostic.Kind.ERROR, "char type is not supported by EasyPrefs!" );
                    return false;
                }

                if (isAbstract(eMethod)){
                    prefsClass.addMethod(MethodSpec.methodBuilder(itemName)
                            .returns(returnType(eMethod))
                            .addAnnotation(Override.class)
                            .addStatement( getDefaultStatement(eMethod) )
                            .build());
                }

                prefsClass.addMethod(getMethod(eMethod));
                prefsClass.addMethod(setMethod(eMethod));
                prefsClass.addMethod(deleteMethod(eMethod));

            }






            try {
                JavaFile.builder(packageName, prefsClass.build())
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(EasyPrefsSchema.class.getCanonicalName() );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}


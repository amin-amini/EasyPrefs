package net.androidcart.easyprefs;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import net.androidcart.easyprefsschema.EasyPrefsSchema;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

public class EasyPrefsProcessor extends AbstractProcessor {

    static final String PARAM_SEPERATOR = "___";

    private Filer filer;
    private Messager messager;
    private Elements elements;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elements = processingEnv.getElementUtils();
    }

    private void log(String str){
        messager.printMessage(Diagnostic.Kind.NOTE, "EasyPrefsLog: " + str );
    }
    private void error(String str){
        messager.printMessage(Diagnostic.Kind.ERROR, "EasyPrefsError: " + str );
    }

    private ClassName context(){
        return ClassName.get("android.content", "Context");
    }
    private ClassName gsonTypeToken(){
        return ClassName.get("com.google.gson.reflect", "TypeToken");
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
        try {
            return (DeclaredType) method.getReturnType();
        }catch (Throwable ignored){}
        return null;
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

    private MethodSpec defaultMethod(ExecutableElement method){
        String name = method.getSimpleName().toString();
        MethodSpec.Builder builder = MethodSpec.methodBuilder(name)
                .returns(returnType(method))
                .addAnnotation(Override.class)
                .addStatement( getDefaultStatement(method) );

        for( VariableElement ve : method.getParameters() ){
            builder.addParameter(TypeName.get(ve.asType()), ve.getSimpleName().toString());
        }
        return builder.build();
    }

    private MethodSpec getMethod(ExecutableElement method, List<? extends TypeParameterElement> typeParameters){
        TypeName retTN = returnType(method);
        String itemName = method.getSimpleName().toString();


        StringBuilder itemNameQuotedBuilder = new StringBuilder("\"" + itemName + "\"");

        ArrayList<String> params = new ArrayList<>();
        for( VariableElement ve : method.getParameters() ){
            String name = ve.getSimpleName().toString();
            params.add(name);
            itemNameQuotedBuilder.append(" + \""+PARAM_SEPERATOR+"\" + ").append(name);
        }

        String itemNameQuoted = itemNameQuotedBuilder.toString();
        String defaultCall = itemName + "("+ StringUtils.join(params,",")+")";
        String getterName = (retTN.equals(TypeName.BOOLEAN) ? "is" : "get") +  getCamel(itemName);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(getterName)
                .addModifiers(Modifier.PUBLIC)
                .returns(retTN)
                .beginControlFlow("if( doesKeyExistsInternal("+itemNameQuoted+") )")
                ;
        for( VariableElement ve : method.getParameters() ){
            builder.addParameter(TypeName.get(ve.asType()), ve.getSimpleName().toString());
        }

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

            DeclaredType returnDeclaredType = returnDeclared(method);
            if (returnDeclaredType == null) {
                boolean couldUseTypeToken = false;
                for(TypeParameterElement tp : typeParameters){
                    TypeName tn = TypeName.get(tp.asType());
                    if (tn.equals(retTN)){
                        String attributeName = "typeTokenFor" + tp.getSimpleName().toString();

                        builder.addStatement(String.format("return gson.fromJson(serializedStr, %s.getType() )", attributeName));
                        couldUseTypeToken = true;
                        break;
                    }
                }

                if (!couldUseTypeToken){
                    //TODO: make sure
                    //builder.addStatement(String.format("return gson.fromJson(serializedStr, new com.google.gson.reflect.TypeToken< %s >(){}.getType() )", fullTypeName));
                }
            } else if (returnDeclaredType.getTypeArguments().size() > 0) {
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

        StringBuilder itemNameQuotedBuilder = new StringBuilder("\"" + itemName + "\"");
        for( VariableElement ve : method.getParameters() ){
            String name = ve.getSimpleName().toString();
            itemNameQuotedBuilder.append(" + \""+PARAM_SEPERATOR+"\" + ").append(name);
        }
        String itemNameQuoted = itemNameQuotedBuilder.toString();

        String setterName = "set" +  getCamel(itemName);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(setterName)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.SYNCHRONIZED)
                .addStatement("SharedPreferences.Editor editor = mSharedPreferences.edit()")
                ;

        for( VariableElement ve : method.getParameters() ){
            builder.addParameter(TypeName.get(ve.asType()), ve.getSimpleName().toString());
        }

        builder.addParameter(retTN, itemName);

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
        TypeName retTN = returnType(method);
        String itemName = method.getSimpleName().toString();

        StringBuilder itemNameQuotedBuilder = new StringBuilder("\"" + itemName + "\"");
        for( VariableElement ve : method.getParameters() ){
            String name = ve.getSimpleName().toString();
            itemNameQuotedBuilder.append(" + \""+PARAM_SEPERATOR+"\" + ").append(name);
        }
        String itemNameQuoted = itemNameQuotedBuilder.toString();

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

        for( VariableElement ve : method.getParameters() ){
            builder.addParameter(TypeName.get(ve.asType()), ve.getSimpleName().toString());
        }
        return builder.build();
    }


    private MethodSpec existsMethod(ExecutableElement method){
        TypeName retTN = returnType(method);
        String itemName = method.getSimpleName().toString();

        StringBuilder itemNameQuotedBuilder = new StringBuilder("\"" + itemName + "\"");
        for( VariableElement ve : method.getParameters() ){
            String name = ve.getSimpleName().toString();
            itemNameQuotedBuilder.append(" + \""+PARAM_SEPERATOR+"\" + ").append(name);
        }
        String itemNameQuoted = itemNameQuotedBuilder.toString();

        String getterName = "has" +  getCamel(itemName);

        MethodSpec.Builder builder = MethodSpec.methodBuilder(getterName)
                .returns(TypeName.BOOLEAN)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.SYNCHRONIZED)
                .addStatement("return doesKeyExistsInternal("+itemNameQuoted+")")
                ;

        for( VariableElement ve : method.getParameters() ){
            builder.addParameter(TypeName.get(ve.asType()), ve.getSimpleName().toString());
        }
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
                error( "Your EasyPrefs schema must provide preferences class name!" );
                return false;
            }
            if (schemaNames.contains(prefsClassName)){
                error( String.format("You have used same names (%s) for different preferences!" , prefsClassName) );
                return false;
            }
            schemaNames.add(prefsClassName);

            ClassName prefsCN = ClassName.get(packageName, prefsClassName);



            TypeSpec.Builder prefsClass = TypeSpec
                    .classBuilder( prefsCN )
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(schema);

            List<? extends TypeParameterElement> typeParameters = schemaTE.getTypeParameters();
            if ( typeParameters != null){
                for(TypeParameterElement tp : typeParameters){
                    prefsClass.addTypeVariable(TypeVariableName.get(tp));
                    if (useStatic){
                        error( "Static EasyPrefs cannot be generic" );
                        return false;
                    }
                }
            }

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

            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                    .addModifiers(useStatic ? Modifier.PRIVATE : Modifier.PUBLIC)
                    .addParameter(context(), "context")
                    .addStatement(String.format("mSharedPreferences = context.getSharedPreferences(\"%s\", android.content.Context.MODE_PRIVATE)", prefsClassName))
                    .addStatement("gson = new Gson()");

            if ( typeParameters != null){
                for(TypeParameterElement tp : typeParameters){
                    String attributeName = "typeTokenFor" + tp.getSimpleName().toString();

                    TypeName genType = ClassName.get(tp.asType());
                    ParameterizedTypeName typeToken = ParameterizedTypeName.get(gsonTypeToken(), genType);

                    prefsClass.addField(FieldSpec.builder(
                            typeToken,
                            attributeName,
                            Modifier.PRIVATE).build());
                    constructorBuilder.addParameter(typeToken , attributeName);
                    constructorBuilder.addStatement("this." + attributeName + " = " + attributeName );
                }
            }
            prefsClass.addMethod(constructorBuilder.build());

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
//                if( eMethod.getParameters().size() > 0 ){
//                    error( "Your EasyPrefs schema methods cannot have parameters!" );
//                    return false;
//                }

                if( returnType(eMethod).equals(TypeName.CHAR) ){
                    error( "char type is not supported by EasyPrefs!" );
                    return false;
                }

                if (isAbstract(eMethod)){
                    prefsClass.addMethod(defaultMethod(eMethod));
                }

                prefsClass.addMethod(getMethod(eMethod, typeParameters));
                prefsClass.addMethod(setMethod(eMethod));
                prefsClass.addMethod(deleteMethod(eMethod));
                prefsClass.addMethod(existsMethod(eMethod));

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


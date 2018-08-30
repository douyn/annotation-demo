package com.dou.demo.knife_compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Author: dou
 * Time: 18-8-28  下午6:31
 * Decription:
 */

public class AnnotationClass {

    private TypeElement typeElement;
    private Elements elements;

    List<BindViewField> bindViewFields;
    List<OnClickMethod> onClickMethods;

    public AnnotationClass(TypeElement typeElement, Elements elements){
        this.typeElement = typeElement;
        this.elements = elements;
        bindViewFields = new LinkedList<>();
        onClickMethods = new LinkedList<>();
    }

    public void addField(BindViewField field) {
        bindViewFields.add(field);
    }

    public void addField(OnClickMethod field) {
        onClickMethods.add(field);
    }

    public JavaFile generateInjector(){
        System.out.println("processor: generateInjector");
        // to create method declear
        // @Override
        // public void inject(MainActivity host, Object source, Finder finder) {
        // }
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(typeElement.asType()), "host", Modifier.FINAL)
                .addParameter(TypeName.OBJECT, "source")
                .addParameter(ClassName.get("com.dou.demo.knife_api.finder", "Finder"), "finder");

        // to create method body
        // host.targetview = (TextView) finder.findView)(source, R.id.xx)
        for (BindViewField field : bindViewFields) {
            methodBuilder.addStatement("host.$N=($T)finder.findView(source,$L)",
                    field.getFieldName(),
                    ClassName.get(field.getFieldType()),
                    field.getViewId());
        }

        // to create variable declear
        // OnClickListener listener;
        if (onClickMethods.size() > 0) {
            methodBuilder.addStatement("$T listener",
                    ClassName.get("android.view", "View", "OnClickListener"));
        }

        // to create varible define
        // listener = new OnClickListener(){
        //      @Override
        //      public void onClick(View v ){
        //          host.onClick();
        //      }
        // }
        for (OnClickMethod onClickMethod : onClickMethods) {
            TypeSpec listener = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(ClassName.get("android.view", "View", "OnClickListener"))
                    .addMethod(MethodSpec.methodBuilder("onClick")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(TypeName.VOID)
                            .addParameter(ClassName.get("android.view", "View"), "view")
                            .addStatement("host.$N()", onClickMethod.methodName)
                            .build())
                    .build();

            methodBuilder.addStatement("listener=$L", listener);

            for (int id : onClickMethod.viewIds) {
                methodBuilder.addStatement("finder.findView(source, $L).setOnClickListener(listener)", id);
            }
        }

        String packagename = getPackageName(typeElement);
        String classname = getBinderClassName(packagename, typeElement);

        ClassName binderClassname = ClassName.get(packagename, classname);
        TypeSpec injectorClass = TypeSpec.classBuilder(binderClassname.simpleName() + "$$Injector")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get("com.dou.demo.knife_api", "Injector"), TypeName.get(typeElement.asType())))
                .addMethod(methodBuilder.build())
                .build();

        return JavaFile.builder(packagename, injectorClass).build();
    }

    private String getPackageName(TypeElement typeElement) {
        return elements.getPackageOf(typeElement).toString();
    }


    private String getBinderClassName(String packagename, TypeElement typeelement){
        String fullClassName = typeElement.getQualifiedName().toString();
        return fullClassName.substring(packagename.length() + 1);
    }
}

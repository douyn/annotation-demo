package com.dou.demo.knife_compiler;

import com.dou.demo.knife_annotation.BindView;
import com.dou.demo.knife_annotation.OnClick;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Author: dou
 * Time: 18-8-28  下午6:24
 * Decription:
 */

@AutoService(Processor.class)
public class KnifeProcessor extends AbstractProcessor {

    private static final String TAG = KnifeProcessor.class.getSimpleName();

    private Elements elements;
    private Messager messager;
    private Filer filer;

    Map<String, AnnotationClass> annotationClassMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        System.out.println("processor: init");
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        elements = processingEnvironment.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        System.out.println("processor: getSupportedAnnotationTypes");
        Set<String> types = new LinkedHashSet<>();
        types.add(BindView.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        System.out.println("processor: getSupportedSourceVersion");
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("processor: process");
        processBindview(roundEnvironment);
        processOnClick(roundEnvironment);

        System.out.println("processor: annotationCount = " + annotationClassMap.size());

        for (AnnotationClass annotation : annotationClassMap.values()) {
            try {
                annotation.generateInjector().writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    private void processOnClick(RoundEnvironment roundEnvironment) {
        System.out.println("processor: processOnClick");

        for (Element element : roundEnvironment.getElementsAnnotatedWith(OnClick.class)){

            AnnotationClass annotationClass = getAnnotationClass(element);
            OnClickMethod field = new OnClickMethod(element);
            annotationClass.addField(field);
        }
    }

    private void processBindview(RoundEnvironment roundEnvironment) {

        System.out.println("processor: processBindview");

        for (Element element : roundEnvironment.getElementsAnnotatedWith(BindView.class)){
            AnnotationClass annotationClass = getAnnotationClass(element);
            BindViewField bindViewField = new BindViewField(element);
            annotationClass.addField(bindViewField);
        }
    }

    private AnnotationClass getAnnotationClass(Element element){
        String className = ((TypeElement) element.getEnclosingElement()).getQualifiedName().toString();
        AnnotationClass annotationClass = annotationClassMap.get(className);
        if (annotationClass == null) {
            annotationClass = new AnnotationClass((TypeElement) element.getEnclosingElement(), elements);
            annotationClassMap.put(className, annotationClass);
        }
        return annotationClass;
    }
}

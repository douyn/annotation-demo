package com.dou.demo.knife_compiler;

import com.dou.demo.knife_annotation.BindView;

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Author: dou
 * Time: 18-8-28  下午6:32
 * Decription:
 */

public class BindViewField {

    VariableElement variableElement;

    int viewId;

    public BindViewField(Element element){
        variableElement = (VariableElement) element;

        BindView bindView = element.getAnnotation(BindView.class);

        viewId = bindView.id();

        if (viewId < 0) {
            throw new IllegalArgumentException("the id must > 0");
        }
    }

    public Name getFieldName(){
        return variableElement.getSimpleName();
    }

    public int getViewId(){
        return viewId;
    }

    public TypeMirror getFieldType(){
        return variableElement.asType();
    }
}

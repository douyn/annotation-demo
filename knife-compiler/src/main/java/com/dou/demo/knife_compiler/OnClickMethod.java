package com.dou.demo.knife_compiler;

import com.dou.demo.knife_annotation.OnClick;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;

/**
 * Author: dou
 * Time: 18-8-29  下午5:57
 * Decription:
 */

public class OnClickMethod {

    ExecutableElement executableElement;

    Name methodName;

    int[] viewIds;

    public OnClickMethod(Element element){
        executableElement = ((ExecutableElement) element);
        methodName = executableElement.getSimpleName();
        OnClick onClick = executableElement.getAnnotation(OnClick.class);
        viewIds = onClick.id();
    }

    private int[] getViewIds(){
        return viewIds;
    }

    private Name getMethodName(){
        return methodName;
    }
}

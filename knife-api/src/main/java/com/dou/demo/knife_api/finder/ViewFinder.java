package com.dou.demo.knife_api.finder;

import android.content.Context;
import android.view.View;

/**
 * Author: dou
 * Time: 18-8-28  下午5:50
 * Decription:
 */

public class ViewFinder implements Finder {

    Context context;

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public View findView(Object view, int resId) {
        context = ((View) view).getContext();
        return ((View) view).findViewById(resId);
    }
}

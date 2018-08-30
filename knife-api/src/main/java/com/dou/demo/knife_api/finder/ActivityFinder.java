package com.dou.demo.knife_api.finder;

import android.app.Activity;
import android.content.Context;
import android.view.View;

/**
 * Author: dou
 * Time: 18-8-28  下午5:50
 * Decription:
 */

public class ActivityFinder implements Finder {

    Context context;

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public View findView(Object activity, int resId) {
        context = ((Activity) activity);
        return ((Activity) activity).findViewById(resId);
    }
}

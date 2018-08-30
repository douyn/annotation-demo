package com.dou.demo.knife_api.finder;

import android.content.Context;
import android.view.View;

/**
 * Author: dou
 * Time: 18-8-28  下午5:50
 * Decription:
 */

public interface Finder {
    Context getContext();
    View findView(Object source, int resId);
}

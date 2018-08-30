package com.dou.demo.knife_api;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.dou.demo.knife_api.finder.ActivityFinder;
import com.dou.demo.knife_api.finder.Finder;
import com.dou.demo.knife_api.finder.ViewFinder;

/**
 * Author: dou
 * Time: 18-8-28  下午5:45
 * Decription:
 */

public class Knife {

    static Finder ACTIVITY_FINDER = new ActivityFinder();
    static Finder VIEW_FINDER = new ViewFinder();

    public Knife() {
        throw new AssertionError("not available for instance.");
    }

    public static void bind(Context context){
        bind(context, ACTIVITY_FINDER);
    }

    public static void bind(View view){
        bind(view, VIEW_FINDER);
    }

    public static void bind(Object host, Finder finder){
        bind(host, host, finder);
    }

    public static void bind(Object host, Object source, Finder finder){
        String className = host.getClass().getName();

        try {
            Class findClass = Class.forName(className + "$$Injector");
            Injector injector = null;
            try {
                injector = (Injector) findClass.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            if (host == null) {
                Log.d("xxx", "bind: host");
                return;
            } else if (finder == null) {
                Log.d("xxx", "bind: finder");
                return;
            }

            injector.inject(host, source, finder);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

package com.dou.demo.knife_api;

import com.dou.demo.knife_api.finder.Finder;

/**
 * Author: dou
 * Time: 18-8-28  下午6:17
 * Decription:
 */

public interface Injector<T> {
    void inject(T host, Object source, Finder finder);
}

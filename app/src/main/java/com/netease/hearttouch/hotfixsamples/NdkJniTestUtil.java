/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hotfixsamples;

/**
 * Created by lht on 16/7/27.
 */
public class NdkJniTestUtil {
    public native String getStringFromNative();

    static {
        System.loadLibrary("HotfixSampleJniLib");
    }
}

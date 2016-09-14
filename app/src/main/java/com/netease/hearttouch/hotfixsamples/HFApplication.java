/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hotfixsamples;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.netease.hearttouch.hthotfix.HTHotFix;
import com.netease.hearttouch.hthotfix.PatchSignVerifyFailedException;

public class HFApplication extends Application{

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    HTHotFix.install(this);
    try{
        HTHotFix.loadPatch(this, Environment.getExternalStorageDirectory().getAbsolutePath().concat("/patch.apk"), false);
    }catch (PatchSignVerifyFailedException e){
        e.printStackTrace();
        Log.e("hotfix","sign is not right");
    }
    }
}

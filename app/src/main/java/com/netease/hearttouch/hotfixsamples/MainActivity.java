/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hotfixsamples;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = (TextView) findViewById(R.id.textView);

        String s = "SDK:" + Build.VERSION.SDK_INT +
                "\nABI:" + Build.CPU_ABI +
                "\nABI2:" + Build.CPU_ABI2 + "\n";
        textView.setText(s);

        BugClass bugClass = new BugClass();
        textView.append("\n" + bugClass.bug());

        NdkJniTestUtil ndkUtil = new NdkJniTestUtil();
        textView.append("\n" + ndkUtil.getStringFromNative());
    }
}

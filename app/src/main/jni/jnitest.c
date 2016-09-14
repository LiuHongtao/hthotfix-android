/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

#include "com_netease_hearttouch_hotfixsamples_NdkJniTestUtil.h"
/*
 * Class:     com_netease_hearttouch_hotfixsamples_NdkJniTestUtil
 * Method:    getStringFromNative
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_netease_hearttouch_hotfixsamples_NdkJniTestUtil_getStringFromNative
  (JNIEnv *env, jobject obj){
     return (*env)->NewStringUTF(env, "There is a NDK bug");
//     return (*env)->NewStringUTF(env, "NDK Bug Fixed");
  }

/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix;

/**
 * 签名验证失败异常
 * Created by zw on 16/6/12.
 */
public class PatchSignVerifyFailedException extends RuntimeException {
    public PatchSignVerifyFailedException(String message){
        super(message);
    }
}

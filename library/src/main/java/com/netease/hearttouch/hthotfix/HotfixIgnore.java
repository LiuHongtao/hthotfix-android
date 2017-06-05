/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 注解标记不被更新的类
 * Created by zw on 16/6/14.
 */
@Target(ElementType.TYPE)
public @interface HotfixIgnore {}


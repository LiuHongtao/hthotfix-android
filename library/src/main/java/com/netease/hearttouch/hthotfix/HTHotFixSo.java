/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 加载更新so文件
 * Created by lht on 16/7/27.
 */
public class HTHotFixSo extends AbstractHotFix {
    static final String TAG = "HotfixSo";

    /**
     * 加载so文件
     * @param context
     * @param soDirPath so文件所在文件夹路径
     */
    public static void loadSo(Context context, String soDirPath) {
        ArrayList<String> extraSoPaths = new ArrayList();
        try {
            extraSoPaths.add(soDirPath);
        } catch (Exception e) {
            Log.e(TAG, "Exception");
            e.printStackTrace();
        } finally {
            if (soDirPath != null) {
                loadSo(context, extraSoPaths);
            }
        }
    }

    private static boolean loadSo(Context context, ArrayList<String> soPaths){
        ClassLoader loader = context.getClassLoader();

        ArrayList<File> extraSoFiles = new ArrayList<>();
        for (String soPath: soPaths) {
            extraSoFiles.add(new File(soPath));
        }

        try {
            if (!soPaths.isEmpty()) {
                if (Build.VERSION.SDK_INT >= 23) {
                    V23.install(loader, extraSoFiles);
                } else if (Build.VERSION.SDK_INT >= 14) {
                    V14.install(loader, extraSoFiles);
                } else {

                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "inject failed");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static class V23 {
        private static void install(ClassLoader loader, List<File> extraSoFiles)
                throws NoSuchFieldException, IllegalAccessException,
                InvocationTargetException, NoSuchMethodException {
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            ArrayList<IOException> suppressedExceptions = new ArrayList<IOException>();
            expandFieldArray(dexPathList,
                    "nativeLibraryPathElements",
                    makePathElements(dexPathList,
                            new ArrayList<>(extraSoFiles),
                            suppressedExceptions));

            if (suppressedExceptions.size() > 0) {
                for (IOException e : suppressedExceptions) {
                    Log.w(TAG, "Exception in makePathElements", e);
                }
                Field suppressedExceptionsField =
                        findField(dexPathList, "dexElementsSuppressedExceptions");
                IOException[] dexElementsSuppressedExceptions =
                        (IOException[]) suppressedExceptionsField.get(dexPathList);

                if (dexElementsSuppressedExceptions == null) {
                    dexElementsSuppressedExceptions =
                            suppressedExceptions.toArray(
                                    new IOException[suppressedExceptions.size()]);
                } else {
                    IOException[] combined =
                            new IOException[suppressedExceptions.size() +
                                    dexElementsSuppressedExceptions.length];
                    suppressedExceptions.toArray(combined);
                    System.arraycopy(dexElementsSuppressedExceptions, 0, combined,
                            suppressedExceptions.size(), dexElementsSuppressedExceptions.length);
                    dexElementsSuppressedExceptions = combined;
                }

                suppressedExceptionsField.set(dexPathList, dexElementsSuppressedExceptions);
            }
        }

        /**
         * A wrapper around
         * {@code private static final dalvik.system.DexPathList#makeP
         * athElements}.
         */
        private static Object[] makePathElements(
                Object dexPathList,
                ArrayList<File> extraSoFiles,
                ArrayList<IOException> suppressedExceptions)
                throws IllegalAccessException, InvocationTargetException,
                NoSuchMethodException {
            Method makePathElements =
                    findMethod(dexPathList, "makePathElements", List.class, File.class,
                            List.class);

            return (Object[]) makePathElements.invoke(dexPathList, extraSoFiles, null,
                    suppressedExceptions);
        }
    }

    private static class V14 {
        private static void install(ClassLoader loader, List<File> extraSoFiles)
                throws NoSuchFieldException, IllegalAccessException {
            Field pathListField = findField(loader, "pathList");
            Object dexPathList = pathListField.get(loader);
            expandFieldArray(dexPathList,
                    "nativeLibraryDirectories",
                    extraSoFiles.toArray());
        }
    }
}

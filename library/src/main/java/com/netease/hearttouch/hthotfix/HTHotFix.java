/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.netease.hearttouch.hthotfix;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by lht on 16/7/27.
 */
public class HTHotFix {
    static final String TAG = "HotFix";

    private static final String HOTFIX_DIR = "hthotfix";

    private static final String HACK_DEX = "hack.apk";

    /**
     * HTHotfix初始化
     * @param context
     */
    public static void install(Context context) {
        ClassLoader loader;
        try {
            loader = context.getClassLoader();
        } catch (RuntimeException e) {
            Log.w(TAG, "Failure while trying to obtain Context class loader. " +
                    "Must be running in test mode. Skip patching.", e);
            return;
        }
        if (loader == null) {
            Log.e(TAG,
                    "Context class loader is null. Must be running in test mode. "
                            + "Skip patching.");
            return;
        }

        //补丁文件存放路径
        File hotfixDir = new File(context.getFilesDir(), HOTFIX_DIR);
        hotfixDir.mkdir();

        //防止CLASS_ISPREVERIFIED
        String dexPath = null;
        try {
            dexPath = FileHelper.copyAsset(context, HACK_DEX, hotfixDir);
        } catch (IOException e) {
            Log.e(TAG, "copy " + HACK_DEX + " failed");
            e.printStackTrace();
        }

        try {
            HTHotFixDex.loadPatch(context, dexPath);
        }catch (PatchSignVerifyFailedException e){
            e.printStackTrace();
        }
    }

    /**
     * 加载补丁包
     * @param context
     * @param patchPath 补丁包路径
     * @param bCheckPatchSign 是否验证签名
     */
    public static void loadPatch(Context context, String patchPath, boolean bCheckPatchSign) {
        if (bCheckPatchSign) {
            boolean bSuccess = checkPatchSign(context,patchPath);
            if(!bSuccess)
                throw new  PatchSignVerifyFailedException("sign is not equal with main apk:" + patchPath);
        }

        //patch.apk
        File patchFile = new File(patchPath);

        // /files/hthotfix/
        File hotfixDir = new File(context.getFilesDir(), HOTFIX_DIR);
        String hotfixDirPath = hotfixDir.getAbsolutePath() + File.separator;

        if (patchFile.exists()) {
            try {
                //将上一个版本的更新文件删除
                for (File file: hotfixDir.listFiles()) {
                    if (file.isDirectory()) {
                        FileHelper.deleteDir(file);
                        continue;
                    }
                    if (!file.getName().equals(HACK_DEX)) {
                        file.delete();
                    }
                }

                //复制新版本patch.apk到HOTFIX_DIR
                ArrayList<String> fileList = new ArrayList<>();
                fileList.add(patchPath);
                FileHelper.copyFile(fileList, hotfixDir);

                //解压HOTFIX_DIR下patch.apk
                FileHelper.unZip(hotfixDirPath + patchFile.getName(), hotfixDirPath);

                //删除来源patch.apk
                patchFile.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HTHotFixDex.loadPatch(context, hotfixDirPath + "classes.dex");
        HTHotFixSo.loadSo(context, hotfixDirPath + "libs");
    }

    private static WeakReference<byte[]> mReadBuffer;

    /**
     * 检查补丁包签名
     * @param context
     * @param dexPath
     * @return
     */
    private static boolean checkPatchSign(Context context, String dexPath){

        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            if (null != pi) {
                byte[] myCertBytes = CertificateFactory.getInstance("X.509")
                        .generateCertificate(new ByteArrayInputStream(pi.signatures[0].toByteArray()))
                        .getEncoded();

                Signature mSignatures[] = null;
                WeakReference<byte[]> readBufferRef;
                byte[] readBuffer = null;
                readBufferRef = mReadBuffer;
                if (readBufferRef != null) {
                    mReadBuffer = null;
                    readBuffer = readBufferRef.get();
                }
                if (readBuffer == null) {
                    readBuffer = new byte[8192];
                    readBufferRef = new WeakReference<byte[]>(readBuffer);
                }

                JarFile jarFile = new JarFile(dexPath);
                try {
                    final List<JarEntry> toVerify = new ArrayList<>();
                    Enumeration<JarEntry> i = jarFile.entries();
                    while (i.hasMoreElements()) {
                        final JarEntry entry = i.nextElement();
                        if (entry.isDirectory()) continue;
                        if (entry.getName().startsWith("META-INF/")) continue;
                        toVerify.add(entry);
                    }

                    for (JarEntry entry : toVerify) {
                        final Certificate[] entryCerts = loadCertificates(jarFile, entry, readBuffer);
                        Certificate cert = entryCerts[0];
                        if (null != cert) {
                            if (!Arrays.equals(myCertBytes, cert.getEncoded())) {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                    return true;
                } finally {
                    if (null != jarFile) {
                        try {
                            jarFile.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Certificate[] loadCertificates(JarFile jarFile, JarEntry je,
                                                  byte[] readBuffer) {
        try {
            InputStream is = new BufferedInputStream(jarFile.getInputStream(je));
            while (is.read(readBuffer, 0, readBuffer.length) != -1) {
            }
            is.close();
            return je != null ? je.getCertificates() : null;
        } catch (IOException e) {
            Log.e(TAG, "Exception reading " + je.getName() + " in "
                    + jarFile.getName(), e);
        } catch (RuntimeException e) {
            Log.e(TAG, "Exception reading " + je.getName() + " in "
                    + jarFile.getName(), e);
        }
        return null;
    }
}

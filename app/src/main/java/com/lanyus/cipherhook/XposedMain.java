package com.lanyus.cipherhook;

import static com.lanyus.cipherhook.EnumerateClass.getClassNameList;

import android.content.Context;

import com.google.gson.Gson;

import org.apache.commons.codec.binary.Base64;

import java.util.ArrayList;
import java.util.Iterator;

import dalvik.system.BaseDexClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedMain implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (lpparam.packageName.equals("com.lanyus.cipherhook")) {
            return;
        }
        XposedBridge.log("com.lanyus.cipherhook Loaded app: " + lpparam.packageName);

        hook(lpparam.classLoader);

        new Timing(lpparam, false) {
            @Override
            protected void onNewActivity(XC_MethodHook.MethodHookParam param) {
                super.onNewActivity(param);
                try {
                    hook(lpparam.classLoader);
                } catch (Exception e) {
                    XposedBridge.log(e.getLocalizedMessage());
                }
            }
        };
    }

    public void hook(ClassLoader classLoader) {

        XposedHelpers.findAndHookConstructor("javax.crypto.spec.SecretKeySpec", classLoader, byte[].class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                byte[] key = (byte[]) param.args[0];
                String method = (String) param.args[1];
                XposedBridge.log("com.lanyus.cipherhook new javax.crypto.spec.SecretKeySpec('" + Base64.encodeBase64String(key) + "', '" + method + "')");
            }
        });

        XposedHelpers.findAndHookConstructor("javax.crypto.spec.IvParameterSpec", classLoader, byte[].class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                byte[] iv = (byte[]) param.args[0];
                XposedBridge.log("com.lanyus.cipherhook new javax.crypto.spec.IvParameterSpec('" + Base64.encodeBase64String(iv) + "')");
            }
        });

        XposedHelpers.findAndHookMethod("javax.crypto.Cipher", classLoader, "getInstance", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String method = (String) param.args[0];
                XposedBridge.log("com.lanyus.cipherhook javax.crypto.Cipher.getInstance('" + method + "')");
            }
        });

        XposedHelpers.findAndHookMethod("javax.crypto.Cipher", classLoader, "doFinal", byte[].class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                byte[] plain = (byte[]) param.args[0];
                byte[] result = (byte[]) param.getResult();
                XposedBridge.log("com.lanyus.cipherhook javax.crypto.Cipher.doFinal('" + Base64.encodeBase64String(plain) + "') -> " + Base64.encodeBase64String(result));
            }
        });

        XposedHelpers.findAndHookMethod("java.security.MessageDigest", classLoader, "getInstance", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String method = (String) param.args[0];
                XposedBridge.log("com.lanyus.cipherhook java.security.MessageDigest.getInstance('" + method + "')");
            }
        });

        XposedHelpers.findAndHookMethod("java.security.MessageDigest", classLoader, "update", byte[].class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                byte[] plain = (byte[]) param.args[0];
                XposedBridge.log("com.lanyus.cipherhook java.security.MessageDigest.update('" + Base64.encodeBase64String(plain) + "')");
            }
        });

        XposedHelpers.findAndHookMethod("java.lang.String", classLoader, "substring", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String thisObject = (String) param.thisObject;
                int arg = (int) param.args[0];
                XposedBridge.log("com.lanyus.cipherhook java.lang.String.substring('" + thisObject + ", " + arg + "')");
//                printStackTrace();
            }
        });

        XposedHelpers.findAndHookMethod("java.lang.String", classLoader, "substring", int.class, int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String thisObject = (String) param.thisObject;
                int arg1 = (int) param.args[0];
                int arg2 = (int) param.args[1];
                XposedBridge.log("com.lanyus.cipherhook java.lang.String.substring('" + thisObject + ", " + arg1 + ", " + arg2 + "')");
//                printStackTrace();
            }
        });

    }

    private static void printStackTrace() {
        XposedBridge.log("--------------->");
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        for (int i = 0; i < stackElements.length; i++) {
            StackTraceElement element = stackElements[i];
            XposedBridge.log("at " + element.getClassName() + "." + element.getMethodName() + "(" + element
                    .getFileName() + ":" + element.getLineNumber() + ")");
        }
    }
}

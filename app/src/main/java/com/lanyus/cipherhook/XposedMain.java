package com.lanyus.cipherhook;

import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import org.apache.commons.codec.binary.Base64;

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


    }
}

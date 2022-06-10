package com.lanyus.cipherhook;

import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

import dalvik.system.DexFile;

public class EnumerateClass {
    private static final String TAG = "FRiDA_UNPACK";

    public static Object getObjectField(Object object, String fieldName) {
        Class clazz = object.getClass();
        while (!clazz.getName().equals(Object.class.getName())) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(object);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }

    public static ArrayList<String> getClassNameList(ClassLoader classLoader) {
        ArrayList<String> classNameList = new ArrayList();
        try {
            Object dexElements = getObjectField(getObjectField(classLoader, "pathList"), "dexElements");
            int dexElementsLength = Array.getLength(dexElements);
            for (int i = 0; i < dexElementsLength; i++) {
                Enumeration<String> enumerations = ((DexFile) getObjectField(Array.get(dexElements, i), "dexFile")).entries();
                while (enumerations.hasMoreElements()) {
                    classNameList.add((String) enumerations.nextElement());
                }
            }
        } catch (Exception e) {
        }
        Collections.sort(classNameList);
        return classNameList;
    }

    public static String[] getClassNameListArray(ClassLoader classLoader) {
        ArrayList<String> namelist = getClassNameList(classLoader);
        String[] retval = new String[namelist.size()];
        namelist.toArray(retval);
        return retval;
    }

    public static void loadAllClass(ClassLoader classLoader) {
        try {
            Iterator it = getClassNameList(classLoader).iterator();
            while (it.hasNext()) {
                Class<?> clazz = classLoader.loadClass((String) it.next());
                Method[] methods = clazz.getDeclaredMethods();
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("load class: ");
                stringBuilder.append(clazz.getName());
                Log.d(str, stringBuilder.toString());
                for (Method method : methods) {
                    Object[] objs = new Object[method.getParameterTypes().length];
                    String str2 = TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("try to load method: ");
                    stringBuilder2.append(clazz.getName());
                    stringBuilder2.append("-->");
                    stringBuilder2.append(method.getName());
                    Log.d(str2, stringBuilder2.toString());
                    method.invoke(null, objs);
                    Log.d(TAG, "success");
                }
            }
        } catch (Throwable th) {
        }
    }

}

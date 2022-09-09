package top.canyie.pine.utils;

import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import top.canyie.pine.PineConfig;

/**
 * @author canyie
 */
public final class ReflectionHelper {
    private static Field override;

    private ReflectionHelper() {
    }
    public static void forceAccessible(AccessibleObject member) {
        try {
            member.setAccessible(true);
            if (member.isAccessible()) return;
        } catch (SecurityException ignored) {
        }

        if (override == null) {
            override = getField(AccessibleObject.class, PineConfig.sdkLevel >= Build.VERSION_CODES.N ? "override" : "flag");
        }

        try {
            override.setBoolean(member, true);
        } catch (IllegalAccessException e) {
            throw new SecurityException("Cannot set AccessibleObject.override", e);
        }
    }

    public static Object getFieldValue(Class<?> clazz, String fieldName, Object instance) {
        try {
            Field addr = getField(clazz, fieldName);
            if (addr != null) {
                return addr.get(instance);
            }
        } catch (Throwable e) {
        }
        return null;
    }

    public static Field getField(Class<?> clazz, String name) {
        Field field = findField(clazz, name);
        if (field == null)
            throw new IllegalArgumentException("No field " + name + " found in " + clazz);
        return field;
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        if (clazz == null || TextUtils.isEmpty(fieldName)) {
            return null;
        }
        Field field = null;
        while (clazz != Object.class) {
            try {
                field = clazz.getDeclaredField(fieldName);

                if (field != null) {
                    setFinalFieldReadable(field);
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    return field;
                }
            } catch (Throwable e) {
            }
            clazz = clazz.getSuperclass();
        }
        return field;
    }

    private static void setFinalFieldReadable(Field field) throws NoSuchFieldException, IllegalAccessException {
        int modify = field.getModifiers();
        if (Modifier.isFinal(modify)) {
            Field modifiersField = Field.class.getDeclaredField("accessFlags");
            if (modifiersField == null) {
                modifiersField = Field.class.getDeclaredField("modifiers");
            }
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, modify & ~Modifier.FINAL);
        }
    }

    public static Method getMethod(Class<?> c, String name, Class<?>... paramTypes) {
        Method method = findMethod(c, name, paramTypes);
        if (method == null)
            throw new IllegalArgumentException("No method " + name + " with params " + Arrays.toString(paramTypes) + " found in " + c);
        return method;
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... types) {
        if (clazz == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        Method method = null;
        while (clazz != Object.class) {
            try {
                method = clazz.getDeclaredMethod(methodName, types);
                if (method != null) {
                    method.setAccessible(true);
                    return method;
                }
            } catch (Throwable e) {
            }
            clazz = clazz.getSuperclass();
        }
        return method;
    }

    public static <T> Constructor<T> getConstructor(Class<T> c, Class<?>... paramTypes) {
        try {
            Constructor<T> constructor = c.getDeclaredConstructor(paramTypes);
            forceAccessible(constructor);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No constructor found with params " + Arrays.toString(paramTypes), e);
        }
    }

    public static <T> Constructor<T> findConstructor(Class<T> c, Class<?>... paramTypes) {
        try {
            Constructor<T> constructor = c.getDeclaredConstructor(paramTypes);
            forceAccessible(constructor);
            return constructor;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}

package top.canyie.pine.utils;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

public class Utils {
    private static Field artMethod = null;

    static {
        if (artMethod == null) {
            artMethod = getField(Method.class, "artMethod");
        }
    }

    private static final String TAG = "Pine.utils";

    public static long getThreadPeer() {
        Object resulst = ReflectionHelper.getFieldValue(Thread.class, "nativePeer", Thread.currentThread());
        if (resulst != null
                && (resulst instanceof Long
                || resulst.getClass() == long.class
                || resulst.getClass() == Long.class)
        ) {
            return ((Long) resulst).longValue();
        }
        return 0L;
    }


    static class Unsafe {

        private static Object unsafeObj;
        private static Class unsafeClass;

        public static boolean isOK() {
            return unsafeObj != null && unsafeClass != null;
        }

        static {
            try {
                if (unsafeClass == null) {
                    unsafeClass = Class.forName("sun.misc.Unsafe");
                }
                //private static final Unsafe theUnsafe = THE_ONE;
                unsafeObj = getFieldValue(unsafeClass, "theUnsafe", null);
                if (unsafeObj == null) {
                    //private static final Unsafe THE_ONE = new Unsafe();
                    unsafeObj = getFieldValue(unsafeClass, "THE_ONE", null);
                }
                if (unsafeObj == null) {
                    unsafeObj = call(unsafeClass, "getUnsafe", null, null, null);
                }
            } catch (Throwable e) {
                e(e);
            }
        }

        public static long objectFieldOffset(Field field) {
            if (field == null) {
                return 0L;
            }
            Object result = call(unsafeClass, "objectFieldOffset", unsafeObj, new Class[]{Field.class}, new Object[]{field});
            return result != null && (result.getClass() == Long.class || result.getClass() == long.class) ? ((Long) result).longValue() : 0L;
        }

        public static long getLong(Object obj, long offset) {
            Object result = call(unsafeClass, "getLongVolatile", unsafeObj, new Class[]{Object.class, long.class}, new Object[]{obj, offset});
            if (result == null) {
                result = call(unsafeClass, "getLong", unsafeObj, new Class[]{Object.class, long.class}, new Object[]{obj, offset});
            }
            return result != null && (result.getClass() == Long.class || result.getClass() == long.class) ? ((Long) result).longValue() : 0L;
        }


        public static void putLong(Object obj, long offset, long newValue) {
            try {
                Method method = getMethod(unsafeClass, "putLongVolatile", Object.class, long.class, long.class);
                if (method == null) {
                    method = getMethod(unsafeClass, "putLong", Object.class, long.class, long.class);
                }
                if (method != null) {
                    method.setAccessible(true);
                    method.invoke(unsafeObj, obj, offset, newValue);
                }
            } catch (Throwable e) {
                e(e);
            }
        }

        public static int getInt(long offset) {
            Object result = call(unsafeClass, "getInt", unsafeObj, new Class[]{long.class}, new Object[]{offset});
            return result != null && (result.getClass() == Integer.class || result.getClass() == int.class) ? ((Integer) result).intValue() : 0;
        }


        public static int arrayIndexScale(Class clazz) {
            Object result = call(unsafeClass, "arrayIndexScale", unsafeObj, new Class[]{Class.class}, new Object[]{clazz});
            if (result == null) {
                if (Build.VERSION.SDK_INT > 20) {
                    result = call(unsafeClass, "getArrayIndexScaleForComponentType", unsafeObj, new Class[]{Class.class}, new Object[]{clazz});
                } else {
                    // 4.x
                    result = call(unsafeClass, "arrayIndexScale0", unsafeObj, new Class[]{Class.class}, new Object[]{clazz});
                }
            }
            return result != null && (result.getClass() == Integer.class || result.getClass() == int.class) ? ((Integer) result).intValue() : 0;
        }

        public static int arrayBaseOffset(Class clazz) {
            Object result = call(unsafeClass, "arrayBaseOffset", unsafeObj, new Class[]{Class.class}, new Object[]{clazz});
            if (result == null) {
                result = call(unsafeClass, "getArrayBaseOffsetForComponentType", unsafeObj, new Class[]{Class.class}, new Object[]{clazz});
            }
            return result != null && (result.getClass() == Integer.class || result.getClass() == int.class) ? ((Integer) result).intValue() : 0;
        }

        public static int getInt(Object obj, long offset) {
            Object result = call(unsafeClass, "getIntVolatile", unsafeObj, new Class[]{Object.class, long.class}, new Object[]{obj, offset});
            if (result == null) {
                result = call(unsafeClass, "getInt", unsafeObj, new Class[]{Object.class, long.class}, new Object[]{obj, offset});
            }
            return result != null && (result.getClass() == Integer.class || result.getClass() == int.class) ? ((Integer) result).intValue() : 0;
        }


        public static long toAddress(Object obj) {
            try {
                Object[] array = new Object[]{obj};
                //返回数组中一个元素占用的大小
                if (arrayIndexScale(Object[].class) == 8) {
                    return getLong(array, arrayBaseOffset(Object[].class));
                } else {
                    return 0xffffffffL & getInt(array, arrayBaseOffset(Object[].class));
                }
            } catch (Throwable e) {
                e(e);
            }
            return 0L;
        }
    }



    public static long getMethodAddress(Member method) {
        try {
            if (method == null) {
                return 0L;
            }
            if (!hasArtMethod()) {
                return 0L;
            }
            Object mirrorMethod = null;
            if (method instanceof Method) {
                mirrorMethod = getFieldValue(artMethod, (Method) method);
            } else if (method instanceof Constructor) {
                mirrorMethod = getFieldValue(artMethod, (Constructor) method);
            }

            if (mirrorMethod != null && (
                    mirrorMethod.getClass().equals(Long.class)
                            || mirrorMethod.getClass().equals(long.class)
                            || mirrorMethod instanceof Long
            )) {
                return (Long) mirrorMethod;
            }
            return Unsafe.toAddress(mirrorMethod);
        } catch (Throwable e) {
            e(e);
        }
        return 0L;
    }

    //java.lang.reflect.AccessibleObject
    //  安卓4.0.1 ---> 变量private int slot;
    //  安卓4.0.2 ---> 变量private int slot;
    //  安卓4.0.3 ---> 变量private int slot;
    //  安卓4.0.4 ---> 变量private int slot;
    //  安卓4.1.1 ---> 变量private int slot;
    //  安卓4.1.2 ---> 变量private int slot;
    //  安卓4.2 ---> 变量private int slot;
    //  安卓4.3 ---> 变量private int slot;
    // java.lang.reflect.AbstractMethod
    //  安卓4.4 ---> 变量protected final ArtMethod artMethod; ArtMethod中包含很多其他变量
    //  安卓5.0 ---> 变量protected final ArtMethod artMethod; ArtMethod中包含很多其他变量
    //  安卓5.1 ---> 变量protected final ArtMethod artMethod; ArtMethod中包含很多其他变量
    //  安卓6 ---> 变量protected long artMethod; 包含很多其他变量
    //  安卓7 ---> 变量protected long artMethod; 包含很多其他变量  {libcore/libart/src/main/java/java/lang/reflect/AbstractMethod.java}
    // java.lang.reflect.Executable-->变量 protected long artMethod; 包含很对变量
    // android 8 ---> 变量  private long artMethod;
    // android 9 ---> 变量  private long artMethod;
    // android 10 ---> 变量  private long artMethod;
    // android 11 ---> 变量  private long artMethod;
    // android 12 ---> 变量  private long artMethod;
    // android 13 ---> 变量  private long artMethod;
    private static boolean hasArtMethod() {
        if (artMethod == null) {
            artMethod = getField(Method.class, "artMethod");
        }
        return artMethod != null;
    }

    public static Object getFieldValue(Field filed, Object instance) {
        try {
            if (filed == null) {
                return null;
            }
            if (!filed.isAccessible()) {
                filed.setAccessible(true);
            }
            return filed.get(instance);
        } catch (Throwable e) {
            e(e);
        }
        return null;
    }

    public static Object getFieldValue(Class<?> clazz, String fieldName, Object instance) {
        try {
            Field addr = getField(clazz, fieldName);
            if (addr != null) {
                return addr.get(instance);
            }
        } catch (Throwable e) {
            e(e);
        }
        return null;
    }

    public static Object call(Class<?> clazz, String methodName, Object receiver, Class[]
            types, Object[] params) {
        if (clazz == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        try {
            if (types == null || params == null) {
                Method method = getMethod(clazz, methodName);
                if (method != null) {
                    return method.invoke(receiver);
                }
            } else {
                Method method = getMethod(clazz, methodName, types);
                if (method != null) {
                    return method.invoke(receiver, params);
                }
            }

        } catch (Throwable throwable) {
            e(throwable);
        }
        return null;
    }

    public static Field getField(Class topClass, String fieldName) {
        Field field = null;
        while (topClass != null && topClass != Object.class) {
            try {
                field = topClass.getDeclaredField(fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    return field;
                }
            } catch (Exception e) {
            }
            topClass = topClass.getSuperclass();
        }
        return field;
    }

    public static Method getMethod(Class<?> topClass, String methodName, Class<?>... types) {
        if (topClass == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        Method method = null;
        while (topClass != Object.class) {
            try {
                method = topClass.getDeclaredMethod(methodName, types);

                if (method != null) {
                    method.setAccessible(true);
                    return method;
                }
            } catch (Throwable e) {
            }
            topClass = topClass.getSuperclass();
        }
        return method;
    }

    private static void v(String s) {
        Log.println(Log.VERBOSE, TAG, s);
    }

    private static void d(String s) {
        Log.println(Log.DEBUG, TAG, s);
    }

    private static void i(String s) {
        Log.println(Log.INFO, TAG, s);
    }

    private static void e(String s) {
        Log.println(Log.ERROR, TAG, s);
    }

    private static void e(Throwable e) {
        Log.println(Log.ERROR, TAG, Log.getStackTraceString(e));
    }

}

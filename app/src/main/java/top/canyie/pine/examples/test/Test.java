package top.canyie.pine.examples.test;

import android.util.Log;

import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;
import top.canyie.pine.examples.ExampleApp;
import top.canyie.pine.utils.ReflectionHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author canyie
 */
public abstract class Test extends MethodHook {
    public static final int IGNORED = 0;
    public static final int SUCCESS = 1;
    public static final int FAILED = -1;
    private Member target;
    private boolean hookEnabled = true;
    public boolean isCallbackInvoked;
    protected Test() {
    }

    protected Test(String targetName, Class<?>... paramTypes) {
        init(getClass(), targetName, paramTypes);
    }

    protected Test(Class<?> c, String targetName, Class<?>... paramTypes) {
        init(c, targetName, paramTypes);
    }

    protected Test(Member target) {
        this.target = target;
    }

    private void init(Class<?> c, String targetName, Class<?>... paramTypes) {
        Log.i("Pine.Test","init " +c.getName()+"----"+targetName);
        if (targetName != null) {
            target = ReflectionHelper.getMethod(c, targetName, paramTypes);
        } else {
            target = ReflectionHelper.getConstructor(c, paramTypes);
        }
    }

    public void setHookEnabled(boolean enabled) {
        this.hookEnabled = enabled;
    }

    public int run() {
//        if (hookEnabled) {
//            MethodHook.Unhook unhook = Pine.hook(target, this);
//
//            unhook.unhook();
//        }
//        return 0;
        if (hookEnabled) {
            MethodHook.Unhook unhook = Pine.hook(target, this);
            int result = testImpl();
            unhook.unhook();
            return result;
        } else {
            return testImpl();
        }


    }

    protected abstract int testImpl();

    @Override public void beforeCall(Pine.CallFrame callFrame) throws Throwable {
        isCallbackInvoked = true;
        Log.i(ExampleApp.TAG, "Before " + target.getDeclaringClass().getName() + "."
                + target.getName() + "() with thisObject " + callFrame.thisObject
                + " and args " + Arrays.toString(callFrame.args));
    }

    @Override public void afterCall(Pine.CallFrame callFrame) throws Throwable {
        Log.i(ExampleApp.TAG, "After " + target.getDeclaringClass().getName() + "."
                + target.getName() + "(): result " + callFrame.getResult()
                + " throwable " + callFrame.getThrowable());
    }
}

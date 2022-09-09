package top.canyie.pine.examples.test;

import android.util.Log;

import java.util.Random;

/**
 * @author canyie
 */
public class DynamicLookupJNITest extends Test {
    public DynamicLookupJNITest() {
        super("target", int.class);
    }
    
    static {
        System.loadLibrary("examples");
    }

    @Override protected int testImpl() {
        int i = new Random().nextInt();
        Log.e("Pine.demo","testImpl---"+i);
        return target(i) == i * i ? SUCCESS : FAILED;
    }

    private static native int target(int i);
}

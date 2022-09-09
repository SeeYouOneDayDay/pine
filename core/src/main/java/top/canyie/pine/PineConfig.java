package top.canyie.pine;

import android.os.Build;

import java.util.Locale;

/**
 * A class to stores some configures.
 * @author canyie
 */
@SuppressWarnings("WeakerAccess")
public final class PineConfig {
    public static int sdkLevel;
    /**
     * Whether we need to print more detailed logs.
     */
    public static boolean debug = true;

    /**
     * Whether the current process is debuggable.
     */
    public static boolean debuggable;

    /**
     * Whether all Pine hooks won't take effect.
     */
    public static boolean disableHooks;

    /**
     * Internal API. Whether we should use fast-native to speedup jni method calling.
     */
    public static boolean useFastNative;
    /** Set to true will try to hide certain features. Some information used for debugging may be erased.  */
    public static boolean antiChecks;
    /** Set to true will disable the hidden api policy for application domain */
    public static boolean disableHiddenApiPolicy = true;
    /** Set to true will disable the hidden api policy for platform domain */
    public static boolean disableHiddenApiPolicyForPlatformDomain = true;

    /**
     * A function to load our native library (libpine.so)
     * @see Pine.LibLoader
     */
    public static Pine.LibLoader libLoader = new Pine.LibLoader() {
        @Override
        public void loadLib() {
            System.loadLibrary("pine");
        }
    };

    static {
        sdkLevel = Build.VERSION.SDK_INT;

        if (sdkLevel > 22) {
            //Build.VERSION.PREVIEW_SDK_INT  Added in API level 23
            if (sdkLevel == 30 && Build.VERSION.PREVIEW_SDK_INT > 0) {
                // Android S Preview
                sdkLevel = 31;
            }
//            if (Build.VERSION.PREVIEW_SDK_INT > 0) {
//                // preview version
//                sdkLevel += 1;
//            }
        }

        if (sdkLevel < 19)
            throw new RuntimeException("Unsupported android sdk level " + sdkLevel);
        else if (sdkLevel > 30) {
            Pine.warn("Android version too high, not tested now...");
            if (sdkLevel >= 32 && isAtLeastPreReleaseCodename("Tiramisu")) {
                // Android 13 (Tiramisu) Preview
                sdkLevel = 33;
            } else if (sdkLevel == 31 && isAtLeastPreReleaseCodename("Sv2")) {
                // Android 12.1 (SL) Preview
                sdkLevel = 32;
            }
        }

    }

    // https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:core/core/src/main/java/androidx/core/os/BuildCompat.java;l=49;drc=f8ab4c3030c3fbadca32a9593c522c89a9f2cadf
    private static boolean isAtLeastPreReleaseCodename(String codename) {
        final String buildCodename = Build.VERSION.CODENAME.toUpperCase(Locale.ROOT);

        // Special case "REL", which means the build is not a pre-release build.
        if ("REL".equals(buildCodename)) {
            return false;
        }

        return buildCodename.compareTo(codename.toUpperCase(Locale.ROOT)) >= 0;
    }

    private PineConfig() {
        throw new RuntimeException();
    }
}

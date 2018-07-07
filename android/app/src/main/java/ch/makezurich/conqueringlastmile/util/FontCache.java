package ch.makezurich.conqueringlastmile.util;


import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

public class FontCache {

    private static Hashtable<String, Typeface> fontCache = new Hashtable<String, Typeface>();

    private static final String FONT_PATH = "fonts/";

    public static final String FONT_POMPIERE = "Pompiere-Regular.otf";

    public static Typeface get(String name, Context context) {
        Typeface tf = fontCache.get(name);
        if(tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), FONT_PATH + name);
            }
            catch (Exception e) {
                return null;
            }
            fontCache.put(name, tf);
        }
        return tf;
    }
}

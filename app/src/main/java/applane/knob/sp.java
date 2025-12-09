package applane.knob;

import android.content.Context;

public class sp extends Prefs
{
    public static void cardPrefix(Context ctx, String cardPrefix)
    {
        Prefs.setStr(ctx, "cardPrefix", cardPrefix);
    }
    public static String cardPrefix(Context ctx)
    {
        return Prefs.getStr(ctx, "cardPrefix", "");
    }
}

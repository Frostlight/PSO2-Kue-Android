package frostlight.pso2kue;

/**
 * Created by Vincent on 5/19/2015.
 */
//Source: http://stackoverflow.com/questions/8355632/how-do-you-usually-tag-log-entries-android
public class App {

    public static String getTag() {
        String tag = "";
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int i = 0; i < ste.length; i++) {
            if (ste[i].getMethodName().equals("getTag")) {
                tag = ste[i + 1].getClassName() + "_" + ste[i + 1].getLineNumber();
            }
        }
        return tag;
    }

}

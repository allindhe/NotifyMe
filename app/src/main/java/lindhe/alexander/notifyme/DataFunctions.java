package lindhe.alexander.notifyme;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

public class DataFunctions {
    public static boolean isInSharedPreferences(Context context, String key){
        // Get handle for shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);

        // Increment counter if it exists, else save it as 1
        if (sharedPref.contains(key)) {
            return true;
        }
        else {
            return false;
        }
    }

    public static void saveData(Context context, String key, String value){
        // Get handle for shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(key, value);
        editor.apply();
    }

    public static void saveData(Context context, String key, int value){
        // Get handle for shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(key, value);
        editor.apply();
    }

    public static void saveData(Context context, String key, Boolean value){
        // Get handle for shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void incrementCounter(Context context, String key){
        // Get handle for shared preferences and editor
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        // Increment counter if it exists, else save it as 1
        int value;
        if (sharedPref.contains(key)) {
            value = sharedPref.getInt(key, 0);
            value++;
        }
        else {
            value = 1;
        }

        // Write updated value to SharedPreferences
        editor.putInt(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key){
        // Get handle for shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);

        if (sharedPref.contains(key)) {
            return sharedPref.getString(key, "");
        }
        else{
            return "";
        }
    }

    public static int getInt(Context context, String key){
        // Get handle for shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.sharedPreferencesFile), Context.MODE_PRIVATE);

        if (sharedPref.contains(key)) {
            return sharedPref.getInt(key, 0);
        }
        else{
            return 0;
        }
    }
}

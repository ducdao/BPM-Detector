package dao.duc.bpmdetector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.wearable.activity.ConfirmationActivity;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Duc Dao on 6/3/2017.
 */

public class Helper {
    // Content = abstract class that allows you to reference application specific methods
    public static String saveNote(Note note, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String id = String.valueOf(System.currentTimeMillis());
        editor.putString(id, note.getTitle());

        editor.commit();

        return id;
    }

    public static ArrayList<Note> getAllNotes(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        ArrayList<Note> notes = new ArrayList<>();

        Map<String, ?> key = sharedPreferences.getAll();

        for (Map.Entry<String, ?> entry : key.entrySet()) {
            String savedData = (String) entry.getValue();

            if(savedData != null) {
                notes.add(new Note(entry.getKey(), savedData));
            }
        }

        return notes;
    }

    public static void removeNote(String id, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove(id);

        editor.commit();
    }

    public static void displayConfirmation(String message, Context context) {
        Intent intent = new Intent(context, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);

        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, message);
        context.startActivity(intent);
    }
}

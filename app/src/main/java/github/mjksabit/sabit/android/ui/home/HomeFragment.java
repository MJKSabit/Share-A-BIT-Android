package github.mjksabit.sabit.android.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import github.mjksabit.sabit.android.R;

public class HomeFragment extends Fragment {

    // Checks if a volume containing external storage is available
    // for read and write.
    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED;
    }

    // Checks if a volume containing external storage is available to at least read.
    private boolean isExternalStorageReadable() {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED ||
                Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED_READ_ONLY;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        SharedPreferences preferences = getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

        Editor editor = preferences.edit();
        editor.putString("name", "Sabit");
        editor.apply();

//        System.out.println(preferences.getString("name", "Unknown"));
        File[] files = new File[5];
        files[0] = new File(getContext().getFilesDir(), "FilesDir");
        files[1] = new File(getContext().getObbDir(), "ObbDir");
        files[2] = new File(getContext().getCodeCacheDir(), "cacheDir");
        files[3] = new File(getContext().getExternalCacheDir(), "exCacheDir");
        files[4] = new File(getContext().getExternalFilesDir(""), "exDir");

        for (File file : files) {
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return root;
    }
}
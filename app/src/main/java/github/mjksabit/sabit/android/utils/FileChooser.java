package github.mjksabit.sabit.android.utils;

import android.content.Context;

import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FileChooser {
    private Context context;

    public interface Selection {
        void OnSelected(File file);
    }

    public FileChooser(Context context) {
        this.context = context;
    }

    public void chooseFiles(Selection selection) {
        Map<String, File> temporaryFiles = new LinkedHashMap<>();

        new ChooserDialog(context)
                .withFilter(false, false)
                .titleFollowsDir(true)
                .enableMultiple(true)
                .enableOptions(true)
                .displayPath(true)
                .withChosenListener((dir, dirFile) -> {
                    if (dirFile.isFile()) {
                        if (temporaryFiles.containsKey(dir))
                            temporaryFiles.remove(dir);
                        else
                            temporaryFiles.put(dir, dirFile);
                    } else {
                        Set<String> files =  temporaryFiles.keySet();

                        for (String fileName : files)
                            selection.OnSelected(temporaryFiles.get(fileName));
                    }
                })
                .build()
                .show();
    }

    public void chooseFolder(Selection selection) {
        new ChooserDialog(context)
                .titleFollowsDir(true)
                .withFilter(true, false)
                .displayPath(true)
                .withChosenListener((dir, dirFile) -> selection.OnSelected(dirFile))
                .build()
                .show();
    }
}

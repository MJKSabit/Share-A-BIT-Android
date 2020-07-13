package github.mjksabit.sabit.android.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.textfield.TextInputEditText;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;

import github.mjksabit.sabit.android.R;
import github.mjksabit.sabit.android.utils.Constants;
import github.mjksabit.sabit.android.utils.FileUtils;

public class SettingsFragment extends Fragment {

    private TextInputEditText username;
    private String usernameText;

    private EditText receiveLocation;
    private String receiveLocationText;

    private Button changeDirectory;
    private Button saveSettings;

    private SharedPreferences settings;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getContext().getSharedPreferences(Constants.SETTINGS_SHARED_PREF_NAME, Context.MODE_PRIVATE);

        usernameText = settings.getString(Constants.SETTINGS_ID, getResources().getString(R.string.default_id_name));
        receiveLocationText = settings.getString(Constants.SETTINGS_PATH, getContext().getObbDir().getAbsolutePath());

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        username = root.findViewById(R.id.username);
        username.setText(usernameText);

        receiveLocation = root.findViewById(R.id.receive_directory);
        receiveLocation.setText(receiveLocationText);

        changeDirectory = root.findViewById(R.id.select_directory);
        changeDirectory.setOnClickListener((v) -> changeDirectory());

        saveSettings = root.findViewById(R.id.save_settings);
        saveSettings.setOnClickListener((v) -> save());

        return root;
    }

    private void showDirectoryPicker(String initialDirectory) {
        new ChooserDialog(getContext())
                .enableOptions(true)
                .withFilter(true, false)
                .withStartFile(initialDirectory)
                .withChosenListener((path, pathFile) -> {
                    receiveLocationText = path;
                    receiveLocation.setText(receiveLocationText);
                })
                .build()
                .show();
    }

    private void changeDirectory() {
        if (FileUtils.permissionGranted(getContext())) {
            showDirectoryPicker(receiveLocationText);
        }
        else {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.permission_required)
                    .setMessage(R.string.permission_details)
                    .show();
            FileUtils.requestPermission(getActivity());
        }
    }

    private void save() {
        usernameText = username.getText().toString();

        Editor editor = settings.edit();
        editor.putString(Constants.SETTINGS_ID, usernameText);
        editor.putString(Constants.SETTINGS_PATH, receiveLocationText);
        Boolean done = editor.commit();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        if (done) {
            builder.setTitle("Settings saved!");
            builder.setMessage("Dear "+usernameText+",\n" +
                    "Your Received files will be stored in\n" +
                    receiveLocationText);
        } else {
            builder.setTitle("Settings not saved!");
            builder.setMessage("Unable to save settings in your device");
        }

        builder.show();
    }
}
package github.mjksabit.sabit.android.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import github.mjksabit.sabit.android.R;

public class FileUtils {
    public static boolean permissionGranted(Context context){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    public static void requestPermission(Activity context){
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.permission_required)
                .setMessage(R.string.permission_details)
                .show();

        ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }
}

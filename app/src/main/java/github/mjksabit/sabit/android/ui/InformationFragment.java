package github.mjksabit.sabit.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import github.mjksabit.sabit.android.R;

public class InformationFragment extends Fragment {

    private Button update;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_info, container, false);
        update = root.findViewById(R.id.update_check);
        update.setOnClickListener((v) -> checkUpdate());

        return root;
    }

    private void checkUpdate() {
        String link = getResources().getString(R.string.update_link);
        Intent webBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(webBrowser);
    }
}
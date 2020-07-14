package github.mjksabit.sabit.android.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import github.mjksabit.sabit.android.R;
import github.mjksabit.sabit.android.activities.ReceiverActivity;
import github.mjksabit.sabit.android.activities.SenderActivity;
import github.mjksabit.sabit.android.utils.Constants;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private String usernameText;
    private String receiveLocationText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        Button send = root.findViewById(R.id.send_button);
        Button receive = root.findViewById(R.id.receive_button);

        send.setOnClickListener(this);
        receive.setOnClickListener(this);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settings = getContext().getSharedPreferences(Constants.SETTINGS_SHARED_PREF_NAME, Context.MODE_PRIVATE);

        usernameText = settings.getString(Constants.SETTINGS_ID, Settings.Secure.getString(getContext().getContentResolver(), "bluetooth_name"));
        receiveLocationText = settings.getString(Constants.SETTINGS_PATH, getContext().getObbDir().getAbsolutePath());
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.send_button : {
                intent = new Intent(getContext(), SenderActivity.class);
                break;
            }
            case R.id.receive_button : {
                intent = new Intent(getContext(), ReceiverActivity.class);
                break;
            }
        }
        intent.putExtra(Constants.USERNAME_KEY, usernameText);
        intent.putExtra(Constants.RECEIVE_PATH_KEY, receiveLocationText);
        startActivity(intent);
    }
}
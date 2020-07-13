package github.mjksabit.sabit.android.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import github.mjksabit.sabit.android.R;
import github.mjksabit.sabit.android.activities.ReceiverConnection;
import github.mjksabit.sabit.android.activities.SenderConnection;
import github.mjksabit.sabit.android.utils.Constants;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private SharedPreferences settings;
    private String usernameText;
    private String receiveLocationText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getContext().getSharedPreferences(Constants.SETTINGS_SHARED_PREF_NAME, Context.MODE_PRIVATE);

        usernameText = settings.getString(Constants.SETTINGS_ID, getResources().getString(R.string.default_id_name));
        receiveLocationText = settings.getString(Constants.SETTINGS_PATH, getContext().getObbDir().getAbsolutePath());
    }

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
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.send_button : {
                intent = new Intent(getContext(), SenderConnection.class);
                break;
            }
            case R.id.receive_button : {
                intent = new Intent(getContext(), ReceiverConnection.class);
                break;
            }
        }
        intent.putExtra(Constants.SETTINGS_ID, usernameText);
        intent.putExtra(Constants.SETTINGS_PATH, receiveLocationText);
        startActivity(intent);
    }
}
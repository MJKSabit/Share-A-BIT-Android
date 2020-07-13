package github.mjksabit.sabit.android.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import github.mjksabit.sabit.android.R;
import github.mjksabit.sabit.android.utils.Constants;
import github.mjksabit.sabit.android.utils.SConnection;
import github.mjksabit.sabit.core.Receiver;

public class ReceiverActivity extends AppCompatActivity {

    private TextView username;

    private String usernameID;
    private String receiveDirectory;

    private Receiver receiver;
    private ExecutorService connectionThread = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receiver_connection_layout);

        Intent prevData = getIntent();
        usernameID = prevData.getStringExtra(Constants.USERNAME_KEY);
        receiveDirectory = prevData.getStringExtra(Constants.RECEIVE_PATH_KEY);

        username = findViewById(R.id.usernameID);
        username.setText(usernameID);

        receiver = new Receiver(usernameID);
        receiver.setFileSaveDirectory(receiveDirectory);

        connectionThread.execute(() -> {
            try {
                String senderName = receiver.waitForSender();

                Intent connection = new Intent(this, ConnectedActivity.class);
                connection.putExtra(Constants.USERNAME_KEY, usernameID);
                connection.putExtra(Constants.CONNECTED_TO_KEY, senderName);
                connection.putExtra(Constants.RECEIVE_PATH_KEY, receiveDirectory);

                SConnection.setConnection(receiver);

                runOnUiThread(() -> startActivity(connection));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onBackPressed() {
        try {
            receiver.stopListening();
            receiver.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    public void cancelReceiver(View v) {
        onBackPressed();
    }
}
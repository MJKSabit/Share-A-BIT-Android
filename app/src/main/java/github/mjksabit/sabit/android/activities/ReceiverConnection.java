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
import github.mjksabit.sabit.core.Receiver;

public class ReceiverConnection extends AppCompatActivity {

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
        usernameID = prevData.getStringExtra(Constants.SETTINGS_ID);
        receiveDirectory = prevData.getStringExtra(Constants.SETTINGS_PATH);

        username = findViewById(R.id.usernameID);
        username.setText(usernameID);

        receiver = new Receiver(usernameID);

        connectionThread.execute(() -> {
            try {
                String senderName = receiver.waitForSender();

                Intent connection = new Intent(this, SenderConnection.class);
                connection.putExtra(Constants.SETTINGS_ID, senderName);
                connection.putExtra(Constants.SETTINGS_PATH, receiveDirectory);

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
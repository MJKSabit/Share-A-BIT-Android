package github.mjksabit.sabit.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import github.mjksabit.sabit.android.R;
import github.mjksabit.sabit.android.utils.Constants;
import github.mjksabit.sabit.android.utils.SConnection;
import github.mjksabit.sabit.core.Receiver;

public class ReceiverActivity extends AppCompatActivity {

    private String TAG = ReceiverActivity.class.getSimpleName();

    private TextView username;

    private String usernameID;
    private String receiveDirectory;

    private Receiver receiver;
    private ExecutorService connectionThread = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receiver_connection_layout);

        Log.d(TAG, "onCreate: Created Receiver Activity");

        Intent prevData = getIntent();
        usernameID = prevData.getStringExtra(Constants.USERNAME_KEY);
        receiveDirectory = prevData.getStringExtra(Constants.RECEIVE_PATH_KEY);

        username = findViewById(R.id.usernameID);
        username.setText(usernameID);

        try {
            receiver = new Receiver(usernameID);
        } catch (IOException | JSONException | NullPointerException e) {
            e.printStackTrace();
            finish();
        }

        connectionThread.execute(() -> {
            try {
                Log.d(TAG, "connectionThread: Waiting For Receiver in thread");
                String senderName = receiver.waitForSender();

                Intent connection = new Intent(getBaseContext(), ConnectedActivity.class);
                connection.putExtra(Constants.USERNAME_KEY, usernameID);
                connection.putExtra(Constants.CONNECTED_TO_KEY, senderName);
                connection.putExtra(Constants.RECEIVE_PATH_KEY, receiveDirectory);

                Log.d(TAG, "connectionThread: Setting New Connection");
                SConnection.setConnection(receiver, receiveDirectory);

                runOnUiThread(() -> {
                    startActivity(connection);
                    finish();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean cancelled = false;
    @Override
    public void onBackPressed() {

        if (cancelled) return;
        cancelled = true;

        receiver.stopListening();

        try { receiver.close(); }
        catch (IOException e) { e.printStackTrace(); }

        Button button = findViewById(R.id.cancelButton);
        button.setEnabled(false);
        button.setText(getResources().getString(R.string.cancelling));

        new Handler().postDelayed(() -> runOnUiThread(() -> {
            finish();
            super.onBackPressed();
        }), 2000);

    }

    public void cancelReceiver(View v) {
        onBackPressed();
    }
}
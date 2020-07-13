package github.mjksabit.sabit.android.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.net.SocketException;

import github.mjksabit.sabit.core.Connection;

import github.mjksabit.sabit.android.R;
import github.mjksabit.sabit.android.utils.Constants;
import github.mjksabit.sabit.android.utils.SConnection;
import github.mjksabit.sabit.core.ftp.IFTP;

public class ConnectedActivity extends AppCompatActivity implements IFTP.ProgressUpdater {

    private Connection connection;

    private TextView connectedToUserText;
    private TextView transferSpeedText;
    private RecyclerView transferRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        connection = SConnection.getConnection();
        try {
            connection.startReceiving(this);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        Intent data = getIntent();
        String connectedTo = data.getStringExtra(Constants.CONNECTED_TO_KEY);

        connectedToUserText = findViewById(R.id.connected_to_username);
        connectedToUserText.setText(connectedTo);

        transferSpeedText = findViewById(R.id.speed_text);
        transferRecyclerView = findViewById(R.id.transfer_list);
    }

    @Override
    public void startProgress(File file) {

    }

    @Override
    public void continueProgress(long l, long l1) {

    }

    @Override
    public void cancelProgress(File file) {

    }

    @Override
    public void endProgress(File file) {
        transferSpeedText.setText(file.getName());
    }
}
package github.mjksabit.sabit.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import github.mjksabit.sabit.android.R;
import github.mjksabit.sabit.android.utils.Constants;
import github.mjksabit.sabit.android.utils.SConnection;
import github.mjksabit.sabit.core.Connection;

public class ConnectedActivity extends AppCompatActivity {

    private Connection connection;

    private TextView connectedToUserText;
    private TextView transferSpeedText;
    private RecyclerView transferRecyclerView;

    String receiveFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        connection = SConnection.getConnection();

        Intent data = getIntent();
        String connectedTo = data.getStringExtra(Constants.CONNECTED_TO_KEY);
        receiveFolder = data.getStringExtra(Constants.RECEIVE_PATH_KEY);

        connectedToUserText = findViewById(R.id.connected_to_username);
        connectedToUserText.setText(connectedTo);

        transferSpeedText = findViewById(R.id.speed_text);
        transferRecyclerView = findViewById(R.id.transfer_list);
    }

    public void addSendFiles(View v) {

    }

    public void cancelSending(View v) {
        connection.cancelSending();
        connection.cancelSendingCurrent();
    }

    public void showReceivedFolder(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(receiveFolder);
        intent.setDataAndType(uri, "vnd.android.cursor.dir/*");
        startActivity(Intent.createChooser(intent, "Open folder"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SConnection.endConnection();
    }

    public void endConnection(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
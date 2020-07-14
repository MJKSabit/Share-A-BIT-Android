package github.mjksabit.sabit.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import github.mjksabit.sabit.android.R;
import github.mjksabit.sabit.android.utils.Constants;
import github.mjksabit.sabit.android.utils.SConnection;
import github.mjksabit.sabit.core.Connection;
import github.mjksabit.sabit.core.ftp.IFTP;

import static github.mjksabit.sabit.android.utils.Constants.bytePerMB;

public class ConnectedActivity extends AppCompatActivity {

    private Connection connection;

    final static int UPDATE_TIME = 1;
    volatile Long byteTransferredInTime = 0l;

    ExecutorService speedUpdaterThread = Executors.newSingleThreadExecutor();

    private volatile boolean isReceiving = false;


    private String getInMB(long size) {
        return String.format("%.3f MB", (double) size / bytePerMB);
    }

    private class ReceiveProgress implements IFTP.ProgressUpdater {
        private long lastProgress = 0;
        private boolean firstTime = true;
        private long fileSize;

        @Override
        public void startProgress(File file) {
            runOnUiThread(() -> {
                receiveFileProgressBar.setIndeterminate(false);
                receiveFileName.setText(file.getName());
                receiveLayout.setVisibility(View.VISIBLE);
            });
            lastProgress = 0;
            isReceiving = true;
        }

        @Override
        public void continueProgress(long currentProgress, long totalProgress) {
            byteTransferredInTime += currentProgress - lastProgress;
            lastProgress = currentProgress;
            if (firstTime){
                firstTime = false;
                fileSize = totalProgress;
                runOnUiThread(()->receiveFileTotal.setText(getInMB(totalProgress)));
            }
            double percentage = currentProgress*100 / totalProgress;
            String string = getInMB(currentProgress);
            runOnUiThread(() -> {
                receiveFileDone.setText(string);
                receiveFileProgressBar.setProgress((int) percentage);
                receiveFilePercentage.setText(String.format("%.1f %%", percentage));
            });
        }

        @Override
        public void cancelProgress(File file) {
            runOnUiThread(() -> {
                receiveFileProgressBar.setIndeterminate(true);
                receiveFileName.setText("Canceled");
            });
            isReceiving = false;
        }

        @Override
        public void endProgress(File file) {
            byteTransferredInTime += fileSize - lastProgress;
            runOnUiThread(() -> {
//                transmissionList.add(new FileNode(false, file.getName(), fileSize));
                receiveLayout.setVisibility(View.INVISIBLE);
            });
            isReceiving = false;
        }
    }

//    private class SendProgress implements IFTP.ProgressUpdater {
//        private long lastProgress = 0;
//        private int index;
//
//        @Override
//        public void startProgress(File file) {
//            runOnUiThread(() -> {
//                sendPane.setVisible(true);
//                sendFileNameText.setText(file.getName());
//                index = transmissionList.size();
//                transmissionList.add(new FileNode(true, file.getName(), file.length()));
//                sendTotalInMB.setText(getInMB(file.length()));
//            });
//            lastProgress = 0;
//        }
//
//        @Override
//        public void continueProgress(long currentProgress, long totalProgress) {
//            byteTransferredInTime += currentProgress - lastProgress;
//            lastProgress = currentProgress;
//            double percentage = (double) currentProgress / totalProgress;
//            String string = getInMB(currentProgress);
//            runOnUiThread(() -> {
//                sentInMB.setText(string);
//                sendProgressBar.setProgress(percentage);
//                sentPercentage.setText(String.format("%.1f %%",percentage*100));
//            });
//        }
//
//        @Override
//        public void cancelProgress(File file) {
//            runOnUiThread(() -> {
//                sendProgressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
//                sendFileNameText.setText("Canceled");
//            });
//        }
//
//        @Override
//        public void endProgress(File file) {
//            byteTransferredInTime += file.length() - lastProgress;
//            runOnUiThread(() -> {
//                transmissionList.get(index).markDone();
//                sendPane.setVisible(false);
//            });
//        }
//    }

    private ConstraintLayout sendLayout;
    private TextView sendFileName;
    private TextView sendFileDone;
    private TextView sendFileTotal;
    private TextView sendFilePercentage;
    private ProgressBar sendFileProgressBar;

    private ConstraintLayout receiveLayout;
    private TextView receiveFileName;
    private TextView receiveFileDone;
    private TextView receiveFileTotal;
    private TextView receiveFilePercentage;
    private ProgressBar receiveFileProgressBar;

    private TextView connectedToUserText;
    private TextView transferSpeedText;

    private RecyclerView transferRecyclerView;

    String receiveFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        connectedToUserText = findViewById(R.id.connected_to_username);
        transferSpeedText = findViewById(R.id.speed_text);

        sendLayout = findViewById(R.id.send_layout);
        sendFileName = findViewById(R.id.send_file_name);
        sendFileTotal = findViewById(R.id.send_file_size);
        sendFileDone = findViewById(R.id.send_progress_done);
        sendFilePercentage = findViewById(R.id.send_progress_percentage);
        sendFileProgressBar = findViewById(R.id.send_progress_bar);

        receiveLayout = findViewById(R.id.receive_layout);
        receiveFileName = findViewById(R.id.receive_file_name);
        receiveFileTotal = findViewById(R.id.receive_file_size);
        receiveFileDone = findViewById(R.id.receive_progress_done);
        receiveFilePercentage = findViewById(R.id.receive_progress_percentage);
        receiveFileProgressBar = findViewById(R.id.receive_progress_bar);

        transferRecyclerView = findViewById(R.id.transfer_list);

        connection = SConnection.getConnection();

        Intent data = getIntent();
        String connectedTo = data.getStringExtra(Constants.CONNECTED_TO_KEY);
        receiveFolder = data.getStringExtra(Constants.RECEIVE_PATH_KEY);

        connectedToUserText.setText(connectedTo);

        try {
            connection.startReceiving(new ReceiveProgress());
        } catch (SocketException e) {
            e.printStackTrace();
        }
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
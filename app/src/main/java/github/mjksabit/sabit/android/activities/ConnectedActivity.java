package github.mjksabit.sabit.android.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import github.mjksabit.sabit.android.R;
import github.mjksabit.sabit.android.adapter.FileListAdapter;
import github.mjksabit.sabit.android.data.InfoFile;
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
    private volatile boolean isSending = false;


    public static String getInMB(long size) {
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
            firstTime = true;
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
            double percentage = currentProgress*100.0 / totalProgress;
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
                InfoFile infoFile = new InfoFile(InfoFile.ShareState.RECEIVE, file);
                infoFile.setFileSize(fileSize);
                fileReceived(infoFile);
                receiveLayout.startAnimation(receiveCompletedAnimation);
            });
            isReceiving = false;
        }
    }

    private class SendProgress implements IFTP.ProgressUpdater {
        private long lastProgress = 0;

        @Override
        public void startProgress(File file) {
            runOnUiThread(() -> {
                sendFileProgressBar.setIndeterminate(false);
                sendLayout.setVisibility(View.VISIBLE);
                sendFileName.setText(file.getName());
                sendFileTotal.setText(getInMB(file.length()));
            });
            lastProgress = 0;
            isSending = true;
        }

        @Override
        public void continueProgress(long currentProgress, long totalProgress) {
            byteTransferredInTime += currentProgress - lastProgress;
            lastProgress = currentProgress;
            double percentage = (double) currentProgress*100 / totalProgress;
            String string = getInMB(currentProgress);
            runOnUiThread(() -> {
                sendFileDone.setText(string);
                sendFileProgressBar.setProgress((int) percentage);
                sendFilePercentage.setText(String.format("%.1f %%",percentage));
            });
        }

        @Override
        public void cancelProgress(File file) {
            runOnUiThread(() -> {
                sendFileProgressBar.setIndeterminate(true);
                sendFileName.setText("Canceled");
            });
        }

        @Override
        public void endProgress(File file) {
            byteTransferredInTime += file.length() - lastProgress;
            isSending = false;
            runOnUiThread(() -> {
                sendLayout.startAnimation(sendCompletedAnimation);
            });
        }
    }

    private Animation receiveCompletedAnimation;
    private Animation sendCompletedAnimation;

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
    private FileListAdapter fileListAdapter;

    private String receiveFolder;

    private final ArrayList<InfoFile> transmissionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        init();

        connection = SConnection.getConnection();

        Intent data = getIntent();
        String connectedTo = data.getStringExtra(Constants.CONNECTED_TO_KEY);
        receiveFolder = data.getStringExtra(Constants.RECEIVE_PATH_KEY);

        connectedToUserText.setText(connectedTo);

        speedUpdaterThread.execute(() -> {
            while (true) {
                try {
                    Thread.sleep(UPDATE_TIME*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                synchronized (byteTransferredInTime) {
                    long transferredNow = Math.max(byteTransferredInTime, 0);
                    byteTransferredInTime = 0l;
                    runOnUiThread(() -> transferSpeedText.setText(getInMB(transferredNow/UPDATE_TIME)));
                }
            }
        });

        try {
            connection.startReceiving(new ReceiveProgress());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        connectedToUserText = findViewById(R.id.connected_to_username);
        transferSpeedText = findViewById(R.id.speed_text);

        receiveCompletedAnimation = AnimationUtils.loadAnimation(this, R.anim.completed_transfer);
        receiveCompletedAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                receiveLayout.setBackgroundColor(Color.parseColor(Constants.COMPLETED_BG_COLOR));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!isReceiving)
                    receiveLayout.setVisibility(View.GONE);
                receiveLayout.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        sendCompletedAnimation = AnimationUtils.loadAnimation(this, R.anim.completed_transfer);
        sendCompletedAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                sendLayout.setBackgroundColor(Color.parseColor(Constants.COMPLETED_BG_COLOR));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!isSending)
                    sendLayout.setVisibility(View.GONE);
                sendLayout.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        sendLayout = findViewById(R.id.send_layout);
        sendLayout.setVisibility(View.GONE);
        sendFileName = findViewById(R.id.send_file_name);
        sendFileTotal = findViewById(R.id.send_file_size);
        sendFileDone = findViewById(R.id.send_progress_done);
        sendFilePercentage = findViewById(R.id.send_progress_percentage);
        sendFileProgressBar = findViewById(R.id.send_progress_bar);

        receiveLayout = findViewById(R.id.receive_layout);
        receiveLayout.setVisibility(View.GONE);
        receiveFileName = findViewById(R.id.receive_file_name);
        receiveFileTotal = findViewById(R.id.receive_file_size);
        receiveFileDone = findViewById(R.id.receive_progress_done);
        receiveFilePercentage = findViewById(R.id.receive_progress_percentage);
        receiveFileProgressBar = findViewById(R.id.receive_progress_bar);

        transferRecyclerView = findViewById(R.id.transfer_list);
        transferRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transferRecyclerView.setHasFixedSize(true);

        fileListAdapter = new FileListAdapter(this, transmissionList);
        transferRecyclerView.setAdapter(fileListAdapter);
    }

    private void fileReceived(InfoFile infoFile) {
        synchronized (transmissionList) {
            int position = transmissionList.size();
            transmissionList.add(infoFile);
            fileListAdapter.notifyItemInserted(position);
        }
    }

    private void sendFile(File file) {
        try {
            connection.sendFile(file, new SendProgress());
            synchronized (transmissionList) {
                int position = transmissionList.size();
                transmissionList.add(new InfoFile(InfoFile.ShareState.SEND, file));
                fileListAdapter.notifyItemInserted(position);
            }
        } catch (FileNotFoundException | SocketException e) {
            e.printStackTrace();
        }
    }

    public void addSendFolder(View v) {
        new ChooserDialog(this)
                .titleFollowsDir(true)
                .withFilter(true, false)
                .displayPath(true)
                .withChosenListener((dir, dirFile) -> sendFile(dirFile))
                .build()
                .show();
    }

    private Map<String, File> temporaryFiles = new LinkedHashMap<>();
    public void addSendFiles(View v) {
        new ChooserDialog(this)
                .withFilter(false, false)
                .titleFollowsDir(true)
                .enableMultiple(true)
                .enableOptions(true)
                .displayPath(true)
                .withChosenListener((dir, dirFile) -> {
                    if (dirFile.isFile()) {
                        if (temporaryFiles.containsKey(dir))
                            temporaryFiles.remove(dir);
                        else
                            temporaryFiles.put(dir, dirFile);
                    } else {
                        Set<String> files =  temporaryFiles.keySet();

                        for (String fileName : files)
                            sendFile(temporaryFiles.get(fileName));
                    }
                })
                .build()
                .show();
    }

    public void cancelSending(View v) {
        connection.cancelSending();
    }

    public void showReceivedFolder(View v) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        Uri uri = Uri.parse(receiveFolder);
//        intent.setDataAndType(uri, "vnd.android.cursor.dir/*");
//        startActivity(Intent.createChooser(intent, "Open folder"));
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
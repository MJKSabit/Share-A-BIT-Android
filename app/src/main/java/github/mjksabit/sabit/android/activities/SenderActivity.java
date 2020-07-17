package github.mjksabit.sabit.android.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import github.mjksabit.autoconnect.ClientSide;
import github.mjksabit.autoconnect.ServerDiscoveryObserver;
import github.mjksabit.autoconnect.SharedInfo;
import github.mjksabit.sabit.android.R;
import github.mjksabit.sabit.android.adapter.FileListAdapter;
import github.mjksabit.sabit.android.data.InfoFile;
import github.mjksabit.sabit.android.utils.Constants;
import github.mjksabit.sabit.android.utils.FileChooser;
import github.mjksabit.sabit.android.utils.SConnection;
import github.mjksabit.sabit.core.Constant;
import github.mjksabit.sabit.core.Sender;

public class SenderActivity extends AppCompatActivity implements ServerDiscoveryObserver {

    private String TAG = SenderActivity.class.getSimpleName();

    private final FileChooser fileChooser = new FileChooser(this);

    private RecyclerView transferRecyclerView;
    private final ArrayList<InfoFile> sendFiles = new ArrayList<>();
    private final FileListAdapter fileListAdapter = new FileListAdapter(this, sendFiles);

    private RelativeLayout receiverSelectionPanel;

    private Button showReceiverSelection;
    private ImageView refreshReceivers;
    private TextView warningText;

    private Animation refreshAnimation;

    private String senderName;
    private String receiveDirectory;

    private Sender sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sender_connection_layout);
        init();

        Intent data = getIntent();
        senderName = data.getStringExtra(Constants.USERNAME_KEY);
        receiveDirectory = data.getStringExtra(Constants.RECEIVE_PATH_KEY);

        getSupportActionBar().setTitle(senderName);

        Log.d(TAG, "onCreate: New Sender Created");
        try {
            sender = new Sender(senderName, this);
        } catch (SocketException | JSONException e) {
            e.printStackTrace();
            finish();
            new AlertDialog.Builder(getBaseContext()).setTitle("Unable To Create Sender Instance")
                    .setMessage("Make Sure You're Connected to a WiFi (Not Your Hotspot Network!)")
                    .show();
        }
        sender.setFileSaveDirectory(receiveDirectory);
    }

    private void init() {
        transferRecyclerView = findViewById(R.id.sender_file_list);
        transferRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transferRecyclerView.setHasFixedSize(true);
        transferRecyclerView.setAdapter(fileListAdapter);

        receiverSelectionPanel = findViewById(R.id.receiver_selection_layout);
        receiverSelectionPanel.setVisibility(View.GONE);

        showReceiverSelection = findViewById(R.id.receiver_selection_mode_button);
        showReceiverSelection.setVisibility(View.VISIBLE);

        refreshReceivers = findViewById(R.id.refresh_receiver_button);
        refreshReceivers.setVisibility(View.GONE);
        warningText = findViewById(R.id.waring_text_sender);
        warningText.setVisibility(View.GONE);

        refreshAnimation = AnimationUtils.loadAnimation(this, R.anim.refresh_receivers_button);
        refreshAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                refreshReceivers.setEnabled(false);
                servers.clear();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                refreshReceivers.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void addFileToList(File file) {
        int position = sendFiles.size();
        sendFiles.add(new InfoFile(InfoFile.ShareState.SEND, file));
        fileListAdapter.notifyItemInserted(position);
    }

    public void addSendFolder(View view) {
        fileChooser.chooseFolder(this::addFileToList);
    }

    public void addSendFiles(View view) {
        fileChooser.chooseFiles(this::addFileToList);
    }

    public void showReceiverList(View view) {
        receiverSelectionPanel.setVisibility(View.VISIBLE);
        refreshReceivers.setVisibility(View.VISIBLE);
        warningText.setVisibility(View.VISIBLE);

        showReceiverSelection.setVisibility(View.GONE);
        WIDTH = transferRecyclerView.getMeasuredWidth();
        middle = WIDTH/2;
        refreshReceiversList(null);
    }

    volatile boolean isRefreshing = false;
    public void refreshReceiversList(View view) {
        receiverSelectionPanel.removeAllViews();
        usedAngles.clear();
        servers.clear();

        new Thread(() -> {
            isRefreshing = true;
            runOnUiThread(() -> {
                refreshReceivers.startAnimation(refreshAnimation);
            });

            try {
                sender.sendPresence();
            } catch (ConnectException e) {
                e.printStackTrace();
                runOnUiThread( () -> new AlertDialog.Builder(this).setTitle("Unable To Find Receiver")
                        .setMessage("Make Sure You're Connected to a WiFi (Not Your Hotspot Network!)")
                        .show() );
            }
            isRefreshing = false;
        }).start();

    }


    Map<InetAddress, Sender.ReceiverInfo> servers = new LinkedHashMap<>();
    @Override
    public void serverDiscovered(SharedInfo serverInfo) {
        Sender.ReceiverInfo info = new Sender.ReceiverInfo(serverInfo);

        if (!servers.containsKey(info.getAddress())) {
            addServer(info);
        }
    }


    private int WIDTH;
    private int middle;
    ArrayList<Double> usedAngles = new ArrayList<>();

    private synchronized void addServer(Sender.ReceiverInfo info) {
        Double useAngle = 0.0;

        if (usedAngles.size() != 0) {
            Collections.sort(usedAngles);

            double currentMaxAngleDiff = 360 - (usedAngles.get(usedAngles.size()-1) - usedAngles.get(0));
            double currentUseAngle = currentMaxAngleDiff/2;

            for (int i=1; i<usedAngles.size(); i++) {
                if (currentMaxAngleDiff < usedAngles.get(i) - usedAngles.get(i - 1)) {
                    currentMaxAngleDiff = usedAngles.get(i) - usedAngles.get(i - 1);
                    currentUseAngle = (usedAngles.get(i - 1) + usedAngles.get(i)) / 2;
                }
            }
            useAngle = currentUseAngle;
        }

        usedAngles.add(useAngle);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        View selector = LayoutInflater.from(this).inflate(R.layout.unit_receiver, receiverSelectionPanel, false);
        TextView userNameText = selector.findViewById(R.id.receiver_name_unit);
        userNameText.setText(info.getName());
        selector.setTag(info);
        selector.setOnClickListener(this::establishConnection);

        Log.d(TAG, "addServer: at Angle: " + useAngle);

        if (useAngle>=180) {
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            double marginRight = middle*(1-Math.cos(Math.toRadians(270-useAngle)));
            params.rightMargin = (int) marginRight;
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            double marginLeft = middle*(1-Math.cos(Math.toRadians(90-useAngle)));
            params.leftMargin = (int) marginLeft;
        }

        if (useAngle>90 && useAngle<270) {
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            double marginBottom = middle*(1-Math.cos(Math.toRadians(180-useAngle)));
            params.bottomMargin = (int) marginBottom;
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            double marginTop = middle*(1-Math.cos(Math.toRadians(useAngle)));
            params.topMargin = (int) marginTop;
        }

        runOnUiThread( () -> receiverSelectionPanel.addView(selector, params));
    }

    private void establishConnection(View view) {
        Sender.ReceiverInfo info = (Sender.ReceiverInfo) view.getTag();
        new Thread(() -> {
            try {
                Socket connectionSocket = new Socket(info.getAddress(), info.getPort());
                String receiver = sender.makeConnection(connectionSocket, senderName);

                SConnection.setConnection(sender, receiveDirectory);

                JSONArray filePaths = new JSONArray();
                for (InfoFile file : sendFiles)
                    filePaths.put(file.getFile().getAbsolutePath());

                Intent connection = new Intent(getBaseContext(), ConnectedActivity.class);
                connection.putExtra(Constants.USERNAME_KEY, senderName);
                connection.putExtra(Constants.CONNECTED_TO_KEY, receiver);
                connection.putExtra(Constants.RECEIVE_PATH_KEY, receiveDirectory);
                connection.putExtra(Constants.INITIAL_FILES_KEY, filePaths.toString());

                runOnUiThread(() -> {
                    startActivity(connection);
                    finish();
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: Sender Stop Listing");
        sender.stopListening();
        // No need To Close as "makeConnection" is Never called
        finish();
        super.onBackPressed();
    }
}
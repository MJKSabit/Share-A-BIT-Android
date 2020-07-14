package github.mjksabit.sabit.android.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

import github.mjksabit.sabit.android.R;
import github.mjksabit.sabit.android.adapter.FileListAdapter;
import github.mjksabit.sabit.android.data.InfoFile;
import github.mjksabit.sabit.android.utils.FileChooser;

public class SenderActivity extends AppCompatActivity {

    private final FileChooser fileChooser = new FileChooser(this);

    private RecyclerView transferRecyclerView;
    private final ArrayList<InfoFile> sendFiles = new ArrayList<>();
    private final FileListAdapter fileListAdapter = new FileListAdapter(this, sendFiles);

    private RelativeLayout receiverSelectionPanel;

    private Button showReceiverSelection;
    private Button refreshReceivers;
    private TextView warningText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sender_connection_layout);
        init();
    }

    private void init() {
        transferRecyclerView = findViewById(R.id.sender_file_list);
        transferRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transferRecyclerView.setHasFixedSize(true);
        transferRecyclerView.setAdapter(fileListAdapter);

        receiverSelectionPanel = findViewById(R.id.receiver_selection_layout);
        receiverSelectionPanel.setVisibility(View.GONE);

        showReceiverSelection = findViewById(R.id.receiver_selection_mode_button);

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
}
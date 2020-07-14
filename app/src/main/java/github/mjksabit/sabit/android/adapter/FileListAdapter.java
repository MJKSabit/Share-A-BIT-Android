package github.mjksabit.sabit.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import github.mjksabit.sabit.android.R;
import github.mjksabit.sabit.android.data.InfoFile;

import static github.mjksabit.sabit.android.activities.ConnectedActivity.getInMB;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileViewHolder> {

    private Context context;
    private final ArrayList<InfoFile> fileList;

    public FileListAdapter(Context context, ArrayList<InfoFile> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transfer_file_list_layout, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        InfoFile file = fileList.get(position);

        if (file.getState() == InfoFile.ShareState.SEND)
            holder.receiveIcon.setRotation(180);

        holder.fileName.setText(file.getFile().getName());

        String text = "FOLDER";
        if (file.getFile().isFile())
            text = getInMB(file.getFileSize());

        holder.fileSize.setText(text);
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {
        ImageView receiveIcon;
        TextView fileName;
        TextView fileSize;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);

            receiveIcon = itemView.findViewById(R.id.receive_icon_file_list);
            fileName = itemView.findViewById(R.id.file_list_file_name);
            fileSize = itemView.findViewById(R.id.file_list_file_size);
        }
    }
}

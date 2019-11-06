package konkuk.sunggeun.helloworld.fragment;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.hdodenhof.circleimageview.CircleImageView;
import konkuk.sunggeun.helloworld.R;
import konkuk.sunggeun.helloworld.chat.MessageActivity;
import konkuk.sunggeun.helloworld.model.ChatListModel;
import konkuk.sunggeun.helloworld.model.ChatModel;
import org.w3c.dom.Text;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;
    private String uid;


    private static String ip = "192.168.43.168";
//    private static String ip = "192.168.0.11";
    private static int port = 5568;
    private String extStorageDirectory = Environment.getExternalStorageDirectory().toString()+ "/helloworld/";

//    public void onDestroy() {
//        super.onDestroy();
//        try {
//            dos.writeUTF("quit");
//            dos.writeUTF("null");
//            dos.close();
//            dis.close();
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.chatfragment_recyclerview);
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));


        return view;
    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private List<ChatListModel> chatList = new ArrayList<>();

        public ChatRecyclerViewAdapter(){

            try {
                socket = new Socket(ip, port);
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            SharedPreferences pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
            uid = pref.getString("nickname", null);

            chatList = getChatList();

            try {
                dos.writeUTF("quit");
                dos.writeUTF("null");
                dos.close();
                dis.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat, viewGroup, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
            ((CustomViewHolder)viewHolder).textView.setText(chatList.get(i).getDesUid());
            File imgFile = new File(extStorageDirectory + chatList.get(i).getDesUid() +".png");
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ((CustomViewHolder)viewHolder).imageView.setImageBitmap(myBitmap);
            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getView().getContext(), MessageActivity.class);
                    intent.putExtra("destinationUid", chatList.get(i).getDesUid());
                    if(chatList.get(i).isRoom()){
                        intent.putExtra("isRoom", true);
                    }
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright, R.anim.toleft);
                    startActivity(intent, activityOptions.toBundle());
                }
            });

        }

        @Override
        public int getItemCount() {
            return chatList.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;
            public CircleImageView imageView;

            public CustomViewHolder(View view) {
                super(view);
                textView = view.findViewById(R.id.chatitem_textview_title);
                imageView = view.findViewById(R.id.chatitem_imageview);
            }
        }
    }

    private List<ChatListModel> getChatList(){
        List<ChatListModel> chatList = new ArrayList<>();
        String recvMsg;
        try {
            dos.writeUTF("getChatList");

            dos.writeUTF(uid);

            do {
                recvMsg = dis.readUTF();
                if(recvMsg.equals("chatList_null")){
                    break;
                }

                ChatListModel data = new ChatListModel();

                String roomName = dis.readUTF();
                data.setDesUid(roomName);
                String isRoom = dis.readUTF();
                if(isRoom.equals("room")){
                    mLog(roomName + "is true");
                    data.setRoom(true);
                }else{
                    data.setRoom(false);
                }
                chatList.add(data);

            }while (!(recvMsg.equals("chatList_end")));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return chatList;
    }

    private void mLog(String msg){
        Log.d("sunggeun", msg);
    }
}

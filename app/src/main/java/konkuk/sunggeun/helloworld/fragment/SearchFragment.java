package konkuk.sunggeun.helloworld.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import konkuk.sunggeun.helloworld.R;
import konkuk.sunggeun.helloworld.chat.MessageActivity;
import konkuk.sunggeun.helloworld.model.RoomModel;
import konkuk.sunggeun.helloworld.model.UserModel;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {
    private Socket socket;
    private String recvMsg;
    private DataOutputStream dos;
    private DataInputStream dis;

        private static String ip = "192.168.43.168";
//    private static String ip = "192.168.0.11";
    private static int port = 5568;

    private EditText searchRoom;
    private Button search_btn;

    private SearchFragmentRecyclerViewAdapter adapter;

//    private SharedPreferences pref;


    @Override
    public void onDestroy() {
        super.onDestroy();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchRoom = (EditText)view.findViewById(R.id.searchfragment_edittext_interesting);
        search_btn = (Button)view.findViewById(R.id.searchfragment_button);

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mLog("btn click : " + searchRoom.getText().toString());
                adapter.filter(searchRoom.getText().toString());

//                mLog("btn end");

            }
        });

        FloatingActionButton floatingActionButton = (FloatingActionButton)view.findViewById(R.id.searchfragment_floatingButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent( view.getContext(), MakeRoomActivity.class));
            }
        });

        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.searchfragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        adapter = new SearchFragmentRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);


        return view;
    }

    class SearchFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<RoomModel> roomModelList = new ArrayList<>();
        List<RoomModel> showList;

        public SearchFragmentRecyclerViewAdapter(){
            roomModelList = getRoomList();
            showList = new ArrayList<>();
            showList.addAll(roomModelList);

        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_roomlist, viewGroup, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
            ((CustomViewHolder)viewHolder).roomName_textview.setText(showList.get(i).getRoomName());
            ((CustomViewHolder)viewHolder).interesting_textview.setText(showList.get(i).getInteresting());


//
            viewHolder.itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getView().getContext(), MessageActivity.class);
                    intent.putExtra("destinationUid", showList.get(i).getRoomName());
                    intent.putExtra("isRoom", true);
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright, R.anim.toleft);
                    startActivity(intent, activityOptions.toBundle());



                }
            });

        }

        @Override
        public int getItemCount() {
            return showList.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder{
            public TextView roomName_textview;
            public TextView interesting_textview;

            public CustomViewHolder(View v){
                super(v);
                roomName_textview = (TextView)v.findViewById(R.id.roomlist_textview_name);
                interesting_textview = (TextView)v.findViewById(R.id.roomlist_textview_interesting);

            }
        }

        public void filter(String charText){
            charText = charText.toLowerCase(Locale.getDefault());
            mLog("char text : " + charText);
            showList.clear();
            if(charText.length() == 0){
                showList.addAll(roomModelList);
                mLog("is this here?");
            }else{
                mLog("else in : " + roomModelList.size());
                for(RoomModel i : roomModelList){
                    String interesting = i.getInteresting();
                    mLog("here?" + interesting);
                    if(interesting.toLowerCase().contains(charText)){
                        showList.add(i);
                    }
                }
            }

            notifyDataSetChanged();

        }
    }

    private List<RoomModel> getRoomList(){
        List<RoomModel> roomLists = new ArrayList<>();

        try {
            socket = new Socket(ip, port);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            //서버에게 요청.
            dos.writeUTF("roomList");

            //서버가 준다고 하면.
            recvMsg = dis.readUTF();
            if(recvMsg.equals("roomList_ok")){
                mLog("roomList_start");
                //받아오자.
                do {
                    recvMsg = dis.readUTF();
                    mLog("recvMsg : " + recvMsg);
                    if(recvMsg.equals("null")){
                        break;
                    }

                    String roomName = dis.readUTF();
                    String interesting = dis.readUTF();

                    roomLists.add(new RoomModel(roomName, interesting));

                }while (!(recvMsg.equals("roomList_end")));
            }
            mLog("roomList_end");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            dos.writeUTF("quit");
            dos.writeUTF("null");
            dos.close();
            dis.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return roomLists;
    }

    private void mLog(String msg){
        Log.d("sunggeun", msg);
    }
}

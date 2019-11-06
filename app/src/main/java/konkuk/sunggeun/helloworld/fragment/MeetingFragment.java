package konkuk.sunggeun.helloworld.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import de.hdodenhof.circleimageview.CircleImageView;
import konkuk.sunggeun.helloworld.R;
import konkuk.sunggeun.helloworld.model.UserModel;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MeetingFragment extends Fragment {

    private String uid;

    private Random r;
    private Socket socket;
    private String recvMsg;
    private DataOutputStream dos;
    private DataInputStream dis;

    private static String ip = "192.168.43.168";
//    private static String ip = "192.168.0.11";
    private static int port = 5568;

    private Button refresh;

    private MeetingFragmentRecyclerViewAdapter adapter;

    private String extStorageDirectory = Environment.getExternalStorageDirectory().toString()+ "/helloworld/";


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

        final View view = inflater.inflate(R.layout.fragment_meeting, container, false);
        SharedPreferences pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        uid = pref.getString("nickname", null);
        refresh = (Button)view.findViewById(R.id.meetingfragment_button);
        r = new Random();

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.refresh();
            }
        });



        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.meetingfragment_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(inflater.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MeetingFragmentRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        return view;
    }

    class MeetingFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> userModelList;
        List<UserModel> showList;
        int userModelListSize = 0;

        public MeetingFragmentRecyclerViewAdapter(){
            try {
                socket = new Socket(ip, port);
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());

            }catch (Exception e){
                e.printStackTrace();
            }

            userModelList = new ArrayList<>();
            userModelList = getMeetingList();
            showList = new ArrayList<>();

            userModelListSize = userModelList.size();

            if(userModelListSize > 2){

                int first = r.nextInt(userModelListSize);
                int second = 0;

                while (true){
                    second = r.nextInt(userModelListSize);
                    if(second != first){
                        break;
                    }
                }

                showList.add(userModelList.get(first));
                showList.add(userModelList.get(second));

            }else{
                showList.addAll(userModelList);
            }


        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_meeting, viewGroup, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
            ((CustomViewHolder)viewHolder).nickname.setText(showList.get(i).getNickName());
            ((CustomViewHolder)viewHolder).country.setText(showList.get(i).getCountry());
            ((CustomViewHolder)viewHolder).interesting.setText(showList.get(i).getInteresting());

            File imgFile = new File(extStorageDirectory+ showList.get(i).getNickName()+".png");
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ((CustomViewHolder)viewHolder).imageView.setImageBitmap(myBitmap);
            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    try {
                        dos.writeUTF("add_friend");
                        dos.writeUTF(uid);
                        dos.writeUTF(showList.get(i).getNickName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for(UserModel j : userModelList){
                        if(j.getNickName().equals(showList.get(i).getNickName())){
                            mLog(showList.get(i).getNickName());
                            userModelList.remove(j);
                            break;
                        }
                    }

                    Toast.makeText(getActivity(), showList.get(i).getNickName() + " add friend", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return showList.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder{
            public TextView nickname;
            public TextView country;
            public TextView interesting;
            public CircleImageView imageView;

            public CustomViewHolder(View v){
                super(v);
                imageView = (CircleImageView)v.findViewById(R.id.meetingitem_imageview) ;
                nickname = (TextView)v.findViewById(R.id.meetingitem_textview_nickname);
                country = (TextView)v.findViewById(R.id.meetingitem_textview_country);
                interesting = (TextView)v.findViewById(R.id.meetingitem_textview_interesting);
            }
        }

        public void refresh(){
            mLog("refresh meeting");
            showList.clear();

            userModelListSize = userModelList.size();

            if(userModelListSize > 2){
                int first = r.nextInt(userModelListSize);
                int second = 0;

                while (true){
                    second = r.nextInt(userModelListSize);
                    if(second != first){
                        break;
                    }
                }

                showList.add(userModelList.get(first));
                showList.add(userModelList.get(second));

            }else{
                showList.addAll(userModelList);
            }

            notifyDataSetChanged();

        }
    }

    private List<UserModel> getMeetingList(){
        List<UserModel> meetingLists = new ArrayList<>();
        try {
            //서버에게 요청.
            dos.writeUTF("meetingList");
            dos.writeUTF(uid);
                do {
                    recvMsg = dis.readUTF();
                    if(recvMsg.equals("null")){
                        break;
                    }

                    String nickname = dis.readUTF();
                    String country = dis.readUTF();
                    String interesting = dis.readUTF();

                    meetingLists.add(new UserModel(nickname, country, interesting));

                    mLog("add " + nickname + "to meetingList");

                }while (!(recvMsg.equals("meetingList_end")));

            for(UserModel i : meetingLists){
                File imgFile = new File(extStorageDirectory  + i.getNickName()+".png");
                if(!imgFile.exists()){
                    getImage(dos, dis, i.getNickName());
                }else{
                    mLog(i.getNickName() + "is EXIST");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return meetingLists;
    }

    private void getImage(DataOutputStream out, DataInputStream in, String nickname){
        try{
            out.writeUTF("ImageReq");

            out.writeUTF(nickname+".png");
            recvMsg = in.readUTF();
            int size = Integer.parseInt(recvMsg.substring(5));
            FileOutputStream fos = new FileOutputStream(extStorageDirectory + nickname+".png", false);

            byte[] buf = new byte[3000000];
            int read = 0;
            int rcvdSize = 0;

            while (rcvdSize < size) {
                read = in.read(buf) ;
                rcvdSize += read;
                fos.write(buf, 0, read);
            }

            fos.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        mLog("end of getImage : " + recvMsg);
    }

    private void mLog(String msg){
        Log.d("sunggeun", msg);
    }

}

package konkuk.sunggeun.helloworld.fragment;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import de.hdodenhof.circleimageview.CircleImageView;
import konkuk.sunggeun.helloworld.R;
import konkuk.sunggeun.helloworld.chat.MessageActivity;
import konkuk.sunggeun.helloworld.model.UserModel;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PeopleFragment extends Fragment {

    private String uid;

    private Socket socket;
    private String recvMsg;
    private DataOutputStream dos;
    private DataInputStream dis;

    private static String ip = "192.168.43.168";
//    private static String ip = "192.168.0.11";
    private static int port = 5568;

    private String extStorageDirectory = Environment.getExternalStorageDirectory().toString()+ "/helloworld/";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people, container, false);

        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.peoplefragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerViewAdapter());

        return view;
    }

    class PeopleFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<UserModel> userModels;

        public PeopleFragmentRecyclerViewAdapter(){
            SharedPreferences pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
            uid = pref.getString("nickname", null);

            userModels = new ArrayList<>();
            userModels = getFriends();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_friend, viewGroup, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int i) {

            ((CustomViewHolder)viewHolder).textView.setText(userModels.get(i).getNickName());
            File imgFile = new File(extStorageDirectory + userModels.get(i).getNickName()+".png");
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ((CustomViewHolder)viewHolder).imageView.setImageBitmap(myBitmap);
            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getView().getContext(), MessageActivity.class);
                    intent.putExtra("destinationUid", userModels.get(i).getNickName());
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(v.getContext(), R.anim.fromright, R.anim.toleft);
                    startActivity(intent, activityOptions.toBundle());

                }
            });

        }

        @Override
        public int getItemCount() {
            return userModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder{
            public CircleImageView imageView;
            public TextView textView;

            public CustomViewHolder(View v){
                super(v);
                imageView = (CircleImageView) v.findViewById(R.id.frienditem_imageview);
                textView = (TextView)v.findViewById(R.id.frienditem_textview);

            }
        }
    }

    private List<UserModel> getFriends(){
        List<UserModel> userModels = new ArrayList<>();

        try {
            socket = new Socket(ip, port);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            dos.writeUTF("friendList");
            dos.writeUTF(uid);

            do {
                recvMsg = dis.readUTF();
                mLog("recvMsg : " + recvMsg);
                if(recvMsg.equals("null")){
                    break;
                }

                String nickname = dis.readUTF();

                userModels.add(new UserModel(nickname));

                mLog("add nickname : " + nickname);
            }while (!(recvMsg.equals("friendList_end")));

            for(UserModel i : userModels){
                File imgFile = new File(extStorageDirectory+ i.getNickName()+".png");
                if(!imgFile.exists()){
                    getImage(dos, dis, i.getNickName());
                }else {
                    mLog(i.getNickName() + "is EXIST");
                }
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



            mLog("firendList_end");
        } catch (IOException e) {
            e.printStackTrace();
        }


        return userModels;
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
        mLog("end of getImage : " + nickname + ".png");
    }

    private void mLog(String msg){
        Log.d("sunggeun", msg);
    }

}

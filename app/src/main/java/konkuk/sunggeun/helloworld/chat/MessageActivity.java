package konkuk.sunggeun.helloworld.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import de.hdodenhof.circleimageview.CircleImageView;
import konkuk.sunggeun.helloworld.R;
import konkuk.sunggeun.helloworld.model.ChatModel;
import konkuk.sunggeun.helloworld.model.RoomModel;
import org.w3c.dom.Text;

import java.io.*;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MessageActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 10;

    private Socket socket;
    private String recvMsg;
    private DataOutputStream dos;
    private DataInputStream dis;

    private static String ip = "192.168.43.168";
//    private static String ip = "192.168.0.11";
    private static int port = 5568;

    private Button button;
    private EditText editText;
    private Button imgBtn;
    private RecyclerView recyclerView;

    private String uid;
    private String destinationUid;
    private SharedPreferences pref;
    private boolean isRoom;
    private List<ChatModel> comments;


    RecyclerViewAdapter mAdapter;

    private Uri imageUri;
    private String imagePath;
    String extStorageDirectory = Environment.getExternalStorageDirectory().toString() + "/helloworld/";

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            dos.writeUTF("quit");
            dos.writeUTF(uid);
            dos.close();
            dis.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        pref = getSharedPreferences("pref", MODE_PRIVATE);
        uid = pref.getString("nickname", null);
        destinationUid = getIntent().getStringExtra("destinationUid");
        isRoom = getIntent().getBooleanExtra("isRoom", false);
        mLog("uid : " + uid + ", des : " + destinationUid + "is Room" + isRoom);

        button = (Button) findViewById(R.id.messageActivity_button);
        imgBtn = (Button)findViewById(R.id.messageActivity_button_addimage);
        editText = (EditText) findViewById(R.id.messageActivity_editText);
        recyclerView = (RecyclerView)findViewById(R.id.messageActivitiy_recyclerview);
        try{
            socket = new Socket(ip, port);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            //여기에서 init메시지 보내고.
            dos.writeUTF("init");
            dos.writeUTF(uid);
            dos.writeUTF(destinationUid);

            if(isRoom){
                dos.writeUTF("getInRoom");
                dos.writeUTF(destinationUid);
                dos.writeUTF(uid);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

//        editText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                recyclerView.scrollToPosition(comments.size() - 1);
//            }
//        });

        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                    try {
                        if(editText.getText().toString().equals("")){
                            return;
                        }
                        dos.writeUTF("send_msg");
                        dos.writeUTF(uid);
                        dos.writeUTF(destinationUid);
                        dos.writeUTF(editText.getText().toString());
                        if(isRoom){
                            dos.writeUTF("room");
                        }else{
                            dos.writeUTF("noroom");
                        }
//                        ChatModel data = new ChatModel(uid, destinationUid, editText.getText().toString());
                        //comments.add(data);
                        mAdapter.addNewComment(uid, destinationUid, editText.getText().toString(), false);

                        editText.setText(null);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }



            }
        });

        imgBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);
            }
        });



        checkChatRoom();

        Thread worker = new Thread(){
            public void run(){

                try{
                    while(true){
                        String message = dis.readUTF();
                            String[] mData = message.split("&");
                            if(mData[2].equals("image")){
//                                mAdapter.addNewComment(mData[0], mData[1], mData[3], true);
                                comments.add(new ChatModel(mData[0], mData[1], mData[3], true));
                                File imgFile = new File(extStorageDirectory + mData[3]);
//                                mLog("mData[3] : " + mData[3]);
                                if(!imgFile.exists()){
//                                    mLog(mData[3] + "is here");

                                    getImage(dos, dis, mData[3]);
                                }
                            }else{
//                                mLog("here1");


//                                mAdapter.addNewComment(mData[0], mData[1], mData[2], false);
                                ChatModel data = new ChatModel(mData[0], mData[1], mData[2], false);

                                comments.add(data);
                            }

//                        mLog("here 2");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyDataSetChanged();
                                mAdapter.downScroll();
                            }
                        });



                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        worker.start();

    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK) {

            imageUri = data.getData();
            imagePath = getRealPathFromUri(imageUri);
            final String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            mAdapter.addNewComment(uid, destinationUid, imageName , true);


                sendImg(imagePath, imageName);
                copyfile(imagePath, extStorageDirectory + imageName);

                Handler handler = new Handler();

                handler.postDelayed(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            dos.writeUTF("sendImageMessage");
                            dos.writeUTF(uid);
                            dos.writeUTF(destinationUid);
                            dos.writeUTF(imageName);
                            if(isRoom){
                                dos.writeUTF("room");
                            }else{
                                dos.writeUTF("noroom");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }, 500);


        }
    }

    private String getRealPathFromUri(Uri contentUri){
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        return cursor.getString(column_index);
    }

    private void checkChatRoom(){
        try{
                recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                mAdapter = new RecyclerViewAdapter();
                recyclerView.setAdapter(mAdapter);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void mLog(String msg){
        Log.d("sunggeun", msg);
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
//        private List<ChatModel> comments;


        public RecyclerViewAdapter(){
            comments = new ArrayList<>();

            try {
                dos.writeUTF("commentsList");
                dos.writeUTF(uid);
                dos.writeUTF(destinationUid);
                if(isRoom){
                    dos.writeUTF("room");
                }else{
                    dos.writeUTF("noroom");
                }

                do {
                    recvMsg = dis.readUTF();
                    if(recvMsg.equals("messageList_null")){
                        break;
                    }

                    String message = dis.readUTF();
                    String[] mData = message.split("&");

                    ChatModel data = new ChatModel();
                    data.setSendUid(mData[0]);
                    data.setRecvUid(mData[1]);
                    if(mData[2].equals("image")){
                        data.setMessage(mData[3]);
                        data.setImg(true);
                    }else{
                        data.setMessage(mData[2]);
                        data.setImg(false);
                    }
                    comments.add(data);

                }while (!(recvMsg.equals("messageList_end")));

                for(ChatModel i : comments){
                    if(i.isImg()){
                        File imgFile = new File(extStorageDirectory + i.getMessage());
                        if(!imgFile.exists()){
                            getImage(dos, dis, i.getMessage());
                        }
                    }
                }

                recyclerView.scrollToPosition(comments.size() - 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
           View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_message, viewGroup, false);

            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            MessageViewHolder messageViewHolder = ((MessageViewHolder) viewHolder);

            if(comments.get(i).getSendUid().equals(uid)){
                messageViewHolder.linearLayout_destination.setVisibility(View.INVISIBLE);
                messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);

                if(comments.get(i).isImg()){

                    messageViewHolder.imageView.setBackgroundResource(R.drawable.rightbublle);
                    messageViewHolder.textView_message.setVisibility(View.INVISIBLE);
                    messageViewHolder.imageView.setVisibility(View.VISIBLE);


                    File imgFile = new File(extStorageDirectory + comments.get(i).getMessage());
                    if(imgFile.exists()){
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 4;
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(),options);

                        messageViewHolder.imageView.setImageBitmap(myBitmap);
                    }else {
                        mLog("파일이 없어요 : "+ comments.get(i).getMessage());
                    }

                }else{

                    messageViewHolder.textView_message.setBackgroundResource(R.drawable.rightbublle);
                    messageViewHolder.linearLayout_main.setGravity(Gravity.RIGHT);
                    messageViewHolder.textView_message.setVisibility(View.VISIBLE);

                    messageViewHolder.imageView.setImageResource(R.drawable.baseline_account_circle_black_18dp);
                    messageViewHolder.imageView.setVisibility(View.INVISIBLE);
                    messageViewHolder.textView_message.setText(comments.get(i).getMessage());
                    messageViewHolder.textView_message.setTextSize(25);
                }

            }else{
                messageViewHolder.textView_nickname.setText(comments.get(i).getSendUid());
                messageViewHolder.linearLayout_destination.setVisibility(View.VISIBLE);
                messageViewHolder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                messageViewHolder.linearLayout_main.setGravity(Gravity.LEFT);

                File imgFile = new File(extStorageDirectory + comments.get(i).getSendUid() + ".png");
                if(imgFile.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                    messageViewHolder.profileimageView.setImageBitmap(myBitmap);
                }

                if(comments.get(i).isImg()){

                    messageViewHolder.imageView.setBackgroundResource(R.drawable.leftbubble);
                    messageViewHolder.textView_message.setVisibility(View.INVISIBLE);
                    messageViewHolder.imageView.setVisibility(View.VISIBLE);


                    imgFile = new File(extStorageDirectory + comments.get(i).getMessage());
                    if(imgFile.exists()){
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 4;
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(),options);

                        messageViewHolder.imageView.setImageBitmap(myBitmap);
                    }else{
                        mLog("이미지가 없나? : " + comments.get(i).getMessage());
                    }

                }else{

                    messageViewHolder.textView_message.setVisibility(View.VISIBLE);
                    messageViewHolder.imageView.setImageResource(R.drawable.baseline_account_circle_black_18dp);
                    messageViewHolder.imageView.setVisibility(View.INVISIBLE);
                    messageViewHolder.textView_message.setText(comments.get(i).getMessage());
                    messageViewHolder.textView_message.setTextSize(25);
                }

            }

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_nickname;
            public LinearLayout linearLayout_destination;
            public LinearLayout linearLayout_main;
            public CircleImageView profileimageView;
            public ImageView imageView;

            public MessageViewHolder(View view) {
                super(view);
                textView_message = (TextView) view.findViewById(R.id.messageItem_textView_message);
                textView_nickname = (TextView) view.findViewById(R.id.messageItem_textview_nickname);
                linearLayout_destination = (LinearLayout) view.findViewById(R.id.messageItem_linearLayout_destination);
                linearLayout_main = (LinearLayout) view.findViewById(R.id.messageItem_linearLayout_main);
                profileimageView = view.findViewById(R.id.messageItem_imageview_profile);
                imageView = view.findViewById(R.id.messgeItem_imageview_image);
            }
        }

        public void addNewComment(String sendUid, String recvUid, String message, boolean isImg){
            mLog("add New Comment");
            ChatModel newChat = new ChatModel(sendUid, recvUid, message, isImg);

            comments.add(newChat);

            notifyDataSetChanged();
            downScroll();

        }

        public void downScroll(){
            recyclerView.scrollToPosition(comments.size()-1);
        }



    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }

//    private void getImg(DataOutputStream out, DataInputStream in, String imagePath, String fileName){
//        try{
//            out.writeUTF("getImage");
//            mLog(fileName + "을 받아온다.");
//
//            out.writeUTF(fileName);
//            recvMsg = in.readUTF();
//            int size = Integer.parseInt(recvMsg.substring(5));
//            FileOutputStream fos = new FileOutputStream(imagePath+fileName, false);
//
//            byte[] buf = new byte[3000000];
//            int read = 0;
//            int rcvdSize = 0;
//
//            while (rcvdSize < size) {
//                read = in.read(buf) ;
//                rcvdSize += read;
//                fos.write(buf, 0, read);
//                mLog("rcvdSize : " + rcvdSize);
//            }
//
//            mLog("여기가 들어와야된다.");
//
//            fos.close();
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }

    private void getImage(DataOutputStream out, DataInputStream in, String msg){
        try{
            out.writeUTF("ImageReq");

            out.writeUTF(msg);
            recvMsg = in.readUTF();
            int size = Integer.parseInt(recvMsg.substring(5));
            FileOutputStream fos = new FileOutputStream(extStorageDirectory + msg, false);

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
        mLog("end of getImage : " + msg);
    }

    private void sendImg(String imagePath, String fileName){

        File file = new File(imagePath);
        try {
            dos.writeUTF("getImagemessage");
            dos.writeUTF(fileName);
            dos.writeUTF("Start" + file.length());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            FileInputStream fis = new FileInputStream(imagePath);
            byte[] buffer = new byte[3000000];

            int readSize = 0;

            while((readSize = fis.read(buffer)) > 0){
                dos.write(buffer, 0, readSize);
                dos.flush();
            }

            fis.close();

        }catch (Exception e){
            e.printStackTrace();
        }

        mLog("img send Success");
    }

    private void copyfile(String from, String to){
        mLog("from : " + from + ", to : " + to);
        if(from.equals(to)){
            return;
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel in = null;
        FileChannel out = null;
        try{
            fis = new FileInputStream(from);
            fos = new FileOutputStream(to);
            in = fis.getChannel();
            out = fos.getChannel();
            in.transferTo(0, in.size(), out);

            out.close();
            in.close();
            fis.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}

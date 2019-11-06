package konkuk.sunggeun.helloworld.fragment;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import konkuk.sunggeun.helloworld.R;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MakeRoomActivity extends AppCompatActivity {
    private EditText roomName;
    private EditText interesting;
    private Button makebtn;

    private Socket socket;
    private String recvMsg;
    private DataOutputStream dos;
    private DataInputStream dis;

//    private static String ip = "192.168.0.11";
    private static String ip = "192.168.43.168";
    private static int port = 5568;

    private SharedPreferences pref;
    private String uid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_room);

        pref = getSharedPreferences("pref", MODE_PRIVATE);
        uid = pref.getString("nickname", null);

        roomName = (EditText) findViewById(R.id.makeroomActivity_edittext_roomname);
        interesting = (EditText)findViewById(R.id.makeroomActivity_edittext_interesting);
        makebtn = (Button)findViewById(R.id.makeroomActivity_button_make);

        makebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    socket = new Socket(ip, port);
                    dis = new DataInputStream(socket.getInputStream());
                    dos = new DataOutputStream(socket.getOutputStream());

                    dos.writeUTF("make_chatRoom");
                    dos.writeUTF(roomName.getText().toString());
                    dos.writeUTF(interesting.getText().toString());
                    dos.writeUTF(uid);


                    MakeRoomActivity.this.finish();
                    try {
                        dos.writeUTF("quit");
                        dos.writeUTF("null");
                        dos.close();
                        dis.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

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
}

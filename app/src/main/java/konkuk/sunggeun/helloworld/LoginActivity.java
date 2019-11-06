package konkuk.sunggeun.helloworld;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.*;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {
    private enum Mode{
        LOGIN, LOGOUT
    }

    private String nickname;

    private EditText id;
    private EditText password;
    private Button login;
    private Button signup;

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String recvMsg;

    private static String ip = "192.168.43.168";
//    private static String ip = "192.168.0.11";
    private static int port = 5568;

    private Mode mode = Mode.LOGOUT;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pref = getSharedPreferences("pref", MODE_PRIVATE);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        id = (EditText)findViewById(R.id.loginActivity_edittext_id);
        password = (EditText)findViewById(R.id.loginActivity_edittext_pw);
        login = (Button)findViewById(R.id.loginActuvuty_button_login);
        signup = (Button)findViewById(R.id.loginActuvuty_button_signup);

        login.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                sendLoginInfo();
                if(mode == Mode.LOGIN){
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("nickname", nickname);
                    editor.commit();
                    startActivity(intent);
                    mode = Mode.LOGOUT;
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });


    }

    private void sendLoginInfo(){
        try{
            socket = new Socket(ip,  port);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());

            dos.writeUTF("login");
            dos.writeUTF(id.getText().toString());
            dos.writeUTF(password.getText().toString());
            recvMsg = dis.readUTF();

            if(recvMsg.equals("can't find ID")){
                Toast.makeText(LoginActivity.this, "ID Error!", Toast.LENGTH_SHORT).show();
            }else if(recvMsg.equals("password failed")){
                Toast.makeText(LoginActivity.this, "PW Error!", Toast.LENGTH_SHORT).show();
            }else{
                nickname = recvMsg;
                mLog("nickname login : " + nickname);
                mode = Mode.LOGIN;
            }

            dos.writeUTF("quit");
            dos.writeUTF("null");

            dos.close();
            dis.close();
            socket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void mLog(String msg){
        Log.d("sunggeun", msg);
    }
}

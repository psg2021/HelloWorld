package konkuk.sunggeun.helloworld;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import de.hdodenhof.circleimageview.CircleImageView;

import java.io.*;
import java.net.Socket;

public class SignupActivity extends AppCompatActivity {
    private static final int PICK_FROM_ALBUM = 10;
    private EditText id;
    private EditText pw;
    private EditText country;
    private EditText nickname;
    private EditText interesting;
    private Button signup;
    private CircleImageView profile;
    private LinearLayout linearLayout;

    private Socket socket;
    private String recvMsg;
    private DataOutputStream dos;
    private DataInputStream dis;

    private static String ip = "192.168.43.168";
//    private static String ip = "192.168.0.11";
    private static int port = 5568;

    private Uri imageUri;
    private Bitmap profileImg;

    private static final int PERMISSIONS_REQUSET_CODE = 100;
    public static String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grandResults){
        if(requestCode == PERMISSIONS_REQUSET_CODE && grandResults.length == REQUIRED_PERMISSIONS.length){
            boolean check_result = true;

            for(int result : grandResults){
                if(result != PackageManager.PERMISSION_GRANTED){
                    check_result = false;
                    break;
                }
            }

            if(check_result){
                ;
            }else{
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])){
                    Snackbar.make(linearLayout, "거부됨. 앱재실행필요", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    }).show();
                }else{
                    Snackbar.make(linearLayout, "거부됨.설정에서 퍼미션허용필요함", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    }).show();
                }
            }
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        linearLayout = (LinearLayout)findViewById(R.id.signupActivity_linearlayout_main);

        int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])){

                Snackbar.make(linearLayout, "외부저장소 권한 필요", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(SignupActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUSET_CODE);
                    }
                }).show();

            }else{
                ActivityCompat.requestPermissions(SignupActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUSET_CODE);
            }
        }

        profile = (CircleImageView)findViewById(R.id.signupActivity_imageview_profile);
        id = (EditText)findViewById(R.id.signupActivity_edittext_id);
        pw = (EditText)findViewById(R.id.signupActivity_edittext_pw);
        country = (EditText)findViewById(R.id.signupActivity_edittext_country);
        nickname = (EditText)findViewById(R.id.signupActivity_edittext_nickname);
        interesting = (EditText)findViewById(R.id.signupActivity_edittext_interesting);
        signup = (Button)findViewById(R.id.signupActivity_button_signup);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //여기서 이제 버튼눌렀을때 정보를 서버에 보내주는거 구현하면 된다.
                if(id.getText().toString().equals("")){
                    Toast.makeText(SignupActivity.this, "id Error!", Toast.LENGTH_SHORT).show();
                    return;
                }else if(pw.getText().toString().equals("")){
                    Toast.makeText(SignupActivity.this, "pw Error!", Toast.LENGTH_SHORT).show();
                    return;
                }else if(nickname.getText().toString().equals("")){
                    Toast.makeText(SignupActivity.this, "nickname Error!", Toast.LENGTH_SHORT).show();
                    return;
                }else if(interesting.getText().toString().equals("")){
                    Toast.makeText(SignupActivity.this, "interesting Error!", Toast.LENGTH_SHORT).show();
                    return;
                }else if(imageUri == null){
                    Toast.makeText(SignupActivity.this, "image Error!", Toast.LENGTH_SHORT).show();
                    return;
                }else if(country.getText().toString().equals("ko") ||
                        country.getText().toString().equals("ja") || country.getText().toString().equals("zh-CN")){
                    sendUserInfo();
                    SignupActivity.this.finish();
                }
                else{

                    Toast.makeText(SignupActivity.this, "country Error!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });

        profile.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, PICK_FROM_ALBUM);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK) {
            profile.setImageURI(data.getData());
            imageUri = data.getData();
            try {
                profileImg = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getRealPathFromUri(Uri contentUri){
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        return cursor.getString(column_index);
    }

    private void sendUserInfo(){
        try{
            socket = new Socket(ip,  port);

            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            sendImg();

            recvMsg = dis.readUTF();
            if(!recvMsg.equals("image_end")){
                return;
            }

            dos.writeUTF("signup");

            dos.writeUTF(id.getText().toString());
            dos.writeUTF(pw.getText().toString());
            dos.writeUTF(country.getText().toString());
            dos.writeUTF(nickname.getText().toString());
            dos.writeUTF(interesting.getText().toString());

            dos.writeUTF("quit");
            dos.writeUTF("null");
            dos.close();
            dis.close();
            socket.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendImg(){
        try {
            dos.writeUTF("send_profileimg");
            mLog("send_image(signup)");
            dos.writeUTF(nickname.getText().toString());
            String imagePath = getRealPathFromUri(imageUri);

            File file = new File(imagePath);
            dos.writeUTF("Start" + file.length());
            FileInputStream fis = new FileInputStream(imagePath);
            byte[] buffer = new byte[3000000];

            int readSize = 0;

            while((readSize = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, readSize);
                dos.flush();
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mLog("send_image Success");
    }

    private void mLog(String msg){
        Log.d("sunggeun", msg);
    }

}

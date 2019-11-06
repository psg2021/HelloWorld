package konkuk.sunggeun.helloworld;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import konkuk.sunggeun.helloworld.fragment.ChatFragment;
import konkuk.sunggeun.helloworld.fragment.MeetingFragment;
import konkuk.sunggeun.helloworld.fragment.PeopleFragment;
import konkuk.sunggeun.helloworld.fragment.SearchFragment;

public class MainActivity extends AppCompatActivity {

   // private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // userId = getIntent().getStringExtra("id");
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.mainactivity_bottomnavigationview);
        getSupportFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout, new MeetingFragment()).commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.action_people:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout, new PeopleFragment()).commit();
                        return true;
                    case R.id.action_chat:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout, new ChatFragment()).commit();
                        return true;
                    case R.id.action_search:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout, new SearchFragment()).commit();
                        return true;
                    case R.id.action_meeting:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainactivity_framelayout, new MeetingFragment()).commit();
                        return true;
                }
                return false;
            }
        });




    }
}

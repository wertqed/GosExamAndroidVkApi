package com.example.vladimir.gosexamappvkapi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String[] scope = new String[]{VKScope.MESSAGES, VKScope.GROUPS, VKScope.FRIENDS, VKScope.PHOTOS};
    Button findButton;
    RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    SharedPreferences sPref;

    String oldTime;
    private final String LAST_TIME = "LAST_TIME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        findButton = (Button) findViewById(R.id.findButton);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        // используем linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // создаем адаптер
        VKSdk.login(this, scope);
        findButton.setOnClickListener(findButtonList);


        sPref = getPreferences(MODE_PRIVATE);
        // Пример работы с преференс
        oldTime = sPref.getString(LAST_TIME, "");
        Toast toast = Toast.makeText(getApplicationContext(),
                "Последний вход в систему " + oldTime + "!", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        SharedPreferences.Editor ed = sPref.edit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ed.putString(LAST_TIME, LocalDateTime.now().toString());
        }
        ed.apply();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Обработка событий на клик
    View.OnClickListener findButtonList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FriendsTask task = new FriendsTask();
            task.execute();
        }
    };

    //Класс для работы в фоновом потоке
    class FriendsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            VKParameters parameters = VKParameters.from(VKApiConst.USER_ID, "140179955");
            parameters.put(VKApiConst.FIELDS, "city, domain");
            VKRequest request = VKApi.friends().get(parameters);
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {

                    //получили ответ
                    super.onComplete(response);

                    //Складываем ответ в вк лист

                    VKList list = (VKList) response.parsedModel;

                    List<String> listNames = new ArrayList<>();
                    for (VKApiUserFull user : (List<VKApiUserFull>) list) {
                        listNames.add(user.first_name + " " + user.last_name);
                    }

                    CustomRecyclerViewAdapter adapter = new CustomRecyclerViewAdapter(listNames);
                    recyclerView.setAdapter(adapter);
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    //Для Vk api результат всплывающего окна
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Чтобы загрузить друзей кликните на соотв кнопку!", Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                Toast toast = Toast.makeText(getApplicationContext(),
                        "что то пошло не по плану!", Toast.LENGTH_SHORT);
                toast.show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}

package com.herorickystudios.sockshttp;

import android.content.Intent;
import android.os.Bundle;
import com.herorickystudios.sockshttp.R;
import com.herorickystudios.sockshttp.activities.BaseActivity;

/**
 * @author anuragdhunna
 */
public class LauncherActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // inicia atividade principal
        Intent intent = new Intent(this, SocksHttpMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);

        // encerra o launcher
        finish();
    }

}
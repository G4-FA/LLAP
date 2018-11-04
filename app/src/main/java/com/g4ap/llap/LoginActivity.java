package com.g4ap.llap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.List;

public class LoginActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        Button testBtn = (Button)findViewById(R.id.act_login_login);
		testBtn.setOnClickListener(MyOnClickListener);
    }
    
    
	private View.OnClickListener MyOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{

			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(intent);
			finish();

			new Thread(new Runnable(){
				@Override
				public void run() {

					LLCOSUtils llCosUtil = new LLCOSUtils();
					List<tObjectInfo> COSObjList = llCosUtil.getAllCOSObjectList( getApplicationContext() );

					COSLLBrowser cosLLBrowser = new COSLLBrowser();
					try {
						cosLLBrowser.Init( COSObjList );
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}).start();

		}
	};
	
}

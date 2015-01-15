package com.soniq.tvremotecontrol;

import com.baidu.mobstat.StatService;
import com.example.mytabhost.R;
import com.soniq.tvremotecontrol.fragment.AppmanageFragment;
import com.soniq.tvremotecontrol.fragment.EmarketFragment;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity {

	private AppmanageFragment appfragment;
	private EmarketFragment emarketfragment;
	private int mBackKeyPressedTimes = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}
		
		final TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
		//��ȫ��tabHost��ֵ���������ڵ��MouseFragment�еķ��ذ�ťʱ��������tabHost�еĵ�һ������ѡ�У���������ʾ������
		GlobalParams.TH=tabHost;
		tabHost.setup();
		
		ImageButton iv1 = new ImageButton(MainActivity.this);
		iv1.setBackgroundResource(R.drawable.btn_remotecontrol);
		ImageButton iv2 = new ImageButton(MainActivity.this);
		iv2.setBackgroundResource(R.drawable.btn_mouse);
		ImageButton iv3 = new ImageButton(MainActivity.this);
		iv3.setBackgroundResource(R.drawable.btn_appmanage);
		ImageButton iv4 = new ImageButton(MainActivity.this);
		iv4.setBackgroundResource(R.drawable.btn_emarket);
		ImageButton iv5 = new ImageButton(MainActivity.this);
		iv5.setBackgroundResource(R.drawable.btn_set);
		
		FragmentManager fm = getFragmentManager();
		appfragment = (AppmanageFragment) fm.findFragmentById(R.id.appmanage_tab);
		emarketfragment = (EmarketFragment) fm.findFragmentById(R.id.emarket_tab);
		
		tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator(iv1).setContent(R.id.controller_tab));
		tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator(iv2).setContent(R.id.mouse_tab));
		tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator(iv3).setContent(R.id.appmanage_tab));
		tabHost.addTab(tabHost.newTabSpec("tab4").setIndicator(iv4).setContent(R.id.emarket_tab));
		tabHost.addTab(tabHost.newTabSpec("tab5").setIndicator(iv5).setContent(R.id.set_tab));
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			
			@Override
			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub
				if("tab2".equals(tabId)){
					//��ѡ�еڶ���tabʱ�õײ���tabHost���أ������Ϳ���ȫ����ʾMouseFragment��
					tabHost.getTabWidget().setVisibility(View.GONE);
				}else if("tab3".equals(tabId)){
					if(appfragment.applists.size()==0)
						//��ѡ��Ӧ�ù���ģ��ʱ�����Ӧ���б�Ϊ�ղ���û�����ڼ��أ����Զ���ʼ���أ�����״̬Ϊtrue�������ܹ������ظ�����
						if(!appfragment.isloading)
						{
						appfragment.isloading = true;
						appfragment.loadData();
						}
					
				}else if("tab4".equals(tabId)){
					//Ч�������Ӧ�ù���ģ��һ��
					if(emarketfragment.applists.size()==0)
						if(!emarketfragment.isloading)
						{
						emarketfragment.isloading = true;
					    emarketfragment.loadData();
						}
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return false;
	}
	//������д�������������ڷ������ڰٶ�ͳ��
	public void onResume() {
		Log.w("MainActivity", "Activity1.OnResume()");
		super.onResume();

		StatService.onResume(this);
	}

	public void onPause() {
		Log.w("MainActivity", "Activity1.onPause()");
		super.onPause();

		StatService.onPause(this);
	}
	
	@Override  
    public void onDestroy() {
        super.onDestroy();
        
	}
	
	@Override
    public void onBackPressed() {
		ExitApp();
    }
	
	private long exitTime = 0;
	//�����η��ؼ��˳�����
    public void ExitApp()
    {
            if ((System.currentTimeMillis() - exitTime) > 2000)
            {
                    Toast.makeText(this, getResources().getString(R.string.exit_apllication), Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
            } else
            {
                    this.finish();
            }

    }

}

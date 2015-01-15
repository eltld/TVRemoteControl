package com.soniq.tvremotecontrol.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.example.mytabhost.R;
import com.soniq.tvremotecontrol.GlobalParams;
import com.soniq.tvremotecontrol.ServerData;
import com.soniq.tvremotecontrol.adapter.AppListAdapter;
import com.soniq.tvremotecontrol.bean.AppInfo;
import com.soniq.tvremotecontrol.view.CustomPopupWindow;
import com.soniq.tvremotecontrol.view.OpenPopupWindow;

public class AppmanageFragment extends Fragment implements OnClickListener,
		OnItemClickListener, OnScrollListener {

	public ArrayList<AppInfo> applists;
	private ListView appmanage_applist;
	private CustomPopupWindow menuWindow;
	private OpenPopupWindow openWindow;
	private AppListAdapter adapter;
	private Button btn_finish;
	private ImageView ivloading;
	private Animation animation;
	private TextView tv_fail;
	public boolean isloading;

	private void unInstallPopWindow(AdapterView<?> parent, int position) {
		menuWindow = new CustomPopupWindow(getActivity());
		menuWindow.showAtLocation(parent, Gravity.BOTTOM
				| Gravity.CENTER_HORIZONTAL, 0, 0); // ����layout��PopupWindow����ʾ��λ��
		final int i = position;
		menuWindow.getTv_start().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// �ر�popupwindow����Ӧ�ó���
				dismissPopupWindow();
				openApp(i);
			}

		});

		menuWindow.getTv_uninstall().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismissPopupWindow();
				uninstallApp(i);
			}

		});

		menuWindow.getTv_cancel().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismissPopupWindow();
			}

		});
	}

	private void openPopWindow(AdapterView<?> parent, int position) {
		openWindow = new OpenPopupWindow(getActivity());
		openWindow.showAtLocation(parent, Gravity.BOTTOM
				| Gravity.CENTER_HORIZONTAL, 0, 0); // ����layout��PopupWindow����ʾ��λ��
		final int i = position;
		openWindow.getTv_open().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismissPopupWindow();
				openApp(i);
			}

		});

		openWindow.getTv_cancel().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismissPopupWindow();
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		applists = new ArrayList<AppInfo>();
		// �ж��Ƿ����ڼ���Ӧ���б�
		isloading = false;
		return inflater.inflate(R.layout.layout_appmanage, container, false);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initView();
	}

	private void initView() {
		appmanage_applist = (ListView) getView().findViewById(
				R.id.appmanage_applist);
		ivloading = (ImageView) getView()
				.findViewById(R.id.appmanage_ivLoading);
		btn_finish = (Button) getView().findViewById(R.id.appmanage_refresh);
		tv_fail = (TextView) getView().findViewById(R.id.appmanage_tvfailed);
		setListener();
	}

	private void setListener() {
		btn_finish.setOnClickListener(this);
		appmanage_applist.setOnItemClickListener(this);
		appmanage_applist.setOnScrollListener(this);
	}

	// ���popupwindow��Ϊ�գ�����ر�
	private void dismissPopupWindow() {
		if (menuWindow != null && menuWindow.isShowing()) {
			menuWindow.dismiss();
			menuWindow = null;
		}
		if (openWindow != null && openWindow.isShowing()) {
			openWindow.dismiss();
			openWindow = null;
		}
	}

	private void openApp(int itemIndex) {
		if (itemIndex == -1)
			return;
		AppInfo appInfo = applists.get(itemIndex);

		ServerData.openApp(GlobalParams.IP, appInfo.getPkgname());
	}

	private void uninstallApp(int itemIndex) {
		if (itemIndex == -1)
			return;

		AppInfo appInfo = applists.get(itemIndex);
		applists.remove(itemIndex);
		adapter.notifyDataSetChanged();
		ServerData.uninstallApp(GlobalParams.IP, appInfo.getPkgname());

	}

	public void loadData() {
		animation = AnimationUtils.loadAnimation(getActivity(),
				R.anim.loading_animation);
		// ʹ��ImageView��ʾ����
		ivloading.setVisibility(View.VISIBLE);
		appmanage_applist.setVisibility(View.INVISIBLE);
		tv_fail.setVisibility(View.INVISIBLE);
		ivloading.startAnimation(animation);

		new LoadThread().start();
	}

	private class LoadThread extends Thread {
		public void run() {
			int iret = 1;
			try {
				iret = doLoadData();
			} catch (Exception e) {

			}

			Message msg = new Message();
			msg.what = iret;
			handler.sendMessage(msg);
		}
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				// ���سɹ�
				Log.v("success", "success");
				animation.cancel();
				ivloading.setAnimation(null);
				ivloading.setVisibility(View.GONE);
				adapter = new AppListAdapter(getActivity(), applists);
				appmanage_applist.setAdapter(adapter);
				appmanage_applist.setVisibility(View.VISIBLE);
				isloading = false;
				// _loadingView.setVisibility(View.GONE);
			} else {
				animation.cancel();
				ivloading.setAnimation(null);
				ivloading.setVisibility(View.GONE);
				tv_fail.setVisibility(View.VISIBLE);
				Log.v("loss", "loss");
				isloading = false;
				// ����ʧ��
			}
		}
	};

	private int doLoadData() {
		// applists.clear();
		Log.v("ipadreess", "ip" + GlobalParams.IP);
		applists.clear();
		// �����ӵ��豸�ϻ�ȡӦ���б�������洢��applists��
		int iret = ServerData.getAppListFromServer(GlobalParams.IP, applists);

		return iret;
	}

	public void onResume() {
		super.onResume();
		StatService.onResume(this);
	}

	public void onPause() {
		super.onPause();
		StatService.onPause(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int position,
			long arg3) {
		AppInfo appInfo = applists.get(position);
		// ��ȡ��ÿ��Ӧ�ó�����Ϣ�У���һ�����������Ƿ��ܹ���ж�ء���Ϊ"yes"ʱ�����Ա�ж�أ��򿪰���ж��ѡ���popupwindow�����򣬴���ж��ѡ���popupwindow��
		if ("yes".equals(appInfo.getUninstall())) {
			unInstallPopWindow(parent, position);
		} else {
			openPopWindow(parent, position);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.appmanage_refresh:
			Log.i("loaddata", "loaddata");
			// ����������ڼ������ݣ���������
			if (!isloading) {
				Log.i("refresh_btn", "refresh_btn");
				isloading = true;
				loadData();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
		// ��listview����ʱ���ر�popupwindow
		dismissPopupWindow();
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {

	}

}

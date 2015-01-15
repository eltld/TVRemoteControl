package com.soniq.tvremotecontrol.fragment;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mobstat.StatService;
import com.example.mytabhost.R;
import com.soniq.tvremotecontrol.GetDeviceList;
import com.soniq.tvremotecontrol.GlobalParams;
import com.soniq.tvremotecontrol.WAPI;
import com.soniq.tvremotecontrol.bean.AppInfo;
import com.soniq.tvremotecontrol.bean.DeviceInfo;
import com.soniq.tvremotecontrol.utils.CustomUI;
import com.soniq.tvremotecontrol.utils.ImageCache;
import com.soniq.tvremotecontrol.utils.ImageCallBack;
import com.soniq.tvremotecontrol.utils.ImageDownload;
import com.soniq.tvremotecontrol.utils.MyUtils;
import com.soniq.tvremotecontrol.view.UserLogin;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EmarketFragment extends Fragment {

	private ListView emarket_list;
	public List<AppInfo> applists;
	private ImageView ivloading;
	private Animation animation;
	private TextView tvloading;
	private LoadThread loadingThread = null;
	private EMarketAdapter adapter;
	private Button btn_refresh;
	public static final int REQUSET_LOGIN = 1001;
	public static final int REQUSET_REGISTER = 1002;
	private ArrayList<DeviceInfo> devicelists;
	private GetDeviceList getdevicelist;
	private SetFragment setfragment;
	private FragmentManager fm;
	public boolean isloading;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		applists = new ArrayList<AppInfo>();
		View view = inflater.inflate(R.layout.layout_emarket, container, false);
		emarket_list = (ListView) view.findViewById(R.id.emarket_applist);
		ivloading = (ImageView) view.findViewById(R.id.emarket_ivLoading);
		//Ĭ��״̬�£�����Ϊfalse����ʾû�����ڼ���
		isloading = false;
		tvloading = (TextView) view.findViewById(R.id.emarket_tvloading);
		btn_refresh = (Button) view.findViewById(R.id.emarket_refresh);
	
		fm = getFragmentManager();
		setfragment = (SetFragment) fm.findFragmentById(R.id.set_tab);
		
		btn_refresh.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//û�м������ݣ���ô�ӷ���������
				if(!isloading)
				{
					Log.i("refresh_btn", "refresh_btn");
				isloading = true;
				loadData();
				}
			}
			
		});
		
		adapter = new EMarketAdapter(getActivity(),applists,new ImageCallBack() {

			@Override
			public void imageLoaded(Bitmap bitmap, String tag) {
				ImageView iv=(ImageView) emarket_list.findViewWithTag(tag);
				if(iv!=null){
					iv.setImageBitmap(bitmap);
				}
			}
		});
		return view;
	}
	
	public void onResume() {
		super.onResume();
		Log.d("EmarketFragment", "Control Fragment resume");
		StatService.onResume(this);
	}

	public void onPause() {
		super.onPause();
		Log.d("EmarketFragment", "Control Fragment pause");
		StatService.onPause(this);
	}
	
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);//super�������뱻����
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);//super�������뱻����
	}
	
	public void loadData()
	{
		tvloading.setVisibility(View.INVISIBLE);
		emarket_list.setVisibility(View.INVISIBLE);
		applists.clear();
		animation = AnimationUtils.loadAnimation(
				getActivity(), R.anim.loading_animation);
		// ʹ��ImageView��ʾ����
		ivloading.startAnimation(animation);
		
		loadingThread = new LoadThread();
		loadingThread.start();
	}
	
	public int doLoadData()
	{
		String urlString = WAPI.addLangParams(MyUtils.GetLangString());
		String content = WAPI.http_get_content(urlString);
		if( content == null || content.length() < 1)
			return 1;
		
		
		int iret = WAPI.parseAppListJSONResponse(content, applists);
		if( iret != 0 )
			return iret;

		// �ɹ���
		return 0;
	}
	

	private class LoadThread extends Thread
	{
		public void run()
		{
			int iret = 1;
			try
			{
				iret = doLoadData();
			}
			catch(Exception e)
			{
				
			}
			Message msg = new Message();
			msg.what = iret;
			handler_marketapp.sendMessage(msg);
		}
	}	
	
	Handler handler_marketapp = new Handler(){
		@Override
		public void handleMessage(Message msg)
		{
			if( msg.what == 0 )
			{
				// ���سɹ�
				if( applists.size() == 0 )
				{
					// û������
					animation.cancel();
					ivloading.setAnimation(null);
					ivloading.setVisibility(View.GONE);
					tvloading.setVisibility(View.VISIBLE);
					tvloading.setText(getActivity().getResources().getString(R.string.market_get_list_failed));
					isloading = false;
				}
				else
				{
					animation.cancel();
					ivloading.setAnimation(null);
					ivloading.setVisibility(View.GONE);
					emarket_list.setAdapter(adapter);
					emarket_list.setVisibility(View.VISIBLE);
					isloading = false;
				}
			}
			else
			{
				animation.cancel();
				ivloading.setAnimation(null);
				ivloading.setVisibility(View.GONE);
				tvloading.setVisibility(View.VISIBLE);
				tvloading.setText(getActivity().getResources().getString(R.string.market_get_list_failed));
				isloading = false;
			}
		}
	};	
	/**/
	public class EMarketAdapter extends BaseAdapter {

		private List<AppInfo> applists;
		private ImageCallBack icb;
		
	public EMarketAdapter(Context cxt,List<AppInfo> lists,ImageCallBack icb){
		applists = lists;
		this.icb = icb;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return applists.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if(convertView == null){
			holder = new ViewHolder();
			convertView = View.inflate(getActivity(), R.layout.emarket_item, null);
			holder.appname = (TextView) convertView.findViewById(R.id.emarketapp_name);
			holder.iv_icon = (ImageView) convertView.findViewById(R.id.emarketapp_iv);
			holder.version = (TextView) convertView.findViewById(R.id.emarketapp_version);
			holder.description = (TextView) convertView.findViewById(R.id.emarketapp_description);
			holder.btn_install = (Button) convertView.findViewById(R.id.emarket_install);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		final AppInfo appinfo = applists.get(position);
		holder.appname.setText(appinfo.getAppName());
		holder.iv_icon.setTag(appinfo.getAppName());
		Bitmap bm = ImageCache.getInstance().get(appinfo.getAppName());
		String imgUrl = appinfo.getIcon();
		if (bm != null) {
			holder.iv_icon.setImageBitmap(bm);
		} else {
			holder.iv_icon.setImageResource(R.drawable.defualt);
			if (icb != null) {
				new ImageDownload(icb).execute(imgUrl, appinfo.getAppName());
			}
		}
		
		holder.btn_install.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//����Ѿ���¼���ӷ������˻�ȡ���б�
				if(!TextUtils.isEmpty(GlobalParams.userid)){//����Ѿ���¼
					Log.i("userid", "userid"+GlobalParams.userid);
					CustomUI.showtips(getActivity(), R.string.market_tips_get_info);
					final String getdeviceurl = WAPI.addUseridParams(GlobalParams.userid);
					new Thread(){
						public void run() {
							int ret = 1;
							String content = WAPI.http_get_content(getdeviceurl);
							devicelists = new ArrayList<DeviceInfo>();
							ret = WAPI.parseDeviceList(content, devicelists);
							Message msg = Message.obtain();
							msg.what = ret;
							msg.obj = Integer.valueOf(appinfo.getId());
							handler.sendMessage(msg);
						};
					}.start();
			}
				//���û�е�¼�����ж�����״̬�������������������򿪵�¼���棬������ʾ�û����������쳣
				else{
					if (MyUtils.isNetworkConnect(getActivity())) {
						Intent intent = new Intent(getActivity(), UserLogin.class);
						startActivityForResult(intent, REQUSET_LOGIN);
						getActivity().overridePendingTransition(R.anim.push_up_in,
								R.anim.push_no_move);
					} else {
						Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.setting_please_connect_network), Toast.LENGTH_LONG)
								.show();
					}
				}
			}
			
		});
		holder.keyId = appinfo.getId();
		holder.version.setText(getActivity().getResources().getString(R.string.version)+" "+appinfo.getAppVersion());
		holder.description.setText(appinfo.getDescription());
		return convertView;
	}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUSET_LOGIN && resultCode == Activity.RESULT_OK) {
			setfragment = (SetFragment) fm.findFragmentById(R.id.set_tab);
			setfragment.setData();
			setfragment.adapter.notifyDataSetChanged();
		}
	}
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == 0){
			//	Toast.makeText(context, "��ȡ���ݳɹ�", 0).show();
				if(CustomUI.tipdialog!=null)
				CustomUI.tipdialog.dismiss();
				if(devicelists.size()==0){
					//������б�Ϊ�գ������ֻ�ң����û�����ӵ��豸����ô���������豸����ʾ��
					if(GlobalParams.IP == null){
						CustomUI.showBindTipDialog(getActivity());
					}else{
						//������б�Ϊ�գ����ֻ�ң�����Ѿ����ӵ��豸����ô�����󶨵�ǰ�����ӵ��豸����ʾ��
						bindToDeviceDialog(GlobalParams.DEVICEID,GlobalParams.DEVICENAME);
					}
				}else{
					//������б�Ϊ�գ���ô���б�ͨ��listviewչ�ֳ���
					getdevicelist = new GetDeviceList(getActivity(),devicelists);
					getdevicelist.getBindListDialog((Integer)msg.obj);
				}/**/
			}else{
				if(CustomUI.tipdialog!=null)
					CustomUI.tipdialog.dismiss();
				CustomUI.showtips(getActivity(), R.string.market_get_bind_list_failed,2);
				
			}
		};
	};
	
	class GetHttpContentAsyncTask extends AsyncTask<String,Integer,String>{

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			return WAPI.http_get_content(params[0]);
		}  
		
		@Override  
		protected void onPostExecute(String str) {  
		           
	    } 

	}
	
	public void bindToDeviceDialog(final String deviceid,final String devicename){
		AlertDialog.Builder builder = new Builder(getActivity());
    	builder.setMessage(getActivity().getResources().getString(R.string.market_tips_not_bind_any));
    	builder.setPositiveButton(getActivity().getResources().getString(R.string.market_btn_bind),new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which)
    	    {
    	     dialog.dismiss();
    	     String addbinddeviceurl = WAPI.addBindDeviceParams(GlobalParams.userid, deviceid, devicename);
    	     Log.v("newbinddevice", addbinddeviceurl);
    	     GetHttpContentAsyncTask getcontentasy = new GetHttpContentAsyncTask();
    	     String content =null;
    	     try {
 				content = getcontentasy.execute(addbinddeviceurl).get();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
     	     if(content!=null && (WAPI.parseBindDevice(content)==0))
     	    		 CustomUI.showtips(getActivity(),R.string.market_bind_success,2);
     	     else
     	    		 CustomUI.showtips(getActivity(),R.string.market_bind_failed,2);
     	   }
     	   });
    	builder.setNegativeButton(getActivity().getResources().getString(R.string.market_btn_cancel),new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which)
    	    {
    	     dialog.dismiss();

    	   }
    	   });
    	AlertDialog  dialog = builder.create();
    	dialog.show();
	}
	
	class ViewHolder{
		private Button btn_install;
		private int keyId;
		private ImageView iv_icon;
		private TextView appname;
		private TextView version;
		private TextView description;
	}
	
	
}

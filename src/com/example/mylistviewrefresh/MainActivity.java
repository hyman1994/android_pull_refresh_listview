package com.example.mylistviewrefresh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.mylistviewfresh.R;
import com.example.mylistviewrefresh.RefreshListView.IRefreshListener;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity  implements IRefreshListener {
	private RefreshListView listView;
	private SimpleAdapter simple_adapter;
	private List<Map<String, Object>> list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (RefreshListView) findViewById(R.id.listview);
		iniData();   //��ʼ�����ݣ����Ǹ�����20��Item
		// ����SimpleAdapter������
		/**
		 * SimpleAdapter����������ĺ��壺
		 * ��һ����context������
		 * �ڶ�����������ʾ�����ݣ�map��list
		 * ��������Item�Ĳ��֣��������Զ�����Ǹ��ļ�
		 * ���ĸ�����ڶ�������������ϵ��������������ϵ������map�еļ�ֵ
		 * ����������ǿ�����id��int���ͣ������飬���������Ķ������������ģ��������Լ��ڲ����ļ��ж���ģ����ǵĶ��߿��Իع�ͷȥ��һ��
		 * �⼸�����������������ܲ�֪���Ǹ���ģ������Ҿ���������һ���ͦ������ˡ�
		 */
		simple_adapter = new SimpleAdapter(MainActivity.this, list,
				R.layout.listview_item, new String[] { "image", "text" },
				new int[] { R.id.image, R.id.text });
		//����������
		listView.setAdapter(simple_adapter);
		//���ø������ݵĽӿ�
        listView.setInterface(this);
	}

	// ��ʼ��SimpleAdapter���ݼ�
	private List<Map<String, Object>> iniData() {
		list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < 20; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			//��������������ݣ�key��ӦSimpleAdapter�ĵ��������������붼�������ǡ�ֵ��Ӧ������������ֱ���ͼƬ������
			map.put("text", i);
			map.put("image", R.drawable.ic_launcher);
			list.add(map);
		}

		return list;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
	 * �ӿڻص�����RefreshListView�п��Ե��ô˷��������������
	 */
	@Override
	public void onRefresh() {
		// TODO �Զ����ɵķ������
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("text", "������� ");
				map.put("image", R.drawable.ic_launcher);
				list.add(0, map);
				listView.setAdapter(simple_adapter);
				simple_adapter.notifyDataSetChanged();
				listView.refreshComplete();
			}
		}, 2000);
	}

}

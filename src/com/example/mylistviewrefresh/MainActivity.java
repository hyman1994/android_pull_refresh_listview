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
		iniData();   //初始化数据，我们给它加20条Item
		// 设置SimpleAdapter监听器
		/**
		 * SimpleAdapter的五个参数的含义：
		 * 第一个：context上下文
		 * 第二个：用于显示的数据，map的list
		 * 第三个：Item的布局，即我们自定义的那个文件
		 * 第四个：与第二个参数紧密联系，与第五个紧密联系，是在map中的键值
		 * 第五个：我们看到是id（int类型）的数组，这个数组里的东西是哪里来的？是我们自己在布局文件中定义的，忘记的读者可以回过头去看一下
		 * 这几个参数独立开来可能不知道是干吗的，但是我觉得联合在一起就挺好理解了。
		 */
		simple_adapter = new SimpleAdapter(MainActivity.this, list,
				R.layout.listview_item, new String[] { "image", "text" },
				new int[] { R.id.image, R.id.text });
		//设置适配器
		listView.setAdapter(simple_adapter);
		//设置更新数据的接口
        listView.setInterface(this);
	}

	// 初始化SimpleAdapter数据集
	private List<Map<String, Object>> iniData() {
		list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < 20; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			//解释下这里的数据，key对应SimpleAdapter的第三个参数，必须都包含它们。值对应第五个参数，分别是图片和文字
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
	 * 接口回调，在RefreshListView中可以调用此方法进行数据添加
	 */
	@Override
	public void onRefresh() {
		// TODO 自动生成的方法存根
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("text", "滚动添加 ");
				map.put("image", R.drawable.ic_launcher);
				list.add(0, map);
				listView.setAdapter(simple_adapter);
				simple_adapter.notifyDataSetChanged();
				listView.refreshComplete();
			}
		}, 2000);
	}

}

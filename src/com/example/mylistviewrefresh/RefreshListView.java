package com.example.mylistviewrefresh;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.example.mylistviewfresh.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class RefreshListView extends ListView implements OnScrollListener {
	View header;// 顶部布局文件；
	int headerHeight;// 顶部布局文件的高度；
	int firstVisibleItem;// 当前第一个可见的item的位置；
	int scrollState;// listview 当前滚动状态；
	boolean isRemark;// 标记，当前是在listview最顶端摁下的；
	int startY;// 摁下时的Y值；

	int state;// 当前的状态；
	final int NONE = 0;// 正常状态；
	final int PULL = 1;// 提示下拉状态；
	final int RELEASE = 2;// 提示释放状态；
	final int REFRESHING = 3;// 刷新状态；
	IRefreshListener iRefreshListener;//刷新数据的接口
	public RefreshListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initView(context);
	}

	/**
	 * 初始化界面，添加顶部布局文件到 listview
	 * 
	 * @param context
	 */
	private void initView(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		header = inflater.inflate(R.layout.header, null);
		measureView(header);
		headerHeight = header.getMeasuredHeight();
		Log.i("tag", "headerHeight = " + headerHeight);
		topPadding(-headerHeight);
		this.addHeaderView(header);
		this.setOnScrollListener(this);
	}

	/**
	 * 通知父布局，占用的宽，高；
	 * 
	 * @param view
	 */
	private void measureView(View view) {
		ViewGroup.LayoutParams p = view.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int width = ViewGroup.getChildMeasureSpec(0, 0, p.width);
		int height;
		int tempHeight = p.height;
		if (tempHeight > 0) {
			height = MeasureSpec.makeMeasureSpec(tempHeight,
					MeasureSpec.EXACTLY);
		} else {
			height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		view.measure(width, height);
	}

	/**
	 * 设置header 布局 上边距；
	 * 
	 * @param topPadding
	 */
	private void topPadding(int topPadding) {
		header.setPadding(header.getPaddingLeft(), topPadding,
				header.getPaddingRight(), header.getPaddingBottom());
		header.invalidate();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		this.firstVisibleItem = firstVisibleItem;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		this.scrollState = scrollState;
	}
	
	/**
	 * 对屏幕触摸的监控，
	 * 先判断当前是否是在顶端。如果是在最顶端，记录下你开始滑动的Y值
	 * 然后在滑动过程中（监听到的是ACTION_MOVE)，不断地判断当前滑动的范围是否到达应该刷新的程度。
	 * (根据当前的Y-之前的startY的值 与我们的控件的高度之间关系来判断）
	 * 然后在监听到手指松开时，根据当前的状态（我们在onmove（）中计算的），做相应的操作。
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (firstVisibleItem == 0) {
				isRemark = true;
				startY = (int) ev.getY();
			}
			break;

		case MotionEvent.ACTION_MOVE:
			onMove(ev);
			break;
		case MotionEvent.ACTION_UP:
			if (state == RELEASE) {
				//即提示松开刷新的状态，一旦松开，进入到正在刷新；这时候就可以加载数据了！
				state = REFRESHING;
				// 加载最新数据；
				refreshViewByState();
				iRefreshListener.onRefresh();
			} else if (state == PULL) {
				//提示下拉状态状态，如果放掉的话，把一切还原，什么都没有做
				state = NONE;
				isRemark = false;
				refreshViewByState();
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 判断移动过程操作：
	 * 如果不是顶端，不需要做任何的操作
	 * 否则就获取当前的Y值，与开始的Y值做比较。
	 * 判断下拉的高度，与我们定义的一些临界值做判断（其实这个临界值你可以自己定义）
	 * 
	 * @param ev
	 */
	private void onMove(MotionEvent ev) {
		if (!isRemark) {
			return;
		}
		int tempY = (int) ev.getY();
		int space = tempY - startY;
		int topPadding = space - headerHeight;
		switch (state) {
		case NONE:
			if (space > 0) {   
				state = PULL;  //正在下拉
				refreshViewByState();
			}
			break;
		case PULL:
			topPadding(topPadding);
			//如果大于一定高度，并且滚动状态是正在滚动时，就到了松开可以刷新的状态
			if (space > headerHeight + 30
					&& scrollState == SCROLL_STATE_TOUCH_SCROLL) {
				state = RELEASE;
				refreshViewByState();
			}
			break;
		case RELEASE:
			topPadding(topPadding);
			//在提示松开刷新时，如果你往上拖，距离小于一定高度时，提示下拉可以刷新
			if (space < headerHeight + 30) {  
				state = PULL;
				refreshViewByState();
			} 
			break;
		}
	}

	/**
	 * 根据当前状态，改变界面显示；
	 */
	private void refreshViewByState() {
		//如果要提高性能，这些应该在oncreate中写，但是。。那里面参数太多了，为了大家读代码更舒服，就写在这里了。
		TextView tip = (TextView) header.findViewById(R.id.tip);
		ImageView arrow = (ImageView) header.findViewById(R.id.arrow);
		ProgressBar progress = (ProgressBar) header.findViewById(R.id.progress);
		RotateAnimation anim = new RotateAnimation(0, 180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim.setDuration(500);
		anim.setFillAfter(true);
		RotateAnimation anim1 = new RotateAnimation(180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim1.setDuration(500);
		anim1.setFillAfter(true);
		switch (state) {
		case NONE:     //正常状态不显示
			arrow.clearAnimation();
			topPadding(-headerHeight);
			break;

		case PULL:     //下拉状态显示箭头，隐藏进度条，以下的状态也类似。自己根据实际情况去修改。
			arrow.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			tip.setText("下拉可以刷新！");
			arrow.clearAnimation();
			arrow.setAnimation(anim1);
			break;
		case RELEASE:  
			arrow.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			tip.setText("松开可以刷新！");
			arrow.clearAnimation();
			arrow.setAnimation(anim);
			break;
		case REFRESHING:
			topPadding(50);
			arrow.setVisibility(View.GONE);
			progress.setVisibility(View.VISIBLE);
			tip.setText("正在刷新...");
			arrow.clearAnimation();
			break;
		}
	}

	/**
	 * 获取完数据之后
	 */
	public void refreshComplete() {
		state = NONE;
		isRemark = false;
		refreshViewByState();
		TextView lastupdatetime = (TextView) header
				.findViewById(R.id.lastupdate_time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		String time = format.format(date);
		lastupdatetime.setText(time);
	}
	
	public void setInterface(IRefreshListener iRefreshListener){
		this.iRefreshListener = iRefreshListener;
	}
	
	/**
	 * 刷新数据接口
	 * @author Administrator
	 */
	public interface IRefreshListener{
		public void onRefresh();
	}
}

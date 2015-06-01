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
	View header;// ���������ļ���
	int headerHeight;// ���������ļ��ĸ߶ȣ�
	int firstVisibleItem;// ��ǰ��һ���ɼ���item��λ�ã�
	int scrollState;// listview ��ǰ����״̬��
	boolean isRemark;// ��ǣ���ǰ����listview������µģ�
	int startY;// ����ʱ��Yֵ��

	int state;// ��ǰ��״̬��
	final int NONE = 0;// ����״̬��
	final int PULL = 1;// ��ʾ����״̬��
	final int RELEASE = 2;// ��ʾ�ͷ�״̬��
	final int REFRESHING = 3;// ˢ��״̬��
	IRefreshListener iRefreshListener;//ˢ�����ݵĽӿ�
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
	 * ��ʼ�����棬��Ӷ��������ļ��� listview
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
	 * ֪ͨ�����֣�ռ�õĿ��ߣ�
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
	 * ����header ���� �ϱ߾ࣻ
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
	 * ����Ļ�����ļ�أ�
	 * ���жϵ�ǰ�Ƿ����ڶ��ˡ����������ˣ���¼���㿪ʼ������Yֵ
	 * Ȼ���ڻ��������У�����������ACTION_MOVE)�����ϵ��жϵ�ǰ�����ķ�Χ�Ƿ񵽴�Ӧ��ˢ�µĳ̶ȡ�
	 * (���ݵ�ǰ��Y-֮ǰ��startY��ֵ �����ǵĿؼ��ĸ߶�֮���ϵ���жϣ�
	 * Ȼ���ڼ�������ָ�ɿ�ʱ�����ݵ�ǰ��״̬��������onmove�����м���ģ�������Ӧ�Ĳ�����
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
				//����ʾ�ɿ�ˢ�µ�״̬��һ���ɿ������뵽����ˢ�£���ʱ��Ϳ��Լ��������ˣ�
				state = REFRESHING;
				// �����������ݣ�
				refreshViewByState();
				iRefreshListener.onRefresh();
			} else if (state == PULL) {
				//��ʾ����״̬״̬������ŵ��Ļ�����һ�л�ԭ��ʲô��û����
				state = NONE;
				isRemark = false;
				refreshViewByState();
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * �ж��ƶ����̲�����
	 * ������Ƕ��ˣ�����Ҫ���κεĲ���
	 * ����ͻ�ȡ��ǰ��Yֵ���뿪ʼ��Yֵ���Ƚϡ�
	 * �ж������ĸ߶ȣ������Ƕ����һЩ�ٽ�ֵ���жϣ���ʵ����ٽ�ֵ������Լ����壩
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
				state = PULL;  //��������
				refreshViewByState();
			}
			break;
		case PULL:
			topPadding(topPadding);
			//�������һ���߶ȣ����ҹ���״̬�����ڹ���ʱ���͵����ɿ�����ˢ�µ�״̬
			if (space > headerHeight + 30
					&& scrollState == SCROLL_STATE_TOUCH_SCROLL) {
				state = RELEASE;
				refreshViewByState();
			}
			break;
		case RELEASE:
			topPadding(topPadding);
			//����ʾ�ɿ�ˢ��ʱ������������ϣ�����С��һ���߶�ʱ����ʾ��������ˢ��
			if (space < headerHeight + 30) {  
				state = PULL;
				refreshViewByState();
			} 
			break;
		}
	}

	/**
	 * ���ݵ�ǰ״̬���ı������ʾ��
	 */
	private void refreshViewByState() {
		//���Ҫ������ܣ���ЩӦ����oncreate��д�����ǡ������������̫���ˣ�Ϊ�˴�Ҷ�������������д�������ˡ�
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
		case NONE:     //����״̬����ʾ
			arrow.clearAnimation();
			topPadding(-headerHeight);
			break;

		case PULL:     //����״̬��ʾ��ͷ�����ؽ����������µ�״̬Ҳ���ơ��Լ�����ʵ�����ȥ�޸ġ�
			arrow.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			tip.setText("��������ˢ�£�");
			arrow.clearAnimation();
			arrow.setAnimation(anim1);
			break;
		case RELEASE:  
			arrow.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			tip.setText("�ɿ�����ˢ�£�");
			arrow.clearAnimation();
			arrow.setAnimation(anim);
			break;
		case REFRESHING:
			topPadding(50);
			arrow.setVisibility(View.GONE);
			progress.setVisibility(View.VISIBLE);
			tip.setText("����ˢ��...");
			arrow.clearAnimation();
			break;
		}
	}

	/**
	 * ��ȡ������֮��
	 */
	public void refreshComplete() {
		state = NONE;
		isRemark = false;
		refreshViewByState();
		TextView lastupdatetime = (TextView) header
				.findViewById(R.id.lastupdate_time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy��MM��dd�� hh:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		String time = format.format(date);
		lastupdatetime.setText(time);
	}
	
	public void setInterface(IRefreshListener iRefreshListener){
		this.iRefreshListener = iRefreshListener;
	}
	
	/**
	 * ˢ�����ݽӿ�
	 * @author Administrator
	 */
	public interface IRefreshListener{
		public void onRefresh();
	}
}

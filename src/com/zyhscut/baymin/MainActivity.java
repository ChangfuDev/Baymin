package com.zyhscut.baymin;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.security.auth.PrivateCredentialPermission;

import org.json.JSONException;
import org.json.JSONObject;

import android.R.interpolator;
import android.R.string;
import android.app.Activity;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts.Data;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

public class MainActivity extends Activity implements HttpGetDataListener, android.view.View.OnClickListener{

	private HttpData httpData;
	private List<ListData> lists;
	private ListView lv;
	private EditText sendText;
	private Button send_btn;
	private Button voice_btn;
	private String content_str;
	private TextAdapter adapter;
	private String[] welcome_array;
	private double currentTime, oldTime = 0;
	private RecognizerDialog mIatDialog;
	private SpeechSynthesizer mTts;
	private String TAG = "shitou";
	private String voicer="vinn";
	// �������
	private int mPercentForBuffering = 0;
	// ���Ž���
	private int mPercentForPlaying = 0;
	// ��HashMap�洢��д���
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
		lv = (ListView) findViewById(R.id.lv);
		sendText = (EditText) findViewById(R.id.sendText);
		send_btn = (Button) findViewById(R.id.send_btn);
		voice_btn = (Button) findViewById(R.id.voice_btn); 
    	lists = new ArrayList<ListData>();
    	send_btn.setOnClickListener(this);
    	voice_btn.setOnClickListener(this);
    	adapter = new TextAdapter(lists, this);
    	lv.setAdapter(adapter);
    	String randomWelcomeTips = getRandomWelcomeTips();
    	ListData listData;
    	listData = new ListData(randomWelcomeTips, ListData.RECEIVER,getTime());
    	lists.add(listData);
    	
    	SpeechUtility.createUtility(this, "appid=557aceb9");
    	
    	mIatDialog = new RecognizerDialog(this, mInitListener);
    	setIatParam();
    	
    	mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
    	setTtsParam();
		mTts.startSpeaking(randomWelcomeTips, mTtsListener);
	}
    
    private String getRandomWelcomeTips() {
		String welcome_tip = null;
		welcome_array = this.getResources().getStringArray(R.array.welcome_tips);
		int index = (int) (Math.random()*(welcome_array.length-1));
		welcome_tip = welcome_array[index];
		return welcome_tip;
	}
    
	@Override
	public void getDataUrl(String data) {
		//System.out.println(data);
		parseText(data);
	}

	public void parseText(String str) {
		try {
			JSONObject jb = new JSONObject(str);
//			System.out.println(jb.getString("code"));
//			System.out.println(jb.getString("text"));
			String text = jb.getString("text");
			ListData listData;
			listData = new ListData(text,ListData.RECEIVER,getTime());
			lists.add(listData);
			adapter.notifyDataSetChanged();
    		mTts.startSpeaking(text, mTtsListener);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}

	@Override
	public void onClick(View arg0) {
		content_str = "";
		if (arg0.getId() == R.id.send_btn) {
			content_str = sendText.getText().toString();
			sendText.setText("");
			sendContent(content_str);
		} 
		if (arg0.getId() == R.id.voice_btn) {
			mIatDialog.setListener(recognizerDialogListener);
			mIatDialog.show();
//			int ret = mIat.startListening(recognizerListener);
//			Log.d(TAG, "SpeechRecognizer ret:" + ret);
		}	
	}
	
	private String getTime() {
		currentTime = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("MM��dd�� hh:mm");
		Date curDate = new Date((long) currentTime);
		String str = format.format(curDate);
		if (currentTime - oldTime >= 300000) {
			oldTime = currentTime;
			return str;
		}else {
			return "";
		}
		
	}
	
	private void sendContent(String content_str) {
		if (!content_str.equals("")) {
			String dropk = content_str.replace(" ", "");
			String droph = dropk.replace("\n", "");
			ListData listData;
			listData = new ListData(content_str,ListData.SEND,getTime());
			lists.add(listData);
			if (lists.size()>30) {
				for (int i = 0; i < 10; i++) {
					lists.remove(0);
				}
			}
			adapter.notifyDataSetChanged();
			httpData = (HttpData) new HttpData(
					"http://www.tuling123.com/openapi/api?key=8f57ba49bb7d13c44599c8b0cd430773&info="+droph, 
					this).execute();
			}
	}
	
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
		}
	};
	
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        	} else {
				// ��ʼ���ɹ���֮����Ե���startSpeaking����
        		// ע���еĿ�������onCreate�����д�����ϳɶ���֮�����Ͼ͵���startSpeaking���кϳɣ�
        		// ��ȷ�������ǽ�onCreate�е�startSpeaking������������
			}		
		}
	};
	
	/**
	 * ��������
	 * 
	 * @param param
	 * @return
	 */
	public void setIatParam() {
		mIatDialog.setParameter(SpeechConstant.DOMAIN, "iat");
		mIatDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		// ������������
		mIatDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
	}
	
	private void setTtsParam(){
		// ���÷�����
		mTts.setParameter(SpeechConstant.VOICE_NAME,voicer);
		//���úϳ�����
		mTts.setParameter(SpeechConstant.SPEED,"80");
		//���úϳ�����
		mTts.setParameter(SpeechConstant.VOLUME,"100");
		//���ò�������Ƶ������
		mTts.setParameter(SpeechConstant.STREAM_TYPE,"3");
		// ���ò��źϳ���Ƶ������ֲ��ţ�Ĭ��Ϊtrue
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
	}
	
	/**
	 * ��д��������
	 */
//	private RecognizerListener recognizerListener = new RecognizerListener() {
//
//		@Override
//		public void onBeginOfSpeech() {
//		}
//
//		@Override
//		public void onError(SpeechError error) {
//		}
//
//		@Override
//		public void onEndOfSpeech() {
//		}
//
//		@Override
//		public void onResult(RecognizerResult results, boolean isLast) {
//			Log.d(TAG, results.getResultString());
//			collectResult(results);
//
//			if (isLast) {
//				// TODO ���Ľ��
//				printResult(mIatResults);
//			}
//		}
//
//		@Override
//		public void onVolumeChanged(int volume) {
//		}
//
//		@Override
//		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
//		}
//	};
	
	/**
	 * ��дUI������
	 */
	private RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
			collectResult(results);

			if (isLast) {
				// TODO ���Ľ��
				printResult(mIatResults);
			}
		}

		/**
		 * ʶ��ص�����.
		 */
		public void onError(SpeechError error) {
		}

	};
	
	private void collectResult(RecognizerResult results) {
		String text = JsonParser.parseIatResult(results.getResultString());

		String sn = null;
		// ��ȡjson����е�sn�ֶ�
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, text);
		
	}
	
	private void printResult(HashMap<String, String> mIatResults) {
		
		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
			}
		content_str = resultBuffer.toString();
		sendContent(content_str);
		mIatResults.clear();
		
	}
	
	/**
	 * �ϳɻص�������
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		@Override
		public void onSpeakBegin() {
		}

		@Override
		public void onSpeakPaused() {
		}

		@Override
		public void onSpeakResumed() {
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
			// �ϳɽ���
			mPercentForBuffering = percent;
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// ���Ž���
			mPercentForPlaying = percent;
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error == null) {
			} else if (error != null) {
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			
		}
	};
}

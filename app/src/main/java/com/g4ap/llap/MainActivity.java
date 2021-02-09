package com.g4ap.llap;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
	private COSLLBrowser cosLLBrowser;
	private MyCOSLLAdapter cosLLAdapter;
	private Service_MusicPlayer m_Service;
	
	private TextView m_tvPlayingFilename;
	private ListView m_lvLiveLib;
	private ImageView m_ivCover;
	private TextView m_tvBarPos;
    private SeekBar m_sbSeekBar;

    private Button m_btnPlayPause;
    private Button m_btnSeekMinus;
    private Button m_btnSeekPlus;
    private Button m_btnSeekTop;
    private Button m_btnPlayNext;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// bind service
		m_Service = null;
        Intent intent = new Intent( this, Service_MusicPlayer.class );
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}

    ServiceConnection conn = new ServiceConnection()
    {
        @Override
        public void onServiceDisconnected(ComponentName name) {}
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            // sync var
            m_Service = ((Service_MusicPlayer.Service_MusicPlayerBinder)service).getService();

            AsyncTask<String,Void,String> syncTaskInitUI = new AsyncTask<String, Void, String>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected String doInBackground( String... arg ) {

					cosLLBrowser = new COSLLBrowser();
					try {
						cosLLBrowser.Init( getApplicationContext() );
					} catch (Exception e) {
						e.printStackTrace();
					}

					m_Service.init( getApplicationContext(), cosLLBrowser );

					cosLLAdapter = new MyCOSLLAdapter( getApplicationContext(), cosLLBrowser, m_Service );

                    return "ok";
                }

                @Override
                protected void onPostExecute( String ret ) {
                    // setup ui
                    initView();
                    // start update thread
                    handler.post(updateThread);

					Toast toast = Toast.makeText(MainActivity.this,"init done",Toast.LENGTH_SHORT);
					toast.show();
                }
            };
            syncTaskInitUI.execute();
        }
    };
	
	private void initView() {

		m_tvPlayingFilename = (TextView)findViewById(R.id.act_main_curpath);
		m_lvLiveLib = (ListView)findViewById(R.id.act_main_listview);
		m_ivCover = (ImageView)findViewById(R.id.act_main_imageview);
		m_ivCover.setImageAlpha(80);
		m_tvBarPos = (TextView)findViewById(R.id.act_main_barpos);
	    m_sbSeekBar = (SeekBar)findViewById(R.id.act_main_seekbar);
	    

		m_btnPlayPause = (Button)findViewById(R.id.act_main_playpause);
	    m_btnSeekMinus = (Button)findViewById(R.id.act_main_seekminus);
	    m_btnSeekPlus = (Button)findViewById(R.id.act_main_seekplus);
	    m_btnSeekTop = (Button)findViewById(R.id.act_main_seektop);
		m_btnPlayNext = (Button)findViewById(R.id.act_main_playnext);

		m_lvLiveLib.setAdapter(cosLLAdapter);
		m_lvLiveLib.setOnItemClickListener(cosLLAdapter);

        m_btnPlayPause.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				m_Service.playPause();
			}
		});
        m_btnSeekMinus.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				int nPos = m_Service.getCurPos();
				if ( nPos > 0 ) {
					nPos = ((nPos-15000)>0)?nPos-15000:0;
					m_Service.seekTo( nPos );
				}
			}
		});
        m_btnSeekPlus.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				int nPos = m_Service.getCurPos();
				if ( (nPos+15000) < m_Service.getMaxPos() ) {
					m_Service.seekTo( nPos + 15000 );
				}
			}
		});
        m_btnSeekTop.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				int nPos = m_Service.getCurPos();
				if ( nPos > 0 ) {
					m_Service.seekTo( 0 );
					cosLLBrowser.incCurPlayTimes();
				}
			}
		});
        m_btnPlayNext.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View arg0) {
				m_Service.playNext();
			}
		});
		
		m_sbSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser == true) {
					m_Service.seekTo(progress);
				}
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(updateThread);
		unbindService(conn);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		else if ( id == R.id.action_addfav ) {
			//m_MyLLAdapter.m_MyBrowser.addCurToFav( m_Service.getPlayFileRealPath() + "/" + m_Service.getPlayFilename() );
			return true;
		}
		else if ( id == R.id.action_delcachefile ) {
			cosLLBrowser.DelCurPlayingFileCache();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if ( cosLLAdapter.gotoUpperDir() == 1 ) {
				return true;
			} else {
				cosLLBrowser.close();
				finish();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	


	private llObjectNode lastPlayingNode = null;
	private ArrayList<Bitmap> m_CoverList = null;
	private int m_CurCover = 0;
	private int m_CoverTimer = 0;

	Handler handler = new Handler();
	Runnable updateThread = new Runnable() {
		@Override
		public void run() {

			if ( cosLLBrowser.playingNode != null && cosLLBrowser.playingNode != lastPlayingNode ) {

				lastPlayingNode = cosLLBrowser.playingNode;
				m_tvPlayingFilename.setText( lastPlayingNode.key );
				cosLLAdapter.notifyUpdate();

				AsyncTask<String,Void,ArrayList<Bitmap>> syncLoadCover = new AsyncTask<String, Void, ArrayList<Bitmap>>() {
					@Override
					protected void onPreExecute() {
						super.onPreExecute();
					}
					@Override
					protected ArrayList<Bitmap> doInBackground( String... arg ) {
						ArrayList<Bitmap> ret = m_CoverList = cosLLBrowser.getCoverImage( lastPlayingNode );
						return ret;
					}
					@Override
					protected void onPostExecute( ArrayList<Bitmap> ret ) {
						m_CoverList = ret;
						if ( m_CoverList != null && m_CoverList.size() > 0  ) {
							m_ivCover.setImageBitmap(m_CoverList.get(0));
						} else {
							m_ivCover.setImageBitmap(null);
						}
					}
				};
				syncLoadCover.execute();
			}

			if ( m_CoverList != null && m_CoverList.size() > 0 ) {
				m_CoverTimer++;
				if ( m_CoverTimer >= 15 ) {
					m_CoverTimer = 0;
					m_CurCover++;
					if ( m_CurCover > m_CoverList.size()-1 ) {
						m_CurCover = 0;
					}
					m_ivCover.setImageBitmap( m_CoverList.get(m_CurCover) ); 
				}
			}

			int nCur = m_Service.getCurPos()/1000;
			int nMax = m_Service.getMaxPos()/1000;
			String strPos;
			if ( nMax < 99*60 ) {
				strPos = String.format("%02d:%02d / %02d:%02d", nCur/60, nCur%60, nMax/60, nMax%60 );
			} else {
				strPos = String.format("%03d:%03d / %03d:%03d", nCur/60, nCur%60, nMax/60, nMax%60 );
			}
			m_tvBarPos.setText( strPos );
			
			m_sbSeekBar.setMax( m_Service.getMaxPos() );
			m_sbSeekBar.setProgress( m_Service.getCurPos() );
			handler.postDelayed( updateThread, 200 );
		}
	};
	

}

class MyCOSLLAdapter extends BaseAdapter implements OnItemClickListener {

	private Context myContext;
	private COSLLBrowser cosLLBrowser;
	private Service_MusicPlayer playerService;

	private int colorSel = Color.rgb(128, 128, 128);
	private int colorNor = Color.rgb(0, 0, 0);

	public MyCOSLLAdapter( Context context, COSLLBrowser brower, Service_MusicPlayer service ) {
		myContext = context;
		cosLLBrowser = brower;
		playerService = service;
	}

	public int gotoUpperDir() {
		int ret = cosLLBrowser.gotoUpperDir();
		notifyDataSetChanged();
		return ret;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if ( arg2 == 0 ) {
			cosLLBrowser.gotoUpperDir();
            notifyDataSetChanged();
			return;
		}

		llClickType ret = cosLLBrowser.onItemClick( arg2-1 );
		switch ( ret ) {
			case FOLDER:
				break;
			case AUDIO_SAMEDIR:
			case AUDIO_DIFFDIR:
				playerService.playNewAudioFile();
				break;
			case VEDIO_SAMEDIR:
			case VEDIO_DIFFDIR:
				playerService.playNewVideoFile();
				break;
		}
		notifyDataSetChanged();
	}

	public void notifyUpdate() {
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if ( cosLLBrowser.browsingNode == null ) return 0;
		return cosLLBrowser.browsingNode.childs.size()+1;
	}

	@Override
	public Object getItem(int position) {
		if ( cosLLBrowser.browsingNode == null ) return null;
		if ( position == 0 ) return null;
		return cosLLBrowser.browsingNode.childs.get(position-1);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if ( convertView == null ) {
			convertView = LayoutInflater.from(myContext).inflate(R.layout.listlayout_ll, parent, false);
		}

		TextView tvFilename = (TextView)convertView.findViewById(R.id.ll_ll_filename);

		String strFileName;
		if ( position == 0 ) {
			strFileName = "...";
		}
		else {
			strFileName = cosLLBrowser.browsingNode.childs.get(position-1).name;
		}
		tvFilename.setText( strFileName );

		int curColor = colorNor;
		llObjectNode playingNode = cosLLBrowser.playingNode;
		if ( playingNode != null && playingNode.name.equals(strFileName) ) { curColor = colorSel; }
		tvFilename.setTextColor( curColor );

		return convertView;
	}

}
package com.g4ap.llap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity
{

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
	
	private MyLLAdapter m_MyLLAdapter;
	

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
	
	
	private void initView()
	{

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
	

		m_MyLLAdapter = new MyLLAdapter();
        m_MyLLAdapter.setCurDir( "default" );
        m_lvLiveLib.setAdapter( m_MyLLAdapter );
        m_lvLiveLib.setOnItemClickListener( m_MyLLAdapter );
        

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
				if ( nPos > 0 )
				{
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
				if ( (nPos+15000) < m_Service.getMaxPos() )
				{
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
				if ( nPos > 0 )
				{
					m_Service.seekTo( 0 );
				}
			}
		});
        m_btnPlayNext.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				m_Service.playNext();
			}
		});
		
		m_sbSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				if (fromUser == true)
				{
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
	protected void onDestroy()
	{
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
		if (id == R.id.action_settings)
		{
			return true;
		}
		else if ( id == R.id.action_addfav )
		{
			m_MyLLAdapter.m_MyBrowser.addCurToFav( m_Service.getPlayFileRealPath() + "/" + m_Service.getPlayFilename() );
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			if ( m_MyLLAdapter.gotoUpperDir() )
			{
				return true;
			}
			else
			{
				finish();
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}
	
	

	
	private class MyLLAdapter extends BaseAdapter implements OnItemClickListener
	{

		private LLBrowser m_MyBrowser;
	    private int colorSel = Color.rgb(128, 128, 128);
	    private int colorNor = Color.rgb(0, 0, 0);
		
		public MyLLAdapter()
		{
			m_MyBrowser = new LLBrowser();
		}

		public void setCurDir( String strDir )
		{
			m_MyBrowser.setCurDir( strDir );
			notifyDataSetChanged();
		}
		
		
		public boolean gotoUpperDir()
		{
			boolean ret = m_MyBrowser.gotoUpperDir();
			if ( ret )
			{
				notifyDataSetChanged();
			}
			
			return ret;
		}
		
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		{
			t_LLItem clickItem = m_MyBrowser.onItemClick( arg2 );
			switch ( clickItem.m_Type )
			{
			case PARENTDIR:
			case DIR:
			case PLDIR:
				notifyDataSetChanged();
				break;

			case FILE:
			case PLFILE:
				if ( clickItem.m_Name.endsWith(".mp3") || clickItem.m_Name.endsWith(".wma") || clickItem.m_Name.endsWith(".wav") || clickItem.m_Name.endsWith(".m4a") ||
						clickItem.m_Name.endsWith(".MP3") || clickItem.m_Name.endsWith(".WMA") || clickItem.m_Name.endsWith(".WAV") || clickItem.m_Name.endsWith(".M4A")
						)
				{
					// need create new playlist
					if ( m_Service.m_PL.m_PlayDir == null || !m_Service.m_PL.m_PlayDir.equals(m_MyBrowser.m_CurDir) )
					{
						LLPlayList newPL = m_MyBrowser.genCurPlayList();
						newPL.m_CurPlay.m_Name = clickItem.m_Name;
						newPL.m_CurPlay.m_RealPath = clickItem.m_RealPath;
						
						m_Service.setNewPlayList( newPL );
						m_Service.startPlayer();
					}
					// play new file in cur playlist
					else
					{
						t_LLPLItem playItem = new t_LLPLItem();
						playItem.m_Name = clickItem.m_Name;
						playItem.m_RealPath = clickItem.m_RealPath;
						
						m_Service.setNewPlayFile( playItem );
						m_Service.startPlayer();
					}
				}
				break;
			}
		}

		@Override
		public int getCount()
		{
			return m_MyBrowser.m_CurList.size();
		}

		@Override
		public Object getItem(int position)
		{
			return m_MyBrowser.m_CurList.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if ( convertView == null )
			{
				convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.listlayout_ll, parent, false);
				//convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.listlayout_lld, parent, false);
			}

			TextView tvFilename = (TextView)convertView.findViewById(R.id.ll_ll_filename);
			//TextView tvFilename = (TextView)convertView.findViewById(R.id.ll_lld_filename);
			String strFileName = m_MyBrowser.m_CurList.get(position).m_Name;
			tvFilename.setText( strFileName );
			
			int curColor = colorNor;
			if ( m_Service != null && m_Service.getPlayDir() != null && m_Service.getPlayFilename() != null )
			{
				if ( m_MyBrowser.m_CurDir.equals(m_Service.getPlayDir()) && strFileName.equals(m_Service.getPlayFilename()) )
				{
					curColor = colorSel;
				}
			}
			tvFilename.setTextColor( curColor );
			
			return convertView;
		}

	}
	
	
	String m_strLastPlay = "none";
	String m_strLastDir = "none";
	ArrayList<Bitmap> m_CoverList = null;
	int m_CurCover = 0;
	int m_CoverTimer = 0;

	Handler handler = new Handler();
	Runnable updateThread = new Runnable()
	{
		public void run()
		{
			if ( m_Service.getPlayDir() != null && m_Service.getPlayFilename() != null && m_Service.getPlayFileRealPath() != null )
			{
				String strCurPlay = m_Service.getPlayDir() + "/" + m_Service.getPlayFilename();
				if ( !strCurPlay.equals(m_strLastPlay) )
				{
					m_strLastPlay = strCurPlay;
					m_tvPlayingFilename.setText( m_strLastPlay );
					m_MyLLAdapter.notifyDataSetChanged();
				}
				
				if ( !m_strLastDir.equals( m_Service.getPlayFileRealPath() ) )
				{
					m_strLastDir = m_Service.getPlayFileRealPath();
					m_CoverList = m_MyLLAdapter.m_MyBrowser.getCoverImage( m_strLastDir );
					m_CurCover = 0;
					m_CoverTimer = 0;
					if ( m_CoverList.size() <= 0 )
					{
						m_ivCover.setImageBitmap(null);
					}
					else
					{
						m_ivCover.setImageBitmap(m_CoverList.get(0)); 
					}
				}
			}
			
			if ( m_CoverList != null && m_CoverList.size() > 0 )
			{
				m_CoverTimer++;
				if ( m_CoverTimer >= 15 )
				{
					m_CoverTimer = 0;
					m_CurCover++;
					if ( m_CurCover > m_CoverList.size()-1 )
					{
						m_CurCover = 0;
					}
					m_ivCover.setImageBitmap( m_CoverList.get(m_CurCover) ); 
				}
			}
			
			
			int nCur = m_Service.getCurPos()/1000;
			int nMax = m_Service.getMaxPos()/1000;
			String strPos;
			if ( nMax < 99*60 )
			{
				strPos = String.format("%02d:%02d / %02d:%02d", nCur/60, nCur%60, nMax/60, nMax%60 );
			}
			else
			{
				strPos = String.format("%03d:%03d / %03d:%03d", nCur/60, nCur%60, nMax/60, nMax%60 );
			}
			m_tvBarPos.setText( strPos );
			
			m_sbSeekBar.setMax( m_Service.getMaxPos() );
			m_sbSeekBar.setProgress( m_Service.getCurPos() );
			handler.postDelayed( updateThread, 200 );
		}
	};
	
	
    ServiceConnection conn = new ServiceConnection()
    {
        @Override  
        public void onServiceDisconnected(ComponentName name) {}  
        @Override  
        public void onServiceConnected(ComponentName name, IBinder service)
        {
        	// sync var
            m_Service = ((Service_MusicPlayer.Service_MusicPlayerBinder)service).getService();

            // setup ui
            initView();

            // update listview
            String strCurDir = m_Service.getPlayDir();
            if ( strCurDir != null )
            {
            	m_MyLLAdapter.setCurDir(strCurDir);
            }

            // start update thread
            handler.post(updateThread);
        }
    };
	
}

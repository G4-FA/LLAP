package com.g4ap.llap;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

public class Service_MusicPlayer extends Service
{
    PlayerState	m_State;
    public enum PlayerState
    {
    	Nil, Initialized, Playing, Pause
    }
	private IBinder binder = new Service_MusicPlayerBinder();
	private MediaPlayer m_mpPlayer;
	private COSLLBrowser cosLLBrowser;

	@Override
	public IBinder onBind(Intent arg0)
	{
		return binder;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		m_mpPlayer = new MediaPlayer();
		m_mpPlayer.setOnCompletionListener( new MyOnCompletionListener() );
		m_State = PlayerState.Nil;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		m_mpPlayer.release();
		m_mpPlayer = null;
		m_State = PlayerState.Nil;
	}

	@Override
	public void onStart(Intent intent, int startId)
	{

	}

	public void init( COSLLBrowser browser )
	{
		cosLLBrowser = browser;
	}


    public void playNewFile()
    {
		m_mpPlayer.reset();
		m_State = PlayerState.Nil;

		AsyncTask<String,Void,String> syncTaskPlayNewFile = new AsyncTask<String,Void,String>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected String doInBackground( String... arg ) {
				String filename = cosLLBrowser.getLocalFile( cosLLBrowser.playingNode.key, cosLLBrowser.playingNode.eTag );
				return filename;
			}

			@Override
			protected void onPostExecute( String filename ) {
				try {
					m_mpPlayer.reset();
					m_mpPlayer.setDataSource( filename );
					m_mpPlayer.prepare();
					m_State = PlayerState.Initialized;
					startPlayer();
				}
				catch(Exception e)  {
					e.printStackTrace();
				}
			}
		};
		syncTaskPlayNewFile.execute();

    }
	
    public void startPlayer()
    {
    	switch ( m_State )
    	{
    	case Nil:
    		return;
    	
    	case Initialized:
    		m_mpPlayer.start();
    		break;

    	case Playing:
    		//if ( m_mpPlayer.getCurrentPosition() + 500 > m_mpPlayer.getDuration() )
    		if ( !m_mpPlayer.isPlaying() )
    		{
    			m_mpPlayer.seekTo(0);
    			m_mpPlayer.start();
    		}
    		break;
    		
    	case Pause:
    		m_mpPlayer.start();
    		break;
    	}
    	
    	m_State = PlayerState.Playing;
    }
    
    public void playPause()
    {
    	if ( m_State == PlayerState.Playing )
    	{
        	m_mpPlayer.pause();
        	m_State = PlayerState.Pause;
    	}
    	else if ( m_State == PlayerState.Pause )
    	{
        	m_mpPlayer.start();
        	m_State = PlayerState.Playing;
    	}
    }
    
    
    public boolean playNext()
    {
    	if ( cosLLBrowser.goToNextMedia() != llClickType.AUDIO_SAMEDIR ) return false;

		m_mpPlayer.reset();
		m_State = PlayerState.Nil;

		AsyncTask<String,Void,String> syncTaskPlayNext = new AsyncTask<String,Void,String>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected String doInBackground( String... arg ) {
				String filename = cosLLBrowser.getLocalFile( cosLLBrowser.playingNode.key, cosLLBrowser.playingNode.eTag );
				return filename;
			}

			@Override
			protected void onPostExecute( String filename ) {
				try {
					m_mpPlayer.reset();
					m_mpPlayer.setDataSource( filename );
					m_mpPlayer.prepare();
					m_State = PlayerState.Initialized;
					startPlayer();
				}
				catch(Exception e)  {
					e.printStackTrace();
				}
			}
		};
		syncTaskPlayNext.execute();
		return true;
    }
    
    
    
    public void seekTo( int pos )
    {
    	if ( m_State == PlayerState.Playing || m_State == PlayerState.Pause )
    	{
    		m_mpPlayer.seekTo(pos);
    	}
    }

    
    
    public int getCurPos()
    {
    	if ( m_State == PlayerState.Playing || m_State == PlayerState.Pause )
    	{
    		return m_mpPlayer.getCurrentPosition();
    	}
    	else
    	{
    		return 0;
    	}
    }
    public int getMaxPos()
    {
    	if ( m_State == PlayerState.Initialized ||
    			m_State == PlayerState.Playing ||
    			m_State == PlayerState.Pause )
    	{
    		return m_mpPlayer.getDuration();
    	}
    	else
    	{
    		return 0;
    	}
    }
    
	public class Service_MusicPlayerBinder extends Binder
	{
		public Service_MusicPlayer getService()
		{
			return Service_MusicPlayer.this;
		}
	}
	
	public class MyOnCompletionListener implements MediaPlayer.OnCompletionListener
	{
		@Override
		public void onCompletion(MediaPlayer mp)
		{
			if ( !playNext() ) {
				m_State = PlayerState.Pause;
			}
		}
		
	}

	
}

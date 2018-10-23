package com.g4ap.llap;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class Service_MusicPlayer extends Service
{
	LLPlayList m_PL;
    PlayerState	m_State;
    public enum PlayerState
    {
    	Nil, Initialized, Playing, Pause
    }
	
	private IBinder binder = new Service_MusicPlayerBinder();
	private MediaPlayer m_mpPlayer;

	@Override
	public IBinder onBind(Intent arg0)
	{
		return binder;
	}


	@Override
	public void onCreate()
	{
		super.onCreate();
		
		m_PL = new LLPlayList();
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
	
	
	
    public void setNewPlayFile( t_LLPLItem playItem )
    {
    	m_mpPlayer.reset();
    	
    	m_PL.m_CurPlay = playItem;
    	
		try
		{
			m_mpPlayer.setDataSource( playItem.m_RealPath + "/" + playItem.m_Name );
			m_mpPlayer.prepare();
		}
		catch( Exception e )
		{
		}

    	m_State = PlayerState.Initialized;
    }
    
    
    public void setNewPlayList( LLPlayList playlist )
    {
    	m_mpPlayer.reset();
    	
    	m_PL = playlist;
    	
		try
		{
			m_mpPlayer.setDataSource( m_PL.m_CurPlay.m_RealPath + "/" + m_PL.m_CurPlay.m_Name );
			m_mpPlayer.prepare();
		}
		catch( Exception e )
		{
		}

    	m_State = PlayerState.Initialized;
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
    	if ( m_PL.moveToNext() )
    	{
			try
			{
				m_mpPlayer.reset();
				m_mpPlayer.setDataSource( m_PL.m_CurPlay.m_RealPath + "/" + m_PL.m_CurPlay.m_Name );
				m_mpPlayer.prepare();
				m_mpPlayer.start();
				return true;
			}
			catch(Exception e)
			{
				return false;
			}
    	}
    	else
    	{
    		return false;
    	}
    	
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
    public String getPlayDir()
    {
    	return m_PL.m_PlayDir;
    }
    public String getPlayFilename()
    {
    	return m_PL.m_CurPlay.m_Name;
    }
    public String getPlayFileRealPath()
    {
    	return m_PL.m_CurPlay.m_RealPath;
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
			if ( !playNext() )
			{
				m_State = PlayerState.Pause;
			}
		}
		
	}
	
	
	
	
	
	
}

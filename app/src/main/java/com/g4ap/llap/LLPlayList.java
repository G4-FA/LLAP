package com.g4ap.llap;

import java.util.ArrayList;

public class LLPlayList
{
	public String m_PlayDir;
	public t_LLPLItem m_CurPlay;
	public ArrayList<t_LLPLItem> m_List;
	

	public LLPlayList()
	{
		m_PlayDir = null;
		m_CurPlay = null;
		m_List = null;
	}
	
	
    public boolean moveToNext()
    {
    	if ( m_PlayDir == null || m_CurPlay == null || m_List == null ) return false;

		boolean bCanFind = false;
		for ( t_LLPLItem cur : m_List )
		{
			if ( cur.m_Name.equals(m_CurPlay.m_Name) )
			{
				bCanFind = true;
				continue;
			}
			if ( bCanFind )
			{
				if ( cur.m_Name.endsWith(".MP3") ||
						cur.m_Name.endsWith(".mp3") ||
						cur.m_Name.endsWith("WMA") ||
						cur.m_Name.endsWith(".wma") ||
						cur.m_Name.endsWith(".WAV") ||
						cur.m_Name.endsWith(".wav") ||
						cur.m_Name.endsWith(".M4A") ||
						cur.m_Name.endsWith(".m4a")
						)
				{
					m_CurPlay = cur;
					return true;
				}
			}
		}
		
		return false;
    }
	
	
}


class t_LLPLItem
{
	String m_Name;
	String m_RealPath;
}



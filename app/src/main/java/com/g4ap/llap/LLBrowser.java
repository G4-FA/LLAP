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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

public class LLBrowser
{
	
	//private String m_RootDir = "/sdcard";
	private String m_RootDir = "NULL";
	
	
	public String m_CurDir;
	public ArrayList<t_LLItem> m_CurList;
	private Map<String,Bitmap> m_CoverCache;
	//private Set<String> m_checkSet;


	public LLBrowser()
	{
		//m_RootDir = loadConfig();
		m_RootDir = "/sdcard/LiveLib/";
		setCurDir( "default" );
		m_CoverCache = new HashMap<String,Bitmap>();
		
		//m_checkSet = new HashSet<String>();

	}

	String loadConfig()
	{
		try {
			InputStream is = new FileInputStream("/sdcard/G4-MI/G4-MI_cfg.json");
			BufferedReader bufr = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line ;
			StringBuilder builder = new StringBuilder();
			while((line = bufr.readLine()) != null){
				builder.append(line);
			}
			is.close();
			bufr.close();

			try {
				JSONObject root = new JSONObject(builder.toString());
				JSONObject llapcfg = root.getJSONObject("LLAP");
				return llapcfg.getString("RootDir");
			} catch (JSONException e) {
				e.printStackTrace();
			}


		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "NULL";
	}
	
	
	public void setCurDir( String strDir )
	{
		// init
		if ( strDir.equals("default") )
		{
			m_CurDir = m_RootDir;
		}
		else if ( strDir.length() <= m_RootDir.length() )
		{
			m_CurDir = m_RootDir;
		}
		else
		{
			m_CurDir = strDir;
		}
		m_CurList = new ArrayList<t_LLItem>();
		

		// add parent dir
		if ( !m_CurDir.equals( m_RootDir ) )
		{
			t_LLItem curItem = new t_LLItem();
			curItem.m_Name = "...";
			curItem.m_Type = LLLItemType.PARENTDIR;
			curItem.m_RealPath = m_CurDir.substring( 0, m_CurDir.lastIndexOf("/") );
			m_CurList.add(curItem);
		}
		
		
		// check is playlist dir ( rootdir/PL/*.txt )
		boolean isPLDir = false;
		if ( !m_CurDir.equals( m_RootDir ) )
		{
			if ( m_CurDir.startsWith( m_RootDir + "/PL/" ) && m_CurDir.endsWith(".txt") )
			{
				isPLDir = true;
			}
		}
		
		// normal dir
		if ( isPLDir == false )
		{
			File curFile = new File( m_CurDir );
			File curFiles[] = curFile.listFiles();
			
			// add list item
			if ( curFiles == null )
			{
				return;
			}
			for ( File f : curFiles )
			{
				t_LLItem curItem = new t_LLItem();
				curItem.m_Name = f.getName();
				curItem.m_RealPath = m_CurDir;
				if ( f.isDirectory() )
				{
					curItem.m_Type = LLLItemType.DIR;
				}
				else
				{
					if ( curItem.m_RealPath.startsWith( m_RootDir + "/PL" ) && curItem.m_Name.endsWith(".txt") )
					{
						curItem.m_Type = LLLItemType.PLDIR;
					}
					else
					{
						curItem.m_Type = LLLItemType.FILE;
					}
				}

				m_CurList.add(curItem);
			}
		}
		// playlist
		else
		{
			File plFile = new File( m_CurDir );
			try
			{
				InputStream instream = new FileInputStream(plFile);
				if (instream != null)
				{
					InputStreamReader inputreader = new InputStreamReader(instream);
					BufferedReader buffreader = new BufferedReader(inputreader);
					String line;
					while ( (line = buffreader.readLine()) != null )
					{
						t_LLItem curItem = new t_LLItem();
						curItem.m_Name = line.substring( line.lastIndexOf("/")+1, line.length() );;
						curItem.m_Type = LLLItemType.PLFILE;
						curItem.m_RealPath = m_RootDir + "/" + line.substring( 0, line.lastIndexOf("/") );;
						m_CurList.add(curItem);
					}
					instream.close();
				}
			}
			catch (java.io.FileNotFoundException e)
			{
			}
			catch (IOException e)
			{
			}
		}
	}
	
	
	public t_LLItem onItemClick( int index )
	{
		t_LLItem ret = m_CurList.get(index);
		
		switch ( ret.m_Type )
		{
		case PARENTDIR:
			gotoUpperDir();
			break;
			
		case DIR:
		case PLDIR:
			setCurDir( ret.m_RealPath + "/" + ret.m_Name );
			break;
			
		case FILE:
		case PLFILE:
			break;
		}
		
		
		return ret;
	}
	
	
	public LLPlayList genCurPlayList()
	{
		LLPlayList ret = new LLPlayList();
		
		ret.m_PlayDir = m_CurDir;
		ret.m_CurPlay = new t_LLPLItem();
		ret.m_List = new ArrayList<t_LLPLItem>();
		for ( t_LLItem cur : m_CurList )
		{
			t_LLPLItem newItem = new t_LLPLItem();
			newItem.m_Name = cur.m_Name;
			newItem.m_RealPath = cur.m_RealPath;
			ret.m_List.add( newItem );
		}
		
		return ret;
	}

	
	public boolean gotoUpperDir()
	{
		if ( m_CurDir.equals( m_RootDir ) )
		{
			return false;
		}
		
		String strNewDir = m_CurDir.substring( 0, m_CurDir.lastIndexOf("/") );
		setCurDir( strNewDir );
		return true;
	}
	
	
	public ArrayList<Bitmap> getCoverImage( String path )
	{
		ArrayList<Bitmap> ret = new ArrayList<Bitmap>();
		
		File curFile = new File( path );
		File curFiles[] = curFile.listFiles();
		
		if ( curFiles == null )
		{
			return null;
		}
		for ( File f : curFiles )
		{
			if ( f.isDirectory() ) continue;
			if ( f.getName().endsWith(".jpg") ||
					f.getName().endsWith(".jpeg") ||
					f.getName().endsWith(".png") ||
					f.getName().endsWith(".bmp") ||
					f.getName().endsWith(".JPG") ||
					f.getName().endsWith(".JPEG") ||
					f.getName().endsWith(".PNG") ||
					f.getName().endsWith(".BMP")
				)
			{
				String coverFilename = path + "/" + f.getName();
				Bitmap bm = m_CoverCache.get(coverFilename);
				if ( bm == null )
				{
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 1;
					bm = BitmapFactory.decodeFile( coverFilename, options);
					m_CoverCache.put(coverFilename, bm);
				}
				ret.add( bm );
			}
	
		}
		
		return ret;
		
	}
	

	public void addCurToFav( String filename )
	{
		try
		{
			String fixFilename = filename.substring( m_RootDir.length() + 1 );
			FileOutputStream outstream = new FileOutputStream( m_RootDir + "/PL/newPL.txt", true );
			if (outstream != null)
			{
				OutputStreamWriter outputwriter = new OutputStreamWriter(outstream);
				outputwriter.write( fixFilename + "\n" );
				outputwriter.close();
			}
		}
		catch (Exception e)
		{
		}
	}

}



class t_LLItem
{
	LLLItemType m_Type;
	String m_Name;
	String m_RealPath;
}


enum LLLItemType
{  
    PARENTDIR, DIR, FILE, PLDIR, PLFILE
}


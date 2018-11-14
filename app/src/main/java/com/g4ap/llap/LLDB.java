package com.g4ap.llap;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;


class tMediaInfo {

    private String etag;
    private int times;
    private int rank;

    public tMediaInfo(){ }

    public tMediaInfo( String Etag, int Times, int Rank ) {
        etag = Etag;
        times = Times;
        rank = Rank;
    }

    public String getEtag() { return etag; }
    public void setEtag( String etag ) { this.etag = etag; }

    public int getTimes() { return times; }
    public void setTimes( int times ) { this.times = times; }

    public int getRank() { return rank; }
    public void setRank( int rank ) { this.rank = rank; }
}


class LLDB {

    private LLCOSUtils llCOSUtils;
    private Context context;
    private HashMap<String,tMediaInfo> llDB;
    private static String LLDB_FILENAME = "LLDB.txt";

    LLDB( LLCOSUtils utils, Context con ) {
        llCOSUtils = utils;
        context = con;
        llDB = null;
    }

    public void initDBFromLocal() {

        llDB = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();

        try {
            FileReader fr = new FileReader(new File( LLCOSUtils.LOCAL_SYS_DIR + LLDB_FILENAME ));
            BufferedReader br = new BufferedReader( fr );
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }
            br.close();
            fr.close();;
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONArray infoArray = new JSONArray(stringBuilder.toString());
            for (int i = 0; i < infoArray.length(); i++) {
                JSONObject cur = infoArray.getJSONObject(i);
                tMediaInfo info = new tMediaInfo( cur.getString("etag"), cur.getInt("times"), cur.getInt("rank") );
                llDB.put( info.getEtag(), info );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void incPlayTimes( String eTag ) {

        tMediaInfo info;
        info = llDB.get( eTag );
        if ( info == null ) { info = new tMediaInfo(eTag, 0, 0); }
        info.setTimes( info.getTimes()+1 );
        llDB.put( info.getEtag(), info );
    }

    public void saveDB2Local() {

        JSONArray array = new JSONArray();
        try {
            for ( HashMap.Entry<String,tMediaInfo> entry : llDB.entrySet() ) {
                tMediaInfo curInfo = entry.getValue();
                JSONObject curJObj = new JSONObject();
                curJObj.put( "etag", curInfo.getEtag() );
                curJObj.put( "times", curInfo.getTimes() );
                curJObj.put( "rank", curInfo.getRank() );
                array.put( curJObj );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String outStr = array.toString();

        try {
            File file = new File( LLCOSUtils.LOCAL_SYS_DIR );
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        try {
            File file = new File(LLCOSUtils.LOCAL_SYS_DIR + LLDB_FILENAME );
            if (!file.exists()) {
                file.createNewFile();
            }
            out = new FileOutputStream( file );;
            out.write(outStr.getBytes());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) { out.close(); }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }



}

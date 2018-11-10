package com.g4ap.llap;

import org.json.JSONArray;
import java.util.HashMap;
import java.util.Iterator;


class tMediaInfo
{
    private String etag;
    private int times;
    private int rank;

    tMediaInfo() {
    }

    tMediaInfo( String Etag, int Times, int Rank ) {
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


public class LLDB {

    private LLCOSUtils llCOSUtils;
    private HashMap<String,tMediaInfo> llDB;

    public void LLDB( LLCOSUtils utils ) {
        llCOSUtils = utils;
        llDB = null;
    }

    public void initDBFromLocal() {
        llDB = new HashMap<>();
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

        Iterator iter = llDB.entrySet().iterator();
        while (iter.hasNext()) {
            //llDB.Entry entry = (llDB.Entry) iter.next();
            //Object key = entry.getKey();
            //Object val = entry.getValue();
        }

    }



}

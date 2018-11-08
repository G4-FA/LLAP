package com.g4ap.llap;

import android.content.Context;
import android.util.Log;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.bucket.GetBucketRequest;
import com.tencent.cos.xml.model.bucket.GetBucketResult;
import com.tencent.cos.xml.model.tag.ListBucket;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;

import java.util.ArrayList;
import java.util.List;

class tObjectInfo
{
    tObjectInfo( String Key, long Size, String PCFilename, String Etag )
    {
        key = Key;
        size = Size;
        etag = Etag;
    }
    public String key;
    public long size;
    public String etag;
}


public class LLCOSUtils {

    public static String COS_ROOT_DIR = "LiveLib/";

    public List<tObjectInfo> getAllCOSObjectList( Context context ) {

        System.out.println( "[SyncUtilsCOS getAllCOSObjectList]" );

        CosXmlServiceConfig serviceConfig = new CosXmlServiceConfig.Builder()
                .setAppidAndRegion("1257773597", "ap-chengdu")
                .setDebuggable(true)
                .builder();
        QCloudCredentialProvider credentialProvider = new ShortTimeCredentialProvider( "", "", 300);
        CosXmlService cosXmlService = new CosXmlService(context, serviceConfig, credentialProvider);

        List<tObjectInfo> retList = new ArrayList<tObjectInfo>();
        GetBucketRequest getBucketRequest = new GetBucketRequest( "g4-livelib-cos-1257773597" );
        getBucketRequest.setPrefix(COS_ROOT_DIR);
        getBucketRequest.setMaxKeys(1000);
        //getBucketRequest.setDelimiter('');
        GetBucketResult getBucketResult = null;

        do {

            try {
                getBucketResult = cosXmlService.getBucket(getBucketRequest);
            } catch (CosXmlClientException e) {
                e.printStackTrace();
            } catch (CosXmlServiceException e) {
                e.printStackTrace();
            }

            for (ListBucket.Contents curObj : getBucketResult.listBucket.contentsList) {

                String key = curObj.key;
                String etag = curObj.eTag;
                long fileSize = curObj.size;

                if ( key.equals(COS_ROOT_DIR) ) { continue; }

                tObjectInfo info = new tObjectInfo(key, fileSize, "COSFilename", etag);
                retList.add(info);

                String logInfo = String.format( "Find COS Object --- key:%s etag:%s filesize:%d", key,etag,fileSize );
                Log.v( "LLAP",logInfo );
            }

            String nextMarker = getBucketResult.listBucket.nextMarker;
            getBucketRequest.setMarker(nextMarker);

        } while (getBucketResult.listBucket.isTruncated);

        return retList;

    }




}
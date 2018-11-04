package com.g4ap.llap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class llObjectNode implements Comparable<llObjectNode> {

    llObjectNode( String Name, String Key, long Size, String ETag, int IsFolder, llObjectNode Parent ) {

        name = Name;
        key = Key;
        size = Size;
        eTag = ETag;
        isFolder = IsFolder;
        parent = Parent;
        childs = new ArrayList<llObjectNode>();
    }

    String name;
    String key;
    long size;
    String eTag;
    int isFolder;
    llObjectNode parent;
    List<llObjectNode> childs;

    public int compareTo(llObjectNode arg0) {

        if ( isFolder == 1 && arg0.isFolder == 0  ) {
            return -1;
        } else if ( isFolder == 1 && arg0.isFolder == 0 ) {
            return 1;
        } else {
            return name.compareTo(arg0.name);
        }
    }

}


enum llClickType {
    NULL,	// 错误情况 索引非法
    FOLDER,	// 点击进入文件夹 进入下层目录 请手动请求新的列表
    AUDIO_SAMEDIR,	// 点击音频 请开始播放
    VEDIO_SAMEDIR,	// 点击视频 请开始播放
    AUDIO_DIFFDIR,	// 点击音频 请开始播放 播放目录有变更 需更新为本目录的cover页面
    VEDIO_DIFFDIR,	// 点击视频 请开始播放 播放目录有变更 需更新为本目录的cover页面
    OTHER	// 点击其他类型文件 无视
}

public class COSLLBrowser {

    private llObjectNode rootNode;
    private llObjectNode browsingNode;
    private llObjectNode playingNode;


    // 根据COS对象列表构建目录树
    public llObjectNode Init( List<tObjectInfo> COSObjList ) throws Exception {

        rootNode = new llObjectNode( "LiveLib", "DIR_KEY",0,"DIR_ETAG",1,null);
        browsingNode = rootNode;
        playingNode = null;

        int len = LLCOSUtils.COS_ROOT_DIR.length();
        for( int i=0; i<COSObjList.size(); i++) {
            tObjectInfo info = COSObjList.get(i);
            InsertCOSObject( rootNode, info.key.substring(len), info.key.substring(len), info.size, info.etag );
        }

        return browsingNode;
    }

    // 点击当前浏览节点的某个子对象
    public llClickType onItemClick(int index) {

        llObjectNode clickNode = browsingNode.childs.get(index);
        if ( clickNode == null ) {
            return llClickType.NULL;
        }

        if ( clickNode.isFolder == 1 ) {
            browsingNode = clickNode;
            return llClickType.FOLDER;
        }

        if ( isAudioFile(clickNode.key) ) {
            if ( playingNode.parent == clickNode.parent ) {
                playingNode = clickNode;
                return llClickType.AUDIO_SAMEDIR;
            } else {
                playingNode = clickNode;
                return llClickType.AUDIO_DIFFDIR;
            }
        }

        if ( isVedioFile(clickNode.key) ) {
            if ( playingNode.parent == clickNode.parent ) {
                playingNode = clickNode;
                return llClickType.VEDIO_SAMEDIR;
            } else {
                playingNode = clickNode;
                return llClickType.VEDIO_DIFFDIR;
            }
        }

        return llClickType.OTHER;
    }


    // 当前播放已经结束 请求点击播放下个媒体文件
    public llClickType playNextMedia() {

        if ( playingNode == null ) return llClickType.NULL;

        if ( isAudioFile(playingNode.key) ) {

            int findCur = 0;
            llObjectNode nextNode = null;

            Iterator<llObjectNode> itTSet = playingNode.parent.childs.iterator();
            while( itTSet.hasNext() ) {
                llObjectNode curNode = itTSet.next();
                if ( findCur == 1 && isAudioFile(curNode.key) ) {
                    nextNode = curNode;
                    break;
                }
                if ( curNode == playingNode ) {
                    findCur = 1;
                }
            }

            if ( nextNode == null ) {
                return llClickType.NULL;
            } else {
                playingNode = nextNode;
                return llClickType.AUDIO_SAMEDIR;
            }

        }

        if ( isVedioFile(playingNode.key) ) {

            int findCur = 0;
            llObjectNode nextNode = null;

            Iterator<llObjectNode> itTSet = playingNode.parent.childs.iterator();
            while( itTSet.hasNext() ) {
                llObjectNode curNode = itTSet.next();
                if ( findCur == 1 && isVedioFile(curNode.key) ) {
                    nextNode = curNode;
                    break;
                }
                if ( curNode == playingNode ) {
                    findCur = 1;
                }
            }

            if ( nextNode == null ) {
                return llClickType.NULL;
            } else {
                playingNode = nextNode;
                return llClickType.VEDIO_SAMEDIR;
            }

        }

        return llClickType.NULL;

    }


    private void InsertCOSObject( llObjectNode parent, String Name, String Key, long Size, String ETag ) throws Exception {

        int nextPos = Name.indexOf("/");

        // 还未到达路径末尾
        if ( nextPos > 0 ) {

            // 插入本层文件夹
            String curStr = Name.substring(0,nextPos);
            llObjectNode folderNode = AddChildFolder( parent, curStr );

            // 继续递归插入
            String leftStr = Name.substring(nextPos+1);
            InsertCOSObject(folderNode, leftStr, Key, Size, ETag);

        } else {
            // 已到达路径末尾

            // 理论应该不可能是文件夹
            if ( Size <= 0 || Name.endsWith("/") ) {
                throw new Exception("ERROR COSLLBrowser InsertCOSObject end with folder");
            }
            AddChildObject( parent, Name, Key, Size, ETag );

        }

    }


    private llObjectNode AddChildObject( llObjectNode parent, String Name, String Key, long Size, String ETag ) throws Exception {

        // 保险起见判重一下
        Iterator<llObjectNode> itTSet = parent.childs.iterator();
        while( itTSet.hasNext() ) {
            llObjectNode curNode = itTSet.next();
            if ( curNode.key.equals(Key) ) {
                throw new Exception("ERROR COSLLBrowser AddChildObject repeat");
            }
        }

        llObjectNode ret = new llObjectNode(Name, Key, Size, ETag,0, parent );
        parent.childs.add(ret);
        return ret;
    }


    private llObjectNode AddChildFolder( llObjectNode parent, String Name ) {

        // 中间文件夹目录会多次创建 所以需要判重一下
        Iterator<llObjectNode> itTSet = parent.childs.iterator();
        while( itTSet.hasNext() ) {
            llObjectNode curNode = itTSet.next();
            if ( curNode.key.equals(Name) ) return curNode;
        }

        // 能走到这里说明尚未建立
        llObjectNode ret = new llObjectNode(Name, "DIR_KEY",0,"DIR_ETAG",1, parent);
        parent.childs.add(ret);
        return ret;
    }


    private boolean isAudioFile( String filename )
    {
        if ( filename.endsWith(".mp3") || filename.endsWith(".wma") || filename.endsWith(".wav") || filename.endsWith(".m4a") ||
                filename.endsWith(".MP3") || filename.endsWith("WMA") || filename.endsWith(".WAV") || filename.endsWith(".M4A") ) {
            return true;
        }

        return false;
    }


    private boolean isVedioFile( String filename )
    {
        if ( filename.endsWith(".mp4") ||
                filename.endsWith(".MP4") ) {
            return true;
        }

        return false;
    }

}

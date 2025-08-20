package org.example.plugin;

/**
 * @author yufengyang
 * @Package org.example.plugin
 * @date 2025/8/16 20:49
 * @school hnist
 */
public class PageParam {
    private final int pageNum;
    private final int pageSize;
    public  PageParam(int pageNum,int pageSize){
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int offset(){
        return (pageNum-1)*pageSize;
    }
}

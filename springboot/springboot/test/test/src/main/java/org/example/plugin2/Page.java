package org.example.plugin2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author yufengyang
 * @Package org.example.plugin
 * @date 2025/8/21 19:46
 * @school hnist
 */
public class Page <T> extends ArrayList<T> {
    private final long total;
    private final int pageNum;
    private final int pageSize;

    public Page(int pageNum, int pageSize, long total) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
    }
    public Page(int pageNum, int pageSize, long total, Collection<? extends T> c) {
        super(c);
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
    }
    public long getTotal() { return total; }
    public int getPageNum() { return pageNum; }
    public int getPageSize() { return pageSize; }
    public long getPages() { return (pageSize == 0) ? 0 : (long) Math.ceil(total * 1.0 / pageSize); }
    public List<T> getResult(){return (List<T>) this;}
}

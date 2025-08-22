package org.example.plugin2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yufengyang
 * @Package org.example.plugin
 * @date 2025/8/16 21:42
 * @school hnist
 */
public class PageResult2<T> extends ArrayList<T> implements Serializable {
    private final long total;
    private final List<T> data;

    public PageResult2(long total, List<T> data) {
        this.total = total;
        this.data = data;
    }

    public long getTotal(){return total;}
    public List<T> getData(){
        return data;
    }

}

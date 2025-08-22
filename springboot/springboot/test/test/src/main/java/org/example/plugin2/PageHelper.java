package org.example.plugin2;

import org.example.plugin.PageParam;

/**
 * @author yufengyang
 * @Package org.example.plugin
 * @date 2025/8/16 20:41
 * @school hnist
 */
public class PageHelper {
    private PageHelper(){}
    private static final ThreadLocal<PageParam> PAGE_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Long> TOTAL_HOLDER = new ThreadLocal<>();

    public static void startPage(PageParam pageParam){
        PAGE_HOLDER.set(pageParam);
    }
    public static void setTotalHolder(Long totalHoldder){
        TOTAL_HOLDER.set(totalHoldder);
    }

    public static PageParam getPageHolder(){
        return PAGE_HOLDER.get();
    }

    public static Long getTotalHolder(){
        return TOTAL_HOLDER.get();
    }
    public static void clearPageHoler(){
        PAGE_HOLDER.remove();
    }
    public static void clearTotalHolder(){
        TOTAL_HOLDER.remove();
    }
    public static void clearAll(){
        clearPageHoler();
        clearTotalHolder();
    }

}

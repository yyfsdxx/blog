package org.example.plugin;

/**
 * @author yufengyang
 * @Package org.example.plugin
 * @date 2025/8/16 20:41
 * @school hnist
 */
public class PaginationContext {
    private PaginationContext(){}
    private static final ThreadLocal<PageParam> PAGE_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Long> TOTAL_HOLDDER = new ThreadLocal<>();

    public static void setPageHolder(PageParam pageParam){
        PAGE_HOLDER.set(pageParam);
    }
    public static void setTotalHolder(Long totalHoldder){
        TOTAL_HOLDDER.set(totalHoldder);
    }

    public static PageParam getPageHolder(){
        return PAGE_HOLDER.get();
    }

    public static Long getTotalHolder(){
        return TOTAL_HOLDDER.get();
    }
    public static void clearPageHoler(){
        PAGE_HOLDER.remove();
    }
    public static void clearTotalHolder(){
        TOTAL_HOLDDER.remove();
    }
    public static void clearAll(){
        clearPageHoler();
        clearTotalHolder();
    }

}

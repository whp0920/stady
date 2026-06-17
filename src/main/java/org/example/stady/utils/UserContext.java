package org.example.stady.utils;

public class UserContext {
    private static final ThreadLocal<Long> tl = new ThreadLocal<>();

    public static void setUserId(Long id){
        tl.set(id);
    }
    public static Long getUserId(){
        return tl.get();
    }
    public static void remove(){
        tl.remove();
    }
}

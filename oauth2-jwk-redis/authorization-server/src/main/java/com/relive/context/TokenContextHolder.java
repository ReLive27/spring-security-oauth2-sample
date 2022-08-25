package com.relive.context;

/**
 * {@link TokenContext} 的持有者使用 {@code ThreadLocal} 将其与当前线程相关联
 *
 * @author: ReLive
 * @date: 2022/8/21 19:43
 */
public class TokenContextHolder {
    private static final ThreadLocal<TokenContext> holder = new ThreadLocal<>();

    private TokenContextHolder() {
    }

    /**
     * 返回当前线程绑定的 {@link TokenContext}
     *
     * @return
     */
    public static TokenContext getTokenContext() {
        return holder.get();
    }

    /**
     * 将给定的 {@link TokenContext} 绑定到当前线程
     *
     * @param tokenContext
     */
    public static void setTokenContext(TokenContext tokenContext) {
        if (tokenContext == null) {
            resetTokenContext();
        } else {
            holder.set(tokenContext);
        }
    }

    /**
     * 重置绑定到当前线程的 {@link TokenContext}
     */
    public static void resetTokenContext() {
        holder.remove();
    }

}

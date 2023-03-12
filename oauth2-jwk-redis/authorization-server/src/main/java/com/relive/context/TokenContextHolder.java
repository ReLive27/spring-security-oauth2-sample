package com.relive.context;

/**
 * A holder of the {@link TokenContext} that associates it with the current thread using a {@code ThreadLocal}.
 *
 * @author: ReLive
 * @date: 2022/8/21 19:43
 */
public class TokenContextHolder {
    private static final ThreadLocal<TokenContext> holder = new ThreadLocal<>();

    private TokenContextHolder() {
    }

    /**
     * Returns the {@link TokenContext} bound to the current thread.
     *
     * @return
     */
    public static TokenContext getTokenContext() {
        return holder.get();
    }

    /**
     * Bind the given {@link TokenContext} to the current thread.
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
     * Reset the {@link TokenContext} bound to the current thread.
     */
    public static void resetTokenContext() {
        holder.remove();
    }

}

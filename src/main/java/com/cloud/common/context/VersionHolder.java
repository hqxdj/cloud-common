package com.cloud.common.context;

import org.springframework.stereotype.Component;

@Component
public class VersionHolder {

    public static final String VERSION_INFO = "VersionInfo";

    private ThreadLocal<String> holder = new ThreadLocal();

    public void set(String version) {
        holder.set(version);
    }

    public String get() {
        return holder.get();
    }

    public void remove() {
        holder.remove();
    }

}

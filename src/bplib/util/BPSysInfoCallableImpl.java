/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bplib.util;

/**
 *
 * @author Kofola
 */
public abstract class BPSysInfoCallableImpl implements BPSysInfoCallable {

    private final boolean refresh;
    private boolean init;
    private StringBuilder localSb;

    public BPSysInfoCallableImpl(boolean refresh) {
        this.refresh = refresh;
        localSb=new StringBuilder();
    }

    public String call() {
        if (!init) {
            init = true;
            return overrideFn();
        }
        if (isRefresh()) {
            return overrideFn();
        } else {
            return null;
        }
    }

    @Override
    public abstract String overrideFn();

    /**
     * @return the refresh
     */
    public boolean isRefresh() {
        return refresh;
    }

    /**
     * @return the localSb
     */
    public StringBuilder getLocalSb() {
        return localSb;
    }

}

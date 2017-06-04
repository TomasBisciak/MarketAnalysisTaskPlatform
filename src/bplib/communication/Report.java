package bplib.communication;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Kofola
 * @param <T>
 */
public final class Report<T> {

    //immutable , thread safe class!
    private final int threadNum;
    private final long jvmMem;
    private final String state;
    private final long dataRate; // graphing uses DOUBLE so i need to change grpahing etc to use LONG , not double
    private final int progress;
    //private final String[] additionalDetail;//NOT IMMUTABLE HAS TO BE ISED IMMUTABLE LIST

    /*
     recommendation is to not use an array or an unmodifiableList but to use Guava's ImmutableList, which exists for this purpose.
     Guava's ImmutableList is even better than Collections.unmodifiableList ,because it is a separate type.
    
     ImmutableList is often better (it depends on the use case) because it's immutable. 
     Collections.unmodifiableList is not immutable. Rather, it's a view that receivers can't change, but the original source CAN change. 
     */
    private final List<T> additionalDetail;

    public Report(String state, long dataRate, int threadNum, long jvmMem, int progress, T... additionalDetail) {
        this.threadNum = threadNum;
        this.jvmMem = jvmMem;
        this.state = state;
        this.dataRate = dataRate;
        if (progress <= 100 && progress >= 0) {
            this.progress = progress;
        } else {
            this.progress = 0;
        }
        this.additionalDetail = Collections.unmodifiableList(Arrays.asList(additionalDetail));
    }

    @Override
    public String toString() {
        return "Report{" + "threadNum=" + getThreadNum() + ", jvmMem=" + getJvmMem() + ", state=" + getState() + ", dataRate=" + getDataRate() + ", progress=" + getProgress() + ", additionalDetail=" + additionalDetialToString() + '}';
    }

    /**
     * @return the threadNum
     */
    public int getThreadNum() {
        return threadNum;
    }

    /**
     * @return the jvmMem
     */
    public long getJvmMem() {
        return jvmMem;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @return the dataRate
     */
    public long getDataRate() {
        return dataRate;
    }

    /**
     * @return the progress
     */
    public int getProgress() {
        return progress;
    }

    /**
     * @return the additionalDetail
     */
    public List<T> getAdditionalDetail() {
        return additionalDetail;
    }

    private String additionalDetialToString() {//allocated in stack , safe to use sbuidler over sbuffer
        StringBuilder sb = new StringBuilder();
        for (T t : additionalDetail) {
            sb.append(t).append("\t");
        }
        return sb.toString();
    }

}

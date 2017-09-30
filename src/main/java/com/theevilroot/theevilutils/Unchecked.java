package com.theevilroot.theevilutils;

/**
 * Created by TheEvilRoot on 8/2/2017.
 */

public interface UncheckedRunnable extends Runnable {

    void execute() throws Throwable;

    @Override
    default void run() {
        try{
            execute();
        }catch (Throwable e){
            if(e instanceof RuntimeException) {
                throw new RuntimeException();
            }else{
                throw new RuntimeException(e);
            }
        }
    }



}

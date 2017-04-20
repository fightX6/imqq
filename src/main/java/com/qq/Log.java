package com.qq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by qq on 2017/4/20.
 */
public class Log {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void info(String msg,Object... object){
        if(log.isInfoEnabled()){
            log.info(msg,object);
        }
    }
    public void err(String msg,Object... object){
        if(log.isErrorEnabled()){
            log.error(msg,object);
        }
    }
    public void debug(String msg,Object... object){
        if(log.isDebugEnabled()){
            log.debug(msg,object);
        }
    }

    public void warn(String msg,Object... object){
        if(log.isWarnEnabled()){
            log.warn(msg,object);
        }
    }

    public void trace(String msg,Object... object){
        if(log.isWarnEnabled()){
            log.trace(msg,object);
        }
    }

}

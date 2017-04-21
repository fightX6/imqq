package com.qq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 */
public class Main {
    private static Logger log = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws InterruptedException {
        log.info("------------------系统环境初始化中请稍候--------------------");
        try {
            Environment.init();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        log.info("------------------系统环境初始化成功--------------------");
        MyPcap.run();
//        System.getProperties().list(System.out);
//        System.out.println(Environment.SYSTEM32);
    }
}

package com.qq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * 程序环境初始化类
 * Created by qq on 2017/4/20.
 */
public class Environment {
    private static Logger log = LoggerFactory.getLogger(Environment.class);
    public static final String USER_HOME = System.getProperty("user.home") + File.separatorChar + ".imqq" + File.separatorChar;
    public static final String JNETPCAP = "jnetpcap.dll";
    public static final String JNETPCAP_X64 = "jnetpcap_x64.dll";
    public static final boolean IS_X64 = new File("C:"+File.separatorChar+"Program Files (x86)").exists();

    private static String dll = "";
    private static String os_bit = "";
    private Environment() {}

    public static void init() throws Throwable {
        initDLL();
    }

    private static void checkSystem() throws Throwable {
        if (System.getProperty("os.name").contains("Windows")) {
            if (IS_X64) {
                dll = JNETPCAP_X64;
                os_bit = "64位";
            } else {
                dll = JNETPCAP;
                os_bit = "32位";
            }
        } else {
            throw new Throwable("不支持的操作系统！请使用windows系统！");
        }
    }
    private static String getDLL() throws Throwable{
        File userdll = new File(USER_HOME+dll);
        if(!userdll.exists()){
            try {
                if(!userdll.getParentFile().exists()){
                    userdll.getParentFile().mkdirs();
                }
                userdll.createNewFile();
                //返回读取指定资源的输入流
                InputStream is = Environment.class.getClass().getResourceAsStream("/dll/"+dll);
                FileOutputStream fos = new FileOutputStream(userdll);
                // 设定读取的字节数
                byte buffer[] = new byte[1024];
                while(is.read(buffer, 0, 1024) != -1) {
                    fos.write(buffer);
                }
                fos.flush();
                fos.close();
                is.close();
            } catch (IOException e) {
                throw new Throwable("获取不到动态链接库【"+dll+"】:"+e.getLocalizedMessage());
            }
        }
        return  userdll.getAbsolutePath();
    }
    private static void initDLL() throws Throwable {
        log.info("------------------检测系统版本--------------------");
        checkSystem();
        //通过StringBuilder来构建要输出的内容
        StringBuilder sb = new StringBuilder();
        sb.append("\nJava 运行时环境版本:"+System.getProperty("java.version")+"\n");
        sb.append("Java 运行时环境供应商:"+System.getProperty("java.vendor")+"\n");
//        sb.append("Java 供应商的URL:"+System.getProperty("java.vendor.url")+"\n");
        sb.append("Java 安装目录:"+System.getProperty("java.home")+"\n");
        sb.append("Java 虚拟机规范版本:"+System.getProperty("java.vm.specification.version")+"\n");
        sb.append("Java 类格式版本号:"+System.getProperty("java.class.version")+"\n");
//        sb.append("Java类路径："+System.getProperty("java.class.path")+"\n");
//        sb.append("加载库时搜索的路径列表:"+System.getProperty("java.library.path")+"\n");
        sb.append("默认的临时文件路径:"+System.getProperty("java.io.tmpdir")+"\n");
        sb.append("要使用的 JIT 编译器的名称:"+System.getProperty("java.compiler")+"\n");
        sb.append("一个或多个扩展目录的路径:"+System.getProperty("java.ext.dirs")+"\n");
        sb.append("操作系统的名称:"+System.getProperty("os.name")+"\n");
        sb.append("操作系统的架构:"+System.getProperty("os.arch")+"\n");
        sb.append("操作系统的位数:"+os_bit+"\n");
        sb.append("操作系统的版本:"+System.getProperty("os.version")+"\n");
        sb.append("文件分隔符（在 UNIX 系统中是“/”）:"+System.getProperty("file.separator")+"\n");
        sb.append("路径分隔符（在 UNIX 系统中是“:”）:"+System.getProperty("path.separator")+"\n");
        sb.append("行分隔符（在 UNIX 系统中是“/n”）:"+System.getProperty("line.separator")+"\n");
        sb.append("用户的账户名称:"+System.getProperty("user.name")+"\n");
        sb.append("用户的主目录:"+System.getProperty("user.home")+"\n");
        sb.append("用户的当前工作目录:"+System.getProperty("user.dir")+"\n");
        log.info(sb.toString());
        log.info("------------------加载动态连接库--------------------");
        //装载Windows\System32下或jre\bin或Tomcat\bin目录下的本地链接库
        //System.loadLibrary("");
        //根据具体的目录来加截本地链接库，必须是绝对路径
        //System.load("");
        //加载动态连接库
        String dllpath = getDLL();
        System.load(dllpath);
        log.info("成功加载动态连接库："+dllpath );
    }
}

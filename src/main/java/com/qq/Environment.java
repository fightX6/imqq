package com.qq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;

/**
 * 程序环境初始化类
 *
 *
 *   最近在做一个项目的过程中需要用到第三方的jar包和动态链接库（dll），其中jar包可以直接引入，问题是在开发的时候dll可以放在System32下，

     但是当项目完成后build的时候，这种方式就行不通了，所以必须考虑其他的方式来引用所需的dll文件。

     我们知道，在VM参数处通过-Djava.library.path后将加载路径指定到自己的lib目录后，程序可以正常启动。

     但这种方式显然不够灵活，受限于必须从main函数启动，并且要手动的去指定虚拟机参数。

     那么我们现在就需要采取其他的方式，比如在项目的根目录下建一个dll的文件夹，将要用到的dll文件放到此目录下，然后

     通过System类的setProperty函数来在代码中动态的改变一下Java.library.path的值。

     如下：

     System.setProperty("java.library.path","%ProjectPath%/dll");

     问题是这种方式是行不通的，会报错"no JIntellitype in java.library.path"。

     查找原因：

     代码中设置不起作用，主要是因为java.library.path只在jvm启动时读取一次，其他情况下的修改不会起作用的

     使用java的反射机制，完成了对于ClassLoader类中的usr_paths变量的动态修改
 * Created by qq on 2017/4/20.
 */
public class Environment {
    private static Logger log = LoggerFactory.getLogger(Environment.class);
    public static final String USER_HOME = System.getProperty("user.home") + File.separatorChar + ".imqq" + File.separatorChar;
    public static final String JNETPCAP = "jnetpcap";//dll后缀会自动加上去
    public static final boolean IS_X64 = new File("C:"+File.separatorChar+"Program Files (x86)").exists();

    private static String os_bit = "";
    private static String version = "_x64";
    private Environment() {}

    public static void init() throws Throwable {
        initDLL();
    }

    private static void checkSystem() throws Throwable {
        if (System.getProperty("os.name").contains("Windows")) {
            if (IS_X64) {
                os_bit = "64位";
            } else {
                os_bit = "32位";
                version = "";
            }
        } else {
            throw new Throwable("不支持的操作系统！请使用windows系统！");
        }
    }
    private static String getDLL() throws Throwable{
        File userdll = new File(USER_HOME+JNETPCAP+".dll");
        File userdllReadme = new File(USER_HOME+JNETPCAP +"为"+os_bit+"版本");
        if(userdll.exists()){
            userdll.delete();
            userdllReadme.delete();
        }
        try {
            if(!userdll.getParentFile().exists()){
                userdll.getParentFile().mkdirs();
            }
            userdll.createNewFile();
            userdllReadme.createNewFile();
            //返回读取指定资源的输入流
            InputStream is = Environment.class.getClass().getResourceAsStream("/dll/"+JNETPCAP+version+".dll");
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
            throw new Throwable("获取不到动态链接库【"+"/dll/"+JNETPCAP+"】:"+e.getLocalizedMessage());
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
        addLibraryDir(USER_HOME);
        System.loadLibrary(JNETPCAP);
        log.info("成功加载动态连接库："+dllpath );
    }

    /**
     * 使用java的反射机制，完成了对于ClassLoader类中的usr_paths变量的动态修改
     *  文章也同时指出了这种实现的局限性，和jvm的实现强关联，
     *  只要jvm实现不是用的变量usr_paths来保存java.library.path的值，这个方法就不能用了。
     *  通过调用上面的方法来将dll文件目录加入java.library.path路径下，
     *  然后使用System.loadLibrary("glpk_4_55")这样的方式加载dll文件即可。
     * @param libraryPath
     * @throws IOException
     */
    public static void addLibraryDir(String libraryPath) throws IOException {
        try {
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[]) field.get(null);
            for (int i = 0; i < paths.length; i++) {
                if (libraryPath.equals(paths[i])) {
                    return;
                }
            }

            String[] tmp = new String[paths.length + 1];
            System.arraycopy(paths, 0, tmp, 0, paths.length);
            tmp[paths.length] = libraryPath;
            field.set(null, tmp);
        } catch (IllegalAccessException e) {
            throw new IOException(
                    "Failedto get permissions to set library path");
        } catch (NoSuchFieldException e) {
            throw new IOException(
                    "Failedto get field handle to set library path");
        }
    }
}

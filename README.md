##### 如何通过java在windows上获取网卡的数据包，这里我们使用了开源的WinPcap，还有jNetPcap，jNetPcap是对接了WinPcap来截获网卡数据包。
##### 如何截获网卡分3步： 
1. 在自己的机器上安装WinPcap。   http://www.winpcap.org/install/default.htm
2. 下载jNetPcap,http://jnetpcap.com/download.  下载下来之后解压，里面2个重要文件jnetpcap.jar,jnetpcap.dll
3. 向工程加入 jnetpcap.jar 依赖 
#### mvn install:install-file -Dfile=F:\开发安装包\jnetpcap-1.3.0\jnetpcap.jar -DgroupId=com.qq -DartifactId=jnetpcap -Dversion=1.3.0 -Dpackaging=jar
# 
    <dependency>
        <groupId>com.qq</groupId>
        <artifactId>jnetpcap</artifactId>
        <version>1.3.0</version>
    </dependency>
#

####注意：
#####在运行以上代码之前还需要加上jvm参数，为了让jnetpcap找到jnetpcap.dll,我们在vm parameters加入以下参数：
#####-Djava.library.path=E:\jnetpcap 这里的E:\jnetpcap 就是jnetpcap.dll放置的目录。


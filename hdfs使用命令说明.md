HDFS架构

1 Master(NameNode/NN)  带 N个Slaves(DataNode/DN)
HDFS/YARN/HBase

1个文件会被拆分成多个Block
blocksize：128M
130M ==> 2个Block： 128M 和 2M

NN：
1）负责客户端请求的响应
2）负责元数据（文件的名称、副本系数、Block存放的DN）的管理

DN：
1）存储用户的文件对应的数据块(Block)
2）要定期向NN发送心跳信息，汇报本身及其所有的block信息，健康状况

A typical deployment has a dedicated machine that runs only the NameNode software. 
Each of the other machines in the cluster runs one instance of the DataNode software.
The architecture does not preclude running multiple DataNodes on the same machine 
but in a real deployment that is rarely the case.

NameNode + N个DataNode
建议：NN和DN是部署在不同的节点上


replication factor：副本系数、副本因子

All blocks in a file except the last block are the same size



Hadoop伪分布式安装步骤
1）jdk安装
	解压：tar -zxvf jdk-7u79-linux-x64.tar.gz -C ~/app
	添加到系统环境变量： ~/.bash_profile
		export JAVA_HOME=/home/admin/app/jdk1.7.0_79
		export PATH=$JAVA_HOME/bin:$PATH
		
		centos7自带java：
		export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.91-0.b14.el7_2.x86_64
		export JRE_HOME=$JAVA_HOME/jre
		export CLASSPATH=$JAVA_HOME/lib:$JRE_HOME/lib:$CLASSPATH
		export PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH
	删除不适用的JDK:
		 查询:rpm -qa | grep java  或 rpm -qa | grep jdk
		 删除:通过    rpm -e --nodeps   后面跟系统自带的jdk名    这个命令来删除系统自带的jdk，
		
	使得环境变量生效： source ~/.bash_profile
	验证java是否配置成功： 
	 echo $JAVA_HOME
	java -v

2）安装ssh
	sudo yum install ssh
	ssh-keygen -t rsa
	cp ~/.ssh/id_rsa.pub ~/.ssh/authorized_keys	

3）下载并解压hadoop
	下载：直接去cdh网站下载
	解压：tar -zxvf hadoop-2.6.0-cdh5.7.0.tar.gz -C ~/app

4）hadoop配置文件的修改(hadoop_home/etc/hadoop)
	hadoop-env.sh
		export JAVA_HOME=/home/hadoop/app/jdk1.7.0_79

	core-site.xml
		<property>
	        <name>fs.defaultFS</name>
	        <value>hdfs://hadoop000:8020</value>
	    </property>

	    <property>
	        <name>hadoop.tmp.dir</name>
	        <value>/home/hadoop/app/tmp</value>
	    </property>

	hdfs-site.xml
		<property>
	        <name>dfs.replication</name>
	        <value>1</value>
	    </property>

	slaves    

5）启动hdfs
	格式化文件系统（仅第一次执行即可，不要重复执行）：在bin下hdfs ./hadoop namenode -format
	启动hdfs: sbin/start-dfs.sh
	验证是否启动成功：
		jps
			DataNode
			SecondaryNameNode
			NameNode

		浏览器访问方式： http://hadoop000:50070

6）停止hdfs
	sbin/stop-dfs.sh
	./stop-dfs.sh 



Hadoop shell的基本使用
将hadoop文件夹配置到~/.bash_profile

hdfs dfs
hadoop fs

7）hadoop文件操作
hadoop fs -ls /
hadoop fs -put hello.txt /
hadoop fs -text /hello.txt
hadoop fs -mkdir /test
hadoop fs -mkdir -p /test/a/b
展示全部文件和文件夹
hadoop fs -lsr /
hadoop fs -copyFromLocal hello.txt /test/a/b

hadoop fs -rm /hello.txt
hadoop fs -rm -R /test


Java API操作HDFS文件
文件	1	311585484	hdfs://hadoop000:8020/hadoop-2.6.0-cdh5.7.0.tar.gz
文件夹	0	0	hdfs://hadoop000:8020/hdfsapi
文件	1	49	hdfs://hadoop000:8020/hello.txt
文件	1	40762	hdfs://hadoop000:8020/install.log

问题：我们已经在hdfs-site.xml中设置了副本系数为1，为什么此时查询文件看到的3呢？
 如果你是通过hdfs shell的方式put的上去的那么，才采用默认的副本系数1
 如果我们是java api上传上去的，在本地我们并没有手工设置副本系数，所以否则采用的是hadoop自己的副本系数
 
调试问题一览： 
1. ps用centos系统java找不到：使用root用户
 文件配置env有问题；路径写错误，
 50070端口打开
 https://blog.csdn.net/zx110503/article/details/78787483
2. ps用ubuntu系统顺序如下：
 1.winscp 服务器拒绝了SFTP连接，但它监听FTP连接
 https://blog.csdn.net/shenwb110/article/details/70228984
 
3.ssh: Could not resolve hostname hadoop: Name or service not known
需要将hadoop0对应的ip地址加到文件名/etc/hosts中。
如：192.168.80.100  hadoop0
export HADOOP_HOME=/home/admin/software/hadoop-2.6.0-cdh5.7.0
export PATH=$HADOOP_HOME/bin:$PATH
export HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_HOME/lib/native
export HADOOP_OPTS=”-Djava.library.path=$HADOOP_HOME/lib”

4.用java代码创建文件是，出现如下错误；
could only be replicated to 0 nodes instead of minReplication (=1). 
两种解决方法：
4.1关闭防火墙
firewall-cmd --state #查看默认防火墙状态（关闭后显示notrunning，开启后显示running） 
systemctl stop firewalld.service #
停止firewall systemctl disable firewalld.service #禁止firewall开机启动 
init 6 #重启虚拟机，然后再输入第一条命令查看防火墙状态
https://blog.csdn.net/zhezhebie/article/details/72881393

4.2打开datanode端口(此方法没有试)
或者打开端口
-A INPUT -m state --state NEW -m tcp -p tcp --dport 22 -j ACCEPT
-A INPUT -m state --state NEW -m tcp -p tcp --dport 80 -j ACCEPT
-A INPUT -m state --state NEW -m tcp -p tcp --dport 9000 -j ACCEPT
-A INPUT -m state --state NEW -m tcp -p tcp --dport 50020 -j ACCEPT
-A INPUT -m state --state NEW -m tcp -p tcp --dport 20060 -j ACCEPT
-A INPUT -m state --state NEW -m tcp -p tcp --dport 50075 -j ACCEPT
-A INPUT -m state --state NEW -m tcp -p tcp --dport 50090 -j ACCEPT
-A INPUT -m state --state NEW -m tcp -p tcp --dport 50030 -j ACCEPT
-A INPUT -m state --state NEW -m tcp -p tcp --dport 50070 -j ACCEPT
-A INPUT -m state --state NEW -m tcp -p tcp --dport 50010 -j ACCEPT
-A INPUT -m state --state NEW -m tcp -p tcp --dport 8020 -j ACCEPT
-A INPUT -m state --state NEW -m tcp -p tcp --dport 8021 -j ACCEPT
usermod  -l hadoop000 -d /home/hadoop000 -m admin

5.查看log
cat /home/admin/software/hadoop-2.6.0-cdh5.7.0/logs

<property>
    <name>hadoop.proxyuser.root.hosts</name>
    <value>*</value>
</property>
 
<property>
    <name>hadoop.proxyuser.root.groups</name>
    <value>*</value>
</property>

































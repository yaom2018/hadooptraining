package com.ehuanchuang.hadoop.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;


public class HDFSApp {

    public static final String HDFS_PATH = "hdfs://hadoop000:8020";
    FileSystem fileSystem = null;
    Configuration configuration = null;

    /*
     *创建目录
     */
    @Test
    public void mkdir() throws Exception{
        fileSystem.mkdirs(new Path("/hdfsapi/test"));
    }
    /*
    * 创建文件
     */
    @Test
    public void create() throws Exception{
        FSDataOutputStream output = fileSystem.create(new Path("/hdfsapi/test/b.txt"));
        output.write("hello hadoop".getBytes());
        output.flush();
        output.close();
    }

    /*
    *查看文件
     */
    @Test
    public void cat() throws Exception{
        FSDataInputStream input = fileSystem.open(new Path("/hdfsapi/test/b.txt"));
        IOUtils.copyBytes(input,System.out,1024);
        input.close();
    }

    /**
     * 重命名
     * @throws Exception
     */
    @Test
    public void rename() throws Exception{
        Path oldPath = new Path("/hdfsapi/test/b.txt");
        Path newPath = new Path("/hdfsapi/test/newB.txt");
        fileSystem.rename(oldPath,newPath);
    }

    /**
     * copy本地文件到HDFS
     * @throws Exception
     */
    @Test
    public void copyFileFromLocal() throws Exception{
        Path oldPath = new Path("D:\\30Training\\xiaowangzi.txt");
        Path newPath = new Path("/");
        fileSystem.copyFromLocalFile(oldPath,newPath);
    }

    /**
     * copy本地文件到HDFS
     * @throws Exception
     */
    @Test
    public void copyFileFromLocalWithProg() throws Exception{

        InputStream input = new BufferedInputStream(
                new FileInputStream(
                        new File("D:\\10DEVTOOL\\pdfeditor.exe")));
        FSDataOutputStream output = fileSystem.create(
                new Path("/hdfsapi/test/pdfeditor.exe"),
                new Progressable(){
                    public void progress(){
                        System.out.print("."); //进度条
                    }
                });
        IOUtils.copyBytes(input,output,4096);
    }

    /**
     * 从HDFS拉文件到本地
     * @throws Exception
     */
    @Test
    public void copyToLocalFile() throws Exception{
        Path localPath = new Path("D:\\10DEVTOOL\\temp\\pdfeditor.exe");
        Path hdfsPath = new Path("/hdfsapi/test/pdfeditor.exe");
        //Linux系统使用
//        fileSystem.copyToLocalFile(hdfsPath,localPath);
        //Win系统使用
        fileSystem.copyToLocalFile(false,hdfsPath,localPath,true);

    }

    /**
     * 文件遍历显示
     * @throws Exception
     */
    @Test
    public void listFiles() throws  Exception{
        FileStatus[] fileStatuses = fileSystem.listStatus(new Path("/hdfsapi/test"));
        for (FileStatus file: fileStatuses){
            String isDir = file.isDirectory() ? "文件夹" : "文件";
            short replication = file.getReplication();
            long len = file.getLen();
            String path = file.getPath().toString();
            System.out.println(isDir + "\t" + replication + "\t" + len + "\t" + path + "\t");
        }
    }

    /**
     * 删除
     * @throws Exception
     */
    @Test
    public void delete() throws  Exception{
        fileSystem.delete(new Path("/hdfsapi/test"),true);
    }

    @Before
    public void setUp() throws Exception {
        configuration = new Configuration();
        fileSystem = FileSystem.get(new URI(HDFS_PATH), configuration,"atlas");
    }

    @After
    public void tearDown() throws Exception{
        FileSystem fileSystem = null;
        Configuration configuration = null;
        System.out.println("HDFSApp.tearDown");

    }
}

package com.shaoteemo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Create Info: hdfs api
 * <br>Change Info:
 * <br>Create On 8/27/2024 22:08
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
public class HdfsApi {

    private static final Logger log = LogManager.getLogger(HdfsApi.class);

    /**
     * 创建目录
     */
    public void mkdirs() throws IOException {
        // 获取创建配置文件对象
        Configuration conf = new Configuration();
        // 配置NameNode
        conf.set("fs.defaultFS", "hdfs://192.168.20.101:9820");
        System.setProperty("HADOOP_USER_NAME", "root");
        // 创建文件系统对象
        FileSystem fs = FileSystem.get(conf);
        // 创建目录
        boolean result = fs.mkdirs(new Path("/java/idea"));
        System.out.println(result);

        // 关闭
        fs.close();
    }

    /**
     * 另一种创建方式
     */
    public void mkdirs(String test) throws URISyntaxException, IOException, InterruptedException {
        // 获取创建配置文件对象
        Configuration conf = new Configuration();
        // 配置NameNode
//        conf.set("fs.defaultFS", "hdfs://192.168.20.101:9820");
//        System.setProperty("HADOOP_USER_NAME", "root");
        // 创建文件系统对象
        FileSystem fs = FileSystem.get(new URI("hdfs://192.168.20.101:9820"), conf, "root");
        // 创建目录
        boolean result = fs.mkdirs(new Path("/java/idea2"));
        System.out.println(result);

        // 关闭
        fs.close();
    }

    /**
     * 文件上传
     */
    public void upload() throws URISyntaxException, IOException, InterruptedException {
        Configuration conf = new Configuration();
        // 参数的优先级遵循就近原则，默认的参数小于指定的优先级。这一点源码也可以体现出来。
        // 客户端代码中的参数设置的值 > 自定义配置文件中的参数的值（可以将配置文件放在resource目录下） > 服务器默认xxx-default.xml
        // 设置上传block大小,仅针对当前方法。默认128M。
        conf.set("dfs.blocksize", String.valueOf(1024 * 1024));
        // 设置副本数。默认3
        conf.set("dfs.replication", "2");
        FileSystem fs = FileSystem.get(new URI("hdfs://192.168.20.101:9820"), conf, "root");
        // 上传文件
        fs.copyFromLocalFile(new Path("F:/test.txt"), new Path("/java"));
        fs.close();
        System.out.println("上传结束。");
    }

    /**
     * 原始的上传
     */
    public void uploadOld() throws URISyntaxException, IOException, InterruptedException {
        Configuration conf = new Configuration();
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://192.168.20.101:9820"), conf, "root");
        InputStream is = null;
        OutputStream os = null;

        is = Files.newInputStream(Paths.get("F:/test.txt"));
        os = fileSystem.create(new Path("/java/newFile.txt"));

        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) != -1) {
            os.write(buf, 0, len);
        }

        os.flush();
        os.close();
        is.close();

        fileSystem.close();
        System.out.println("上传结束。");
    }

    /**
     * 文件名称变更与文件移动
     */
    public void changeFileName() throws Exception {
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://192.168.20.101:9820"), new Configuration(), "root");
        fileSystem.rename(new Path("/java/newFile.txt"), new Path("/java/changeFileName.txt"));
        fileSystem.close();
    }

    public void moveFile() throws Exception {
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://192.168.20.101:9820"), new Configuration(), "root");
        fileSystem.rename(new Path("/java/changeFileName.txt"), new Path("/java/idea/mvFile.txt"));
        fileSystem.close();
    }

    /**
     * 下载文件
     * 在windows环境需要win HADOOP环境。环境搭建见《README》
     */
    public void download() throws Exception {
        FileSystem fileSystem = getFileSystem();
        fileSystem.copyToLocalFile(false, new Path("/java/idea/mvFile.txt"), new Path("E:/download.txt"), true);
        fileSystem.close();
    }

    /**
     * 删除文件或目录
     */
    public void delete() throws Exception {
        FileSystem fileSystem = getFileSystem();
        // recursive参数指定删除文件或空目录可以是false，不为空的目录需要设置为ture。
        fileSystem.delete(new Path("/java"), true);
        fileSystem.close();

    }

    /**
     * 获取单个文件详情
     */
    public void getFileDetail() throws Exception {
        FileSystem fileSystem = getFileSystem();
        FileStatus fileStatus = fileSystem.getFileStatus(new Path("/java/test.txt"));
        System.out.println(fileStatus);
        System.out.println("是否为文件：" + fileStatus.isFile());
        System.out.println("是否为目录：" + fileStatus.isDirectory());
        fileSystem.close();
    }

    /**
     * 获取某一个目录或文件状态
     */
    public void getFilesDetail(FileSystem fileSystem, Path path) throws Exception {
        if (fileSystem == null) fileSystem = getFileSystem();
        FileStatus[] fileStatus = fileSystem.listStatus(path == null ? new Path("/") : path);
        for (FileStatus item : fileStatus) {
            System.out.println(item.getPath());
            System.out.println("\t文档属性：" + (item.isFile() ? "文件" : "目录"));
            if (item.isDirectory()) {
                try {
                    getFilesDetail(fileSystem, item.getPath());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 遍历文件。仅限文件
     */
    public void getFiles() throws Exception {
        FileSystem fileSystem = getFileSystem();
        // recursive：是否递归遍历
        RemoteIterator<LocatedFileStatus> result = fileSystem.listFiles(new Path("/"), true);
        while (result.hasNext()) {
            LocatedFileStatus fileStatus = result.next();
            System.out.println(fileStatus.getPath() + "/" + fileStatus.getPath().getName());
            //获取文件块信息
            BlockLocation[] blockLocations = fileStatus.getBlockLocations();
            //遍历文件块
            for (BlockLocation blockLocation : blockLocations) {
                //获取
                System.out.println("\t偏移量：" + blockLocation.getOffset());
                String[] hosts = blockLocation.getHosts();
                System.out.println("\thosts:" + Arrays.toString(hosts));
                String[] names = blockLocation.getNames();
                System.out.println("\tnames:" + Arrays.toString(names));
            }
        }
        fileSystem.close();
    }

    private FileSystem getFileSystem() throws Exception {
        return FileSystem.get(new URI("hdfs://192.168.20.101:9820"), new Configuration(), "root");
    }

    private FileSystem getFileSystem(Configuration configuration) throws Exception {
        return FileSystem.get(new URI("hdfs://192.168.20.101:9820"), configuration, "root");
    }
}

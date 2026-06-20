package com.shaoteemo;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Create Info: 测试类
 * <br>Change Info:
 * <br>Create On 8/27/2024 22:09
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
public class HdfsApiTest {

    private final HdfsApi api = new HdfsApi();

    /**
     * 创建目录
     */
    @Test
    public void mkdirs() throws IOException {
        api.mkdirs();
    }

    /**
     * 上传文件
     */
    @Test
    public void upload() throws URISyntaxException, IOException, InterruptedException {
        api.upload();
    }

    @Test
    public void changeFileName() throws Exception {
        api.changeFileName();
    }

    @Test
    public void moveFile() throws Exception {
        api.moveFile();
    }

    @Test
    public void download() throws Exception {
        api.download();
    }

    @Test
    public void del() throws Exception {
        api.delete();
    }

    @Test
    public void getFileDetail() throws Exception {
        api.getFileDetail();
    }

    @Test
    public void getFilesDetail() throws Exception {
        api.getFilesDetail(null, null);
    }

    @Test
    public void getFiles() throws Exception {
        api.getFiles();
    }
}
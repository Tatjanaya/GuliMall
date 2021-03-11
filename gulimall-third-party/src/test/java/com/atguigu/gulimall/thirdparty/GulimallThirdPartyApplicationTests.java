package com.atguigu.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    OSSClient ossClient;

    @Test
    public void testUpload() throws FileNotFoundException {
//        // Endpoint以杭州为例，其它Region请按实际情况填写
//        String endpoint = "http://oss-cn-beijing.aliyuncs.com";
//        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维
//        String accessKeyId = "LTAI4GFAafmwwXs1x382tmRW";
//        String accessKeySecret = "oppmHsjY7A0rN9ma7HmMsOAX0rvS9W";
//        // 创建OSSClient实例
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        // 上传文件流
        InputStream inputStream = new FileInputStream("C:\\Users\\lnj\\Pictures\\Camera Roll\\少女.png");
        ossClient.putObject("gulimall-lnj", "少女.png", inputStream);
        // 关闭OSSClient
        ossClient.shutdown();
        System.out.println("上传完成...");
    }

}

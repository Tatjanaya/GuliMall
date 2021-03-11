package com.atguigu.gulimall.product;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.gulimall.product.entity.BrandEntity;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

//    @Autowired
//    OSSClient ossClient;

    @Autowired
    CategoryService categoryService;

    @Test
    public void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));
    }

//    @Test
//    public void testUpload() throws FileNotFoundException {
////        // Endpoint以杭州为例，其它Region请按实际情况填写
////        String endpoint = "http://oss-cn-beijing.aliyuncs.com";
////        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维
////        String accessKeyId = "LTAI4GFAafmwwXs1x382tmRW";
////        String accessKeySecret = "oppmHsjY7A0rN9ma7HmMsOAX0rvS9W";
////        // 创建OSSClient实例
////        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//        // 上传文件流
//        InputStream inputStream = new FileInputStream("C:\\Users\\lnj\\Pictures\\Camera Roll\\丹霞.jpg");
//        ossClient.putObject("gulimall-lnj", "丹霞.jpg", inputStream);
//        // 关闭OSSClient
//        ossClient.shutdown();
//        System.out.println("上传完成...");
//    }

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("小米");
        brandEntity.setDescript("粗粮");
        brandService.save(brandEntity);
        System.out.println("保存成功...");
    }

}

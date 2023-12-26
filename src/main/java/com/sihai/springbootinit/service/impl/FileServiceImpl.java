package com.sihai.springbootinit.service.impl;

import cn.hutool.core.date.DateTime;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.sihai.springbootinit.service.FileService;
import com.sihai.springbootinit.utils.cos.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

/**
 * @author sihai
 * 阿里云对象存储实现类
 */
@Service
public class FileServiceImpl implements FileService {

    /**
     * 上传头像到OSS
     */
    @Override
    public String uploadFileAvatar(MultipartFile file) {

        //工具类获取值
        String bucketName = FileUtils.BUCKET_NAME;
        String region = FileUtils.REGION;
        InputStream inputStream = null;
        COSClient cosClient = initCos();
        try {
            // 把文件按照日期分类，获取当前日期
            String datePath = new DateTime().toString("yyyy-MM-dd");
            // 获取上传文件的输入流
            inputStream = file.getInputStream();
            // 获取文件名称
            String originalFileName = file.getOriginalFilename();

            // 拼接日期和文件路径
            String fileName = datePath + "/" + originalFileName;

            // 判断文件是否存在
            boolean exists = cosClient.doesObjectExist(bucketName, fileName);
            if (exists) {
                // 如果文件已存在，则先删除原来的文件再进行覆盖
                cosClient.deleteObject(bucketName, fileName);
            }

            // 创建上传 Object 的 Metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(inputStream.available());
            objectMetadata.setContentType(getcontentType(fileName.substring(fileName.lastIndexOf("."))));
            objectMetadata.setContentDisposition("attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 调用 COS 实例中的方法实现上传
            // 参数 1： Bucket 名称
            // 参数 2： 上传到 COS 文件路径和文件名称 /aa/bb/1.jpg
            // 参数 3： 上传文件的输入流
            cosClient.putObject(new PutObjectRequest(bucketName, fileName, inputStream, null));

            // 关闭 COSClient。
            cosClient.shutdown();

            // 把上传后文件路径返回，需要把上传到腾讯云 COS 路径手动拼接出来
            
            String url = "https://" + bucketName + ".cos." + region + ".myqcloud.com/" + fileName;
            return url;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Description: 判断服务文件上传时文件的contentType
     *
     * @param filenameExtension 文件后缀
     * @return String
     */
    public static String getcontentType(String filenameExtension) {
        if (filenameExtension.equalsIgnoreCase("bmp")) {
            return "image/bmp";
        }
        if (filenameExtension.equalsIgnoreCase("gif")) {
            return "image/gif";
        }
        if (filenameExtension.equalsIgnoreCase("jpeg") || filenameExtension.equalsIgnoreCase("jpg")
                || filenameExtension.equalsIgnoreCase("png")) {
            return "image/jpeg";
        }
        if (filenameExtension.equalsIgnoreCase("html")) {
            return "text/html";
        }
        if (filenameExtension.equalsIgnoreCase("txt")) {
            return "text/plain";
        }
        if (filenameExtension.equalsIgnoreCase("vsd")) {
            return "application/vnd.visio";
        }
        if (filenameExtension.equalsIgnoreCase("pptx") || filenameExtension.equalsIgnoreCase("ppt")) {
            return "application/vnd.ms-powerpoint";
        }
        if (filenameExtension.equalsIgnoreCase("docx") || filenameExtension.equalsIgnoreCase("doc")) {
            return "application/msword";
        }
        if (filenameExtension.equalsIgnoreCase("xml")) {
            return "text/xml";
        }
        return "application/octet-stream";
    }


    /**
     * 初始化COSClient
     *
     * @return
     */
    private COSClient initCos() {
        // 1 初始化用户身份信息（secretId, secretKey）
        BasicCOSCredentials credentials = new BasicCOSCredentials(FileUtils.SECRET_ID, FileUtils.SECRET_KEY);
        // 2 设置 bucket 的区域, COS 地域的简称请参照
        Region region = new Region(FileUtils.REGION);
        ClientConfig clientConfig = new ClientConfig(region);
        // 从 5.6.54 版本开始，默认使用了 https
//        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 3 生成 cos 客户端。
        return new COSClient(credentials, clientConfig);

    }

}

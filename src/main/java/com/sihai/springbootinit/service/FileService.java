package com.sihai.springbootinit.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author sihai
 */
public interface FileService {
    /**
     * 上传头像到OSS
     *
     * @param file
     * @return
     */
    String uploadFileAvatar(MultipartFile file);
}

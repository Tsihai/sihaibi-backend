package com.sihai.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.sihai.springbootinit.common.BaseResponse;
import com.sihai.springbootinit.common.ErrorCode;
import com.sihai.springbootinit.common.ResultUtils;
import com.sihai.springbootinit.exception.BusinessException;
import com.sihai.springbootinit.model.dto.image.UploadImageRequest;
import com.sihai.springbootinit.model.entity.Image;
import com.sihai.springbootinit.model.entity.User;
import com.sihai.springbootinit.model.enums.FileUploadBizEnum;
import com.sihai.springbootinit.service.ImageService;
import com.sihai.springbootinit.service.UserService;
import com.sihai.springbootinit.utils.image.AiImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;

/**
 * @Author sihai
 * @Date 2023/12/26 16:53
 */
@RestController
@RequestMapping("/image")
@Slf4j
public class ImageController {

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private ImageService imageService;

    @PostMapping("/upload")
    public BaseResponse<String> uploadImageAnalysis(@RequestPart("file") MultipartFile file,
                                                    UploadImageRequest uploadImageRequest, HttpServletRequest request) {

        String biz = "user_avatar";
        String goal = uploadImageRequest.getGoal();
        // 根据业务标识 biz 获取对应的 FileUploadBizEnum 枚举对象

        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取文件后缀名
        String fileSuffix = validFile(file, fileUploadBizEnum);
        // 获取登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 生成唯一文件名
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + file.getOriginalFilename();
        // 构建文件路径，根据业务和用户来划分
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        File newFile = null;

        try {
            // 上传文件
            newFile = File.createTempFile(filepath, null);

            file.transferTo(newFile);
            System.out.println(newFile);
            Image image = new Image();
            image.setGoal(goal);
            image.setImageType(fileSuffix);
            image.setBaseString("");
            boolean save = imageService.save(image);
            if (!save){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"新增图片信息失败");
            }
            AiImageUtils aiImageUtils = new AiImageUtils(redissonClient);
            String ans = aiImageUtils.getAns(newFile, goal,image.getId());
            image.setGenResult(ans);
            image.setState("succeed");
            boolean update = imageService.updateById(image);
            if (!update){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新图片信息失败");
            }
            // 返回可访问地址
            return ResultUtils.success(ans);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private String validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 5 * 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 5M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
        return fileSuffix;
    }
}

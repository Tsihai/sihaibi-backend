package com.sihai.springbootinit.controller;

import com.sihai.springbootinit.common.BaseResponse;
import com.sihai.springbootinit.common.ErrorCode;
import com.sihai.springbootinit.common.ResultUtils;
import com.sihai.springbootinit.exception.BusinessException;
import com.sihai.springbootinit.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author sihai
 * 文件上传接口
 */
@Api(tags = "文件管理")
@RestController
@RequestMapping("/oss")
//@CrossOrigin(origins = "http://localhost:8000", allowCredentials = "true")
public class FileController {

    @Resource
    private FileService ossService;

    /**
     * 上传头像
     *
     * @param file
     * @return
     */
    @ApiOperation(value = "文件上传")
    @PostMapping("/upload")
    public BaseResponse<String> uploadOssFile(@RequestPart("file") MultipartFile file) {
        //获取上传的文件
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "上传文件为空");
        }
        //返回上传到oss的路径
        String url = ossService.uploadFileAvatar(file);
        //返回url对象
        return ResultUtils.success(url);
    }
}

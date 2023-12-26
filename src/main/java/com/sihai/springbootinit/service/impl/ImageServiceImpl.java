package com.sihai.springbootinit.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sihai.springbootinit.mapper.ImageMapper;
import com.sihai.springbootinit.model.entity.Image;
import com.sihai.springbootinit.service.ImageService;
import org.springframework.stereotype.Service;

/**
* @author sihai
* @description 针对表【image(图片分析表)】的数据库操作Service实现
* @createDate 2023-12-13 22:42:19
*/
@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image>
    implements ImageService {

}





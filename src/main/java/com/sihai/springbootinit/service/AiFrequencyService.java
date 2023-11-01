package com.sihai.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sihai.springbootinit.model.entity.AiFrequency;

/**
* @author sihai
* @description 针对表【ai_frequency(ai调用次数表)】的数据库操作Service
* @createDate 2023-07-11 20:47:00
*/
public interface AiFrequencyService extends IService<AiFrequency> {

    /**
     * 调用智能分析接口次数自动减一
     */
    boolean invokeAutoDecrease(long userId);

    /**
     * 查看用户有无调用次数
     */
    boolean hasFrequency(long userId);

}

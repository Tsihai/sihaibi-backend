package com.sihai.springbootinit.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sihai.springbootinit.exception.R;
import com.sihai.springbootinit.model.entity.AiIntelligent;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author xiaobaitiao
* @description 针对表【t_ai_intelligent】的数据库操作Service
* @createDate 2023-08-27 18:44:26
*/
public interface AiIntelligentService extends IService<AiIntelligent> {

        /**
         * 根据用户ID 获取该用户和AI聊天的最近的五条消息
         * @param userId
         * @return
         */
        R<List<AiIntelligent>> getAiInformationByUserId(Long userId);
}

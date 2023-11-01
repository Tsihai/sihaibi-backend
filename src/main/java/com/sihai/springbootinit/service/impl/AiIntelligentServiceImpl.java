package com.sihai.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sihai.springbootinit.exception.R;
import com.sihai.springbootinit.mapper.AiIntelligentMapper;
import com.sihai.springbootinit.model.entity.AiIntelligent;
import com.sihai.springbootinit.service.AiIntelligentService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author xiaobaitiao
 * @description 针对表【t_ai_intelligent】的数据库操作Service实现
 * @createDate 2023-08-27 18:44:26
 */
@Service
public class AiIntelligentServiceImpl extends ServiceImpl<AiIntelligentMapper, AiIntelligent>
        implements AiIntelligentService {

    @Override
    public R<List<AiIntelligent>> getAiInformationByUserId(Long userId) {
        QueryWrapper<AiIntelligent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(userId != null && userId > 0, "userId", userId);
        queryWrapper.orderByDesc("createTime");
        queryWrapper.last("LIMIT 5");
        List<AiIntelligent> list = this.list(queryWrapper);
        if (list.size() == 0) {
            return R.success(null, "用户暂时没有和AI的聊天记录");
        }
        Collections.reverse(list);
        return R.success(list, "获取和AI最近的5条聊天记录成功");
    }
}





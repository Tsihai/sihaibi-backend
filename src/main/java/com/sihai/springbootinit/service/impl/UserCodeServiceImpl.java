package com.sihai.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sihai.springbootinit.common.ErrorCode;
import com.sihai.springbootinit.exception.BusinessException;
import com.sihai.springbootinit.exception.ThrowUtils;
import com.sihai.springbootinit.mapper.UserCodeMapper;
import com.sihai.springbootinit.model.entity.UserCode;
import com.sihai.springbootinit.service.UserCodeService;
import org.springframework.stereotype.Service;

/**
 * @author sihai
 * @description 针对表【user_code(用户)】的数据库操作Service实现
 * @createDate 2023-07-19 16:00:09
 */
@Service
public class UserCodeServiceImpl extends ServiceImpl<UserCodeMapper, UserCode>
        implements UserCodeService {

    @Override
    public UserCode getUserCodeByUserId(long userId) {
        if (userId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserCode> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId);
        UserCode userCode = this.getOne(wrapper);
        ThrowUtils.throwIf(userCode == null, ErrorCode.NULL_ERROR, "此用户不存在");
        return userCode;
    }
}





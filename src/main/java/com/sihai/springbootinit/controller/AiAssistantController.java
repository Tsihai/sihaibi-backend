package com.sihai.springbootinit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.sihai.springbootinit.annotation.AuthCheck;
import com.sihai.springbootinit.common.BaseResponse;
import com.sihai.springbootinit.common.DeleteRequest;
import com.sihai.springbootinit.common.ErrorCode;
import com.sihai.springbootinit.common.ResultUtils;
import com.sihai.springbootinit.constant.CommonConstant;
import com.sihai.springbootinit.constant.UserConstant;
import com.sihai.springbootinit.exception.BusinessException;
import com.sihai.springbootinit.exception.ThrowUtils;
import com.sihai.springbootinit.manager.RedisLimiterManager;
import com.sihai.springbootinit.model.dto.aiassistant.*;
import com.sihai.springbootinit.model.entity.AiAssistant;
import com.sihai.springbootinit.model.entity.User;
import com.sihai.springbootinit.model.enums.AiAssistantStatusEnum;
import com.sihai.springbootinit.service.AiAssistantService;
import com.sihai.springbootinit.service.AiFrequencyService;
import com.sihai.springbootinit.service.UserService;
import com.sihai.springbootinit.utils.SqlUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.sihai.springbootinit.constant.Mq.AiChatMqConstant.*;


/**
 * @author sihai
 */
@RestController
@RequestMapping("/aiAssistant")
@Api(tags = "AiAssistantController")
@Slf4j
public class AiAssistantController {

    @Resource
    private AiAssistantService aiAssistantService;

    @Resource
    private AiFrequencyService aiFrequencyService;

    @Resource
    private UserService userService;
    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private final static Gson GSON = new Gson();


    /**
     * 创建
     *
     * @param aiAssistantAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "新增对话")
    public BaseResponse<Long> addAiAssistant(@RequestBody AiAssistantAddRequest aiAssistantAddRequest, HttpServletRequest request) {
        if (aiAssistantAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiAssistant aiAssistant = new AiAssistant();
        BeanUtils.copyProperties(aiAssistantAddRequest, aiAssistant);
        User loginUser = userService.getLoginUser(request);
        aiAssistant.setUserId(loginUser.getId());
        boolean result = aiAssistantService.save(aiAssistant);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long aiAssistantId = aiAssistant.getId();
        return ResultUtils.success(aiAssistantId);
    }

    /**
     * 删除图表
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除对话")
    public BaseResponse<Boolean> deleteAiAssistant(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        AiAssistant oldAiAssistant = aiAssistantService.getById(id);
        ThrowUtils.throwIf(oldAiAssistant == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldAiAssistant.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = aiAssistantService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param aiAssistantUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation(value = "管理员更新对话信息")
    public BaseResponse<Boolean> updateAiAssistant(@RequestBody AiAssistantUpdateRequest aiAssistantUpdateRequest) {
        if (aiAssistantUpdateRequest == null || aiAssistantUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiAssistant aiAssistant = new AiAssistant();
        BeanUtils.copyProperties(aiAssistantUpdateRequest, aiAssistant);
        long id = aiAssistantUpdateRequest.getId();
        // 判断是否存在
        AiAssistant oldAiAssistant = aiAssistantService.getById(id);
        ThrowUtils.throwIf(oldAiAssistant == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = aiAssistantService.updateById(aiAssistant);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @ApiOperation(value = "根据Id获取对话")
    public BaseResponse<AiAssistant> getAiAssistantById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiAssistant aiAssistant = aiAssistantService.getById(id);
        if (aiAssistant == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(aiAssistant);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @return
     */
    @PostMapping("/list/page")
    @ApiOperation(value = "分页获取对话")
    public BaseResponse<Page<AiAssistant>> listAiAssistantByPage(@RequestBody AiAssistantQueryRequest aiAssistantQueryRequest) {
        long current = aiAssistantQueryRequest.getCurrent();
        long size = aiAssistantQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AiAssistant> assistantPage = aiAssistantService.page(new Page<>(current, size), getQueryWrapper(aiAssistantQueryRequest));
        return ResultUtils.success(assistantPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    @ApiOperation(value = "获取我的对话")
    public BaseResponse<Page<AiAssistant>> listMyAiAssistantByPage(@RequestBody AiAssistantQueryRequest aiAssistantQueryRequest,
                                                                   HttpServletRequest request) {
        if (aiAssistantQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        aiAssistantQueryRequest.setUserId(loginUser.getId());
        long current = aiAssistantQueryRequest.getCurrent();
        long size = aiAssistantQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AiAssistant> aiAssistantPage = aiAssistantService.page(new Page<>(current, size), getQueryWrapper(aiAssistantQueryRequest));
        return ResultUtils.success(aiAssistantPage);
    }

    /**
     * 管理员编辑图表
     *
     * @param aiAssistantEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    @ApiOperation(value = "编辑对话")
    public BaseResponse<Boolean> editAiAssistant(@RequestBody AiAssistantEditRequest aiAssistantEditRequest, HttpServletRequest request) {
        if (aiAssistantEditRequest == null || aiAssistantEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AiAssistant aiAssistant = new AiAssistant();
        BeanUtils.copyProperties(aiAssistantEditRequest, aiAssistant);

        User loginUser = userService.getLoginUser(request);
        long id = aiAssistantEditRequest.getId();
        // 判断是否存在
        AiAssistant oldAiAssistant = aiAssistantService.getById(id);
        ThrowUtils.throwIf(oldAiAssistant == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldAiAssistant.getUserId().equals(loginUser.getId()) && !userService.isAdmin((HttpServletRequest) loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = aiAssistantService.updateById(aiAssistant);
        return ResultUtils.success(result);
    }

    /**
     * 获取查询包装类
     *
     * @return 查询结果
     */
    private QueryWrapper<AiAssistant> getQueryWrapper(AiAssistantQueryRequest aiAssistantQueryRequest) {

        QueryWrapper<AiAssistant> queryWrapper = new QueryWrapper<>();
        if (aiAssistantQueryRequest == null) {
            return queryWrapper;
        }

        Long id = aiAssistantQueryRequest.getId();
        String questionGoal = aiAssistantQueryRequest.getQuestionGoal();
        String questionStatus = aiAssistantQueryRequest.getQuestionStatus();
        String questionResult = aiAssistantQueryRequest.getQuestionResult();
        Long userId = aiAssistantQueryRequest.getUserId();
        String sortField = aiAssistantQueryRequest.getSortField();
        String sortOrder = aiAssistantQueryRequest.getSortOrder();
        // 根据前端传来条件进行拼接查询条件
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionGoal), "questionGoal", questionGoal);
        queryWrapper.like(ObjectUtils.isNotEmpty(questionStatus), "questionStatus", questionStatus);
        queryWrapper.like(ObjectUtils.isNotEmpty(questionResult), "questionResult", questionResult);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);

        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     * AI 对话助手
     *
     * @param genChatByAiRequest
     * @param request
     * @return
     * @throws IOException
     */
    @PostMapping("/chat")
    @ApiOperation("AI 对话")
    public BaseResponse<?> aiAssistant(@RequestBody GenChatByAiRequest genChatByAiRequest, HttpServletRequest request) throws IOException {
        String questionGoal = genChatByAiRequest.getQuestionGoal();
        User loginUser = userService.getLoginUser(request);

        // 查询是否有调用次数
        boolean hasFrequency = aiFrequencyService.hasFrequency(loginUser.getId());
        if (!hasFrequency) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不足，请先充值！");
        }

        // 校验
        if (StringUtils.isBlank(questionGoal)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "问题描述为空");
        }

        // 用户每秒限流
        boolean tryAcquireRateLimit = redisLimiterManager.doRateLimit("Ai_Rate_" + loginUser.getId());
        if (!tryAcquireRateLimit) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }

        AiAssistant aiAssistant = new AiAssistant();
        aiAssistant.setQuestionGoal(questionGoal);

        // aiAssistant.setQuestionRes(result);
        // 生成等待
        aiAssistant.setQuestionStatus(AiAssistantStatusEnum.WAIT.getValue());
        aiAssistant.setUserId(loginUser.getId());
        
        // 插入到数据库
        boolean save = aiAssistantService.save(aiAssistant);

        String json = GSON.toJson(aiAssistant);
        rabbitTemplate.convertAndSend(AI_EXCHANGE_NAME, AI_ROUTING_KEY, json);

        // 调用次数减一
        boolean invokeAutoDecrease = aiFrequencyService.invokeAutoDecrease(loginUser.getId());
        ThrowUtils.throwIf(!invokeAutoDecrease, ErrorCode.PARAMS_ERROR, "次数减一失败");

        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "保存失败");
        return ResultUtils.success(aiAssistant);
    }

}

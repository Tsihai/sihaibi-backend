package com.sihai.springbootinit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sihai.springbootinit.common.BaseResponse;
import com.sihai.springbootinit.common.DeleteRequest;
import com.sihai.springbootinit.common.ErrorCode;
import com.sihai.springbootinit.common.ResultUtils;
import com.sihai.springbootinit.constant.CommonConstant;
import com.sihai.springbootinit.exception.BusinessException;
import com.sihai.springbootinit.exception.R;
import com.sihai.springbootinit.exception.ThrowUtils;
import com.sihai.springbootinit.manager.GuavaRateLimiterManager;
import com.sihai.springbootinit.manager.RedisLimiterManager;
import com.sihai.springbootinit.manager.SparkAIManager;
import com.sihai.springbootinit.model.dto.aiIntelligent.AiIntelligentChatRequest;
import com.sihai.springbootinit.model.dto.aiIntelligent.AiIntelligentQueryRequest;
import com.sihai.springbootinit.model.dto.aiIntelligent.AiIntelligentUpdateRequest;
import com.sihai.springbootinit.model.entity.AiAssistant;
import com.sihai.springbootinit.model.entity.AiIntelligent;
import com.sihai.springbootinit.model.entity.User;
import com.sihai.springbootinit.model.enums.AiAssistantStatusEnum;
import com.sihai.springbootinit.service.AiFrequencyService;
import com.sihai.springbootinit.service.AiIntelligentService;
import com.sihai.springbootinit.service.UserService;
import com.sihai.springbootinit.service.impl.AiIntelligentServiceImpl;
import com.sihai.springbootinit.utils.SqlUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/xf")
public class AiIntelligentController {
    @Resource
    private AiIntelligentService aiIntelligentService;

    @Resource
    private AiFrequencyService aiFrequencyService;

    @Resource
    private UserService userService;

    @Resource
    private RedisLimiterManager redisLimiterManager;


    @PostMapping("ai_intelligent")
    public R<String> aiRecommend(@RequestBody AiIntelligentChatRequest aiIntelligentChatRequest, HttpServletRequest request){

        // 判断用户输入文本是否过长，超过128字，直接返回，防止资源耗尽
        String message = aiIntelligentChatRequest.getInputMessage();
        if(StringUtils.isBlank(message)){
            return R.error("文本不能为空");
        }
        if (message.length() > 1024) {
            return R.error("文本字数过长");
        }
        // 查看用户接口次数是否足够，如果不够直接返回接口次数不够
        // 查询是否有调用次数
        User loginUser = userService.getLoginUser(request);
        boolean hasFrequency = aiFrequencyService.hasFrequency(loginUser.getId());
        if (!hasFrequency) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不足，请先充值！");
        }
        // 调用自己的密钥

        // 创建开发请求，设置模型id和消息内容

        StringBuilder stringBuilder = new StringBuilder();
        String presetInformation = "请根据输入信息作出回答" + ":";
        stringBuilder.append(presetInformation);
        stringBuilder.append(message);
        stringBuilder.append("\n");

        // 发送请求给AI，进行对话
        // 超时判断 利用ExecutorService
        SparkAIManager manager = new SparkAIManager(loginUser.getId() + "", false);
        String response;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        int timeout = 25; // 超时时间，单位为秒
        int sleepTime = getSleepTimeStrategy(message); // 等待时间
        Future<String> future = executor.submit(() -> {
            try {
                return manager.sendMessageAndGetResponse(stringBuilder.toString(),sleepTime);
            } catch (Exception exception) {
                throw new RuntimeException("遇到异常");
            }
        });

        try {
            response = future.get(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            return R.error("服务器内部错误，请稍后重试");
        }
//        // 关闭ExecutorService
        executor.shutdown();

        // 得到消息
//        System.out.println(response);
        AiIntelligent saveResult = new AiIntelligent();
        saveResult.setInputMessage(aiIntelligentChatRequest.getInputMessage());
        saveResult.setAiResult(response);
        saveResult.setUserId(loginUser.getId());

        boolean save = aiIntelligentService.save(saveResult);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取AI信息失败");
        }
        // 更新调用接口的次数 剩余接口调用次数-1.总共调用次数+1
        // 调用次数减一
        boolean invokeAutoDecrease = aiFrequencyService.invokeAutoDecrease(loginUser.getId());
        ThrowUtils.throwIf(!invokeAutoDecrease, ErrorCode.PARAMS_ERROR, "次数减一失败");

        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "调用接口失败");
        return R.success(response, "获取AI推荐信息成功");
    }

    @PostMapping("/my/list/page")
    @ApiOperation(value = "获取我的对话")
    public BaseResponse<Page<AiIntelligent>> listMyAiInformationByPage(@RequestBody AiIntelligentQueryRequest aiIntelligentQueryRequest,
                                                                   HttpServletRequest request) {
        if (aiIntelligentQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        aiIntelligentQueryRequest.setUserId(loginUser.getId());
        long current = aiIntelligentQueryRequest.getCurrent();
        long size = aiIntelligentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AiIntelligent> aiIntelligentPage = aiIntelligentService.page(new Page<>(current, size), getQueryWrapper(aiIntelligentQueryRequest));
        return ResultUtils.success(aiIntelligentPage);
    }

    private QueryWrapper<AiIntelligent> getQueryWrapper(AiIntelligentQueryRequest aiIntelligentQueryRequest) {

        QueryWrapper<AiIntelligent> queryWrapper = new QueryWrapper<>();
        if (aiIntelligentQueryRequest == null) {
            return queryWrapper;
        }

        Long id = aiIntelligentQueryRequest.getId();
        Long userId = aiIntelligentQueryRequest.getUserId();
        String inputMessage = aiIntelligentQueryRequest.getInputMessage();
        String aiResult = aiIntelligentQueryRequest.getAiResult();
        String sortField = aiIntelligentQueryRequest.getSortField();
        String sortOrder = aiIntelligentQueryRequest.getSortOrder();
        // 根据前端传来条件进行拼接查询条件
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(inputMessage), "inputMessage", inputMessage);
        queryWrapper.eq(ObjectUtils.isNotEmpty(aiResult), "aiResult", aiResult);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除对话")
    public BaseResponse<Boolean> deleteAiIntelligent(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        AiIntelligent oldAiIntelligent = aiIntelligentService.getById(id);
        ThrowUtils.throwIf(oldAiIntelligent == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldAiIntelligent.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = aiIntelligentService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 获取睡眠时间策略
     * @param message
     * @return
     */
    public static int getSleepTimeStrategy(String message){
        int length = message.length();
        if(length<20){
            return 10;
        }else if(length<=30){
            return 12;
        }else if(length<=50){
            return 15;
        }else{
            return 20;
        }
    }
}

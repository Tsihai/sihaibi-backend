package com.sihai.springbootinit.model.dto.aiassistant;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传请求
 *
 * @author sihai
 */
@Data
public class GenChatByAiRequest implements Serializable {


    /**
     * 问题概述
     */
    private String questionGoal;


    private static final long serialVersionUID = 1L;
}

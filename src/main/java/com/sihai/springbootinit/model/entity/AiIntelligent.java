package com.sihai.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sihai.springbootinit.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @TableName t_ai_intelligent
 */
@TableName(value ="ai_intelligent")
@Data
public class AiIntelligent implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户输入信息
     */
    private String inputMessage;
    /**
     * AI生成的信息
     */
   private String aiResult;

    /**
     * 用户id，标识是哪个用户的信息 可以为Null
     */
    private Long userId;

    /**
     * wait-等待,running-生成中,succeed-成功生成,failed-生成失败
     */
    private String questionStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}

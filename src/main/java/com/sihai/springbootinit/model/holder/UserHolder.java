package com.sihai.springbootinit.model.holder;


import com.sihai.springbootinit.model.vo.LoginUserVO;

/**
 * 后端存储登录的用户脱敏后的信息，不含账号密码，密钥，只能在单线程使用！
 */
public class UserHolder {
    private static final ThreadLocal<LoginUserVO> tl = new ThreadLocal<>();

    public static void saveUser(LoginUserVO user){
        tl.set(user);
    }

    public static LoginUserVO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}

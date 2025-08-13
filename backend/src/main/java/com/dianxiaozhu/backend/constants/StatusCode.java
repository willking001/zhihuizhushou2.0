package com.dianxiaozhu.backend.constants;

/**
 * 系统状态码常量定义
 * 此类集中定义了系统中使用的所有状态码及其含义
 */
public class StatusCode {

    /**
     * HTTP状态码
     */
    public static class Http {
        public static final int OK = 200;                     // 请求成功
        public static final int BAD_REQUEST = 400;            // 请求参数错误
        public static final int UNAUTHORIZED = 401;           // 未认证
        public static final int FORBIDDEN = 403;              // 权限不足
        public static final int NOT_FOUND = 404;              // 资源不存在
        public static final int TOO_MANY_REQUESTS = 429;      // 请求频率限制
        public static final int INTERNAL_SERVER_ERROR = 500;  // 服务器内部错误
    }

    /**
     * 业务状态码
     */
    public static class Business {
        public static final int SUCCESS = 20000;              // 操作成功
        public static final int ERROR = 50000;                // 操作失败
        public static final int LOGIN_ERROR = 50001;          // 用户名或密码错误
        public static final int TOKEN_EXPIRED = 50014;        // Token过期
        public static final int USER_NOT_FOUND = 50002;       // 用户不存在
        public static final int OPERATION_FAILED = 50003;     // 操作执行失败
    }

    /**
     * 获取状态码对应的消息
     * @param code 状态码
     * @return 状态码对应的消息
     */
    public static String getMessageByCode(int code) {
        switch (code) {
            // HTTP状态码消息
            case Http.OK:
                return "请求成功";
            case Http.BAD_REQUEST:
                return "请求参数错误";
            case Http.UNAUTHORIZED:
                return "用户未认证";
            case Http.FORBIDDEN:
                return "用户无权限";
            case Http.NOT_FOUND:
                return "请求资源不存在";
            case Http.TOO_MANY_REQUESTS:
                return "请求过于频繁";
            case Http.INTERNAL_SERVER_ERROR:
                return "服务器内部错误";
            
            // 业务状态码消息
            case Business.SUCCESS:
                return "操作成功";
            case Business.ERROR:
                return "操作失败";
            case Business.LOGIN_ERROR:
                return "用户名或密码错误";
            case Business.TOKEN_EXPIRED:
                return "Token已过期，请重新登录";
            case Business.USER_NOT_FOUND:
                return "用户不存在";
            case Business.OPERATION_FAILED:
                return "操作执行失败";
            
            default:
                return "未知错误";
        }
    }

    /**
     * 判断是否为成功的业务状态码
     * @param code 状态码
     * @return 是否成功
     */
    public static boolean isSuccessCode(int code) {
        return code == Business.SUCCESS;
    }

    /**
     * 判断是否需要重新登录的状态码
     * @param code 状态码
     * @return 是否需要重新登录
     */
    public static boolean isReLoginCode(int code) {
        return code == Http.UNAUTHORIZED || code == Http.FORBIDDEN || code == Business.TOKEN_EXPIRED;
    }
}
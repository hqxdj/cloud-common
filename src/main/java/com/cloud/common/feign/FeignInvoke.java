package com.cloud.common.feign;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeignInvoke {

    boolean isRpc() default false;

    boolean isAsync() default false;

    int timeout() default 3000;

}

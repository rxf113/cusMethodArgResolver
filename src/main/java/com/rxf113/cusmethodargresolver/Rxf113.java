package com.rxf113.cusmethodargresolver;

import java.lang.annotation.*;

/**
 * @author rxf113
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented

public @interface Rxf113 {

    String value() default "";

}

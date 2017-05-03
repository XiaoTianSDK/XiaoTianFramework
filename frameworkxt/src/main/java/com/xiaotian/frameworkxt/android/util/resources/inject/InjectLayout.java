package com.xiaotian.frameworkxt.android.util.resources.inject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author XiaoTian
 * @version 1.0.0
 * @name InjectLayout
 * @description
 * @date 2017/5/3
 * @link gtrstudio@qq.com
 * @copyright Copyright © 2010-2017 小天天 Studio, All Rights Reserved.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface InjectLayout {

}

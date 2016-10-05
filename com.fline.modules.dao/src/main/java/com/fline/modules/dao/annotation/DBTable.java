package com.fline.modules.dao.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DBTable {
	String realtablename() default "";
}

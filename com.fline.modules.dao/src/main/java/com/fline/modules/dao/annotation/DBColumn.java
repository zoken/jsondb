package com.fline.modules.dao.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DBColumn {
	boolean autoincreID() default false;

	boolean notempty() default true;

	boolean onlyquery() default false;

	String realcolumnname() default "";

	String defaultvalue() default "";

}

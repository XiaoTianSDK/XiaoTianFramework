package com.xiaotian.frameworkxt.net;

import java.io.Serializable;

/**
 * @version 1.0.0
 * @author XiaoTian
 * @name HttpParam
 * @description Http Request Param Package
 * @date 2013-12-14
 * @link gtrstudio@qq.com
 * @copyright Copyright © 2010-2013 小天天 Studio, All Rights Reserved.
 */
public class HttpParam implements Cloneable, Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private Object value;

	public HttpParam(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return getName() + "=" + getValue();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public int hashCode() {
		return name.hashCode() & value.hashCode();
	}
}

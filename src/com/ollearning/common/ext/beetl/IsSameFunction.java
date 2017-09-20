package com.ollearning.common.ext.beetl;

import org.beetl.core.Context;
import org.beetl.core.Function;

import com.jfinal.kit.StringKit;

/**
 * Created with IntelliJ IDEA. Author: iver Date: 13-4-20
 */
public class IsSameFunction implements Function {
	@Override
	public String call(Object[] params, Context ctx) {
		if (params.length != 3) {
			throw new RuntimeException("length of params must be 3 !");
		}
		if (params[0] != null && params[1] != null) {
			String one = params[0].toString();
			String two = params[1].toString();
			if (StringKit.notBlank(one, two) && one.equals(two)) {
				return params[2].toString();
			}
		}
		return null;
	}
}

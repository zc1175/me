package com.ollearning.common.util;

import com.ollearning.common.jfinal.Const;

public class SysValidate {

	public static void validate() {
		if (false == Const.SYS_VALID) {
			throw new RuntimeException("Licnese has expired");
		}
	}

}

package com.ollearning.jna;

import com.jfinal.kit.PathKit;
import com.sun.jna.Library;
import com.sun.jna.Native;

public class JnaUtil {

	static {
		String rootClassPath = PathKit.getRootClassPath();
		System.out.println(rootClassPath);
		System.load(rootClassPath + "/libhello.dylib");
	}

	public interface hello extends Library {
		void SayHello();

		int Sum(int a, int b);
	}

	public static void main(String[] args) {
		System.out.println(System.getProperty("java.library.path"));
		hello instance = (hello) Native.loadLibrary("hello", hello.class);
		instance.SayHello();
		System.out.println(instance.Sum(1, 2));
	}

}

package tests;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

public class Test2 {

	long ss_id;
	BigDecimal dec = new BigDecimal("1");

	@Before
	public void setUp() throws Exception {
		ss_id = System.currentTimeMillis();
	}

	@Test
	public void test() {
		System.out.println(String.valueOf(ss_id).substring(0, 9));
		System.out.println(dec);
	}

}

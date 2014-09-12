package tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import app_test_files.DbAttendant;
import app_test_files.Server;

public class TestCase {

	DbAttendant db_util;
	Properties props;
	Server servers;

	@Before
	public void setUp() throws Exception {
		servers = new Server();
		props = new Properties();
		 props.setProperty("user", "tj1955be");
		 props.setProperty("password", "becca333");
		 props.setProperty("url", "10.1.0.12:12000/Equinox");
		// props.setProperty("user", "dba");
		// props.setProperty("password", "secret");
		// props.setProperty("url", "192.168.1.251:5000/Equinox");
		db_util = new DbAttendant();
		Assert.assertTrue(db_util.getConnection(props));
		accs.add("01-01-01-1350901011;CR");
		accs.add("1110000064;DR");
		accs.add("1110000055;DR");
		accs.add("1110000054;DR");
	}

	String balance;

	@Test
	public void test_Get_balance() {
		balance = db_util.getBalance("GL", "01-00-37-01-2351017", "");
		Assert.assertNotNull(balance);
		System.out.println("Balance: " + balance.split(";")[0] + ""
				+ balance.split(";")[1]);
	}

	@Test
	public void testSetup() {
		Assert.assertTrue(db_util.is_validGL("Account", "1010000007"));
	}

	List<String> accs = new ArrayList<>();

	int i = 0;

	@Test
	public void testInsertCustom_data() {
		db_util.log_user("douglas");
		for (String ac : accs) {
			db_util.postBatch(ac.split(";")[0], "UGX", 6000000, i, "Douglas",
					ac.split(";")[1]);
			i++;
		}
	}

	@Test
	public void testCommit() {
		Assert.assertTrue(db_util.commit_post());
	}
}

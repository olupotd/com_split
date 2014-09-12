package app_test_files;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

public class DbAttendant {

	private Connection server_con;
	private Statement stmt;
	private String ov_date;

	private void get_ov_date() {
		try {
			ResultSet dt = stmt
					.executeQuery("select dateadd(day, 1, last_to_dt) as Yr from ov_control");
			while (dt.next()) {
				this.ov_date = dt.getObject("Yr").toString().split(" ")[0];
			}
			dt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean getConnection(Properties props) {
		try {
			Class.forName("com.sybase.jdbc3.jdbc.SybDriver").newInstance();
			server_con = DriverManager.getConnection(
					"jdbc:sybase:Tds:" + props.getProperty("url"), props);
			if (!server_con.isClosed()) {
				System.out.println("Connected");
				stmt = server_con.createStatement();
				get_ov_date();
				return true;
			}
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private String sql, user;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	private ResultSet rSet;
	private ResultSetMetaData rMeta;

	public String getBalance(String fromAct, String string2, String date) {
		// TODO Auto-generated method stub
		if (fromAct.contains("GL") || fromAct.contains("-")) {
			// Original statement
			// sql =
			// "select b.cur_bal, c.iso_code from gl_acct a, gl_balances b, ad_gb_crncy c where a.gl_acct_id = b.gl_acct_id and b.crncy_id = 1 and c.crncy_id = 1 and a.status = 'Active' and a.acct_type = 'GL' and a.acct_no = '"
			// + fromAct + "'";
			sql = "select sum(a.amt) as amount from gl_history a where a.create_dt = '"
					+ date
					+ "' and a.acct_type = 'GL' and a.acct_no = '"
					+ string2 + "'";
		} else
			sql = "exec get_balance '" + string2 + "'";
		try {
			System.out.println(sql);
			rSet = stmt.executeQuery(sql);
			rMeta = rSet.getMetaData();
			int col_count = rMeta.getColumnCount();
			String balance = "";
			while (rSet.next()) {
				for (int i = 1; i <= col_count; i++)
					if (rSet.getObject(i) != null)
						balance = rSet.getObject(i).toString();
					else {
						JOptionPane
								.showMessageDialog(null,
										"No transactions were found for '"
												+ date + "'");
						balance = "0.00";
					}
			}
			return balance;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public void rolBack() {
		try {
			server_con.rollback();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private BigDecimal tranCode;

	public boolean postBatch(String acct, String crncy, double amount,
			int item_count, String batch_user, String dr_or_cr) {
		try {
			if (acct.contains("-") && dr_or_cr.equals("CR"))
				tranCode = new BigDecimal(500);
			else if (acct.contains("-") && dr_or_cr.equals("DR"))
				tranCode = new BigDecimal(550);
			else if (!acct.contains("-") && dr_or_cr.equals("CR"))
				tranCode = new BigDecimal(117);
			else if (!acct.contains("-") && dr_or_cr.equals("DR"))
				tranCode = new BigDecimal(157);
			sql = "Declare @rnReturnOut int, @rsErrDescr  Varchar(45) \n exec nep_com_Ins '"
					+ crncy
					+ "','CRDB',"
					+ session_id // Session ID
					+ ","
					+ tranCode // Tran Code
					+ ",'Comisn:"
					+ ov_date // Tran Description
					+ "','"
					+ acct // Affected account number.
					+ "',"
					+ amount // Amount received/removed
					+ ","
					+ ov_date.split("-")[0] // Accounting year
					+ ",4,'N',"
					+ item_count
					+ ", @rnReturnOut Output,@rsErrDescr Output,'"
					+ batch_user
					+ "','COMMISION',";
			if (acct.contains("-"))
				sql = sql + "'GL'";
			else
				sql = sql + "'CA'";
			int done = stmt.executeUpdate(sql);
			if (done > 0)
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private String crncy;

	public boolean commit_post() {
		try {
			sql = "exec Nep_Com_Credit " + session_id + ", 'Commision:"
					+ ov_date + "','" + crncy + "'";
			int update = stmt.executeUpdate(sql);
			if (update > 0)
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	DecimalFormat df = new DecimalFormat("#.##");

	public List<String> partition(String amount, double[] ratios) {
		// TODO Auto-generated method stub
		List<String> results = new ArrayList<>();
		if (Double.parseDouble(amount) <= 0.0) {
			JOptionPane.showMessageDialog(null,
					"Your account balance can't make this transaction");
			return null;
		} else {
			int total = 0;
			for (int i = 0; i < ratios.length; i++) {
				total += ratios[i];
			}
			if (total == 100 || total == 1) {
				// System.out.println("Ratios are Correct..");
				if (total == 100) {
					for (int i = 0; i < ratios.length; i++) {
						results.add(df.format((ratios[i] * Double
								.parseDouble(amount)) / 100));
					}
				} else if (total == 1) {
					for (int i = 0; i < ratios.length; i++) {
						results.add(df.format((ratios[i] * Double
								.parseDouble(amount)) / 1));
					}
				}
			} else {
				JOptionPane
						.showMessageDialog(null,
								"Ratios entered Must be total to 100. Please try again");
			}
		}
		return results;
	}

	public void closeConnection() {
		try {
			if (server_con != null || !server_con.isClosed())
				server_con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean is_validGL(String type, String string) {
		String state = "";
		if (type.equalsIgnoreCase("GL"))
			sql = "select a.status from gl_acct a, gl_balances b, ad_gb_crncy c where a.gl_acct_id = b.gl_acct_id and b.crncy_id = 1 and c.crncy_id = 1 and a.acct_type = 'GL' and a.acct_no = '"
					+ string + "'";
		else
			sql = "select status from dp_display where acct_no = '" + string
					+ "'";
		try {
			ResultSet valid = stmt.executeQuery(sql);
			while (valid.next()) {
				state = valid.getString("status");
			}
			valid.close();
			if (state.contains("Active"))
				return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	String session_id, login_time;

	public void log_user(String user) {
		login_time = format.format(new Date());
		Statement st = null;
		try {
			Class.forName("org.sqlite.JDBC");
			Connection con = DriverManager
					.getConnection("jdbc:sqlite:split_generator.db");
			if (!con.isClosed()) {
				st = con.createStatement();
				st.executeUpdate("create table if not exists user(id integer primary key autoincrement, usern varchar(100) not null, login_time varchar(100) not null)");
			}
			// Add this user to the existing list of users
			st.executeUpdate("INSERT INTO user(usern, login_time) values('"
					+ user + "','" + login_time + "')");
			ResultSet timing = st
					.executeQuery("select max(id) as lg_id from user where usern = '"
							+ user + "'");
			while (timing.next()) {
				this.session_id = String.valueOf(timing.getInt("lg_id"));
				System.out.println("Login_id:" + session_id);
			}
			timing.close();
			con.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public DbAttendant() {
		super();

	}

}

package utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import properties.MysqlProperties2;

public class MYSQL_JDBCUtil2 extends LogFile2 {

	private MYSQL_JDBCUtil2() {
	}

	private static Connection conn;

	/**
	* DB接続を取得
	* @return Connection
	 * @throws Exception
	*/
	public static Connection getConn() throws Exception {
		/** ロガー. */
		//Logger logger = LoggerFactory.getLogger(MYSQL_JDBCUtil.class);
		try {
			Class.forName(MysqlProperties2.driver);
			String url = MysqlProperties2.url;
			String user = MysqlProperties2.user;
			String pwd = MysqlProperties2.pwd;
			conn = DriverManager.getConnection(url, user, pwd);
			// 自動コミット処理設定
			conn.setAutoCommit(false);
		} catch (Exception e) {
			//エラーメッセージ出力
			//logger.error("バッチの処理で、エラーが発生："+ e.getMessage(), e);
			throw e;
		}
		logger.info("MYSQL　接続完了");
		return conn;
	}

	/**
	*DB各Streamをclose
	* @param con
	* @param state
	* @param rs
	 * @throws Exception
	*/
	public static void close(Connection con, Statement state, ResultSet rs) throws Exception {
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception ex) {

			}
		}
		if (state != null) {
			try {
				state.close();
				logger.info("バッチの処理で、Statementオブジェクト終了　Mysql");
			} catch (Exception ex) {
				/*エラーメッセージ出力*/
				logger.error("Mysql_Statementオブジェクトを閉じる時エラーが発生：", ex);
				throw ex;
			}
		}

		if (con != null) {
			try {
				con.close();
				logger.info("バッチの処理で、Connectionオブジェクト終了 Mysql");
			} catch (Exception ex) {
				/*エラーメッセージ出力*/
				logger.error("Mysql_Connectionオブジェクトを閉じる時エラーが発生：", ex);
				throw ex;
			}
		}

	}

	/**
	 * DB各Streamをclose
	 * @param con
	 * @param state
	 * @throws Exception
	 */
	public static void close(Connection con, Statement state) throws Exception {

		if (state != null) {
			try {
				state.close();
				logger.info("バッチの処理で、Statementオブジェクト終了　Mysql");
			} catch (Exception ex) {
				/*エラーメッセージ出力*/
				logger.error("Mysql_Statementオブジェクトを閉じる時エラーが発生：", ex);
				throw ex;
			}
		}

		if (con != null) {
			try {
				con.close();
				logger.info("バッチの処理で、Connectionオブジェクト終了 Mysql");
			} catch (Exception ex) {
				logger.error("Mysql_Connectionオブジェクトを閉じる時エラーが発生：", ex);
				throw ex;
			}
		}
	}
}

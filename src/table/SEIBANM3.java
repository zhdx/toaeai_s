package table;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import utility.LogFile2;
import utility.MYSQL_JDBCUtil2;
import utility.Utils2;


/**
 * 製番マスター
 * @author zhang
 *
 */
public class SEIBANM3 extends LogFile2 {

	// Mysql接続を表すConnectionオブジェクトを初期化
	static Connection conMysql = null;
	// Statementオブジェクト(Mysql)
	static Statement stMysql = null;
//	// Statementオブジェクト(Mysql)
//	static Statement stMysql_2 = null;
//
	// Statementオブジェクト(Mysql)
	static Statement stMysql_3 = null;
//	// Statementオブジェクト(Mysql)
//	static Statement stMysql_4 = null;
	// Statementオブジェクト(Mysql)
	static Statement stMysql_5 = null;




	public static String doSEIBANM3() throws Exception {

		//■共通メソッド対象
		Utils2 utils = new Utils2();

		try {

			//MySqlに接続
			//Mysql_connection
			conMysql = MYSQL_JDBCUtil2.getConn();
			//Statementオブジェクト
			stMysql = conMysql.createStatement();
//			//Statementオブジェクト
//			stMysql_2 = conMysql.createStatement();
			//Statementオブジェクト
			stMysql_3 = conMysql.createStatement();
//			//Statementオブジェクト
//			stMysql_4 = conMysql.createStatement();
			//Statementオブジェクト
			stMysql_5 = conMysql.createStatement();


		} catch (Exception e) {

			/*エラーメッセージ出力*/
			logger.error("バッチの処理で、DB接続エラーが発生：'"+ e.getMessage(), e);
			throw e;
		}

		try {

			// 製番マスタ抜出条件（ステータスサイン(FSAIN)　＝　0(未完了)　
			// and　管理区分(FKANKUBUN)　＝　1(製品)　and　完成予定数(FYOTEISUU)　>　0）
			String sql = "SELECT"
					+ " fseiban       "
					+ " FROM "
					+ " seibanm  where ifnull(fsain, '') != 'C' and fkankubun = '1' and  fyoteisuu > 0";

			// 検索結果を取得
			ResultSet rsetSql_seibanm = stMysql.executeQuery(sql);
			logger.info("製番マスタテーブル検索結果--------処理開始！");
			// 問合せ結果の表示
			while (rsetSql_seibanm.next()) {
				// 製番
				String data_fseiban = utils.getData(rsetSql_seibanm.getString("fseiban"));

				// 仕掛原価
				BigDecimal data_WipCost =  getWipCost(data_fseiban);

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				String str_system_date = sdf.format(new Date());

				// 製番が既に製技開発テーブルに存在していた場合は、仕掛原価のみ更新する
				 String sql_status_up = " update manufacturing_dept_dev_status set wip_cost =  " + data_WipCost
						 + ",  updateusercode = 'system'"
						 + ",  updatedatetime = '" + str_system_date +"'"
						 + " where  fseiban = '" + data_fseiban + "'";
				    stMysql_5.executeUpdate(sql_status_up);

			}

		    logger.info("製番マスタテーブル検索結果--------処理終了！");


		    //MysqlDBをコミットする
		    conMysql.commit();

		} catch (Exception e) {

			// エラーが発生した場合、rollback処理
			if (conMysql != null) {
				conMysql.rollback();
			}

			//Statementオブジェクトを閉じる
			if (stMysql != null) {
				stMysql.close();
				logger.info("バッチの処理で、Statementオブジェクト終了　Mysql");
			}
			//Statementオブジェクトを閉じる
			if (stMysql_3 != null) {
				stMysql_3.close();
				logger.info("バッチの処理で、Statementオブジェクト終了　Mysql");
			}
			//Statementオブジェクトを閉じる
			if (stMysql_5 != null) {
				stMysql_5.close();
				logger.info("バッチの処理で、Statementオブジェクト終了　Mysql");
			}
			//エラーメッセージ出力
			logger.error("バッチの処理で、エラーが発生："+ e.getMessage(), e);
			throw e;

		} finally {
			//Statementオブジェクトを閉じる
			if (stMysql_3 != null) {
				stMysql_3.close();
				logger.info("バッチの処理で、Statementオブジェクト終了　Mysql");
			}
			//Statementオブジェクトを閉じる
			if (stMysql_5 != null) {
				stMysql_5.close();
				logger.info("バッチの処理で、Statementオブジェクト終了　Mysql");
			}
			// Mysql各Streamをclose
			MYSQL_JDBCUtil2.close(conMysql, stMysql);
		}

		logger.info("製番マスターテーブル処理　　　終了！");
		return null;

	}

	/*
	 * 仕掛原価の取得
	 */
	private static BigDecimal getWipCost(String temp_fseiban) throws SQLException {
		//Mysqlから検索結果
		ResultSet rsetSql_se_transf_wipcost = null;

		// 対象のNコードをキーに構成マスタを検索
		String sql_costm_cost = " SELECT "
				+ " sum(famt) as famt "
				+ " FROM "
				+ " se_transf where fseiban = '" + temp_fseiban + "'  AND ftran1 not in ('MRC', 'TRF')";

		rsetSql_se_transf_wipcost = stMysql_3.executeQuery(sql_costm_cost);
		// メーカ
		BigDecimal data_wipcost = null;

		while(rsetSql_se_transf_wipcost.next()) {
			data_wipcost = rsetSql_se_transf_wipcost.getBigDecimal("famt");
		}


		return data_wipcost;
	}


}

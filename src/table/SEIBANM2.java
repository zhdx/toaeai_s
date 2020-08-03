package table;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
public class SEIBANM2 extends LogFile2 {

	// Mysql接続を表すConnectionオブジェクトを初期化
	static Connection conMysql = null;
	// Statementオブジェクト(Mysql)
	static Statement stMysql = null;
	// Statementオブジェクト(Mysql)
	static Statement stMysql_2 = null;

	// Statementオブジェクト(Mysql)
	static Statement stMysql_3 = null;
	// Statementオブジェクト(Mysql)
	static Statement stMysql_4 = null;
	// Statementオブジェクト(Mysql)
	static Statement stMysql_5 = null;




	public static String doSEIBANM2() throws Exception {

		//■共通メソッド対象
		Utils2 utils = new Utils2();

		try {

			//MySqlに接続
			//Mysql_connection
			conMysql = MYSQL_JDBCUtil2.getConn();
			//Statementオブジェクト
			stMysql = conMysql.createStatement();
			//Statementオブジェクト
			stMysql_2 = conMysql.createStatement();
			//Statementオブジェクト
			stMysql_3 = conMysql.createStatement();
			//Statementオブジェクト
			stMysql_4 = conMysql.createStatement();
			//Statementオブジェクト
			stMysql_5 = conMysql.createStatement();


		} catch (Exception e) {

			/*エラーメッセージ出力*/
			logger.error("バッチの処理で、DB接続エラーが発生：'"+ e.getMessage(), e);
			throw e;
		}

		try {

			// A.製番マスタ抜出条件（ステータスサイン(FSAIN)！＝　C　
			// and　管理区分(FKANKUBUN)　＝　1(製品)　and　完成予定数(FYOTEISUU)　>　0）
			// リピート品の対象から以下コードを対象外とする。（NNNNNNN6、NNNNNN27、NNNNNN38）
			String sql = "SELECT"
					+ " fseiban, fmeisyou,  fpartno,  fyoteisuu, fyoteiymd,  fshijinouki  "
					+ " FROM "
					+ " seibanm  where ifnull(fsain, '') != 'C' and fkankubun = '1' and  fyoteisuu > 0   and fpartno not in ('NNNNNNN6', 'NNNNNN27', 'NNNNNN38') ";
			//+ " seibanm  where ifnull(fsain, '') != 'C' and fkankubun = '1' and  fyoteisuu > 0   and fpartno not in ('NNNNNNN6', 'NNNNNN27', 'NNNNNN38')  and fseiban='K0071202' ";
			//			+ " seibanm  where ifnull(fsain, '') != 'C' and fkankubun = '1' and  fyoteisuu > 0   and fpartno  in ('NNMP1090') ";



			// 検索結果を取得
			ResultSet rsetSql_seibanm = stMysql.executeQuery(sql);
			logger.info("製番マスタテーブル検索結果--------処理開始！");
			// 問合せ結果の表示
			while (rsetSql_seibanm.next()) {
				// 製番
				String data_fseiban = utils.getData(rsetSql_seibanm.getString("fseiban"));
				// 製番名称
				String data_fmeisyou = utils.getData2(rsetSql_seibanm.getString("fmeisyou"));
				// 品番（対象のNコード）
				String data_fpartno = utils.getData(rsetSql_seibanm.getString("fpartno"));
				// 完成予定数
				BigDecimal data_fyoteisuu = rsetSql_seibanm.getBigDecimal("fyoteisuu");
				//// 完成予定日
				//Timestamp data_fyoteiymd = rsetSql_seibanm.getTimestamp("fyoteiymd");
				// 指示納期
				Timestamp data_fshijinouki = rsetSql_seibanm.getTimestamp("fshijinouki");
				String data_fshijinouki_str = utils.getTimeStamp(data_fshijinouki);
				// 原価
				//BigDecimal data_fgenkaprice = rsetSql_seibanm.getBigDecimal("fgenkaprice");
				BigDecimal data_fgenkaprice = null;
				if (null != getCost(data_fpartno) &&  null != data_fyoteisuu) {
					data_fgenkaprice = getCost(data_fpartno).multiply(data_fyoteisuu).setScale(1, BigDecimal.ROUND_HALF_UP);
				}
				// 仕掛原価
				BigDecimal data_WipCost =  getWipCost(data_fseiban);



				if (data_fpartno != null && !data_fpartno.isEmpty()) {
					logger.info("製番マスタテーブルのNコード--------" + data_fpartno);
					// 対象のNコードをキーに構成マスタを検索
					// 構成失効日　FOUTDATE　に日付or・展開区分　FTKBN　に"X"がセットされているGA/工程コードは検索対象外とする
					String sql_bom = " SELECT "
							+ "  fcpart             "
							+ " FROM "
							+ " bom where fppart = '" + data_fpartno + "' and foutdate is null and ifnull(ftkbn,'Y') != 'X' ";

					ResultSet rsetSql_bom = stMysql_2.executeQuery(sql_bom);

					boolean checkResult = false;
					while (rsetSql_bom.next()) {

						// 子品番
						String data_fcpart = utils.getData(rsetSql_bom.getString("fcpart"));
						logger.info("構成マスタテーブルの子品番--------"+ data_fcpart);

						// ①　BAコードの場合、B１、B2存在チェックを行う
						if(data_fcpart != null && data_fcpart.startsWith("BA")) {
							 if(checkB1(data_fcpart)) {
								checkResult = true;
								break;
							 }
							 if(checkB2(data_fcpart)) {
								checkResult = true;
								break;
							 }

						}

						// ➁　KOTEIコードの場合、B2存在チェックを行う（KOTE）
						if(data_fcpart != null && data_fcpart.startsWith("KOTE")) {
							 if(checkB2(data_fcpart)) {
								checkResult = true;
								break;
							 }
						}

					}

					if (checkResult) {
						logger.info("存在チェック成功！　処理対象です。");

						// 採番
						int numTemp = getNo();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						String str_system_date = sdf.format(new Date());

						String sql_status_in = " insert into engineering_dept_dev_status "
								+ " ( "
								+ " status                 "
								+ " , develop_info_number  "
								+ " , fshopc               "
								+ " , usercode             "
								+ " , proposal_number      "
								+ " , proposal_status      "
								+ " , order_item_name      "
								+ " , customer_code        "
								+ " , customer_name        "
								+ " , sales_usercode       "
								+ " , n_code               "
								+ " , fseiban              "
								+ " , fpsname              "
								+ " , lot_number           "
								+ " , dev_status1          "
								+ " , deadline             "
								+ " , schedule             "
								+ " , cost                 "
								+ " , wip_cost             "
								+ " , base_info_remarks    "
								+ " , createusercode       "
								+ " , createdatetime       "

								+ " ) "
								+ " VALUES ( "
								+ " '06' "
								+ " , '" +  getStrNo(numTemp) + "'"
								+ " , '09300'  "
								+ " , ''  "
								+ " , ''  "
								+ " , ''  "
								+ " , " + data_fmeisyou
								+ " , '" + getCustomerCode(data_fpartno) + "'"
								+ " , '" +  getCustomerName(data_fpartno) + "'"
								+ " , '" +  getFstanto(data_fpartno) + "'"
								+ " , '" + data_fpartno + "'"
								+ " , '" + data_fseiban + "'"
								+ " , '" + getFpsname(data_fpartno) + "'"
								+ " , " + data_fyoteisuu
								+ " , ''  "
								+ " , " + data_fshijinouki_str
								+ " , " + null
								+ " , " + data_fgenkaprice
								+ " , " + data_WipCost
								+ " , ''  "
								+ " , 'system'  "
								+ " ,   '" + str_system_date + "' "
								+ ");";

						// 開発進捗ステータスに存在していない場合データ追加し、ステータスを「リピート」とする
						if (getCnt(data_fpartno, data_fseiban) <= 0 ) {
						    logger.info("開発進捗ステータステーブル登録用SQL文："+ sql_status_in);
						    stMysql_4.executeUpdate(sql_status_in);
							// 採番の情報を更新
							setNo(numTemp);
						} else if (getCnt(data_fpartno, data_fseiban) == 1) {
							 logger.info("開発進捗ステータステーブルに該当製番情報が存在しました。製番： " + data_fseiban + "  Nコード: " + data_fpartno );
							 // リピート品のJOB実行時、製番、 Nコードが既に開発テーブルに存在していた場合は、仕掛原価のみ更新する
							 String sql_status_up = " update engineering_dept_dev_status set wip_cost =  " + data_WipCost
									 + ",  updateusercode = 'system'"
									 + ",  updatedatetime = '" + str_system_date +"'"
									 + " where n_code = '" + data_fpartno  + "'  and fseiban = '" + data_fseiban + "'  and deleted is  null  ";
							 logger.info("更新のSQL文。" + sql_status_up);
							    stMysql_5.executeUpdate(sql_status_up);
						} else if (getCnt(data_fpartno, data_fseiban) > 1) {
							logger.info("開発進捗ステータステーブルに該当製番情報が複数存在しました。リピート品削除　製番： " + data_fseiban + "  Nコード: " + data_fpartno );
							 // リピート品のJOB実行時、製番、 Nコードが複数件存在した場合、リピート品データのdeleted欄に日付をセットする
							 String sql_status_de = " update engineering_dept_dev_status set deleted =  '" + str_system_date +"'"
									 + ",  updateusercode = 'system'"
									 + ",  updatedatetime = '" + str_system_date +"'"
									 + " where n_code = '" + data_fpartno  + "'  and fseiban = '" + data_fseiban + "' and status = '06' and deleted is  null  ";
							 logger.info("削除のSQL文。" + sql_status_de);
							 stMysql_5.executeUpdate(sql_status_de);

							 // リピート品のJOB実行時、製番、 Nコードが既に開発テーブルに存在していた場合は、仕掛原価のみ更新する
							 String sql_status_up2 = " update engineering_dept_dev_status set wip_cost =  " + data_WipCost
									 + ",  updateusercode = 'system'"
									 + ",  updatedatetime = '" + str_system_date +"'"
									 + " where n_code = '" + data_fpartno  + "'  and fseiban = '" + data_fseiban + "'  and deleted is  null  ";
							 logger.info("更新のSQL文。" + sql_status_up2);
							    stMysql_5.executeUpdate(sql_status_up2);
						}
					} else {
						logger.info("存在チェック不成功！　処理対象外です。");

					}

				}

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
			if (stMysql_2 != null) {
				stMysql_2.close();
				logger.info("バッチの処理で、Statementオブジェクト終了　Mysql");
			}
			//Statementオブジェクトを閉じる
			if (stMysql_3 != null) {
				stMysql_3.close();
				logger.info("バッチの処理で、Statementオブジェクト終了　Mysql");
			}
			//Statementオブジェクトを閉じる
			if (stMysql_4 != null) {
				stMysql_4.close();
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
			if (stMysql_2 != null) {
				stMysql_2.close();
				logger.info("バッチの処理で、Statementオブジェクト終了　Mysql");
			}
			//Statementオブジェクトを閉じる
			if (stMysql_3 != null) {
				stMysql_3.close();
				logger.info("バッチの処理で、Statementオブジェクト終了　Mysql");
			}
			//Statementオブジェクトを閉じる
			if (stMysql_4 != null) {
				stMysql_4.close();
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


	/**
	 * 開発情報管理Noの　採番
	 * @throws SQLException
	 */
	private static int getNo() throws SQLException {
		//Mysqlから検索結果
		ResultSet rsetSql_num = null;
		// 対象のNコードをキーに構成マスタを検索
		String sql_num = " SELECT enginnering_dept_sts_management_num as num FROM numbering; ";
		rsetSql_num = stMysql_3.executeQuery(sql_num);
		// メーカー
		int data_num = 0;
		while(rsetSql_num.next()) {
			data_num = rsetSql_num.getInt("num");
		}

//		// 次の番を更新する
//		int nextNum = data_num + 1;
//		String sql_num_up = " update numbering  set enginnering_dept_sts_management_num = " + nextNum;
//		stMysql_3.executeUpdate(sql_num_up);
//		//MysqlDBをコミットする
//		conMysql.commit();

		return data_num;
	}

	/**
	 * 開発情報管理Noの　採番
	 * @throws SQLException
	 */
	private static int setNo(int data_num) throws SQLException {
		// 次の番を更新する
		int nextNum = data_num + 1;
		String sql_num_up = " update numbering  set enginnering_dept_sts_management_num = " + nextNum;
		stMysql_4.executeUpdate(sql_num_up);

		return data_num;
	}

	/**
	 * 開発情報管理Noの　フォマット
	 * @throws SQLException
	 */
	private static String getStrNo(int data_num) throws SQLException {
		String strKotei = "E00000";
		String str_data_num = String.valueOf(data_num);
		String tempStr = strKotei.substring(0, 6 - str_data_num.length());

		return tempStr+str_data_num;
	}

	/**
	 * 品目マスタ　メーカーの取得
	 * @throws SQLException
	 */
	private static String getFmaker(String temp_fshopc) throws SQLException {
		//Mysqlから検索結果
		ResultSet rsetSql_itemm_fmaker = null;
		//■共通メソッド対象
		Utils2 utils = new Utils2();
		// 対象のNコードをキーに構成マスタを検索
		String sql_itemm_fmaker = " SELECT "
				+ "  fmaker             "
				+ " FROM "
				+ " itemm where fpartno = '" + temp_fshopc + "'";

		rsetSql_itemm_fmaker = stMysql_3.executeQuery(sql_itemm_fmaker);
		// メーカ
		String data_fmaker="";
		while(rsetSql_itemm_fmaker.next()) {
			data_fmaker = utils.getData3(rsetSql_itemm_fmaker.getString("fmaker"));
		}


		return data_fmaker;
	}


	/*
	 * 原価の取得
	 */
	private static BigDecimal getCost(String temp_fshopc) throws SQLException {
		//Mysqlから検索結果
		ResultSet rsetSql_costm_cost = null;

		// 対象のNコードをキーに構成マスタを検索
		String sql_costm_cost = " SELECT "
				+ "   IfNull(fst_zai_this,0) + IfNull(fst_gai_this,0) "
				+ " + IfNull(fst_mkako_this,0) + IfNull(fst_kkako_this,0) "
				+ " + IfNull(fst_kei_this,0) + IfNull(fst_othe1_this,0) as temp_cost   "
				+ " FROM "
				+ " costm where fpartno = '" + temp_fshopc + "'";

		rsetSql_costm_cost = stMysql_3.executeQuery(sql_costm_cost);
		// メーカ
		BigDecimal data_Cost = null;

		while(rsetSql_costm_cost.next()) {
			data_Cost = rsetSql_costm_cost.getBigDecimal("temp_cost");
		}


		return data_Cost;
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


	/**
	 * 品目マスタ　品目略称の取得
	 * @throws SQLException
	 */
	private static String getFpsname(String temp_fshopc) throws SQLException {
		//Mysqlから検索結果
		ResultSet rsetSql_itemm_fpsname = null;
		//■共通メソッド対象
		Utils2 utils = new Utils2();
		// 対象のNコードをキーに構成マスタを検索
		String sql_itemm_fmaker = " SELECT "
				+ "  fpsname             "
				+ " FROM "
				+ " itemm where fpartno = '" + temp_fshopc + "'";

		rsetSql_itemm_fpsname = stMysql_3.executeQuery(sql_itemm_fmaker);
		// メーカ
		String data_fpsname = "";
		while(rsetSql_itemm_fpsname.next()) {
			data_fpsname = utils.getData3(rsetSql_itemm_fpsname.getString("fpsname"));
		}


		return data_fpsname;
	}

	/**
	 * 得意先コード(fcust)の取得
	 * @throws SQLException
	 */
	private static String getCustomerCode(String temp_fshopc) throws SQLException {
		//Mysqlから検索結果
		ResultSet rsetSql_CustomerCode = null;
		//■共通メソッド対象
		Utils2 utils = new Utils2();
		// 取引先略称
		String str_name = "";
		if (temp_fshopc.length() > 3) {
			str_name = temp_fshopc.substring(1, 3);
		}
		// 社内担当者コード
		String sql_CustomerName= "select A.fcust, B.fshopname1 from n_fcust A, shopm B where A.fn2 = '" +  str_name + "' and A.fcust = B.fshopc ";

		rsetSql_CustomerCode = stMysql_3.executeQuery(sql_CustomerName);
		// 社内担当者コード
		String str_CustomerCode  = "";
		while(rsetSql_CustomerCode.next()) {
			str_CustomerCode = utils.getData3(rsetSql_CustomerCode.getString("fcust"));
		}

		return str_CustomerCode;
	}


	/**
	 * 部署名称１(FSHOPNAME1)の取得
	 * @throws SQLException
	 */
	private static String getCustomerName(String temp_fshopc) throws SQLException {
		//Mysqlから検索結果
		ResultSet rsetSql_CustomerName = null;
		//■共通メソッド対象
		Utils2 utils = new Utils2();
		// 取引先略称
		String str_name = "";
		if (temp_fshopc.length() > 3) {
			str_name = temp_fshopc.substring(1, 3);
		}
		// 社内担当者コード
		String sql_CustomerName= "select A.fcust, B.fshopname1 from n_fcust A, shopm B where A.fn2 = '" +  str_name + "' and A.fcust = B.fshopc ";

		rsetSql_CustomerName = stMysql_3.executeQuery(sql_CustomerName);
		// 社内担当者コード
		String str_CustomerName  = "";
		while(rsetSql_CustomerName.next()) {
			String str_fshopname1 = utils.getData3(rsetSql_CustomerName.getString("fshopname1"));

			str_CustomerName = str_fshopname1;

		}

		return str_CustomerName;
	}

	/**
	 * 部署マスタ(SHOPM)　社内担当者コード(fstanto)の取得
	 * @throws SQLException
	 */
	private static String getFstanto(String temp_fshopc) throws SQLException {
		//Mysqlから検索結果
		ResultSet rsetSql_shopm = null;
		//■共通メソッド対象
		Utils2 utils = new Utils2();
		// 取引先略称
		String str_name = "";
		if (temp_fshopc.length() > 3) {
			str_name = temp_fshopc.substring(1, 3);
		}
		// 社内担当者コード
		String sql_fstanto = "select B.fstanto from n_fcust A, shopm B where A.fn2 = '" +  str_name + "' and A.fcust = B.fshopc ";

		rsetSql_shopm = stMysql_3.executeQuery(sql_fstanto);
		// 社内担当者コード
		String str_fstanto  = "";
		while(rsetSql_shopm.next()) {
			str_fstanto = utils.getData3(rsetSql_shopm.getString("fstanto"));

		}

		return str_fstanto;
	}


	/**
	 * B-1.対象のNコードをキーに構成マスタを検索し、存在するBAコードで品名マスタ(ITEMM)を検索し、
	 * 基本在庫場所(FPLACE)＝09300のデータが存在するかチェックする
	 * 存在した場合、trueを戻す、存在しなかった場合、falseを戻す
	 * @throws SQLException
	 */
	private static boolean checkB1(String temp_fcpart) throws SQLException {
		boolean result = false;
		//Mysqlから検索結果
		ResultSet rsetSql_itemm = null;
		// 対象のNコードをキーに構成マスタを検索
		String sql_itemm = " SELECT "
				+ "  fpartno             "
				+ " FROM "
				+ " itemm where fpartno = '" + temp_fcpart  + "' and fplace = '09300' ";
		rsetSql_itemm = stMysql_3.executeQuery(sql_itemm);

		rsetSql_itemm.last();
		int rowCnt = 0;
		rowCnt = rsetSql_itemm.getRow();
		if (rowCnt > 0) {
			result = true;
		}


		return result;
	}


	/**
	 * B-2.対象のNコードをキーに構成マスタを検索し、存在するBAコード、KOTEIコードで
	 * 工程手順マスタ(ROUTEM)を検索し、部署コード(FSHOPC)＝09300が存在するかチェックする
	 * 存在した場合、trueを戻す、存在しなかった場合、falseを戻す
	 * @throws SQLException
	 */
	private static boolean checkB2(String temp_fcpart) throws SQLException {
		boolean result = false;
		//Mysqlから検索結果
		 ResultSet rsetSql_routem = null;
		// 対象のNコードをキーに構成マスタを検索
		String sql_routem = " SELECT "
				+ "  fpartno             "
				+ " FROM "
				+ " routem where fpartno = '" + temp_fcpart  + "' and fshopc = '09300' ";
		rsetSql_routem = stMysql_3.executeQuery(sql_routem);

		rsetSql_routem.last();
		int rowCnt = 0;
		rowCnt = rsetSql_routem.getRow();
		if (rowCnt > 0) {
			result = true;
		}

		return result;
	}


//	/**
//	 * 開発進捗ステータスに存在しているかチェックする（製番、Nコード）
//	 * @throws SQLException
//	 */
//	private static boolean checkC(String temp_fshopc, String temp_fseiban) throws SQLException {
//		boolean result = false;
//		//Mysqlから検索結果
//		 ResultSet rsetSql_status = null;
//		// 対象のNコードをキーに構成マスタを検索
//		String sql_status = " SELECT "
//				+ "  n_code             "
//				+ " FROM "
//				+ " engineering_dept_dev_status where n_code = '" + temp_fshopc  + "'  and fseiban = '" + temp_fseiban + "'";
//		rsetSql_status = stMysql_3.executeQuery(sql_status);
//		rsetSql_status.last();
//		int rowCnt = 0;
//		rowCnt = rsetSql_status.getRow();
//
//		if (rowCnt > 0) {
//			result = true;
//		}
//
//		return result;
//	}


	/**
	 * 開発進捗ステータスに存在しているかチェックする（製番、Nコード）
	 * @throws SQLException
	 */
	private static int getCnt(String temp_fshopc, String temp_fseiban) throws SQLException {
		boolean result = false;
		//Mysqlから検索結果
		 ResultSet rsetSql_status = null;
		// 対象のNコードをキーに構成マスタを検索
		String sql_status = " SELECT "
				+ "  n_code             "
				+ " FROM "
				+ " engineering_dept_dev_status where n_code = '" + temp_fshopc  + "'  and fseiban = '" + temp_fseiban + "' and deleted is null ";
		rsetSql_status = stMysql_3.executeQuery(sql_status);
		logger.info("開発進捗ステータスに存在チェックSQL文："+ sql_status);
		rsetSql_status.last();
		int rowCnt = 0;
		rowCnt = rsetSql_status.getRow();

		return rowCnt;
	}

}

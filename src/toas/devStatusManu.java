package toas;

import java.util.Date;

import org.apache.log4j.Logger;

import table.SEIBANM3;
import utility.LogFile2;

/**
 *製番マスタから次の条件で対象の情報を抜き出し、
 *製技部開発進捗ステータスにデータが存在していた場合、仕掛原価を更新する。
 */
public class devStatusManu {

	public static void main(String[] args) throws Exception {

		//ローグファイル呼び出し
		Logger logger = LogFile2.logWrite();
		// １）本バッチ開始（ロジック部分）
		logger.error("//////////////////////devStatusManu開始ーー" + new Date() + "ーー開始//////////////////////");
		logger.info("devStatusManuバッチの処理で、ロジックの処理開始！");


		try {
			SEIBANM3.doSEIBANM3();

		} catch (Exception e) {
			logger.error("該当バッチが実施中、予想以外のエラーが発生しまいました。！"+ e.getMessage(), e);
		}


		logger.info("devStatusManuバッチの処理で、ロジックの処理終了！！！");
		logger.error("//////////////////////devStatusManu終了ーー" + new Date() + "ーーToaEaiseibanm終了//////////////////////");
	}

}

package toas;

import java.util.Date;

import org.apache.log4j.Logger;

import table.SEIBANM2;
import utility.LogFile2;

/**
 *製番マスタから次の条件で対象の情報を抜き出し、
 *開発進捗ステータスに存在していない場合データ追加し、ステータスを「リピート」とする。
 */
public class devStatus {

	public static void main(String[] args) throws Exception {

		//ローグファイル呼び出し
		Logger logger = LogFile2.logWrite();
		// １）本バッチ開始（ロジック部分）
		logger.error("//////////////////////devStatus開始ーー" + new Date() + "ーー開始//////////////////////");
		logger.info("devStatusバッチの処理で、ロジックの処理開始！");


		try {
			SEIBANM2.doSEIBANM2();

		} catch (Exception e) {
			logger.error("該当バッチが実施中、予想以外のエラーが発生しまいました。！"+ e.getMessage(), e);
		}


		logger.info("devStatusバッチの処理で、ロジックの処理終了！！！");
		logger.error("//////////////////////devStatus終了ーー" + new Date() + "ーーToaEaiseibanm終了//////////////////////");
	}

}

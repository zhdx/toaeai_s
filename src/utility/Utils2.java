package utility;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Utils2 {
	/*
	 * 項目の変換処理、NULLの場合、空文字列戻す
	 */
	public String getData(String strData) {
		String returnStr = strData;

		//if (null == strData || strData.isEmpty() || strData.isBlank()) {
		if (null == strData) {
			return strData;
		}
		else if (strData.isEmpty() || strData.isBlank()) {

			return "";
		} else {
			if (returnStr.contains("'") ) {
				returnStr = returnStr.replace("'", "''");
			}
			if (returnStr.contains("\\") ) {
				returnStr = returnStr.replace("\\", "\\\\");
			}
		}

		return returnStr;
	}


	/*
	 * 項目の変換処理、NULLの場合、空文字列戻す
	 */
	public  String getData2(String strData) {
		String returnStr = strData;

		//if (null == strData || strData.isEmpty() || strData.isBlank()) {
		if (null == strData) {
			return strData;
		}
		else if (strData.isEmpty() || strData.isBlank()) {

			return "''";
		} else {
			if (returnStr.contains("'") ) {
				returnStr = returnStr.replace("'", "''");
			}
			if (returnStr.contains("\\") ) {
				returnStr = returnStr.replace("\\", "\\\\");
			}
		}

		return "'"+ returnStr + "'";
	}


	/*
	 * 項目の変換処理、NULLの場合、空文字列戻す
	 */
	public String getData3(String strData) {
		String returnStr = strData;

		//if (null == strData || strData.isEmpty() || strData.isBlank()) {
		if (null == strData) {
			return  "";
		}
		else if (strData.isEmpty() || strData.isBlank()) {

			return "";
		} else {
			if (returnStr.contains("'") ) {
				returnStr = returnStr.replace("'", "''");
			}
			if (returnStr.contains("\\") ) {
				returnStr = returnStr.replace("\\", "\\\\");
			}
		}

		return returnStr;
	}

	/*
	 * 日時の変換処理、NULL以外の場合、文字列変換戻す
	 */
	public String getTimeStamp(Timestamp strData) {
		String returnTimestamp = null;

		if (null != strData) {
			returnTimestamp = "'" + strData.toString() + "'";
		}

		return returnTimestamp;
	}

	/*
	 * 小数の変換処理、NULL以外の場合、文字列変換戻す
	 */
	public String getBigDecimal(BigDecimal a) {
		String returnBigDecimal = null;

		if (null != a) {
			returnBigDecimal = a.toString();
		}
		return returnBigDecimal;
	}

	/*
	 * 新システムのマスタデータ操作:更新0件の時、異常throw
	 */
	public void upException(int count) throws Exception {
		throw new Exception("新システムのマスタデータ操作:更新" + count + "件です!!!");
	}
}

package utility;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import toas.devStatus;

public class LogFile2 {

	//ローグファイル対象初期化
	public static Logger logger = null;

	public static Logger logWrite() {
		// ■ログ設定
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		System.setProperty("current.date", dateFormat.format(new Date()));

		logger = Logger.getLogger(devStatus.class);
		// 追加して確認<ここから>
		//URL fileXmlUrl = Loader.getResource("log4j.xml");
		//URL fileXmlUrl = Thread.currentThread().getContextClassLoader().getResource("log4j.xml");
		//DOMConfigurator.configure(fileXmlUrl);


		return logger;
	}
}

package properties;

import java.io.InputStream;
import java.util.Properties;

import utility.LogFile2;

public class MysqlProperties2 extends LogFile2 {
	// MySql JDBC Driver
    public static String driver = "";
    // MySql Url
    public static String url = "";
    // MySql 接続User
    public static String user = "";
    // MySql 接続Pass
    public static String pwd = "";
    //バッチ再実施間隔
    public static long timeCnt = 180000;

    static {
        try {
            InputStream input = MysqlProperties2.class.getClassLoader().getResourceAsStream("mysql.properties");
            Properties properties = new Properties();
            properties.load(input);
            driver = properties.getProperty("driver");
            url = properties.getProperty("url");
            user = properties.getProperty("user");
            pwd = properties.getProperty("pwd");
            //timeCnt = Long.valueOf(properties.getProperty("timecnt"));
        }catch(Exception e) {
			//エラーメッセージ出力
			logger.error("mysql.properties呼び出し時エラーが発生しました！！："+ e.getMessage(), e);
            //System.out.println("mysql.properties呼び出し時エラーが発生しました！！"+e);
        }

    }
}

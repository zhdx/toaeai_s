package toas;

public class TEST {

	public static void main(String[] args) throws Exception {

		int data_num = 99999;

		String strKotei = "E00000";
		String str_data_num = String.valueOf(data_num);
		String tempStr = strKotei.substring(0, 6 - str_data_num.length());

		System.out.println(tempStr+str_data_num);
	}

}

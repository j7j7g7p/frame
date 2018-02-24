package test2;

public class T1 {

	public static void main(String[] args) throws Exception {
		String s1 = "values(:id, :owner_type, :owner_id, :no, :amount, ifnull(:status, 2), :creator)";
		StringBuffer s2 = new StringBuffer();
		boolean keyword = false;
		StringBuffer s3 = new StringBuffer();
		for (int i = 0; i < s1.length(); i++) {
			char c = s1.charAt(i);
			if (c == ':') {
				keyword = true;
				s3 = new StringBuffer();
				continue;
			}
			if (keyword) {
				if (c == ',' || c == ' ' || c == ')') {
					System.out.println(s3.toString());
					s2.append("#" + s3 + "#");
					keyword = false;
				} else
					s3.append(c);
			}
			if (!keyword)
				s2.append(c);

		}
		System.out.println(s2);
	}

}

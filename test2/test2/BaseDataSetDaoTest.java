package test2;

import com.base.dao.HolderDataSetDao;
import com.base.dao.SQLMap;

public class BaseDataSetDaoTest {

	public static void main(String[] args) throws Exception {

		HolderDataSetDao dao = new HolderDataSetDao();

		String sql = "insert into fa_biz_wallet(id, owner_type, owner_id, no, amount, status, creator) "
				+ "values(:id, :owner_type, :owner_id, :no, :amount, ifnull(:status, 2), :creator)";
		SQLMap sqlMap = new SQLMap();
		sqlMap.setSQL(sql);
		sqlMap.put("id", "#id#");
		sqlMap.put("owner_id", "#owner_id#");
		sqlMap.put("owner_type", "#owner_type#");
		sqlMap.put("amount", "#amount#");
		sqlMap.put("status", "#status#");
		sqlMap.put("creator", "#creator#");
		sqlMap.put("no", "#no#");

		dao.insert(sqlMap);

	}

}

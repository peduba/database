import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.mysql.cj.jdbc.CallableStatement;
import com.mysql.cj.util.StringUtils;

public class Getcustomerdetails {
	Connection con=null;
	Statement stmt=null;
	ResultSet rs;
	java.sql.CallableStatement cstmt;
	ResultSet rs1;
	ResultSet rs2;
@BeforeClass
void setUp() throws SQLException
{
	con=DriverManager.getConnection("jdbc:mysql://localhost:3306/classicmodels","root","root");
	
}
//@Test()
void test_storedprocedureExists() throws SQLException
{
	stmt=con.createStatement();
	 rs=stmt.executeQuery("show procedure status where name='getcustomerdetails'");
	 rs.next();
	 Assert.assertEquals(rs.getString("Name"), "getcustomerdetails");
}
//@Test
void test_callCustomerdetails() throws SQLException
{
	cstmt=con.prepareCall("{call getcustomerdetails()}");
	rs1=cstmt.executeQuery();
	stmt=con.createStatement();
	rs2=stmt.executeQuery("select * from customers");
	Assert.assertEquals(compareresultSet(rs1,rs2), true);
	
}

//@Test
void test_getdetailsbyCity() throws SQLException
{
	cstmt=con.prepareCall("{call getcitydetails(?)}");
	cstmt.setString(1, "Singapore");
	rs1=cstmt.executeQuery();
	stmt=con.createStatement();
	rs2=stmt.executeQuery("select * from customers where city='Singapore'");
	Assert.assertEquals(compareresultSet(rs1,rs2), true);
}
//@Test
void test_getdetailsbyCityandpin() throws SQLException
{
	cstmt=con.prepareCall("{call getcustomersbycityandpin(?,?)}");
	cstmt.setString(1, "Singapore");
	cstmt.setString(2, "079903");
	rs1=cstmt.executeQuery();
	stmt=con.createStatement();
	rs2=stmt.executeQuery("select * from customers where city='Singapore' and postalcode='079903'");
	Assert.assertEquals(compareresultSet(rs1,rs2), true);
}
//@Test
void test_getorderDetails() throws SQLException
{
	cstmt=con.prepareCall("{call getorderdetails(?,?,?,?,?)}");
	cstmt.setInt(1, 141);
	cstmt.registerOutParameter(2, Types.INTEGER);
	cstmt.registerOutParameter(3, Types.INTEGER);
	cstmt.registerOutParameter(4, Types.INTEGER);
	cstmt.registerOutParameter(5, Types.INTEGER);
	cstmt.executeQuery();
	int shipped=cstmt.getInt(2);
	int cancelled=cstmt.getInt(3);
	int resolved=cstmt.getInt(4);
	int disputed=cstmt.getInt(5);
	
	stmt=con.createStatement();
	rs=stmt.executeQuery("select (select count(*)  from orders where customerNumber=141 and status='Shipped') as shipped ,(select count(*)  from orders where customerNumber=141 and status='Cancelled') as cancelled ,\r\n"
			+ "(select count(*) from orders where customerNumber=141 and status='Resolved') as resolved ,(select count(*) from orders where customerNumber=141 and status='Disputed') as disputed");
	rs.next();
	int exp_shipped=rs.getInt("shipped");
	int exp_cancelled=rs.getInt("cancelled");
	int exp_resolved=rs.getInt("resolved");
	int exp_disputed=rs.getInt("disputed");
	if(shipped==exp_shipped && cancelled==exp_cancelled && resolved==exp_resolved && disputed== exp_disputed)
		Assert.assertTrue(true);
	else
		Assert.assertTrue(false);


}
@Test
void getshippingdetails() throws SQLException
{
	cstmt=con.prepareCall("{call getshippingdetails(?,?)}");
	cstmt.setInt(1, 112);
	cstmt.registerOutParameter(2, Types.VARCHAR);
	
	cstmt.executeQuery();
	String shippingtime=cstmt.getString(2);
	
	
	stmt=con.createStatement();
	rs=stmt.executeQuery("select country,case when country='USA' then '2-days shipping'when country='CANADA' then '3-day shipping' ELSE '5-day shipping'End as shippingtime from customers where customerNumber=112");
	rs.next();
	String exp_shippingtime=rs.getString("shippingtime");
	Assert.assertEquals(shippingtime, exp_shippingtime);
	


}
@AfterClass
void tearDown() throws SQLException
{
	con.close();
}
 public boolean compareresultSet(ResultSet rs1,ResultSet rs2) throws SQLException
{
	while(rs1.next())
	{
		rs2.next();
		int count=rs1.getMetaData().getColumnCount();
		for(int i=1;i<=count;i++)
		{
			if(!org.apache.commons.lang3.StringUtils.equals(rs1.getString(i), rs2.getString(i)))
			{
				return false;
			}
		}
	}
	return true;
}
}

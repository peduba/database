package SFTesting;

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

public class getCustomerLevel {
	Connection con=null;
	Statement stmt=null;
	ResultSet rs;
	ResultSet rs1;
	ResultSet rs2;
	java.sql.CallableStatement cstmt;
	@BeforeClass
	void setUp() throws SQLException
	{
		con=DriverManager.getConnection("jdbc:mysql://localhost:3306/classicmodels","root","root");
		
	}
	//@Test()
	void test_CustomerLevel() throws SQLException
	{
		rs=con.createStatement().executeQuery("show function status where name='customerlevel'");
		rs.next();
		String actualvalue=rs.getString("Name");
		Assert.assertEquals(actualvalue, "customerLevel");
	}
	//@Test
	void test_customerlevel() throws SQLException
	{
		rs1=con.createStatement().executeQuery("select customerName,customerLevel(creditLimit) from customers");
		rs2=con.createStatement().executeQuery("select customerName,case when creditLimit>50000 then 'PLATINUM'when creditLimit>=10000 and creditLimit<=50000 then 'GOLD'when creditLimit<10000 then 'SILVER'end as customerlevel from customers");
        Assert.assertEquals(compareresultSet(rs1,rs2), true);
	}
	@Test
	void test_customerLevel() throws SQLException
	{
		cstmt=con.prepareCall("{call getcustomerLevel(?,?)}");
		cstmt.setInt(1,131);
		cstmt.registerOutParameter(2, Types.VARCHAR);
		cstmt.executeQuery();
		String custlevel=cstmt.getString(2);
		rs=con.createStatement().executeQuery("select customerName,case when creditLimit>50000 then 'PLATINUM' when creditLimit>=10000 and creditLimit<=50000 then 'GOLD'when creditLimit<10000 then 'SILVER'end as customerlevel from customers");
		rs.next();
		String exp_customerlevel=rs.getString("customerlevel");
		Assert.assertEquals(custlevel, exp_customerlevel);
		
		
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

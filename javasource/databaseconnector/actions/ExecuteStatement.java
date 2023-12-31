// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package databaseconnector.actions;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.webui.CustomJavaAction;
import databaseconnector.impl.JdbcConnector;

/**
 * <p>
 * This Java action provides a consistent environment for Mendix projects to perform an arbitrary SQL statement on relational
 * external databases.
 * JDBC (Java Database Connectivity) API, a standard Java API, is used when this Java action attempts
 * to connect with a Relational Database for which a JDBC driver exists.
 * The JDBC drivers for the databases you want to connect to, must be placed inside the userlib directory of a project.
 * </p>
 * 
 * Do not use this Java action for SELECT queries.
 * This Java action returns number of affected rows.
 * The jdbcUrl argument must specify a database URL address that points to your relational database and is dependent
 * upon the particular database and JDBC driver. It will always begin with "jdbc:" protocol, but the rest is up to particular vendor.
 * For example 'jdbc:mysql://hostname/databaseName' jdbcUrl format can be used for MySQL databases.
 * Note: Proper security must be applied as this action can allow SQL Injection in your Mendix application.
 * 
 * @see    JdbcConnector
 * @since  Mendix World 2016
 * @param <String> jdbcUrl
 *    A database URL address that points to your database.
 * 
 * @param <String> userName 
 *    The user name for logging into the database, relative to the jdbcUrl argument.
 * 
 * @param <String> password 
 *    The password for logging into the database, relative to the jdbcUrl argument.
 * 
 * @param <String> sql
 *    The SQL statement to be performed, relative to the database type.
 * 
 * @return <Integer/Long>
 *    Number of affected rows.
 */
public class ExecuteStatement extends CustomJavaAction<java.lang.Long>
{
	private java.lang.String jdbcUrl;
	private java.lang.String userName;
	private java.lang.String password;
	private java.lang.String sql;

	public ExecuteStatement(IContext context, java.lang.String jdbcUrl, java.lang.String userName, java.lang.String password, java.lang.String sql)
	{
		super(context);
		this.jdbcUrl = jdbcUrl;
		this.userName = userName;
		this.password = password;
		this.sql = sql;
	}

	@java.lang.Override
	public java.lang.Long executeAction() throws Exception
	{
		// BEGIN USER CODE
	  return connector.executeStatement(jdbcUrl, userName, password, sql);
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 * @return a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "ExecuteStatement";
	}

	// BEGIN EXTRA CODE
	private final ILogNode logNode = Core.getLogger(this.getClass().getName());

  private final JdbcConnector connector = new JdbcConnector(logNode);
	// END EXTRA CODE
}

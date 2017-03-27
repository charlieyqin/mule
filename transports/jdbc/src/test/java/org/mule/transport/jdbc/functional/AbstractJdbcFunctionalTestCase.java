/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.jdbc.functional;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.mule.api.MuleMessage;
import org.mule.tck.AbstractServiceAndFlowTestCase;
import org.mule.tck.util.MuleDerbyTestUtils;
import org.mule.transport.jdbc.JdbcConnector;
import org.mule.transport.jdbc.JdbcUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;

public abstract class AbstractJdbcFunctionalTestCase extends AbstractServiceAndFlowTestCase
{

    protected static final String[] TEST_VALUES = {"Test", "The Moon", "Terra"};
    protected JdbcConnector jdbcConnector;

    private static final Logger LOGGER = getLogger(AbstractJdbcFunctionalTestCase.class);
    private boolean populateTestData = true;
   
    protected static String getConfig()
    {
        return "jdbc-connector.xml";
    }

    public AbstractJdbcFunctionalTestCase(ConfigVariant variant, String configResources)
    {
        super(variant, configResources);
    }            
    
    @Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();

        jdbcConnector = (JdbcConnector) muleContext.getRegistry().lookupConnector("jdbcConnector");

        try
        {
            deleteTable();
        }
        catch (Exception e)
        {
            createTable();
        }

        if (populateTestData)
        {
            populateTable();
        }
    }

    @Override
    protected void doTearDown() throws Exception
    {
        if (jdbcConnector != null)
        {
            deleteTable();
        }

        super.doTearDown();
    }

    protected void createTable() throws Exception
    {
        QueryRunner qr = jdbcConnector.getQueryRunner();
        qr.update(jdbcConnector.getConnection(), "CREATE TABLE TEST(ID INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 0)  NOT NULL PRIMARY KEY,TYPE INTEGER,DATA VARCHAR(255),ACK TIMESTAMP,RESULT VARCHAR(255))");
        LOGGER.debug("Table created");
    }
    
    protected void deleteTable() throws Exception
    {
        QueryRunner qr = jdbcConnector.getQueryRunner();
        int updated = qr.update(jdbcConnector.getConnection(), "DELETE FROM TEST");
        LOGGER.debug(updated + " rows deleted");
    }
    
    protected void populateTable() throws Exception
    {
        QueryRunner qr = jdbcConnector.getQueryRunner();
        int updated;
        updated = qr.update(jdbcConnector.getConnection(), "INSERT INTO TEST(TYPE, DATA) VALUES (1, '" + TEST_VALUES[0] + "')");
        LOGGER.debug(updated + " rows updated");
        updated = qr.update(jdbcConnector.getConnection(), "INSERT INTO TEST(TYPE, DATA) VALUES (2, '" + TEST_VALUES[1] + "')");
        LOGGER.debug(updated + " rows updated");
        updated = qr.update(jdbcConnector.getConnection(), "INSERT INTO TEST(TYPE, DATA) VALUES (3, '" + TEST_VALUES[2] + "')");
        LOGGER.debug(updated + " rows updated");
    }

    @BeforeClass
    public static void startDatabase() throws Exception
    {
        MuleDerbyTestUtils.defaultDerbyCleanAndInit("derby.properties", "database.name");
    }

    @AfterClass
    public static void stopDatabase() throws SQLException
    {
        MuleDerbyTestUtils.stopDatabase();
    }

    /*
     * org.apache.commons.dbutils.ResultSetHandler (called by QueryRunner which is
     * called by JdbcMessageReceiver) allows either null or a List of 0 rows to be
     * returned so we check for both.
     */
    protected static void assertResultSetEmpty(MuleMessage message)
    {
        assertNotNull(message);
        Object payload = message.getPayload();
        assertTrue(payload instanceof java.util.List);
        List list = (List)payload;
        assertTrue(list.isEmpty());
    }

    protected static void assertResultSetNotEmpty(MuleMessage message)
    {
        assertNotNull(message);
        Object payload = message.getPayload();
        assertTrue(payload instanceof java.util.List);
        List list = (List)payload;
        assertFalse(list.isEmpty());
    }

    public boolean isPopulateTestData()
    {
        return populateTestData;
    }

    public void setPopulateTestData(boolean populateTestData)
    {
        this.populateTestData = populateTestData;
    }
    
    protected List execSqlQuery(String sql) throws Exception
    {
        Connection con = null;
        try
        {
            con = jdbcConnector.getConnection();
            return (List)new QueryRunner().query(con, sql, new ArrayListHandler());
        }
        finally
        {
            JdbcUtils.close(con);
        }
    }

    protected int execSqlUpdate(String sql) throws Exception
    {
        Connection con = null;
        try
        {
            con = jdbcConnector.getConnection();
            return new QueryRunner().update(con, sql);
        }
        finally
        {
            JdbcUtils.close(con);
        }
    }

    protected Integer getCountWithType1() throws Exception
    {
        return (Integer) ((Object[]) execSqlQuery("select count(*) from TEST where TYPE = 1").get(0))[0];
    }

    protected Integer getCountWithType2() throws Exception
    {
        return (Integer) ((Object[]) execSqlQuery("select count(*) from TEST where TYPE = 2").get(0))[0];
    }

    protected Integer getCountWithType3() throws Exception
    {
        return (Integer) ((Object[]) execSqlQuery("select count(*) from TEST where TYPE = 3").get(0))[0];
    }
    
}



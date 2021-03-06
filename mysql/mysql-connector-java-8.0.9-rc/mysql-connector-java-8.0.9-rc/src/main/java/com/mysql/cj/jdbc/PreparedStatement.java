/*
 * Copyright (c) 2002, 2017, Oracle and/or its affiliates. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License, version 2.0, as published by the
 * Free Software Foundation.
 *
 * This program is also distributed with certain software (including but not
 * limited to OpenSSL) that is licensed under separate terms, as designated in a
 * particular file or component or in included license documentation. The
 * authors of MySQL hereby grant you an additional permission to link the
 * program and your derivative works with the separately licensed software that
 * they have included with MySQL.
 *
 * Without limiting anything contained in the foregoing, this file, which is
 * part of MySQL Connector/J, is also subject to the Universal FOSS Exception,
 * version 1.0, a copy of which can be found at
 * http://oss.oracle.com/licenses/universal-foss-exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License, version 2.0,
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301  USA
 */

package com.mysql.cj.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Array;
import java.sql.Clob;
import java.sql.Date;
import java.sql.JDBCType;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.mysql.cj.api.BindValue;
import com.mysql.cj.api.PreparedQuery;
import com.mysql.cj.api.ProfilerEvent;
import com.mysql.cj.api.Query;
import com.mysql.cj.api.QueryBindings;
import com.mysql.cj.api.conf.ReadableProperty;
import com.mysql.cj.api.jdbc.JdbcConnection;
import com.mysql.cj.api.jdbc.ParameterBindings;
import com.mysql.cj.api.jdbc.result.ResultSetInternalMethods;
import com.mysql.cj.api.mysqla.io.PacketPayload;
import com.mysql.cj.api.mysqla.result.ColumnDefinition;
import com.mysql.cj.api.result.Row;
import com.mysql.cj.core.CharsetMapping;
import com.mysql.cj.core.Constants;
import com.mysql.cj.core.Messages;
import com.mysql.cj.core.MysqlType;
import com.mysql.cj.core.conf.PropertyDefinitions;
import com.mysql.cj.core.exceptions.CJException;
import com.mysql.cj.core.exceptions.FeatureNotAvailableException;
import com.mysql.cj.core.exceptions.MysqlErrorNumbers;
import com.mysql.cj.core.exceptions.StatementIsClosedException;
import com.mysql.cj.core.profiler.ProfilerEventImpl;
import com.mysql.cj.core.result.Field;
import com.mysql.cj.core.util.StringUtils;
import com.mysql.cj.core.util.Util;
import com.mysql.cj.jdbc.exceptions.MySQLStatementCancelledException;
import com.mysql.cj.jdbc.exceptions.MySQLTimeoutException;
import com.mysql.cj.jdbc.exceptions.SQLError;
import com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping;
import com.mysql.cj.jdbc.result.CachedResultSetMetaData;
import com.mysql.cj.jdbc.result.ResultSetImpl;
import com.mysql.cj.jdbc.result.ResultSetMetaData;
import com.mysql.cj.mysqla.CancelQueryTask;
import com.mysql.cj.mysqla.ClientPreparedQuery;
import com.mysql.cj.mysqla.ClientPreparedQueryBindValue;
import com.mysql.cj.mysqla.ClientPreparedQueryBindings;
import com.mysql.cj.mysqla.ParseInfo;
import com.mysql.cj.mysqla.result.ByteArrayRow;
import com.mysql.cj.mysqla.result.MysqlaColumnDefinition;
import com.mysql.cj.mysqla.result.ResultsetRowsStatic;

/**
 * A SQL Statement is pre-compiled and stored in a PreparedStatement object. This object can then be used to efficiently execute this statement multiple times.
 * 
 * <p>
 * <B>Note:</B> The setXXX methods for setting IN parameter values must specify types that are compatible with the defined SQL type of the input parameter. For
 * instance, if the IN parameter has SQL type Integer, then setInt should be used.
 * </p>
 * 
 * <p>
 * If arbitrary parameter type conversions are required, then the setObject method should be used with a target SQL type.
 * </p>
 */
public class PreparedStatement extends com.mysql.cj.jdbc.StatementImpl implements java.sql.PreparedStatement, Query {

    /**
     * Does the batch (if any) contain "plain" statements added by
     * Statement.addBatch(String)?
     * 
     * If so, we can't re-write it to use multi-value or multi-queries.
     */
    protected boolean batchHasPlainStatements = false;

    protected MysqlParameterMetadata parameterMetaData;

    private java.sql.ResultSetMetaData pstmtResultMetaData;

    protected String batchedValuesClause;

    private boolean doPingInstead;

    private boolean compensateForOnDuplicateKeyUpdate = false;

    protected ReadableProperty<Boolean> useStreamLengthsInPrepStmts;
    protected ReadableProperty<Boolean> autoClosePStmtStreams;

    protected int rewrittenBatchSize = 0;

    /**
     * Creates a prepared statement instance
     */

    protected static PreparedStatement getInstance(JdbcConnection conn, String sql, String catalog) throws SQLException {
        return new PreparedStatement(conn, sql, catalog);
    }

    /**
     * Creates a prepared statement instance
     */
    protected static PreparedStatement getInstance(JdbcConnection conn, String sql, String catalog, ParseInfo cachedParseInfo) throws SQLException {
        return new PreparedStatement(conn, sql, catalog, cachedParseInfo);
    }

    @Override
    protected void initQuery() {
        this.query = new ClientPreparedQuery(this.session);
    }

    /**
     * Constructor used by server-side prepared statements
     * 
     * @param conn
     *            the connection that created us
     * @param catalog
     *            the catalog in use when we were created
     * 
     * @throws SQLException
     *             if an error occurs
     */
    protected PreparedStatement(JdbcConnection conn, String catalog) throws SQLException {
        super(conn, catalog);

        this.compensateForOnDuplicateKeyUpdate = this.session.getPropertySet()
                .getBooleanReadableProperty(PropertyDefinitions.PNAME_compensateOnDuplicateKeyUpdateCounts).getValue();
        this.useStreamLengthsInPrepStmts = this.session.getPropertySet().getBooleanReadableProperty(PropertyDefinitions.PNAME_useStreamLengthsInPrepStmts);
        this.autoClosePStmtStreams = this.session.getPropertySet().getBooleanReadableProperty(PropertyDefinitions.PNAME_autoClosePStmtStreams);
    }

    /**
     * Constructor for the PreparedStatement class.
     * 
     * @param conn
     *            the connection creating this statement
     * @param sql
     *            the SQL for this statement
     * @param catalog
     *            the catalog/database this statement should be issued against
     * 
     * @throws SQLException
     *             if a database error occurs.
     */
    public PreparedStatement(JdbcConnection conn, String sql, String catalog) throws SQLException {
        this(conn, sql, catalog, null);
    }

    /**
     * Creates a new PreparedStatement object.
     * 
     * @param conn
     *            the connection creating this statement
     * @param sql
     *            the SQL for this statement
     * @param catalog
     *            the catalog/database this statement should be issued against
     * @param cachedParseInfo
     *            already created parseInfo or null.
     * 
     * @throws SQLException
     */
    public PreparedStatement(JdbcConnection conn, String sql, String catalog, ParseInfo cachedParseInfo) throws SQLException {
        this(conn, catalog);

        try {
            ((PreparedQuery<?>) this.query).checkNullOrEmptyQuery(sql);
            ((PreparedQuery<?>) this.query).setOriginalSql(sql);
            ((PreparedQuery<?>) this.query).setParseInfo(cachedParseInfo != null ? cachedParseInfo : new ParseInfo(sql, this.session, this.charEncoding));
        } catch (CJException e) {
            throw SQLExceptionsMapping.translateException(e, this.exceptionInterceptor);
        }

        this.doPingInstead = sql.startsWith(PING_MARKER);

        initializeFromParseInfo();

    }

    public QueryBindings<?> getQueryBindings() {
        return ((PreparedQuery<?>) this.query).getQueryBindings();
    }

    /**
     * Returns this PreparedStatement represented as a string.
     * 
     * @return this PreparedStatement represented as a string.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.getClass().getName());
        buf.append(": ");

        try {
            buf.append(asSql());
        } catch (SQLException sqlEx) {
            buf.append("EXCEPTION: " + sqlEx.toString());
        }

        return buf.toString();
    }

    public void addBatch() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            QueryBindings<?> queryBindings = ((PreparedQuery<?>) this.query).getQueryBindings();
            queryBindings.checkAllParametersSet();
            this.query.addBatch(queryBindings.clone());
        }
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.batchHasPlainStatements = true;

            super.addBatch(sql);
        }
    }

    public String asSql() throws SQLException {
        return ((PreparedQuery<?>) this.query).asSql(false);
    }

    public String asSql(boolean quoteStreamsAndUnknowns) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            return ((PreparedQuery<?>) this.query).asSql(quoteStreamsAndUnknowns);
        }
    }

    @Override
    public void clearBatch() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.batchHasPlainStatements = false;

            super.clearBatch();
        }
    }

    /**
     * In general, parameter values remain in force for repeated used of a
     * Statement. Setting a parameter value automatically clears its previous
     * value. However, in some cases, it is useful to immediately release the
     * resources used by the current parameter values; this can be done by
     * calling clearParameters
     * 
     * @exception SQLException
     *                if a database access error occurs
     */
    public void clearParameters() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            for (BindValue bv : ((PreparedQuery<?>) this.query).getQueryBindings().getBindValues()) {
                bv.setNull(false);
                bv.setIsStream(false);
                bv.setMysqlType(MysqlType.NULL);
                bv.setByteValue(null);
                bv.setStreamValue(null, 0);
            }
        }
    }

    /**
     * Check to see if the statement is safe for read-only slaves after failover.
     * 
     * @return true if safe for read-only.
     * @throws SQLException
     */
    protected boolean checkReadOnlySafeStatement() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            return ((PreparedQuery<?>) this.query).getParseInfo().getFirstStmtChar() == 'S' || !this.connection.isReadOnly();
        }
    }

    /**
     * Some prepared statements return multiple results; the execute method
     * handles these complex statements as well as the simpler form of
     * statements handled by executeQuery and executeUpdate
     * 
     * @return true if the next result is a ResultSet; false if it is an update
     *         count or there are no more results
     * 
     * @exception SQLException
     *                if a database access error occurs
     */
    public boolean execute() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {

            JdbcConnection locallyScopedConn = this.connection;

            if (!this.doPingInstead && !checkReadOnlySafeStatement()) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.20") + Messages.getString("PreparedStatement.21"),
                        MysqlErrorNumbers.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
            }

            ResultSetInternalMethods rs = null;

            this.lastQueryIsOnDupKeyUpdate = false;

            if (this.retrieveGeneratedKeys) {
                this.lastQueryIsOnDupKeyUpdate = containsOnDuplicateKeyUpdateInSQL();
            }

            this.batchedGeneratedKeys = null;

            resetCancelledState();

            implicitlyCloseAllOpenResults();

            clearWarnings();

            if (this.doPingInstead) {
                doPingInstead();

                return true;
            }

            setupStreamingTimeout(locallyScopedConn);

            PacketPayload sendPacket = ((PreparedQuery<?>) this.query).fillSendPacket();

            String oldCatalog = null;

            if (!locallyScopedConn.getCatalog().equals(this.getCurrentCatalog())) {
                oldCatalog = locallyScopedConn.getCatalog();
                locallyScopedConn.setCatalog(this.getCurrentCatalog());
            }

            //
            // Check if we have cached metadata for this query...
            //
            CachedResultSetMetaData cachedMetadata = null;

            boolean cacheResultSetMetadata = locallyScopedConn.getPropertySet().getBooleanReadableProperty(PropertyDefinitions.PNAME_cacheResultSetMetadata)
                    .getValue();
            if (cacheResultSetMetadata) {
                cachedMetadata = locallyScopedConn.getCachedMetaData(((PreparedQuery<?>) this.query).getOriginalSql());
            }

            //
            // Only apply max_rows to selects
            //
            locallyScopedConn.setSessionMaxRows(((PreparedQuery<?>) this.query).getParseInfo().getFirstStmtChar() == 'S' ? this.maxRows : -1);

            rs = executeInternal(this.maxRows, sendPacket, createStreamingResultSet(),
                    (((PreparedQuery<?>) this.query).getParseInfo().getFirstStmtChar() == 'S'), cachedMetadata, false);

            if (cachedMetadata != null) {
                locallyScopedConn.initializeResultsMetadataFromCache(((PreparedQuery<?>) this.query).getOriginalSql(), cachedMetadata, rs);
            } else {
                if (rs.hasRows() && cacheResultSetMetadata) {
                    locallyScopedConn.initializeResultsMetadataFromCache(((PreparedQuery<?>) this.query).getOriginalSql(), null /* will be created */, rs);
                }
            }

            if (this.retrieveGeneratedKeys) {
                rs.setFirstCharOfQuery(((PreparedQuery<?>) this.query).getParseInfo().getFirstStmtChar());
            }

            if (oldCatalog != null) {
                locallyScopedConn.setCatalog(oldCatalog);
            }

            if (rs != null) {
                this.lastInsertId = rs.getUpdateID();

                this.results = rs;
            }

            return ((rs != null) && rs.hasRows());
        }
    }

    @Override
    protected long[] executeBatchInternal() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {

            if (this.connection.isReadOnly()) {
                throw new SQLException(Messages.getString("PreparedStatement.25") + Messages.getString("PreparedStatement.26"),
                        MysqlErrorNumbers.SQL_STATE_ILLEGAL_ARGUMENT);
            }

            if (this.query.getBatchedArgs() == null || this.query.getBatchedArgs().size() == 0) {
                return new long[0];
            }

            // we timeout the entire batch, not individual statements
            int batchTimeout = getTimeoutInMillis();
            setTimeoutInMillis(0);

            resetCancelledState();

            try {
                statementBegins();

                clearWarnings();

                if (!this.batchHasPlainStatements && this.rewriteBatchedStatements.getValue()) {

                    if (((PreparedQuery<?>) this.query).getParseInfo().canRewriteAsMultiValueInsertAtSqlLevel()) {
                        return executeBatchedInserts(batchTimeout);
                    }

                    if (!this.batchHasPlainStatements && this.query.getBatchedArgs() != null
                            && this.query.getBatchedArgs().size() > 3 /* cost of option setting rt-wise */) {
                        return executePreparedBatchAsMultiStatement(batchTimeout);
                    }
                }

                return executeBatchSerially(batchTimeout);
            } finally {
                this.query.getStatementExecuting().set(false);

                clearBatch();
            }
        }
    }

    /**
     * Rewrites the already prepared statement into a multi-statement
     * query of 'statementsPerBatch' values and executes the entire batch
     * using this new statement.
     * 
     * @return update counts in the same fashion as executeBatch()
     * 
     * @throws SQLException
     */

    protected long[] executePreparedBatchAsMultiStatement(int batchTimeout) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            // This is kind of an abuse, but it gets the job done
            if (this.batchedValuesClause == null) {
                this.batchedValuesClause = ((PreparedQuery<?>) this.query).getOriginalSql() + ";";
            }

            JdbcConnection locallyScopedConn = this.connection;

            boolean multiQueriesEnabled = locallyScopedConn.getPropertySet().getBooleanReadableProperty(PropertyDefinitions.PNAME_allowMultiQueries).getValue();
            CancelQueryTask timeoutTask = null;

            try {
                clearWarnings();

                int numBatchedArgs = this.query.getBatchedArgs().size();

                if (this.retrieveGeneratedKeys) {
                    this.batchedGeneratedKeys = new ArrayList<>(numBatchedArgs);
                }

                int numValuesPerBatch = ((PreparedQuery<?>) this.query).computeBatchSize(numBatchedArgs);

                if (numBatchedArgs < numValuesPerBatch) {
                    numValuesPerBatch = numBatchedArgs;
                }

                java.sql.PreparedStatement batchedStatement = null;

                int batchedParamIndex = 1;
                int numberToExecuteAsMultiValue = 0;
                int batchCounter = 0;
                int updateCountCounter = 0;
                long[] updateCounts = new long[numBatchedArgs];
                SQLException sqlEx = null;

                try {
                    if (!multiQueriesEnabled) {
                        locallyScopedConn.getSession().enableMultiQueries();
                    }

                    batchedStatement = this.retrieveGeneratedKeys
                            ? ((Wrapper) locallyScopedConn.prepareStatement(generateMultiStatementForBatch(numValuesPerBatch), RETURN_GENERATED_KEYS))
                                    .unwrap(java.sql.PreparedStatement.class)
                            : ((Wrapper) locallyScopedConn.prepareStatement(generateMultiStatementForBatch(numValuesPerBatch)))
                                    .unwrap(java.sql.PreparedStatement.class);

                    timeoutTask = startQueryTimer((StatementImpl) batchedStatement, batchTimeout);

                    numberToExecuteAsMultiValue = numBatchedArgs < numValuesPerBatch ? numBatchedArgs : numBatchedArgs / numValuesPerBatch;

                    int numberArgsToExecute = numberToExecuteAsMultiValue * numValuesPerBatch;

                    for (int i = 0; i < numberArgsToExecute; i++) {
                        if (i != 0 && i % numValuesPerBatch == 0) {
                            try {
                                batchedStatement.execute();
                            } catch (SQLException ex) {
                                sqlEx = handleExceptionForBatch(batchCounter, numValuesPerBatch, updateCounts, ex);
                            }

                            updateCountCounter = processMultiCountsAndKeys((StatementImpl) batchedStatement, updateCountCounter, updateCounts);

                            batchedStatement.clearParameters();
                            batchedParamIndex = 1;
                        }

                        batchedParamIndex = setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.query.getBatchedArgs().get(batchCounter++));
                    }

                    try {
                        batchedStatement.execute();
                    } catch (SQLException ex) {
                        sqlEx = handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex);
                    }

                    updateCountCounter = processMultiCountsAndKeys((StatementImpl) batchedStatement, updateCountCounter, updateCounts);

                    batchedStatement.clearParameters();

                    numValuesPerBatch = numBatchedArgs - batchCounter;

                    if (timeoutTask != null) {
                        // we need to check the cancel state now because we loose if after the following batchedStatement.close()
                        ((PreparedStatement) batchedStatement).checkCancelTimeout();
                    }

                } finally {
                    if (batchedStatement != null) {
                        batchedStatement.close();
                        batchedStatement = null;
                    }
                }

                try {
                    if (numValuesPerBatch > 0) {

                        batchedStatement = this.retrieveGeneratedKeys
                                ? locallyScopedConn.prepareStatement(generateMultiStatementForBatch(numValuesPerBatch), RETURN_GENERATED_KEYS)
                                : locallyScopedConn.prepareStatement(generateMultiStatementForBatch(numValuesPerBatch));

                        if (timeoutTask != null) {
                            timeoutTask.setQueryToCancel((Query) batchedStatement);
                        }

                        batchedParamIndex = 1;

                        while (batchCounter < numBatchedArgs) {
                            batchedParamIndex = setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.query.getBatchedArgs().get(batchCounter++));
                        }

                        try {
                            batchedStatement.execute();
                        } catch (SQLException ex) {
                            sqlEx = handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex);
                        }

                        updateCountCounter = processMultiCountsAndKeys((StatementImpl) batchedStatement, updateCountCounter, updateCounts);

                        batchedStatement.clearParameters();
                    }

                    if (timeoutTask != null) {
                        stopQueryTimer(timeoutTask, true, true);
                        timeoutTask = null;
                    }

                    if (sqlEx != null) {
                        throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.exceptionInterceptor);
                    }

                    return updateCounts;
                } finally {
                    if (batchedStatement != null) {
                        batchedStatement.close();
                    }
                }
            } finally {
                stopQueryTimer(timeoutTask, false, false);
                resetCancelledState();

                if (!multiQueriesEnabled) {
                    locallyScopedConn.getSession().disableMultiQueries();
                }

                clearBatch();
            }
        }
    }

    protected int setOneBatchedParameterSet(java.sql.PreparedStatement batchedStatement, int batchedParamIndex, Object paramSet) throws SQLException {
        QueryBindings<?> paramArg = (QueryBindings<?>) paramSet;

        BindValue[] bindValues = paramArg.getBindValues();

        for (int j = 0; j < bindValues.length; j++) {
            if (bindValues[j].isNull()) {
                batchedStatement.setNull(batchedParamIndex++, MysqlType.NULL.getJdbcType());
            } else {
                if (bindValues[j].isStream()) {
                    batchedStatement.setBinaryStream(batchedParamIndex++, bindValues[j].getStreamValue(), bindValues[j].getStreamLength());
                } else {
                    ((com.mysql.cj.jdbc.PreparedStatement) batchedStatement).setBytesNoEscapeNoQuotes(batchedParamIndex++, bindValues[j].getByteValue());
                }
            }
        }

        return batchedParamIndex;
    }

    private String generateMultiStatementForBatch(int numBatches) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            String origSql = ((PreparedQuery<?>) this.query).getOriginalSql();
            StringBuilder newStatementSql = new StringBuilder((origSql.length() + 1) * numBatches);

            newStatementSql.append(origSql);

            for (int i = 0; i < numBatches - 1; i++) {
                newStatementSql.append(';');
                newStatementSql.append(origSql);
            }

            return newStatementSql.toString();
        }
    }

    /**
     * Rewrites the already prepared statement into a multi-value insert
     * statement of 'statementsPerBatch' values and executes the entire batch
     * using this new statement.
     * 
     * @return update counts in the same fashion as executeBatch()
     * 
     * @throws SQLException
     */
    protected long[] executeBatchedInserts(int batchTimeout) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            String valuesClause = ((PreparedQuery<?>) this.query).getParseInfo().getValuesClause();

            JdbcConnection locallyScopedConn = this.connection;

            if (valuesClause == null) {
                return executeBatchSerially(batchTimeout);
            }

            int numBatchedArgs = this.query.getBatchedArgs().size();

            if (this.retrieveGeneratedKeys) {
                this.batchedGeneratedKeys = new ArrayList<>(numBatchedArgs);
            }

            int numValuesPerBatch = ((PreparedQuery<?>) this.query).computeBatchSize(numBatchedArgs);

            if (numBatchedArgs < numValuesPerBatch) {
                numValuesPerBatch = numBatchedArgs;
            }

            PreparedStatement batchedStatement = null;

            int batchedParamIndex = 1;
            long updateCountRunningTotal = 0;
            int numberToExecuteAsMultiValue = 0;
            int batchCounter = 0;
            CancelQueryTask timeoutTask = null;
            SQLException sqlEx = null;

            long[] updateCounts = new long[numBatchedArgs];

            try {
                try {
                    batchedStatement = /* FIXME -if we ever care about folks proxying our JdbcConnection */
                            prepareBatchedInsertSQL(locallyScopedConn, numValuesPerBatch);

                    timeoutTask = startQueryTimer(batchedStatement, batchTimeout);

                    numberToExecuteAsMultiValue = numBatchedArgs < numValuesPerBatch ? numBatchedArgs : numBatchedArgs / numValuesPerBatch;

                    int numberArgsToExecute = numberToExecuteAsMultiValue * numValuesPerBatch;

                    for (int i = 0; i < numberArgsToExecute; i++) {
                        if (i != 0 && i % numValuesPerBatch == 0) {
                            try {
                                updateCountRunningTotal += batchedStatement.executeLargeUpdate();
                            } catch (SQLException ex) {
                                sqlEx = handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex);
                            }

                            getBatchedGeneratedKeys(batchedStatement);
                            batchedStatement.clearParameters();
                            batchedParamIndex = 1;

                        }

                        batchedParamIndex = setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.query.getBatchedArgs().get(batchCounter++));
                    }

                    try {
                        updateCountRunningTotal += batchedStatement.executeLargeUpdate();
                    } catch (SQLException ex) {
                        sqlEx = handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex);
                    }

                    getBatchedGeneratedKeys(batchedStatement);

                    numValuesPerBatch = numBatchedArgs - batchCounter;
                } finally {
                    if (batchedStatement != null) {
                        batchedStatement.close();
                        batchedStatement = null;
                    }
                }

                try {
                    if (numValuesPerBatch > 0) {
                        batchedStatement = prepareBatchedInsertSQL(locallyScopedConn, numValuesPerBatch);

                        if (timeoutTask != null) {
                            timeoutTask.setQueryToCancel(batchedStatement);
                        }

                        batchedParamIndex = 1;

                        while (batchCounter < numBatchedArgs) {
                            batchedParamIndex = setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.query.getBatchedArgs().get(batchCounter++));
                        }

                        try {
                            updateCountRunningTotal += batchedStatement.executeLargeUpdate();
                        } catch (SQLException ex) {
                            sqlEx = handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex);
                        }

                        getBatchedGeneratedKeys(batchedStatement);
                    }

                    if (sqlEx != null) {
                        throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.exceptionInterceptor);
                    }

                    if (numBatchedArgs > 1) {
                        long updCount = updateCountRunningTotal > 0 ? java.sql.Statement.SUCCESS_NO_INFO : 0;
                        for (int j = 0; j < numBatchedArgs; j++) {
                            updateCounts[j] = updCount;
                        }
                    } else {
                        updateCounts[0] = updateCountRunningTotal;
                    }
                    return updateCounts;
                } finally {
                    if (batchedStatement != null) {
                        batchedStatement.close();
                    }
                }
            } finally {
                stopQueryTimer(timeoutTask, false, false);
                resetCancelledState();
            }
        }
    }

    /**
     * Executes the current batch of statements by executing them one-by-one.
     * 
     * @return a list of update counts
     * @throws SQLException
     *             if an error occurs
     */
    protected long[] executeBatchSerially(int batchTimeout) throws SQLException {

        synchronized (checkClosed().getConnectionMutex()) {
            if (this.connection == null) {
                checkClosed();
            }

            long[] updateCounts = null;

            if (this.query.getBatchedArgs() != null) {
                int nbrCommands = this.query.getBatchedArgs().size();
                updateCounts = new long[nbrCommands];

                for (int i = 0; i < nbrCommands; i++) {
                    updateCounts[i] = -3;
                }

                SQLException sqlEx = null;

                CancelQueryTask timeoutTask = null;

                try {
                    timeoutTask = startQueryTimer(this, batchTimeout);

                    if (this.retrieveGeneratedKeys) {
                        this.batchedGeneratedKeys = new ArrayList<>(nbrCommands);
                    }

                    int batchCommandIndex = ((PreparedQuery<?>) this.query).getBatchCommandIndex();

                    for (batchCommandIndex = 0; batchCommandIndex < nbrCommands; batchCommandIndex++) {

                        ((PreparedQuery<?>) this.query).setBatchCommandIndex(batchCommandIndex);

                        Object arg = this.query.getBatchedArgs().get(batchCommandIndex);

                        try {
                            if (arg instanceof String) {
                                updateCounts[batchCommandIndex] = executeUpdateInternal((String) arg, true, this.retrieveGeneratedKeys);

                                // limit one generated key per OnDuplicateKey statement
                                getBatchedGeneratedKeys(this.results.getFirstCharOfQuery() == 'I' && containsOnDuplicateKeyInString((String) arg) ? 1 : 0);
                            } else {
                                QueryBindings<?> queryBindings = (QueryBindings<?>) arg;
                                updateCounts[batchCommandIndex] = executeUpdateInternal(queryBindings, true);

                                // limit one generated key per OnDuplicateKey statement
                                getBatchedGeneratedKeys(containsOnDuplicateKeyUpdateInSQL() ? 1 : 0);
                            }
                        } catch (SQLException ex) {
                            updateCounts[batchCommandIndex] = EXECUTE_FAILED;

                            if (this.continueBatchOnError && !(ex instanceof MySQLTimeoutException) && !(ex instanceof MySQLStatementCancelledException)
                                    && !hasDeadlockOrTimeoutRolledBackTx(ex)) {
                                sqlEx = ex;
                            } else {
                                long[] newUpdateCounts = new long[batchCommandIndex];
                                System.arraycopy(updateCounts, 0, newUpdateCounts, 0, batchCommandIndex);

                                throw SQLError.createBatchUpdateException(ex, newUpdateCounts, this.exceptionInterceptor);
                            }
                        }
                    }

                    if (sqlEx != null) {
                        throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.exceptionInterceptor);
                    }
                } catch (NullPointerException npe) {
                    try {
                        checkClosed();
                    } catch (StatementIsClosedException connectionClosedEx) {
                        int batchCommandIndex = ((PreparedQuery<?>) this.query).getBatchCommandIndex();
                        updateCounts[batchCommandIndex] = EXECUTE_FAILED;

                        long[] newUpdateCounts = new long[batchCommandIndex];

                        System.arraycopy(updateCounts, 0, newUpdateCounts, 0, batchCommandIndex);

                        throw SQLError.createBatchUpdateException(SQLExceptionsMapping.translateException(connectionClosedEx), newUpdateCounts,
                                this.exceptionInterceptor);
                    }

                    throw npe; // we don't know why this happened, punt
                } finally {
                    ((PreparedQuery<?>) this.query).setBatchCommandIndex(-1);

                    stopQueryTimer(timeoutTask, false, false);
                    resetCancelledState();
                }
            }

            return (updateCounts != null) ? updateCounts : new long[0];
        }

    }

    /**
     * Actually execute the prepared statement. This is here so server-side
     * PreparedStatements can re-use most of the code from this class.
     * 
     * @param maxRowsToRetrieve
     *            the max number of rows to return
     * @param sendPacket
     *            the packet to send
     * @param createStreamingResultSet
     *            should a 'streaming' result set be created?
     * @param queryIsSelectOnly
     *            is this query doing a SELECT?
     * @param metadata
     *            use this metadata instead of the one provided on wire
     * @param isBatch
     * 
     * @return the results as a ResultSet
     * 
     * @throws SQLException
     *             if an error occurs.
     */
    protected ResultSetInternalMethods executeInternal(int maxRowsToRetrieve, PacketPayload sendPacket, boolean createStreamingResultSet,
            boolean queryIsSelectOnly, ColumnDefinition metadata, boolean isBatch) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            try {

                JdbcConnection locallyScopedConnection = this.connection;

                ((PreparedQuery<?>) this.query).getQueryBindings()
                        .setNumberOfExecutions(((PreparedQuery<?>) this.query).getQueryBindings().getNumberOfExecutions() + 1);

                ResultSetInternalMethods rs;

                CancelQueryTask timeoutTask = null;

                try {
                    timeoutTask = startQueryTimer(this, getTimeoutInMillis());

                    if (!isBatch) {
                        statementBegins();
                    }

                    rs = locallyScopedConnection.getSession().execSQL(this, null, maxRowsToRetrieve, sendPacket, createStreamingResultSet,
                            getResultSetFactory(), this.getCurrentCatalog(), metadata, isBatch);

                    if (timeoutTask != null) {
                        stopQueryTimer(timeoutTask, true, true);
                        timeoutTask = null;
                    }

                } finally {
                    if (!isBatch) {
                        this.query.getStatementExecuting().set(false);
                    }

                    stopQueryTimer(timeoutTask, false, false);
                }

                return rs;
            } catch (NullPointerException npe) {
                checkClosed(); // we can't synchronize ourselves against async connection-close due to deadlock issues, so this is the next best thing for
                              // this particular corner case.

                throw npe;
            }
        }
    }

    /**
     * A Prepared SQL query is executed and its ResultSet is returned
     * 
     * @return a ResultSet that contains the data produced by the query - never
     *         null
     * 
     * @exception SQLException
     *                if a database access error occurs
     */
    public java.sql.ResultSet executeQuery() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {

            JdbcConnection locallyScopedConn = this.connection;

            checkForDml(((PreparedQuery<?>) this.query).getOriginalSql(), ((PreparedQuery<?>) this.query).getParseInfo().getFirstStmtChar());

            this.batchedGeneratedKeys = null;

            resetCancelledState();

            implicitlyCloseAllOpenResults();

            clearWarnings();

            if (this.doPingInstead) {
                doPingInstead();

                return this.results;
            }

            setupStreamingTimeout(locallyScopedConn);

            PacketPayload sendPacket = ((PreparedQuery<?>) this.query).fillSendPacket();

            String oldCatalog = null;

            if (!locallyScopedConn.getCatalog().equals(this.getCurrentCatalog())) {
                oldCatalog = locallyScopedConn.getCatalog();
                locallyScopedConn.setCatalog(this.getCurrentCatalog());
            }

            //
            // Check if we have cached metadata for this query...
            //
            CachedResultSetMetaData cachedMetadata = null;
            boolean cacheResultSetMetadata = locallyScopedConn.getPropertySet().getBooleanReadableProperty(PropertyDefinitions.PNAME_cacheResultSetMetadata)
                    .getValue();

            String origSql = ((PreparedQuery<?>) this.query).getOriginalSql();

            if (cacheResultSetMetadata) {
                cachedMetadata = locallyScopedConn.getCachedMetaData(origSql);
            }

            locallyScopedConn.setSessionMaxRows(this.maxRows);

            this.results = executeInternal(this.maxRows, sendPacket, createStreamingResultSet(), true, cachedMetadata, false);

            if (oldCatalog != null) {
                locallyScopedConn.setCatalog(oldCatalog);
            }

            if (cachedMetadata != null) {
                locallyScopedConn.initializeResultsMetadataFromCache(origSql, cachedMetadata, this.results);
            } else {
                if (cacheResultSetMetadata) {
                    locallyScopedConn.initializeResultsMetadataFromCache(origSql, null /* will be created */, this.results);
                }
            }

            this.lastInsertId = this.results.getUpdateID();

            return this.results;
        }
    }

    /**
     * Execute a SQL INSERT, UPDATE or DELETE statement. In addition, SQL
     * statements that return nothing such as SQL DDL statements can be
     * executed.
     * 
     * @return either the row count for INSERT, UPDATE or DELETE; or 0 for SQL
     *         statements that return nothing.
     * 
     * @exception SQLException
     *                if a database access error occurs
     */
    public int executeUpdate() throws SQLException {
        return Util.truncateAndConvertToInt(executeLargeUpdate());
    }

    /*
     * We need this variant, because ServerPreparedStatement calls this for
     * batched updates, which will end up clobbering the warnings and generated
     * keys we need to gather for the batch.
     */
    protected long executeUpdateInternal(boolean clearBatchedGeneratedKeysAndWarnings, boolean isBatch) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (clearBatchedGeneratedKeysAndWarnings) {
                clearWarnings();
                this.batchedGeneratedKeys = null;
            }

            return executeUpdateInternal(((PreparedQuery<?>) this.query).getQueryBindings(), isBatch);
        }
    }

    /**
     * Added to allow batch-updates
     * 
     * @param bindings
     * @param isReallyBatch
     * 
     * @return the update count
     * 
     * @throws SQLException
     *             if a database error occurs
     */
    protected long executeUpdateInternal(QueryBindings<?> bindings, boolean isReallyBatch) throws SQLException {

        synchronized (checkClosed().getConnectionMutex()) {

            JdbcConnection locallyScopedConn = this.connection;

            if (locallyScopedConn.isReadOnly(false)) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.34") + Messages.getString("PreparedStatement.35"),
                        MysqlErrorNumbers.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
            }

            if ((((PreparedQuery<?>) this.query).getParseInfo().getFirstStmtChar() == 'S') && isSelectQuery()) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.37"), "01S03", this.exceptionInterceptor);
            }

            resetCancelledState();

            implicitlyCloseAllOpenResults();

            ResultSetInternalMethods rs = null;

            PacketPayload sendPacket = ((PreparedQuery<?>) this.query).fillSendPacket(bindings);

            String oldCatalog = null;

            if (!locallyScopedConn.getCatalog().equals(this.getCurrentCatalog())) {
                oldCatalog = locallyScopedConn.getCatalog();
                locallyScopedConn.setCatalog(this.getCurrentCatalog());
            }

            //
            // Only apply max_rows to selects
            //
            locallyScopedConn.setSessionMaxRows(-1);

            rs = executeInternal(-1, sendPacket, false, false, null, isReallyBatch);

            if (this.retrieveGeneratedKeys) {
                rs.setFirstCharOfQuery(((PreparedQuery<?>) this.query).getParseInfo().getFirstStmtChar());
            }

            if (oldCatalog != null) {
                locallyScopedConn.setCatalog(oldCatalog);
            }

            this.results = rs;

            this.updateCount = rs.getUpdateCount();

            if (containsOnDuplicateKeyUpdateInSQL() && this.compensateForOnDuplicateKeyUpdate) {
                if (this.updateCount == 2 || this.updateCount == 0) {
                    this.updateCount = 1;
                }
            }

            this.lastInsertId = rs.getUpdateID();

            return this.updateCount;
        }
    }

    protected boolean containsOnDuplicateKeyUpdateInSQL() {
        return ((PreparedQuery<?>) this.query).getParseInfo().containsOnDuplicateKeyUpdateInSQL();
    }

    /**
     * Returns a prepared statement for the number of batched parameters, used when re-writing batch INSERTs.
     */
    protected PreparedStatement prepareBatchedInsertSQL(JdbcConnection localConn, int numBatches) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            PreparedStatement pstmt = new PreparedStatement(localConn, "Rewritten batch of: " + ((PreparedQuery<?>) this.query).getOriginalSql(),
                    this.getCurrentCatalog(), ((PreparedQuery<?>) this.query).getParseInfo().getParseInfoForBatch(numBatches));
            pstmt.setRetrieveGeneratedKeys(this.retrieveGeneratedKeys);
            pstmt.rewrittenBatchSize = numBatches;

            return pstmt;
        }
    }

    protected void setRetrieveGeneratedKeys(boolean flag) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            this.retrieveGeneratedKeys = flag;
        }
    }

    /**
     * @param parameterIndex
     * 
     * @throws SQLException
     */
    public byte[] getBytesRepresentation(int parameterIndex) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            return ((ClientPreparedQuery) this.query).getBytesRepresentation(parameterIndex);
        }
    }

    /**
     * Get bytes representation for a parameter in a statement batch.
     * 
     * @param parameterIndex
     * @param commandIndex
     * @throws SQLException
     */
    protected byte[] getBytesRepresentationForBatch(int parameterIndex, int commandIndex) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            return ((ClientPreparedQuery) this.query).getBytesRepresentationForBatch(parameterIndex, commandIndex);
        }
    }

    /**
     * The number, types, and properties of a ResultSet's columns are provided by
     * the getMetaData method.
     * 
     * @return the description of a ResultSet's columns
     * 
     * @exception SQLException
     *                if a database-access error occurs.
     */
    public java.sql.ResultSetMetaData getMetaData() throws SQLException {

        synchronized (checkClosed().getConnectionMutex()) {
            //
            // We could just tack on a LIMIT 0 here no matter what the  statement, and check if a result set was returned or not, but I'm not comfortable with
            // that, myself, so we take the "safer" road, and only allow metadata for _actual_ SELECTS (but not SHOWs).
            // 
            // CALL's are trapped further up and you end up with a  CallableStatement anyway.
            //

            if (!isSelectQuery()) {
                return null;
            }

            PreparedStatement mdStmt = null;
            java.sql.ResultSet mdRs = null;

            if (this.pstmtResultMetaData == null) {
                try {
                    mdStmt = new PreparedStatement(this.connection, ((PreparedQuery<?>) this.query).getOriginalSql(), this.getCurrentCatalog(),
                            ((PreparedQuery<?>) this.query).getParseInfo());

                    mdStmt.setMaxRows(1);

                    int paramCount = ((PreparedQuery<?>) this.query).getParameterCount();

                    for (int i = 1; i <= paramCount; i++) {
                        mdStmt.setString(i, "");
                    }

                    boolean hadResults = mdStmt.execute();

                    if (hadResults) {
                        mdRs = mdStmt.getResultSet();

                        this.pstmtResultMetaData = mdRs.getMetaData();
                    } else {
                        this.pstmtResultMetaData = new ResultSetMetaData(this.session, new Field[0],
                                this.session.getPropertySet().getBooleanReadableProperty(PropertyDefinitions.PNAME_useOldAliasMetadataBehavior).getValue(),
                                this.session.getPropertySet().getBooleanReadableProperty(PropertyDefinitions.PNAME_yearIsDateType).getValue(),
                                this.exceptionInterceptor);
                    }
                } finally {
                    SQLException sqlExRethrow = null;

                    if (mdRs != null) {
                        try {
                            mdRs.close();
                        } catch (SQLException sqlEx) {
                            sqlExRethrow = sqlEx;
                        }

                        mdRs = null;
                    }

                    if (mdStmt != null) {
                        try {
                            mdStmt.close();
                        } catch (SQLException sqlEx) {
                            sqlExRethrow = sqlEx;
                        }

                        mdStmt = null;
                    }

                    if (sqlExRethrow != null) {
                        throw sqlExRethrow;
                    }
                }
            }

            return this.pstmtResultMetaData;
        }
    }

    protected boolean isSelectQuery() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            return StringUtils.startsWithIgnoreCaseAndWs(
                    StringUtils.stripComments(((PreparedQuery<?>) this.query).getOriginalSql(), "'\"", "'\"", true, false, true, true), "SELECT");
        }
    }

    /**
     * @see PreparedStatement#getParameterMetaData()
     */
    public ParameterMetaData getParameterMetaData() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.parameterMetaData == null) {
                if (this.session.getPropertySet().getBooleanReadableProperty(PropertyDefinitions.PNAME_generateSimpleParameterMetadata).getValue()) {
                    this.parameterMetaData = new MysqlParameterMetadata(((PreparedQuery<?>) this.query).getParameterCount());
                } else {
                    this.parameterMetaData = new MysqlParameterMetadata(this.session, null, ((PreparedQuery<?>) this.query).getParameterCount(),
                            this.exceptionInterceptor);
                }
            }

            return this.parameterMetaData;
        }
    }

    public ParseInfo getParseInfo() {
        return ((PreparedQuery<?>) this.query).getParseInfo();
    }

    @SuppressWarnings("unchecked")
    private void initializeFromParseInfo() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {

            int parameterCount = ((PreparedQuery<ClientPreparedQueryBindings>) this.query).getParseInfo().getStaticSql().length - 1;
            ((PreparedQuery<?>) this.query).setParameterCount(parameterCount);
            ((PreparedQuery<ClientPreparedQueryBindings>) this.query).setQueryBindings(new ClientPreparedQueryBindings(parameterCount, this.session));
            ((ClientPreparedQuery) this.query).getQueryBindings().setLoadDataQuery(((PreparedQuery<?>) this.query).getParseInfo().isFoundLoadData());

            clearParameters();
        }
    }

    public boolean isNull(int paramIndex) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            return ((PreparedQuery<?>) this.query).getQueryBindings().getBindValues()[paramIndex].isNull();
        }
    }

    /**
     * Closes this statement, releasing all resources
     * 
     * @param calledExplicitly
     *            was this called by close()?
     * 
     * @throws SQLException
     *             if an error occurs
     */
    @Override
    public void realClose(boolean calledExplicitly, boolean closeOpenResults) throws SQLException {
        JdbcConnection locallyScopedConn = this.connection;

        if (locallyScopedConn == null) {
            return; // already closed
        }

        synchronized (locallyScopedConn.getConnectionMutex()) {

            // additional check in case Statement was closed
            // while current thread was waiting for lock
            if (this.isClosed) {
                return;
            }

            if (this.useUsageAdvisor) {
                if (((PreparedQuery<?>) this.query).getQueryBindings().getNumberOfExecutions() <= 1) {
                    String message = Messages.getString("PreparedStatement.43");

                    this.query.getEventSink()
                            .consumeEvent(new ProfilerEventImpl(ProfilerEvent.TYPE_WARN, "", this.getCurrentCatalog(), this.session.getThreadId(), this.getId(),
                                    -1, System.currentTimeMillis(), 0, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
                }
            }

            super.realClose(calledExplicitly, closeOpenResults);

            ((PreparedQuery<?>) this.query).setOriginalSql(null);
            ((PreparedQuery<?>) this.query).setQueryBindings(null);
        }
    }

    public String getPreparedSql() {
        synchronized (checkClosed().getConnectionMutex()) {
            if (this.rewrittenBatchSize == 0) {
                return ((PreparedQuery<?>) this.query).getOriginalSql();
            }

            try {
                return ((PreparedQuery<?>) this.query).getParseInfo().getSqlForBatch();
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getUpdateCount() throws SQLException {
        int count = super.getUpdateCount();

        if (containsOnDuplicateKeyUpdateInSQL() && this.compensateForOnDuplicateKeyUpdate) {
            if (count == 2 || count == 0) {
                count = 1;
            }
        }

        return count;
    }

    /**
     * JDBC 4.2
     * Same as PreparedStatement.executeUpdate() but returns long instead of int.
     */
    public long executeLargeUpdate() throws SQLException {
        return executeUpdateInternal(true, false);
    }

    public ParameterBindings getParameterBindings() throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            return new EmulatedPreparedStatementBindings();
        }
    }

    /**
     * For calling stored functions, this will be -1 as Connector/J does not count
     * the first '?' parameter marker, but JDBC counts it * as 1, otherwise it will return 0
     */
    protected int getParameterIndexOffset() {
        return 0;
    }

    protected void checkBounds(int paramIndex, int parameterIndexOffset) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if ((paramIndex < 1)) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.49") + paramIndex + Messages.getString("PreparedStatement.50"),
                        MysqlErrorNumbers.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
            } else if (paramIndex > ((PreparedQuery<?>) this.query).getParameterCount()) {
                throw SQLError.createSQLException(
                        Messages.getString("PreparedStatement.51") + paramIndex + Messages.getString("PreparedStatement.52")
                                + ((PreparedQuery<?>) this.query).getParameterCount() + Messages.getString("PreparedStatement.53"),
                        MysqlErrorNumbers.SQL_STATE_ILLEGAL_ARGUMENT, this.exceptionInterceptor);
            } else if (parameterIndexOffset == -1 && paramIndex == 1) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.63"), MysqlErrorNumbers.SQL_STATE_ILLEGAL_ARGUMENT,
                        this.exceptionInterceptor);
            }
        }
    }

    protected final int getCoreParameterIndex(int paramIndex) throws SQLException {
        int parameterIndexOffset = getParameterIndexOffset();
        checkBounds(paramIndex, parameterIndexOffset);
        return paramIndex - 1 + parameterIndexOffset;
    }

    public void setArray(int i, Array x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setAsciiStream(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setAsciiStream(getCoreParameterIndex(parameterIndex), x, length);
        }
    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setAsciiStream(getCoreParameterIndex(parameterIndex), x, length);
        }
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setBigDecimal(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setBinaryStream(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setBinaryStream(getCoreParameterIndex(parameterIndex), x, length);
        }
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setBinaryStream(getCoreParameterIndex(parameterIndex), x, length);
        }
    }

    public void setBlob(int i, java.sql.Blob x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setBlob(getCoreParameterIndex(i), x);
        }
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setBlob(getCoreParameterIndex(parameterIndex), inputStream);
        }
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setBlob(getCoreParameterIndex(parameterIndex), inputStream, length);
        }
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setBoolean(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setByte(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setBytes(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setBytes(int parameterIndex, byte[] x, boolean checkForIntroducer, boolean escapeForMBChars) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setBytes(getCoreParameterIndex(parameterIndex), x, checkForIntroducer, escapeForMBChars);
        }
    }

    /**
     * Used by updatable result sets for refreshRow() because the parameter has
     * already been escaped for updater or inserter prepared statements.
     * 
     * @param parameterIndex
     *            the parameter to set.
     * @param parameterAsBytes
     *            the parameter as a string.
     * 
     * @throws SQLException
     *             if an error occurs
     */
    public void setBytesNoEscape(int parameterIndex, byte[] parameterAsBytes) throws SQLException {
        ((PreparedQuery<?>) this.query).getQueryBindings().setBytesNoEscape(getCoreParameterIndex(parameterIndex), parameterAsBytes);
    }

    public void setBytesNoEscapeNoQuotes(int parameterIndex, byte[] parameterAsBytes) throws SQLException {
        ((PreparedQuery<?>) this.query).getQueryBindings().setBytesNoEscapeNoQuotes(getCoreParameterIndex(parameterIndex), parameterAsBytes);
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setCharacterStream(getCoreParameterIndex(parameterIndex), reader);
        }
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setCharacterStream(getCoreParameterIndex(parameterIndex), reader, length);
        }
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setCharacterStream(getCoreParameterIndex(parameterIndex), reader, length);
        }
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setCharacterStream(getCoreParameterIndex(parameterIndex), reader);
        }
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setCharacterStream(getCoreParameterIndex(parameterIndex), reader, length);
        }
    }

    public void setClob(int i, Clob x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setClob(getCoreParameterIndex(i), x);
        }
    }

    public void setDate(int parameterIndex, Date x) throws java.sql.SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setDate(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setDate(getCoreParameterIndex(parameterIndex), x, cal);
        }
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setDouble(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        ((PreparedQuery<?>) this.query).getQueryBindings().setFloat(getCoreParameterIndex(parameterIndex), x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setInt(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setLong(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setBigInteger(int parameterIndex, BigInteger x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setBigInteger(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setNCharacterStream(getCoreParameterIndex(parameterIndex), value);
        }
    }

    public void setNCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setNCharacterStream(getCoreParameterIndex(parameterIndex), reader, length);
        }
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setNClob(getCoreParameterIndex(parameterIndex), reader);
        }
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setNClob(getCoreParameterIndex(parameterIndex), reader, length);
        }
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setNClob(getCoreParameterIndex(parameterIndex), value);
        }
    }

    /**
     * Set a parameter to a Java String value. The driver converts this to a SQL
     * VARCHAR or LONGVARCHAR value with introducer _utf8 (depending on the
     * arguments size relative to the driver's limits on VARCHARs) when it sends
     * it to the database. If charset is set as utf8, this method just call setString.
     * 
     * @param parameterIndex
     *            the first parameter is 1...
     * @param x
     *            the parameter value
     * 
     * @exception SQLException
     *                if a database access error occurs
     */
    public void setNString(int parameterIndex, String x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setNString(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setNull(getCoreParameterIndex(parameterIndex)); // MySQL ignores sqlType
        }
    }

    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setNull(getCoreParameterIndex(parameterIndex));
        }
    }

    public void setNull(int parameterIndex, MysqlType mysqlType) throws SQLException {
        setNull(parameterIndex, mysqlType.getJdbcType());
    }

    public void setObject(int parameterIndex, Object parameterObj) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setObject(getCoreParameterIndex(parameterIndex), parameterObj);
        }
    }

    public void setObject(int parameterIndex, Object parameterObj, int targetSqlType) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            try {
                ((PreparedQuery<?>) this.query).getQueryBindings().setObject(getCoreParameterIndex(parameterIndex), parameterObj,
                        MysqlType.getByJdbcType(targetSqlType));
            } catch (FeatureNotAvailableException nae) {
                throw SQLError.createSQLFeatureNotSupportedException(Messages.getString("Statement.UnsupportedSQLType") + JDBCType.valueOf(targetSqlType),
                        MysqlErrorNumbers.SQL_STATE_DRIVER_NOT_CAPABLE, this.exceptionInterceptor);
            }
        }
    }

    public void setObject(int parameterIndex, Object parameterObj, SQLType targetSqlType) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (targetSqlType instanceof MysqlType) {
                ((PreparedQuery<?>) this.query).getQueryBindings().setObject(getCoreParameterIndex(parameterIndex), parameterObj, (MysqlType) targetSqlType);
            } else {
                setObject(parameterIndex, parameterObj, targetSqlType.getVendorTypeNumber());
            }
        }
    }

    public void setObject(int parameterIndex, Object parameterObj, int targetSqlType, int scale) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            try {
                ((PreparedQuery<?>) this.query).getQueryBindings().setObject(getCoreParameterIndex(parameterIndex), parameterObj,
                        MysqlType.getByJdbcType(targetSqlType), scale);
            } catch (FeatureNotAvailableException nae) {
                throw SQLError.createSQLFeatureNotSupportedException(Messages.getString("Statement.UnsupportedSQLType") + JDBCType.valueOf(targetSqlType),
                        MysqlErrorNumbers.SQL_STATE_DRIVER_NOT_CAPABLE, this.exceptionInterceptor);
            }
        }
    }

    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            if (targetSqlType instanceof MysqlType) {
                ((PreparedQuery<?>) this.query).getQueryBindings().setObject(getCoreParameterIndex(parameterIndex), x, (MysqlType) targetSqlType,
                        scaleOrLength);
            } else {
                setObject(parameterIndex, x, targetSqlType.getVendorTypeNumber(), scaleOrLength);
            }
        }
    }

    public void setRef(int i, Ref x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setShort(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        if (xmlObject == null) {
            setNull(parameterIndex, MysqlType.VARCHAR);
        } else {
            // FIXME: Won't work for Non-MYSQL SQLXMLs
            setCharacterStream(parameterIndex, ((MysqlSQLXML) xmlObject).serializeAsCharacterStream());
        }
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setString(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setTime(int parameterIndex, Time x) throws java.sql.SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setTime(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setTime(int parameterIndex, java.sql.Time x, Calendar cal) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setTime(getCoreParameterIndex(parameterIndex), x, cal);
        }
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws java.sql.SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setTimestamp(getCoreParameterIndex(parameterIndex), x);
        }
    }

    public void setTimestamp(int parameterIndex, java.sql.Timestamp x, Calendar cal) throws SQLException {
        synchronized (checkClosed().getConnectionMutex()) {
            ((PreparedQuery<?>) this.query).getQueryBindings().setTimestamp(getCoreParameterIndex(parameterIndex), x, cal);
        }
    }

    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setBinaryStream(parameterIndex, x, length);
        ((PreparedQuery<?>) this.query).getQueryBindings().getBindValues()[getCoreParameterIndex(parameterIndex)].setMysqlType(MysqlType.TEXT); // TODO was Types.CLOB
    }

    public void setURL(int parameterIndex, URL arg) throws SQLException {
        if (arg == null) {
            setNull(parameterIndex, MysqlType.VARCHAR);
        } else {
            setString(parameterIndex, arg.toString());
            ((PreparedQuery<?>) this.query).getQueryBindings().getBindValues()[getCoreParameterIndex(parameterIndex)].setMysqlType(MysqlType.VARCHAR); // TODO was Types.DATALINK
        }
    }

    class EmulatedPreparedStatementBindings implements ParameterBindings {

        private ResultSetImpl bindingsAsRs;
        private ClientPreparedQueryBindValue[] bindValues;

        EmulatedPreparedStatementBindings() throws SQLException {
            List<Row> rows = new ArrayList<>();
            int paramCount = ((PreparedQuery<?>) PreparedStatement.this.query).getParameterCount();
            this.bindValues = new ClientPreparedQueryBindValue[paramCount];
            for (int i = 0; i < paramCount; i++) {
                this.bindValues[i] = ((ClientPreparedQueryBindings) ((PreparedQuery<?>) PreparedStatement.this.query).getQueryBindings()).getBindValues()[i]
                        .clone();

            }
            byte[][] rowData = new byte[paramCount][];
            Field[] typeMetadata = new Field[paramCount];

            for (int i = 0; i < paramCount; i++) {
                int batchCommandIndex = ((PreparedQuery<?>) PreparedStatement.this.query).getBatchCommandIndex();
                rowData[i] = batchCommandIndex == -1 ? getBytesRepresentation(i) : getBytesRepresentationForBatch(i, batchCommandIndex);

                int charsetIndex = 0;

                switch (((PreparedQuery<?>) PreparedStatement.this.query).getQueryBindings().getBindValues()[i].getMysqlType()) {
                    case BINARY:
                    case BLOB:
                    case GEOMETRY:
                    case LONGBLOB:
                    case MEDIUMBLOB:
                    case TINYBLOB:
                    case UNKNOWN:
                    case VARBINARY:
                        charsetIndex = CharsetMapping.MYSQL_COLLATION_INDEX_binary;
                        break;
                    default:
                        try {
                            charsetIndex = CharsetMapping
                                    .getCollationIndexForJavaEncoding(
                                            PreparedStatement.this.session.getPropertySet()
                                                    .getStringReadableProperty(PropertyDefinitions.PNAME_characterEncoding).getValue(),
                                            PreparedStatement.this.session.getServerVersion());
                        } catch (RuntimeException ex) {
                            throw SQLError.createSQLException(ex.toString(), MysqlErrorNumbers.SQL_STATE_ILLEGAL_ARGUMENT, ex, null);
                        }
                        break;
                }

                Field parameterMetadata = new Field(null, "parameter_" + (i + 1), charsetIndex, PreparedStatement.this.charEncoding,
                        ((PreparedQuery<?>) PreparedStatement.this.query).getQueryBindings().getBindValues()[i].getMysqlType(), rowData[i].length);
                typeMetadata[i] = parameterMetadata;
            }

            rows.add(new ByteArrayRow(rowData, PreparedStatement.this.exceptionInterceptor));

            this.bindingsAsRs = PreparedStatement.this.resultSetFactory.createFromResultsetRows(ResultSet.CONCUR_READ_ONLY, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    new ResultsetRowsStatic(rows, new MysqlaColumnDefinition(typeMetadata)));
            this.bindingsAsRs.next();
        }

        public Array getArray(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getArray(parameterIndex);
        }

        public InputStream getAsciiStream(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getAsciiStream(parameterIndex);
        }

        public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBigDecimal(parameterIndex);
        }

        public InputStream getBinaryStream(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBinaryStream(parameterIndex);
        }

        public java.sql.Blob getBlob(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBlob(parameterIndex);
        }

        public boolean getBoolean(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBoolean(parameterIndex);
        }

        public byte getByte(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getByte(parameterIndex);
        }

        public byte[] getBytes(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBytes(parameterIndex);
        }

        public Reader getCharacterStream(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getCharacterStream(parameterIndex);
        }

        public java.sql.Clob getClob(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getClob(parameterIndex);
        }

        public Date getDate(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getDate(parameterIndex);
        }

        public double getDouble(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getDouble(parameterIndex);
        }

        public float getFloat(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getFloat(parameterIndex);
        }

        public int getInt(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getInt(parameterIndex);
        }

        public BigInteger getBigInteger(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBigInteger(parameterIndex);
        }

        public long getLong(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getLong(parameterIndex);
        }

        public Reader getNCharacterStream(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getCharacterStream(parameterIndex);
        }

        public Reader getNClob(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getCharacterStream(parameterIndex);
        }

        public Object getObject(int parameterIndex) throws SQLException {
            checkBounds(parameterIndex, 0);

            if (this.bindValues[parameterIndex - 1].isNull()) {
                return null;
            }

            // we can't rely on the default mapping for JDBC's ResultSet.getObject() for numerics, they're not one-to-one with PreparedStatement.setObject

            switch (((PreparedQuery<?>) PreparedStatement.this.query).getQueryBindings().getBindValues()[parameterIndex - 1].getMysqlType()) {
                case TINYINT:
                case TINYINT_UNSIGNED:
                    return Byte.valueOf(getByte(parameterIndex));
                case SMALLINT:
                case SMALLINT_UNSIGNED:
                    return Short.valueOf(getShort(parameterIndex));
                case INT:
                case INT_UNSIGNED:
                    return Integer.valueOf(getInt(parameterIndex));
                case BIGINT:
                    return Long.valueOf(getLong(parameterIndex));
                case BIGINT_UNSIGNED:
                    return getBigInteger(parameterIndex);
                case FLOAT:
                case FLOAT_UNSIGNED:
                    return Float.valueOf(getFloat(parameterIndex));
                case DOUBLE:
                case DOUBLE_UNSIGNED:
                    return Double.valueOf(getDouble(parameterIndex));
                default:
                    return this.bindingsAsRs.getObject(parameterIndex);
            }
        }

        public Ref getRef(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getRef(parameterIndex);
        }

        public short getShort(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getShort(parameterIndex);
        }

        public String getString(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getString(parameterIndex);
        }

        public Time getTime(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getTime(parameterIndex);
        }

        public Timestamp getTimestamp(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getTimestamp(parameterIndex);
        }

        public URL getURL(int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getURL(parameterIndex);
        }

        public boolean isNull(int parameterIndex) throws SQLException {
            checkBounds(parameterIndex, 0);

            return this.bindValues[parameterIndex - 1].isNull();
        }
    }
}

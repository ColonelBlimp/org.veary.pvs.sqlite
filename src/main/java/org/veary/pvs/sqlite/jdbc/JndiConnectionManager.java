/*
 * MIT License
 *
 * Copyright (c) 2019 ColonelBlimp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.veary.pvs.sqlite.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.veary.pvs.sqlite.ConnectionManager;

/**
 * Package scoped concrete implemention of the {@link ConnectionManager} interface. Used with
 * Tomcat 9 for JNDI.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
@Singleton
final class JndiConnectionManager implements ConnectionManager {

	private DataSource dataSource;

	/**
	 * Constructor.
	 * @param dataSource a {@link DataSource} object.
	 */
	@Inject
	protected JndiConnectionManager(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return this.dataSource.getConnection();
	}
}

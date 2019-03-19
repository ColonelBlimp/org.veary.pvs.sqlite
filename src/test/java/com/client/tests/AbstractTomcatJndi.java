/*
 *  A bespoke Payment Voucher System (PVS).
 *  Copyright (C) 2019, Marc L. Veary
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.client.tests;

import java.io.File;

import hthurow.tomcatjndi.TomcatJNDI;

public abstract class AbstractTomcatJndi {

	protected TomcatJNDI tomcatJNDI;

	public void tomcatJndiSetup() {
		System.setProperty("pvs.db.home", "./target");
		File contextXml = new File("src/test/resources/context.xml");
		File webXml = new File("src/test/resources/web.xml");
		this.tomcatJNDI = new TomcatJNDI();
		this.tomcatJNDI.processContextXml(contextXml);
		this.tomcatJNDI.processWebXml(webXml);
		this.tomcatJNDI.start();
	}
}

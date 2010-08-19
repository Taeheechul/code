/* NeXus - Neutron & X-ray Common Data Format
 *
 * NeXus file validation GUI tool.
 *
 * Copyright (C) 2010 Nexus Team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For further information, see <http://www.neutron.anl.gov/NeXus/>
 *
 * Attribute.java
 *
 */
package org.nexusformat.nxvalidate;

import java.util.Vector;

public class Attribute extends AbstractNXSnode {
    private static final String ATTR = "ATTR:";
    private String value;

    public Attribute(final String name, final String value) {
	super(name);
	this.value = value;
    }

    public String getValue() {
	return this.value;
    }

    public String toString() {
	return ATTR + this.getName() + ":" + this.value;
    }
}
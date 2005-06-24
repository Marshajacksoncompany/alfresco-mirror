/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.server.filesys;

import org.alfresco.filesys.server.NetworkServer;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.service.ServiceRegistry;

/**
 * Network File Server Class
 * <p>
 * Base class for all network file servers.
 */
public abstract class NetworkFileServer extends NetworkServer
{

    /**
     * Class constructor
     * 
     * @param proto String
     * @param serviceRegistry repository connection
     * @param config ServerConfiguration
     */
    public NetworkFileServer(String proto, ServiceRegistry serviceRegistry, ServerConfiguration config)
    {
        super(proto, serviceRegistry, config);
    }
}

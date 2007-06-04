/* Copyright 2007 Niclas Hedhman.
 * Copyright 2007 Alin Dreghiciu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.web.service.internal.ng;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.util.Dictionary;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;

public class HttpServiceImpl
    implements StoppableHttpService, ServerListener
{

    private static final Log m_logger = LogFactory.getLog( HttpService.class );
    
    private Bundle m_bundle;
    private Registrations m_registrations;
    private ServerController m_serverController;

    public HttpServiceImpl(
        final Bundle bundle,
        final ServerController serverController,
        final Registrations registrations
    )
    {
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "Creating http service for: " + bundle );
        }
        Assert.notNull( "bundle == null", bundle );
        Assert.notNull( "registrationRepository == null", registrations );
        Assert.notNull( "httpServiceServer == null", serverController );
        m_bundle = bundle;
        m_registrations = registrations;
        m_serverController = serverController;
        m_serverController.addListener( this );
    }

    public void registerServlet(
        final String alias,
        final Servlet servlet,
        final Dictionary initParams,
        final HttpContext httpContext )
        throws ServletException, NamespaceException
    {
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "Registering Servlet: [" + alias + "] -> " + servlet );
        }        
        HttpContext context = httpContext;
        if ( context == null )
        {
            context = createDefaultHttpContext();
        }
        HttpTarget httpTarget = m_registrations.registerServlet( alias, servlet, initParams, context);
        httpTarget.register( m_serverController );
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "Registered Servlet: [" + alias + "] -> " + servlet );
        }
    }

    public void registerResources( final String alias, final String name, final HttpContext httpContext )
        throws NamespaceException
    {
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "Registering resource: [" + alias + "] -> " + name );
        }
        HttpContext context = httpContext;
        if ( context == null )
        {
            context = createDefaultHttpContext();
        }
        HttpTarget httpTarget = m_registrations.registerResources( alias, name, context);
        httpTarget.register( m_serverController );
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "Registered resource: [" + alias + "] -> " + name );
        }
    }

    public void unregister( final String alias )
    {
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "Unregistering: [" + alias + "]");
        }
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "Unregistered: [" + alias + "]");
        }
    }

    public HttpContext createDefaultHttpContext()
    {
        return new DefaultHttpContextImpl( m_bundle );
    }

    public void stateChanged( final ServerEvent event )
    {
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "Handling event: [" + event + "]");
        }
        if ( event == ServerEvent.STARTED )
        {
            HttpTarget[] httpTargets = m_registrations.get();
            if ( httpTargets != null )
            {
                for( HttpTarget httpTarget : httpTargets )
                {
                    httpTarget.register( m_serverController );
                }
            }
        }

    }

    public synchronized void stop()
    {
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "Stopping http service: [" + this + "]");
        }
        HttpTarget[] targets = m_registrations.get();
        if ( targets != null)
        {
            for( HttpTarget target : targets )
            {
                if( m_logger.isInfoEnabled() )
                {
                     m_logger.info( "Unregistering: " + target );
                }
                m_registrations.unregister( target );
                target.unregister( m_serverController );
            }            
        }
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "Stopped http service: [" + this + "]");
        }
    }

}

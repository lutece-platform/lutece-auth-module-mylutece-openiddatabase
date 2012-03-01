/*
 * Copyright (c) 2002-2012, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication;

import fr.paris.lutece.plugins.mylutece.authentication.PortalAuthentication;
import fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.business.OpenIdDatabaseHome;
import fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.business.OpenIdDatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.business.OpenIdDatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.web.MyLuteceOpenIdDatabaseApp;
import fr.paris.lutece.plugins.mylutece.modules.openiddatabase.service.OpenIDDatabasePlugin;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.security.LoginRedirectException;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.log4j.Logger;

import org.openid4java.OpenIDException;

import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;

import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;

import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.StoreResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;


import org.openid4java.util.HttpClientFactory;
import org.openid4java.util.ProxyProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.security.auth.login.LoginException;

import javax.servlet.http.HttpServletRequest;


/**
 * The Class provides an implementation of the inherited abstract class PortalAuthentication based on
 * a database.
 *
 * @author Mairie de Paris
 * @version 2.0.0
 *
 * @since Lutece v2.0.0
 */
public class BaseAuthentication extends PortalAuthentication
{
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String AUTH_SERVICE_NAME = AppPropertiesService.getProperty( "mylutece-openiddatabase.service.name" );

    // Messages properties
    private static final String PROPERTY_MESSAGE_USER_NOT_FOUND_DATABASE = "module.mylutece.database.message.userNotFoundDatabase";
    private static final String PLUGIN_NAME = "mylutece-openiddatabase";
    private static final String URL_CALLBACK = "jsp/site/plugins/mylutece/modules/openiddatabase/OpenIdDatabaseProviderCallBack.jsp";
    private static final String CONSTANT_PATH_ICON = "images/local/skin/plugins/mylutece/modules/openiddatabase/mylutece-openid.png";
    private static final String MESSAGE_KEY_AUTHENTICATION_FAILED = "module.mylutece.openid.authenticationFailed";
    private static ConsumerManager _manager;
    private static Logger _logger = Logger.getLogger( "openid" );

    //Properties
    private static final String PROPERTY_PROXY_PASSWORD = "openiddatabase.proxyPassword";
    private static final String PROPERTY_PROXY_HOST_NAME = "openiddatabase.proxyHostName";
    private static final String PROPERTY_PROXY_PORT_NUMBER = "openiddatabase.proxyPort";
    private static final String PROPERTY_PROXY_DOMAIN_NAME = "openiddatabase.domainName";
    private static final String PROPERTY_PROXY_USER_NAME = "openiddatabase.proxyUserName";

    // Parameters
    public static final String PARAMETER_PAGE = "page";
    public static final String PARAMETER_XPAGE_VALUE = "myluteceopeniddatabase";
    public static final String PARAMETER_ERROR = "error";
    private static final String PARAMETER_LOGIN = "login";
    private static final String PARAMETER_LAST_NAME = "last_name";
    private static final String PARAMETER_FIRST_NAME = "first_name";
    private static final String PARAMETER_EMAIL = "email";

    /**
     * Constructor
     *
     */
    public BaseAuthentication(  )
    {
        super(  );

        // instantiate a ConsumerManager object
        if ( _manager == null )
        {
            try
            {
                _manager = new ConsumerManager(  );
            }
            catch ( ConsumerException e )
            {
                AppLogService.error( "Error instantiating OpenID ConsumerManager : " + e.getMessage(  ), e );
            }
        }
    }

    /**
     * Gets the Authentification service name
     * @return The name of the authentication service
     */
    public String getAuthServiceName(  )
    {
        return AUTH_SERVICE_NAME;
    }

    /**
     * Gets the Authentification type
     * @param request The HTTP request
     * @return The type of authentication
     */
    public String getAuthType( HttpServletRequest request )
    {
        return HttpServletRequest.BASIC_AUTH;
    }

    /**
     * This methods checks the login info in the database
     *
     * @param strUserName The username
     * @param strUserPassword The password
     * @param request The HttpServletRequest
     *
     * @return A LuteceUser object corresponding to the login
     * @throws LoginException The LoginException
     */
    public LuteceUser login( String strUserName, String strUserPassword, HttpServletRequest request )
        throws LoginException, LoginRedirectException
    {
        if ( strUserPassword.equals( "dummy" ) )
        {
            return loginOpenId( strUserName, request );
        }
        else
        {
            return loginDatabase( strUserName, strUserPassword, request );
        }
    }

    public LuteceUser loginOpenId( String strUserName, HttpServletRequest request )
        throws LoginException, LoginRedirectException
    {
        String strRedirectUrl = getProviderRedirectUrl( request, strUserName );

        if ( strRedirectUrl != null )
        {
            throw new LoginRedirectException( strRedirectUrl );
        }

        return null;
    }

    public LuteceUser loginDatabase( String strUserName, String strUserPassword, HttpServletRequest request )
        throws LoginException, LoginRedirectException
    {
        Locale locale = request.getLocale(  );
        Plugin plugin = PluginService.getPlugin( PLUGIN_NAME );

        BaseUser user = OpenIdDatabaseHome.findLuteceUserByLogin( strUserName, plugin, this );

        //Unable to find the user
        if ( user == null )
        {
            AppLogService.info( "Unable to find user in the database : " + strUserName );
            throw new LoginException( I18nService.getLocalizedString( PROPERTY_MESSAGE_USER_NOT_FOUND_DATABASE, locale ) );
        }

        //Check password
        if ( !OpenIdDatabaseUserHome.checkPassword( strUserName, strUserPassword, plugin ) )
        {
            AppLogService.info( "User login : Incorrect login or password" + strUserName );
            throw new LoginException( I18nService.getLocalizedString( PROPERTY_MESSAGE_USER_NOT_FOUND_DATABASE, locale ) );
        }

        //Get roles
        ArrayList<String> arrayRoles = OpenIdDatabaseHome.findUserRolesFromLogin( strUserName, plugin );

        if ( !arrayRoles.isEmpty(  ) )
        {
            user.setRoles( arrayRoles );
        }

        //Get groups
        ArrayList<String> arrayGroups = OpenIdDatabaseHome.findUserGroupsFromLogin( strUserName, plugin );

        if ( !arrayGroups.isEmpty(  ) )
        {
            user.setGroups( arrayGroups );
        }

        return user;
    }

    /**
     * This methods logout the user
     * @param user The user
     */
    public void logout( LuteceUser user )
    {
    }

    /**
     * This method returns an anonymous Lutece user
     *
     * @return An anonymous Lutece user
     */
    public LuteceUser getAnonymousUser(  )
    {
        return new BaseUser( LuteceUser.ANONYMOUS_USERNAME, this );
    }

    /**
     * Checks that the current user is associated to a given role
     * @param user The user
     * @param request The HTTP request
     * @param strRole The role name
     * @return Returns true if the user is associated to the role, otherwise false
     */
    public boolean isUserInRole( LuteceUser user, HttpServletRequest request, String strRole )
    {
        String[] roles = setAllRoles( user );

        if ( ( roles != null ) && ( strRole != null ) )
        {
            for ( String role : roles )
            {
                if ( strRole.equals( role ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the View account page URL of the Authentication Service
     * @return The URL
     */
    public String getViewAccountPageUrl(  )
    {
        return MyLuteceOpenIdDatabaseApp.getViewAccountUrl(  );
    }

    /**
     * Returns the New account page URL of the Authentication Service
     * @return The URL
     */
    public String getNewAccountPageUrl(  )
    {
        return MyLuteceOpenIdDatabaseApp.getNewAccountUrl(  );
    }

    /**
     * Returns the Change password page URL of the Authentication Service
     * @return The URL
     */
    public String getChangePasswordPageUrl(  )
    {
        return MyLuteceOpenIdDatabaseApp.getChangePasswordUrl(  );
    }

    /**
     * Returns the lost password URL of the Authentication Service
     * @return The URL
     */
    public String getLostPasswordPageUrl(  )
    {
        return MyLuteceOpenIdDatabaseApp.getLostPasswordUrl(  );
    }

    /**
     * set all roles for this user :
     *    - user's roles
     *    - user's groups roles
     *
     * @param user The user
     * @return Array of roles
     */
    private String[] setAllRoles( LuteceUser user )
    {
        Set<String> setRoles = new HashSet<String>(  );
        String[] strGroups = user.getGroups(  );
        String[] strRoles = user.getRoles(  );

        if ( strRoles != null )
        {
            for ( String strRole : strRoles )
            {
                setRoles.add( strRole );
            }
        }

        String[] strReturnRoles = new String[setRoles.size(  )];
        setRoles.toArray( strReturnRoles );

        return strReturnRoles;
    }

    /**
     * Returns all users managed by the authentication service if this feature is
     * available.
     * @return A collection of Lutece users or null if the service doesn't provide a users list
     */
    public Collection<LuteceUser> getUsers(  )
    {
        Plugin plugin = PluginService.getPlugin( PLUGIN_NAME );

        Collection<BaseUser> BaseUsers = OpenIdDatabaseHome.findDatabaseUsersList( plugin, this );
        Collection<LuteceUser> luteceUsers = new ArrayList<LuteceUser>(  );

        for ( BaseUser user : BaseUsers )
        {
            luteceUsers.add( user );
        }

        return luteceUsers;
    }

    /**
     * Returns the user managed by the authentication service if this feature is
     * available.
     * @return A Lutece users or null if the service doesn't provide a user
     */
    public LuteceUser getUser( String userLogin )
    {
        Plugin plugin = PluginService.getPlugin( PLUGIN_NAME );

        BaseUser user = OpenIdDatabaseHome.findLuteceUserByLogin( userLogin, plugin, this );

        return user;
    }

    /**
     * Build the http request to send to the provider to validate the authentication
     * @param request The HTTP request
     * @param strOpenID The user OpenID URL
     * @return The URL
     */
    private String getProviderRedirectUrl( HttpServletRequest request, String strOpenID )
    {
        String strReturnUrl = getMessageUrl( request, MESSAGE_KEY_AUTHENTICATION_FAILED );
        String strProxyHostName = AppPropertiesService.getProperty( PROPERTY_PROXY_HOST_NAME );

        //Proxy connection can be anonymous
        if ( ( strProxyHostName != null ) && !strProxyHostName.equals( "" ) )
        {
            ProxyProperties proxyProps = new ProxyProperties(  );
            String strDomain = AppPropertiesService.getProperty( PROPERTY_PROXY_DOMAIN_NAME );
            int strProxyPort = Integer.parseInt( AppPropertiesService.getProperty( PROPERTY_PROXY_PORT_NUMBER ) );
            String strUserName = AppPropertiesService.getProperty( PROPERTY_PROXY_USER_NAME );
            String strPassword = AppPropertiesService.getProperty( PROPERTY_PROXY_PASSWORD );

            proxyProps.setProxyHostName( strProxyHostName );
            proxyProps.setProxyPort( strProxyPort );
            proxyProps.setDomain( strDomain );
            proxyProps.setUserName( strUserName );
            proxyProps.setPassword( strPassword );

            HttpClientFactory.setProxyProperties( proxyProps );
        }

        try
        {
            _manager = new ConsumerManager(  );
            _manager.setAssociations( new InMemoryConsumerAssociationStore(  ) );
            _manager.setNonceVerifier( new InMemoryNonceVerifier( 5000 ) );

            // perform discovery on the user-supplied identifier
            List discoveries = _manager.discover( strOpenID.trim(  ) );

            // attempt to associate with the OpenID provider
            // and retrieve one service endpoint for authentication
            DiscoveryInformation discovered = _manager.associate( discoveries );

            // store the discovery information in the user's session
            request.getSession(  ).setAttribute( "openid-disc", discovered );

            // obtain a AuthRequest message to be sent to the OpenID provider

            // Attribute Exchange example: fetching the 'email' attribute
            SRegRequest fetch = SRegRequest.createFetchRequest(  );
            fetch.addAttribute( "fullname", true );
            fetch.addAttribute( "nickname", true );
            fetch.addAttribute( "email", true );

            //  fetch.addAttribute( ATTRIBUTE_FIRST_NAME, "http://schema.openid.net/namePerson/first", true );
            // fetch.addAttribute( ATTRIBUTE_LAST_NAME, "http://schema.openid.net/namePerson/last", true );
            // fetch.addAttribute( ATTRIBUTE_EMAIL, "http://schema.openid.net/contact/email", true );
            AuthRequest authReq = _manager.authenticate( discovered, getReturnUrl( request ) );

            // attach the extension to the authentication request
            authReq.addExtension( fetch );

            strReturnUrl = authReq.getDestinationUrl( true );
        }
        catch ( OpenIDException e )
        {
            _logger.error( "OpenId Error building authentication request : " + e.getMessage(  ), e );
        }

        return strReturnUrl;
    }

    /**
     * The response URL that will be used by the provider to give its response :
     * authentication validated or not. If OK the response will hold uesr's attributes.
     * @param request The HTTP request
     * @return The URL
     */
    private String getReturnUrl( HttpServletRequest request )
    {
        _logger.debug( "Callback URL : " + AppPathService.getBaseUrl( request ) + URL_CALLBACK );

        return AppPathService.getBaseUrl( request ) + URL_CALLBACK;
    }

    /**
     * Build the URL to display a message
     * @param request The HTTP request
     * @param strMessageKey The message key
     * @return The URL to display the message
     */
    private String getMessageUrl( HttpServletRequest request, String strMessageKey )
    {
        UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + AppPathService.getPortalUrl(  ) );
        url.addParameter( PARAMETER_PAGE, PARAMETER_XPAGE_VALUE );
        url.addParameter( PARAMETER_ERROR, strMessageKey );

        return url.getUrl(  );
    }

    /**
     * processing the authentication response
     * @param request The HTTP request
     * @return The URL depending of the result
     * @throws IncompleteUserDetailsException
     * @throws FirstConnectionException
     */
    public String verifyResponse( HttpServletRequest request )
    {
        String strReturnUrl = getMessageUrl( request, MESSAGE_KEY_AUTHENTICATION_FAILED );

        _logger.debug( "Provider callback - host : " + request.getRemoteHost(  ) + " - IP : " +
            request.getRemoteAddr(  ) );

        BaseUser user = null;

        try
        {
            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList response = new ParameterList( request.getParameterMap(  ) );

            // retrieve the previously stored discovery information
            DiscoveryInformation discovered = (DiscoveryInformation) request.getSession(  ).getAttribute( "openid-disc" );

            // extract the receiving URL from the HTTP request
            StringBuffer receivingURL = request.getRequestURL(  );
            String queryString = request.getQueryString(  );

            if ( ( queryString != null ) && ( queryString.length(  ) > 0 ) )
            {
                receivingURL.append( "?" ).append( request.getQueryString(  ) );
            }

            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            VerificationResult verification = _manager.verify( receivingURL.toString(  ), response, discovered );

            // examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId(  );
            _logger.debug( "Authentication verification  : " + verified );

            if ( verified != null )
            {
                user = new BaseUser( verified.getIdentifier(  ), this );

                AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse(  );

                if ( authSuccess.hasExtension( SRegMessage.OPENID_NS_SREG ) )
                {
                    _logger.debug( "Authentication successfull - identifier : " + verified.getIdentifier(  ) );

                    MessageExtension ext = authSuccess.getExtension( SRegMessage.OPENID_NS_SREG );

                    if ( ext instanceof SRegResponse )
                    {
                        /*      for ( String strKey : (Set<String>) ext.getAttributes(  ).keySet(  ) ) {
                                  _logger.debug( "Attribute " + strKey + " - value : " +
                                          ext.getAttributes(  ).get( strKey ) );
                              }*/
                        SRegResponse sregResp = (SRegResponse) ext;

                        String strFirstName = (String) sregResp.getAttributeValue( "fullname" );
                        String strLastName = (String) sregResp.getAttributeValue( "nickname" );

                        //List emails = sregResp.getAttributeValue(  ATTRIBUTE_EMAIL );
                        //String email = (String) emails.get( 0 );
                        String strEmail = sregResp.getAttributeValue( "email" );

                        user.setUserInfo( LuteceUser.NAME_GIVEN, strFirstName );
                        user.setUserInfo( LuteceUser.NAME_FAMILY, strLastName );
                        user.setUserInfo( LuteceUser.BUSINESS_INFO_ONLINE_EMAIL, strEmail );

                        Plugin plugin = PluginService.getPlugin( PLUGIN_NAME );
                        OpenIdDatabaseUser databaseUser = new OpenIdDatabaseUser(  );
                        databaseUser.setEmail( strEmail );
                        databaseUser.setFirstName( strFirstName );
                        databaseUser.setLastName( strLastName );
                        databaseUser.setLogin( verified.getIdentifier(  ) );
                        databaseUser.setAuthentificationType( "openid" );
                        strReturnUrl = AppPathService.getBaseUrl( request ) + AppPathService.getPortalUrl(  ); // success

                        //if login does not exist
                        if ( !OpenIdDatabaseUserHome.checkUserLogin( verified.getIdentifier(  ), plugin ) )
                        {
                            OpenIdDatabaseUserHome.create( databaseUser, "", plugin ); //User is created 

                            if ( databaseUser.isValid(  ) ) //Verify whether all attributes are filled 
                            {
                                SecurityService.getInstance(  ).registerUser( request, user );
                            }
                            else
                            {
                                strReturnUrl = getUserDetailsUrl( request, databaseUser );
                            }
                        }
                        else
                        {
                            SecurityService.getInstance(  ).registerUser( request, user );

                            if ( databaseUser.isValid(  ) ) //Verify whether all attributes are filled 
                            {
                                OpenIdDatabaseUserHome.updateByLogin( databaseUser, plugin );
                            }
                            else
                            {
                                strReturnUrl = getUserDetailsUrl( request, databaseUser );
                            }
                        }

                        //if the id of the openid is present in database delete
                        //update the user data in the database
                    }
                    else if ( ext instanceof StoreResponse )
                    {
                    }
                }
            }
        }
        catch ( OpenIDException e )
        {
            _logger.error( "OpenId Error in provider response : " + e.getMessage(  ), e );
        }

        return strReturnUrl;
    }

    /**
     * Build the URL to display the user details when a needed field is empty
     * @param request The HTTP request
     * @param user The user object
     * @return The URL to display the message
     */
    private String getUserDetailsUrl( HttpServletRequest request, OpenIdDatabaseUser user )
    {
        UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + AppPathService.getPortalUrl(  ) );
        url.addParameter( PARAMETER_PAGE, PARAMETER_XPAGE_VALUE );
        url.addParameter( "action", "detailsOpenId" ); //TODO clean
        url.addParameter( PARAMETER_LOGIN, user.getLogin(  ) );
        url.addParameter( PARAMETER_FIRST_NAME, user.getFirstName(  ) );
        url.addParameter( PARAMETER_LAST_NAME, user.getLastName(  ) );
        url.addParameter( PARAMETER_EMAIL, user.getEmail(  ) );

        return url.getUrl(  );
    }

    /**
     * 
     *{@inheritDoc}
     */
	public String getIconUrl()
	{
		return CONSTANT_PATH_ICON;
	}

	/**
	 * 
	 *{@inheritDoc}
	 */
	public String getName()
	{
		return OpenIDDatabasePlugin.PLUGIN_NAME;
	}

	/**
	 * 
	 *{@inheritDoc}
	 */
	public String getPluginName()
	{
		return OpenIDDatabasePlugin.PLUGIN_NAME;
	}
}

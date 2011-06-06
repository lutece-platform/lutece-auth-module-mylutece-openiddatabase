/*
 * Copyright (c) 2002-2011, Mairie de Paris
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
package fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.web;

import fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.business.OpenIdDatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.business.OpenIdDatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.business.PasswordRecoveryHome;
import fr.paris.lutece.plugins.mylutece.web.MyLuteceApp;
import fr.paris.lutece.portal.service.captcha.CaptchaSecurityService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.portal.web.xpages.XPageApplication;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.string.StringUtil;
import fr.paris.lutece.util.url.UrlItem;

import org.apache.log4j.Logger;

import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;


/**
 * This class provides the XPageApp that manage personalization features for Mylutece Database module
 * : login, account management, ...
 */
public class MyLuteceOpenIdDatabaseApp implements XPageApplication
{
    private static final String PARAMETER_PAGE = "page";
    private static final String PARAMETER_PAGE_VALUE = "openid";
    private static final String PROPERTY_DATABASE_TYPE = "database";

    // Markers
    private static final String MARK_USER = "user";
    private static final String MARK_ROLES = "roles";
    private static final String MARK_GROUPS = "groups";
    private static final String MARK_PLUGIN_NAME = "plugin_name";
    private static final String MARK_ERROR_CODE = "error_code";
    private static final String MARK_ACTION_SUCCESSFUL = "action_successful";
    private static final String MARK_EMAIL = "email";
    private static final String MARK_ID_TOKEN = "id_token";

    // Markers
    private static final String MARK_ERROR_MESSAGE = "error_message";
    private static final String MARK_URL_DOLOGIN = "url_dologin";

    // Parameters
    private static final String PARAMETER_ACTION = "action";
    private static final String PARAMETER_OLD_PASSWORD = "old_password";
    private static final String PARAMETER_NEW_PASSWORD = "new_password";
    private static final String PARAMETER_CONFIRMATION_PASSWORD = "confirmation_password";
    private static final String PARAMETER_PLUGIN_NAME = "plugin_name";
    private static final String PARAMETER_ERROR_CODE = "error_code";
    private static final String PARAMETER_EMAIL = "email";
    private static final String PARAMETER_ACTION_SUCCESSFUL = "action_successful";
    private static final String PARAMETER_LOGIN = "login";
    private static final String PARAMETER_PASSWORD = "password";
    private static final String PARAMETER_LAST_NAME = "last_name";
    private static final String PARAMETER_FIRST_NAME = "first_name";
    private static final String PARAMETER_ID_TOKEN = "id_token";

    //OpenId 
    public static final String PARAMETER_ERROR = "error";

    // Actions
    private static final String ACTION_CHANGE_PASSWORD = "changePassword";
    private static final String ACTION_CHANGE_PASSWORD_LINK = "changePasswordLink";
    private static final String ACTION_VIEW_ACCOUNT = "viewAccount";
    private static final String ACTION_LOST_PASSWORD = "lostPassword";
    private static final String ACTION_ACCESS_DENIED = "accessDenied";
    private static final String ACTION_CREATE_ACCOUNT = "createAccount";
    private static final String ACTION_LOGIN_OPENID = "loginOpenId";
    private static final String ACTION_DETAILS_OPENID = "detailsOpenId";

    // Errors
    private static final String ERROR_OLD_PASSWORD = "error_old_password";
    private static final String ERROR_CONFIRMATION_PASSWORD = "error_confirmation_password";
    private static final String ERROR_SAME_PASSWORD = "error_same_password";
    private static final String ERROR_SYNTAX_EMAIL = "error_syntax_email";
    private static final String ERROR_SENDING_EMAIL = "error_sending_email";
    private static final String ERROR_UNKNOWN_EMAIL = "error_unknown_email";
    private static final String ERROR_MANDATORY_FIELDS = "error_mandatory_fields";
    private static final String ERROR_LOGIN_ALREADY_EXISTS = "error_login_already_exists";

    // Templates
    private static final String TEMPLATE_LOST_PASSWORD_PAGE = "skin/plugins/mylutece/modules/openiddatabase/lost_password.html";
    private static final String TEMPLATE_VIEW_ACCOUNT_PAGE = "skin/plugins/mylutece/modules/openiddatabase/view_account.html";
    private static final String TEMPLATE_CHANGE_PASSWORD_PAGE = "skin/plugins/mylutece/modules/openiddatabase/change_password.html";
    private static final String TEMPLATE_CHANGE_PASSWORD_PAGE_LINK = "skin/plugins/mylutece/modules/openiddatabase/change_password_link.html";
    private static final String TEMPLATE_CREATE_ACCOUNT_PAGE = "skin/plugins/mylutece/modules/openiddatabase/create_account.html";
    private static final String TEMPLATE_USER_CONFIRMATION = "skin/plugins/mylutece/modules/openiddatabase/user_confirmation.html";

    // Properties
    private static final String PROPERTY_MYLUTECE_CHANGE_PASSWORD_URL = "mylutece-openiddatabase.url.changePassword.page";
    private static final String PROPERTY_MYLUTECE_VIEW_ACCOUNT_URL = "mylutece-openiddatabase.url.viewAccount.page";
    private static final String PROPERTY_MYLUTECE_CREATE_ACCOUNT_URL = "mylutece-openiddatabase.url.createAccount.page";
    private static final String PROPERTY_MYLUTECE_LOST_PASSWORD_URL = "mylutece-openiddatabase.url.lostPassword.page";
    private static final String PROPERTY_MYLUTECE_CHANGE_PASSWORD_LINK_URL = "mylutece-openiddatabase.url.changePasswordLink.page";
    private static final String PROPERTY_MYLUTECE_ACCESS_DENIED_URL = "mylutece-openiddatabase.url.accessDenied.page";
    private static final String PROPERTY_MYLUTECE_DEFAULT_REDIRECT_URL = "mylutece-openiddatabase.url.default.redirect";
    private static final String PROPERTY_MYLUTECE_TEMPLATE_ACCESS_DENIED = "mylutece-openiddatabase.template.accessDenied";
    private static final String PROPERTY_MYLUTECE_TEMPLATE_ACCESS_CONTROLED = "mylutece-openiddatabase.template.accessControled";
    private static final String PROPERTY_MAIL_HOST = "mail.server";
    private static final String PROPERTY_PORTAL_NAME = "lutece.name";
    private static final String PROPERTY_NOREPLY_EMAIL = "mail.noreply.email";

    //OpenId
    private static final String PROPERTY_PAGETITLE_LOGIN = "module.mylutece.openiddatabase.xpage.loginPageTitle"; //TODO Add in properties
    private static final String PROPERTY_PATHLABEL_LOGIN = "module.mylutece.openiddatabase.xpage.loginPagePath"; //TODO Add in properties

    // i18n Properties
    private static final String PROPERTY_CHANGE_PASSWORD_LABEL = "module.mylutece.openiddatabase.xpage.changePassword.label";
    private static final String PROPERTY_CHANGE_PASSWORD_TITLE = "module.mylutece.openiddatabase.xpage.changePassword.title";
    private static final String PROPERTY_VIEW_ACCOUNT_LABEL = "module.mylutece.openiddatabase.xpage.viewAccount.label";
    private static final String PROPERTY_VIEW_ACCOUNT_TITLE = "module.mylutece.openiddatabase.xpage.viewAccount.title";
    private static final String PROPERTY_LOST_PASSWORD_LABEL = "module.mylutece.openiddatabase.xpage.lostPassword.label";
    private static final String PROPERTY_LOST_PASSWORD_TITLE = "module.mylutece.openiddatabase.xpage.lostPassword.title";
    private static final String PROPERTY_CREATE_ACCOUNT_LABEL = "module.mylutece.openiddatabase.xpage.createAccount.label";
    private static final String PROPERTY_CREATE_ACCOUNT_TITLE = "module.mylutece.openiddatabase.xpage.createAccount.title";
    private static final String PROPERTY_ACCESS_DENIED_ERROR_MESSAGE = "module.mylutece.openiddatabase.siteMessage.access_denied.errorMessage";
    private static final String PROPERTY_ACCESS_DENIED_TITLE_MESSAGE = "module.mylutece.openiddatabase.siteMessage.access_denied.title";
    private static final String PROPERTY_LINK_EXPIRED_ERROR_MESSAGE = "module.mylutece.openiddatabase.siteMessage.link_expired.errorMessage";
    private static final String PROPERTY_LINK_EXPIRED_TITLE_MESSAGE = "module.mylutece.openiddatabase.siteMessage.link_expired.title";
    private static final String PLUGIN_NAME = "mylutece-openiddatabase";
    private static final String JCAPTCHA_PLUGIN = "jcaptcha";

    // Templates Open Id
    private static final String TEMPLATE_LOGIN_PAGE = "skin/plugins/mylutece/modules/openiddatabase/login_form.html";
    private static final String MARK_CAPTCHA = "captcha";
    private static final String MARK_IS_ACTIVE_CAPTCHA = "is_active_captcha";
    private static final String ERROR_CAPTCHA = "error_captcha";
    private static Logger _logger = Logger.getLogger( "openiddatabase" );
    private static ConsumerManager _manager;

    //Captcha
    private CaptchaSecurityService _captchaService = new CaptchaSecurityService(  );

    // private fields
    private Plugin _plugin;
    private Locale _locale;

    /**
     *
     * @param request The HTTP request
     * @param plugin The plugin
     */
    public void init( HttpServletRequest request, Plugin plugin )
    {
        _locale = request.getLocale(  );
        _plugin = plugin;

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
     *
     * @param request The HTTP request
     * @param nMode The mode (admin, ...)
     * @param plugin The plugin
     * @return The Xpage
     * @throws UserNotSignedException if user not signed
     * @throws SiteMessageException Occurs when a site message need to be displayed
     */
    public XPage getPage( HttpServletRequest request, int nMode, Plugin plugin )
        throws UserNotSignedException, SiteMessageException
    {
        XPage page = new XPage(  );
        String strAction = request.getParameter( PARAMETER_ACTION );
        Locale locale = request.getLocale(  );
        init( request, plugin );

        if ( ( strAction == null ) || strAction.equals( ACTION_LOGIN_OPENID ) )
        {
            return getLoginPage( page, request, locale );
        }

        if ( strAction.equals( ACTION_DETAILS_OPENID ) )
        {
            return getUserConfirmation( page, request, locale );
        }

        if ( strAction.equals( ACTION_CHANGE_PASSWORD ) )
        {
            page = getChangePasswordPage( page, request );
        }
        else if ( strAction.equals( ACTION_VIEW_ACCOUNT ) )
        {
            page = getViewAccountPage( page, request );
        }
        else if ( strAction.equals( ACTION_LOST_PASSWORD ) )
        {
            page = getLostPasswordPage( page, request );
        }
        else if ( strAction.equals( ACTION_CREATE_ACCOUNT ) )
        {
            page = getCreateAccountPage( page, request );
        }
        else if ( strAction.equals( ACTION_CHANGE_PASSWORD_LINK ) )
        {
            page = getChangePasswordLinkPage( page, request );
        }

        if ( strAction.equals( ACTION_ACCESS_DENIED ) || ( page == null ) )
        {
            SiteMessageService.setMessage( request, PROPERTY_ACCESS_DENIED_ERROR_MESSAGE, null,
                PROPERTY_ACCESS_DENIED_TITLE_MESSAGE, null, null, SiteMessage.TYPE_STOP );
        }
        return page;
    }

    /**
     * Returns the NewAccount URL of the Authentication Service
     * @return The URL
     */
    public static String getChangePasswordUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_CHANGE_PASSWORD_URL );
    }

    /**
     * Returns the Change password link
     * @return The URL
     */
    public static String getChangePasswordLinkUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_CHANGE_PASSWORD_LINK_URL );
    }

    /**
     * Returns the ViewAccount URL of the Authentication Service
     * @return The URL
     */
    public static String getViewAccountUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_VIEW_ACCOUNT_URL );
    }

    /**
     * Returns the createAccount URL of the Authentication Service
     * @return The URL
     */
    public static String getNewAccountUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_CREATE_ACCOUNT_URL );
    }

    /**
     * Returns the Lost Password URL of the Authentication Service
     * @return The URL
     */
    public static String getLostPasswordUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_LOST_PASSWORD_URL );
    }

    /**
     * Returns the Default redirect URL of the Authentication Service
     * @return The URL
     */
    public static String getDefaultRedirectUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_DEFAULT_REDIRECT_URL );
    }

    /**
     * Returns the NewAccount URL of the Authentication Service
     * @return The URL
     */
    public static String getAccessDeniedUrl(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_ACCESS_DENIED_URL );
    }

    /**
     * This method is call by the JSP named DoMyLuteceLogout.jsp
     * @param request The HTTP request
     * @return The URL to forward depending of the result of the login.
     */
    public String doLogout( HttpServletRequest request )
    {
        SecurityService.getInstance(  ).logoutUser( request );

        return getDefaultRedirectUrl(  );
    }

    /**
     * Build the ViewAccount page
     * @param page The XPage object to fill
     * @param request The HTTP request
     * @return The XPage object containing the page content
     */
    private XPage getViewAccountPage( XPage page, HttpServletRequest request )
    {
        HashMap<String, Object> model = new HashMap<String, Object>(  );
        OpenIdDatabaseUser user = getRemoteUser( request );

        if ( user == null )
        {
            return null;
        }

        LuteceUser luteceUser = SecurityService.getInstance(  ).getRegisteredUser( request );

        if ( luteceUser == null )
        {
            return null;
        }

        model.put( MARK_USER, user );
        model.put( MARK_ROLES, luteceUser.getRoles(  ) );
        model.put( MARK_GROUPS, luteceUser.getGroups(  ) );

        HtmlTemplate t = AppTemplateService.getTemplate( TEMPLATE_VIEW_ACCOUNT_PAGE, _locale, model );
        page.setContent( t.getHtml(  ) );
        page.setPathLabel( I18nService.getLocalizedString( PROPERTY_VIEW_ACCOUNT_LABEL, _locale ) );
        page.setTitle( I18nService.getLocalizedString( PROPERTY_VIEW_ACCOUNT_TITLE, _locale ) );

        return page;
    }

    /**
     * Build the createAccount page
     * @param page The XPage object to fill
     * @param request The HTTP request
     * @return The XPage object containing the page content
     */
    private XPage getCreateAccountPage( XPage page, HttpServletRequest request )
    {
        HashMap<String, Object> model = new HashMap<String, Object>(  );
        OpenIdDatabaseUser user = new OpenIdDatabaseUser(  );

        String strErrorCode = request.getParameter( PARAMETER_ERROR_CODE );
        String strLogin = request.getParameter( PARAMETER_LOGIN );
        String strLastName = request.getParameter( PARAMETER_LAST_NAME );
        String strFirstName = request.getParameter( PARAMETER_FIRST_NAME );
        String strEmail = request.getParameter( PARAMETER_EMAIL );
        String strSuccess = request.getParameter( PARAMETER_ACTION_SUCCESSFUL );

        if ( strLogin != null )
        {
            user.setLogin( strLogin );
        }

        if ( strLastName != null )
        {
            user.setLastName( strLastName );
        }

        if ( strFirstName != null )
        {
            user.setFirstName( strFirstName );
        }

        if ( strEmail != null )
        {
            user.setEmail( strEmail );
        }

        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( MARK_ERROR_CODE, strErrorCode );
        model.put( MARK_USER, user );
        model.put( MARK_IS_ACTIVE_CAPTCHA, PluginService.isPluginEnable( JCAPTCHA_PLUGIN ) );
        model.put( MARK_CAPTCHA, _captchaService.getHtmlCode(  ) );
        model.put( MARK_ACTION_SUCCESSFUL, strSuccess );

        HtmlTemplate t = AppTemplateService.getTemplate( TEMPLATE_CREATE_ACCOUNT_PAGE, _locale, model );
        page.setContent( t.getHtml(  ) );
        page.setPathLabel( I18nService.getLocalizedString( PROPERTY_CREATE_ACCOUNT_LABEL, _locale ) );
        page.setTitle( I18nService.getLocalizedString( PROPERTY_CREATE_ACCOUNT_TITLE, _locale ) );

        return page;
    }

    /**
     * This method is call by the JSP named DoCreateAccount.jsp
     * @param request The HTTP request
     * @return The URL to forward depending of the result of the change.
     */
    public String doCreateAccount( HttpServletRequest request )
    {
        Plugin plugin = PluginService.getPlugin( request.getParameter( PARAMETER_PLUGIN_NAME ) );
        OpenIdDatabaseUser databaseUser = new OpenIdDatabaseUser(  );
        init( request, plugin );

        UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + getNewAccountUrl(  ) );
        url.addParameter( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );

        String strError = null;
        String strLogin = request.getParameter( PARAMETER_LOGIN );
        String strPassword = request.getParameter( PARAMETER_PASSWORD );
        String strConfirmation = request.getParameter( PARAMETER_CONFIRMATION_PASSWORD );
        String strLastName = request.getParameter( PARAMETER_LAST_NAME );
        String strFirstName = request.getParameter( PARAMETER_FIRST_NAME );
        String strEmail = request.getParameter( PARAMETER_EMAIL );

        url.addParameter( PARAMETER_LOGIN, strLogin );
        url.addParameter( PARAMETER_LAST_NAME, strLastName );
        url.addParameter( PARAMETER_FIRST_NAME, strFirstName );
        url.addParameter( PARAMETER_EMAIL, strEmail );

        if ( ( strLogin == null ) || ( strPassword == null ) || ( strConfirmation == null ) || ( strFirstName == null ) ||
                ( ( strEmail == null ) || ( strLastName == null ) || strLogin.equals( "" ) || strPassword.equals( "" ) ||
                strConfirmation.equals( "" ) || strLastName.equals( "" ) || strFirstName.equals( "" ) ) ||
                strEmail.equals( "" ) )
        {
            strError = ERROR_MANDATORY_FIELDS;
        }

        //Check login unique code
        if ( ( strError == null ) &&
                !OpenIdDatabaseUserHome.findDatabaseUsersListForLogin( strLogin, _plugin ).isEmpty(  ) )
        {
            strError = ERROR_LOGIN_ALREADY_EXISTS;
        }

        //Check password confirmation
        if ( ( strError == null ) && !checkPassword( strPassword, strConfirmation ) )
        {
            strError = ERROR_CONFIRMATION_PASSWORD;
        }

        //Check email format
        if ( ( strError == null ) && !StringUtil.checkEmail( strEmail ) )
        {
            strError = ERROR_SYNTAX_EMAIL;
        }

        // test the captcha
        if ( PluginService.isPluginEnable( JCAPTCHA_PLUGIN ) )
        {
            if ( !_captchaService.validate( request ) )
            {
                strError = ERROR_CAPTCHA;
            }
        }

        if ( strError != null )
        {
            url.addParameter( PARAMETER_ERROR_CODE, strError );

            return url.getUrl(  );
        }
        else
        {
            databaseUser.setLogin( strLogin );
            databaseUser.setLastName( strLastName );
            databaseUser.setFirstName( strFirstName );
            databaseUser.setEmail( strEmail );
            databaseUser.setAuthentificationType( PROPERTY_DATABASE_TYPE );
            OpenIdDatabaseUserHome.create( databaseUser, strPassword, _plugin );
        }

        url.addParameter( PARAMETER_ACTION_SUCCESSFUL, getDefaultRedirectUrl(  ) );

        return url.getUrl(  );
    }

    /**
     * Build the default Lost password page
     * @param page The XPage object to fill
     * @param request The HTTP request
     * @return The XPage object containing the page content
     */
    private XPage getLostPasswordPage( XPage page, HttpServletRequest request )
    {
        HashMap<String, Object> model = new HashMap<String, Object>(  );
        String strErrorCode = request.getParameter( PARAMETER_ERROR_CODE );
        String strStateSending = request.getParameter( PARAMETER_ACTION_SUCCESSFUL );
        String strEmail = request.getParameter( PARAMETER_EMAIL );

        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( MARK_ERROR_CODE, strErrorCode );
        model.put( MARK_ACTION_SUCCESSFUL, strStateSending );
        model.put( MARK_EMAIL, strEmail );

        HtmlTemplate t = AppTemplateService.getTemplate( TEMPLATE_LOST_PASSWORD_PAGE, _locale, model );
        page.setContent( t.getHtml(  ) );
        page.setPathLabel( I18nService.getLocalizedString( PROPERTY_LOST_PASSWORD_LABEL, _locale ) );
        page.setTitle( I18nService.getLocalizedString( PROPERTY_LOST_PASSWORD_TITLE, _locale ) );

        return page;
    }

    /**
     * Build the default Change password page
     * @param page The XPage object to fill
     * @param request The HTTP request
     * @return The XPage object containing the page content
     */
    private XPage getChangePasswordLinkPage( XPage page, HttpServletRequest request )
    {
        HashMap<String, Object> model = new HashMap<String, Object>(  );
        String strErrorCode = request.getParameter( PARAMETER_ERROR_CODE );
        String strSuccess = request.getParameter( PARAMETER_ACTION_SUCCESSFUL );
        String strToken = request.getParameter( PARAMETER_ID_TOKEN );

        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( MARK_ERROR_CODE, strErrorCode );
        model.put( MARK_ACTION_SUCCESSFUL, strSuccess );
        model.put( MARK_ID_TOKEN, strToken );

        HtmlTemplate t = AppTemplateService.getTemplate( TEMPLATE_CHANGE_PASSWORD_PAGE_LINK, _locale, model );
        page.setContent( t.getHtml(  ) );
        page.setPathLabel( I18nService.getLocalizedString( PROPERTY_CHANGE_PASSWORD_LABEL, _locale ) );
        page.setTitle( I18nService.getLocalizedString( PROPERTY_CHANGE_PASSWORD_TITLE, _locale ) );

        return page;
    }

    /**
     * Build the default Change password page
     * @param page The XPage object to fill
     * @param request The HTTP request
     * @return The XPage object containing the page content
     */
    private XPage getChangePasswordPage( XPage page, HttpServletRequest request )
    {
        HashMap<String, Object> model = new HashMap<String, Object>(  );
        String strErrorCode = request.getParameter( PARAMETER_ERROR_CODE );
        String strSuccess = request.getParameter( PARAMETER_ACTION_SUCCESSFUL );

        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( MARK_ERROR_CODE, strErrorCode );
        model.put( MARK_ACTION_SUCCESSFUL, strSuccess );

        HtmlTemplate t = AppTemplateService.getTemplate( TEMPLATE_CHANGE_PASSWORD_PAGE, _locale, model );
        page.setContent( t.getHtml(  ) );
        page.setPathLabel( I18nService.getLocalizedString( PROPERTY_CHANGE_PASSWORD_LABEL, _locale ) );
        page.setTitle( I18nService.getLocalizedString( PROPERTY_CHANGE_PASSWORD_TITLE, _locale ) );

        return page;
    }

    /**
     * This method is call by the JSP named DoChangePassword.jsp
     * @param request The HTTP request
     * @return The URL to forward depending of the result of the change.
     */
    public String doChangePassword( HttpServletRequest request )
    {
        Plugin plugin = PluginService.getPlugin( request.getParameter( PARAMETER_PLUGIN_NAME ) );
        init( request, plugin );

        UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + getChangePasswordUrl(  ) );
        url.addParameter( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );

        String strError = null;
        OpenIdDatabaseUser user = getRemoteUser( request );
        String strOldPassword = request.getParameter( PARAMETER_OLD_PASSWORD );
        String strNewPassword = request.getParameter( PARAMETER_NEW_PASSWORD );
        String strConfirmationPassword = request.getParameter( PARAMETER_CONFIRMATION_PASSWORD );

        if ( user == null )
        {
            try
            {
                SiteMessageService.setMessage( request, PROPERTY_ACCESS_DENIED_ERROR_MESSAGE, null,
                    PROPERTY_ACCESS_DENIED_TITLE_MESSAGE, null, null, SiteMessage.TYPE_STOP );
            }
            catch ( SiteMessageException e )
            {
                return AppPathService.getBaseUrl( request );
            }
        }

        if ( ( strOldPassword == null ) || ( strNewPassword == null ) || ( strConfirmationPassword == null ) ||
                strOldPassword.equals( "" ) || strNewPassword.equals( "" ) || strConfirmationPassword.equals( "" ) )
        {
            strError = ERROR_MANDATORY_FIELDS;
        }

        if ( ( strError == null ) &&
                !OpenIdDatabaseUserHome.checkPassword( user.getLogin(  ), strOldPassword, _plugin ) )
        {
            strError = ERROR_OLD_PASSWORD;
        }

        if ( ( strError == null ) && !checkPassword( strNewPassword, strConfirmationPassword ) )
        {
            strError = ERROR_CONFIRMATION_PASSWORD;
        }

        if ( ( strError == null ) && strNewPassword.equals( strOldPassword ) )
        {
            strError = ERROR_SAME_PASSWORD;
        }

        if ( strError != null )
        {
            url.addParameter( PARAMETER_ERROR_CODE, strError );
        }
        else
        {
            OpenIdDatabaseUserHome.updatePassword( user, strNewPassword, _plugin );
            url.addParameter( PARAMETER_ACTION_SUCCESSFUL, getDefaultRedirectUrl(  ) );
        }

        return url.getUrl(  );
    }

    /**
     * This method is call by the JSP named DoChangePassword.jsp
     * @param request The HTTP request
     * @return The URL to forward depending of the result of the change.
     */
    public String doChangePasswordLink( HttpServletRequest request )
    {
        Plugin plugin = PluginService.getPlugin( request.getParameter( PARAMETER_PLUGIN_NAME ) );
        init( request, plugin );

        UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + getChangePasswordUrl(  ) );
        url.addParameter( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );

        String strError = null;

        String strIdToken = request.getParameter( PARAMETER_ID_TOKEN );
        String strNewPassword = request.getParameter( PARAMETER_NEW_PASSWORD );
        String strConfirmationPassword = request.getParameter( PARAMETER_CONFIRMATION_PASSWORD );
        int nUserId = PasswordRecoveryHome.findUserId( strIdToken, plugin );

        OpenIdDatabaseUser user = OpenIdDatabaseUserHome.findByPrimaryKey( nUserId, plugin );

        if ( user == null )
        {
            try
            {
                SiteMessageService.setMessage( request, PROPERTY_ACCESS_DENIED_ERROR_MESSAGE, null,
                    PROPERTY_ACCESS_DENIED_TITLE_MESSAGE, null, null, SiteMessage.TYPE_STOP );
            }
            catch ( SiteMessageException e )
            {
                return AppPathService.getBaseUrl( request );
            }
        }

        boolean bExpired = PasswordRecoveryHome.isExpired( strIdToken, plugin );

        if ( bExpired )
        {
            try
            {
                SiteMessageService.setMessage( request, PROPERTY_LINK_EXPIRED_ERROR_MESSAGE, null,
                    PROPERTY_LINK_EXPIRED_TITLE_MESSAGE, null, null, SiteMessage.TYPE_STOP );
            }
            catch ( SiteMessageException e )
            {
                return AppPathService.getBaseUrl( request );
            }
        }

        if ( ( strNewPassword == null ) || ( strConfirmationPassword == null ) || strNewPassword.equals( "" ) ||
                strConfirmationPassword.equals( "" ) )
        {
            strError = ERROR_MANDATORY_FIELDS;
        }

        if ( ( strError == null ) && !checkPassword( strNewPassword, strConfirmationPassword ) )
        {
            strError = ERROR_CONFIRMATION_PASSWORD;
        }

        if ( strError != null )
        {
            url.addParameter( PARAMETER_ERROR_CODE, strError );
        }
        else
        {
            OpenIdDatabaseUserHome.updatePassword( user, strNewPassword, _plugin );
            url.addParameter( PARAMETER_ACTION_SUCCESSFUL, getDefaultRedirectUrl(  ) );
        }

        return url.getUrl(  );
    }

    /**
     * Check the password with the password confirmation string
     * Check if password is empty
     *
     * @param strPassword The password
     * @param strConfirmation The password confirmation
     * @return true if password is equal to confirmation password and not empty
     */
    private boolean checkPassword( String strPassword, String strConfirmation )
    {
        Boolean bReturn = true;

        if ( ( strPassword == null ) || ( strConfirmation == null ) || strPassword.equals( "" ) ||
                !strPassword.equals( strConfirmation ) )
        {
            bReturn = false;
        }

        return bReturn;
    }

    /**
     * This method is call by the JSP named DoSendPassword.jsp
     * @param request The HTTP request
     * @return The URL to forward depending of the result of the sending.
     */
    public String doSendPassword( HttpServletRequest request )
    {
        Plugin plugin = PluginService.getPlugin( request.getParameter( PARAMETER_PLUGIN_NAME ) );
        init( request, plugin );

        HashMap<String, Object> model = new HashMap<String, Object>(  );
        String strError = null;
        UrlItem url = null;
        OpenIdDatabaseUser user = null;

        String strEmail = request.getParameter( PARAMETER_EMAIL );
        url = new UrlItem( AppPathService.getBaseUrl( request ) + getLostPasswordUrl(  ) );
        url.addParameter( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );
        url.addParameter( PARAMETER_EMAIL, strEmail );

        // Check mandatory fields
        if ( ( strEmail == null ) || strEmail.equals( "" ) )
        {
            strError = ERROR_MANDATORY_FIELDS;
        }

        // Check email format
        if ( ( strError == null ) && !StringUtil.checkEmail( strEmail ) )
        {
            strError = ERROR_SYNTAX_EMAIL;
        }

        user = OpenIdDatabaseUserHome.selectDatabaseUserByEmail( strEmail, _plugin );

        if ( ( strError == null ) && ( user == null ) )
        {
            strError = ERROR_UNKNOWN_EMAIL;
        }

        if ( strError == null )
        {
            model.put( MARK_USER, user );

            String strHost = AppPropertiesService.getProperty( PROPERTY_MAIL_HOST );
            String strName = AppPropertiesService.getProperty( PROPERTY_PORTAL_NAME );
            String strSender = AppPropertiesService.getProperty( PROPERTY_NOREPLY_EMAIL );

            if ( ( strError == null ) && ( strHost.equals( "" ) || strName.equals( "" ) || strSender.equals( "" ) ) )
            {
                strError = ERROR_SENDING_EMAIL;
            }
            else
            {
                PasswordRecoveryHome.processOperations( user, _locale, plugin );
            }
        }

        else
        {
            url.addParameter( PARAMETER_ERROR_CODE, strError );

            return url.getUrl(  );
        }

        url.addParameter( PARAMETER_ACTION_SUCCESSFUL, getDefaultRedirectUrl(  ) );

        return url.getUrl(  );
    }

    /**
     * Returns the template for access denied
     * @return The template path
     */
    public static String getAccessDeniedTemplate(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_TEMPLATE_ACCESS_DENIED );
    }

    /**
     * Returns the template for access controled
     * @return The template path
     */
    public static String getAccessControledTemplate(  )
    {
        return AppPropertiesService.getProperty( PROPERTY_MYLUTECE_TEMPLATE_ACCESS_CONTROLED );
    }

    /**
     * Get the remote user
     *
     * @param request The HTTP request
     * @return The Database User
     */
    private OpenIdDatabaseUser getRemoteUser( HttpServletRequest request )
    {
        LuteceUser luteceUser = SecurityService.getInstance(  ).getRegisteredUser( request );

        if ( luteceUser == null )
        {
            return null;
        }

        Collection<OpenIdDatabaseUser> listUsers = OpenIdDatabaseUserHome.findDatabaseUsersListForLogin( luteceUser.getName(  ),
                _plugin );

        if ( listUsers.size(  ) != 1 )
        {
            return null;
        }

        OpenIdDatabaseUser user = (OpenIdDatabaseUser) listUsers.iterator(  ).next(  );

        return user;
    }

    /**
    * Build the Login page
    * @param page The XPage object to fill
    * @param request The HTTP request
    * @param locale The current locale
    * @return The XPage object containing the page content
    */
    private XPage getLoginPage( XPage page, HttpServletRequest request, Locale locale )
    {
        HashMap<String, Object> model = new HashMap<String, Object>(  );

        String strError = request.getParameter( PARAMETER_ERROR );
        String strErrorMessage = "";

        if ( strError != null )
        {
            strErrorMessage = I18nService.getLocalizedString( strError, locale );
        }

        model.put( MARK_ERROR_MESSAGE, strErrorMessage );
        model.put( MARK_URL_DOLOGIN, MyLuteceApp.getDoLoginUrl(  ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_LOGIN_PAGE, locale, model );

        page.setContent( template.getHtml(  ) );
        page.setTitle( I18nService.getLocalizedString( PROPERTY_PAGETITLE_LOGIN, locale ) );
        page.setPathLabel( I18nService.getLocalizedString( PROPERTY_PATHLABEL_LOGIN, locale ) );

        return page;
    }

    /**
     * Build the user confirmation page
     * @param page The XPage object to fill
     * @param request The HTTP request
     * @param locale The current locale
     * @return The XPage object containing the page content
     */
    private XPage getUserConfirmation( XPage page, HttpServletRequest request, Locale locale )
    {
        HashMap<String, Object> model = new HashMap<String, Object>(  );

        String strError = request.getParameter( PARAMETER_ERROR );
        String strErrorMessage = "";

        if ( strError != null )
        {
            strErrorMessage = I18nService.getLocalizedString( strError, locale );
        }

        String strEmail = request.getParameter( PARAMETER_EMAIL );
        String strFirstName = request.getParameter( PARAMETER_FIRST_NAME );
        String strLastName = request.getParameter( PARAMETER_LAST_NAME );
        String strLogin = request.getParameter( PARAMETER_LOGIN );

        OpenIdDatabaseUser user = new OpenIdDatabaseUser(  );
        user.setEmail( strEmail );
        user.setFirstName( strFirstName );
        user.setLastName( strLastName );
        user.setLogin( strLogin );
        user.setAuthentificationType( "openid" );

        model.put( MARK_USER, user );
        model.put( MARK_ERROR_MESSAGE, strErrorMessage );
        model.put( MARK_URL_DOLOGIN, MyLuteceApp.getDoLoginUrl(  ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_USER_CONFIRMATION, locale, model );

        page.setContent( template.getHtml(  ) );
        page.setTitle( I18nService.getLocalizedString( PROPERTY_PAGETITLE_LOGIN, locale ) );
        page.setPathLabel( I18nService.getLocalizedString( PROPERTY_PATHLABEL_LOGIN, locale ) );

        return page;
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
        url.addParameter( PARAMETER_PAGE, PARAMETER_PAGE_VALUE );
        url.addParameter( PARAMETER_ERROR, strMessageKey );

        return url.getUrl(  );
    }

    /**
     * This method is call by the JSP named DoCreateAccount.jsp
     * @param request The HTTP request
     * @return The URL to forward depending of the result of the change.
     */
    public String doModifyUserDetails( HttpServletRequest request )
    {
        Plugin plugin = PluginService.getPlugin( PLUGIN_NAME );
        OpenIdDatabaseUser databaseUser = new OpenIdDatabaseUser(  );
        init( request, plugin );

        UrlItem url = new UrlItem( AppPathService.getBaseUrl( request ) + getNewAccountUrl(  ) );
        url.addParameter( PARAMETER_PLUGIN_NAME, PLUGIN_NAME );

        String strError = null;
        String strLogin = request.getParameter( PARAMETER_LOGIN );
        String strLastName = request.getParameter( PARAMETER_LAST_NAME );
        String strFirstName = request.getParameter( PARAMETER_FIRST_NAME );
        String strEmail = request.getParameter( PARAMETER_EMAIL );

        url.addParameter( PARAMETER_LOGIN, strLogin );
        url.addParameter( PARAMETER_LAST_NAME, strLastName );
        url.addParameter( PARAMETER_FIRST_NAME, strFirstName );
        url.addParameter( PARAMETER_EMAIL, strEmail );

        if (  strLogin == null  ||  strFirstName == null ||
                ( ( strEmail == null ) || ( strLastName == null ) || strLogin.equals( "" ) || strLastName.equals( "" ) ||
                strFirstName.equals( "" ) ) || strEmail.equals( "" ) )
        {
            strError = ERROR_MANDATORY_FIELDS;
        }

        //Check email format
        if ( ( strError == null ) && !StringUtil.checkEmail( strEmail ) )
        {
            strError = ERROR_SYNTAX_EMAIL;
        }

        if ( strError != null )
        {
            url.addParameter( PARAMETER_ERROR_CODE, strError );

            return url.getUrl(  );
        }
        else
        {
            databaseUser.setLogin( strLogin );
            databaseUser.setLastName( strLastName );
            databaseUser.setFirstName( strFirstName );
            databaseUser.setEmail( strEmail );
            databaseUser.setAuthentificationType( "openid" );
            OpenIdDatabaseUserHome.updateByLogin( databaseUser, plugin );
        }

        url.addParameter( PARAMETER_ACTION_SUCCESSFUL, getDefaultRedirectUrl(  ) );

        return url.getUrl(  );
    }
}

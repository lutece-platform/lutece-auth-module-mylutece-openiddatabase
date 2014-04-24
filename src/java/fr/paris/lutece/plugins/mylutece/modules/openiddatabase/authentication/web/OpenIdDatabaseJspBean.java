/*
 * Copyright (c) 2002-2014, Mairie de Paris
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

import fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.business.OpenIdDatabaseHome;
import fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.business.OpenIdDatabaseUser;
import fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.business.OpenIdDatabaseUserHome;
import fr.paris.lutece.plugins.mylutece.service.RoleResourceIdService;
import fr.paris.lutece.portal.business.role.Role;
import fr.paris.lutece.portal.business.role.RoleHome;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.web.admin.PluginAdminPageJspBean;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.string.StringUtil;
import fr.paris.lutece.util.url.UrlItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;


/**
 * This class provides the user interface to manage roles features ( manage, create, modify, remove )
 */
public class OpenIdDatabaseJspBean extends PluginAdminPageJspBean
{
    // Right
    public static final String RIGHT_MANAGE_DATABASE_USERS = "OPENID_DATABASE_MANAGEMENT_USERS";

    // Contants
    private static final String MANAGE_USERS = "ManageUsers.jsp";
    private static final String REGEX_DATABASE_USER_ID = "^[\\d]+$";

    //JSP
    private static final String JSP_DO_REMOVE_USER = "jsp/admin/plugins/mylutece/modules/openiddatabase/DoRemoveUser.jsp";

    //Propety
    private static final String PROPERTY_PAGE_TITLE_MANAGE_USERS = "module.mylutece.openiddatabase.manage_users.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_USER = "module.mylutece.openiddatabase.create_user.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_USER = "module.mylutece.openiddatabase.modify_user.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MANAGE_ROLES_USER = "module.mylutece.openiddatabase.manage_roles_user.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MANAGE_GROUPS_USER = "module.mylutece.openiddatabase.manage_groups_user.pageTitle";
    private static final String PROPERTY_DATABASE_TYPE = "database";

    //Messages
    private static final String MESSAGE_CONFIRM_REMOVE_USER = "module.mylutece.openiddatabase.message.confirmRemoveUser";
    private static final String MESSAGE_USER_EXIST = "module.mylutece.openiddatabase.message.user_exist";
    private static final String MESSAGE_EMAIL_INVALID = "module.mylutece.database.message.email_invalid";
    private static final String MESSAGE_ERROR_MODIFY_USER = "module.mylutece.openiddatabase.message.modify.user";
    private static final String MESSAGE_ERROR_REMOVE_USER = "module.mylutece.openiddatabase.message.remove.user";
    private static final String MESSAGE_ERROR_MANAGE_ROLES = "module.mylutece.openiddatabase.message.manage.roles";
    private static final String MESSAGE_ERROR_MANAGE_GROUPS = "module.mylutece.openiddatabase.message.manage.groups";

    // Parameters
    private static final String PARAMETER_PLUGIN_NAME = "plugin_name";
    private static final String PARAMETER_MYLUTECE_DATABASE_USER_ID = "mylutece_database_user_id";
    private static final String PARAMETER_MYLUTECE_DATABASE_ROLE_ID = "mylutece_database_role_id";
    private static final String PARAMETER_MYLUTECE_DATABASE_GROUP_KEY = "mylutece_database_group_key";
    private static final String PARAMETER_LOGIN = "login";
    private static final String PARAMETER_PASSWORD = "password";
    private static final String PARAMETER_LAST_NAME = "last_name";
    private static final String PARAMETER_FIRST_NAME = "first_name";
    private static final String PARAMETER_EMAIL = "email";

    // Marks FreeMarker
    private static final String MARK_USERS_LIST = "user_list";
    private static final String MARK_USER = "user";
    private static final String MARK_PLUGIN_NAME = "plugin_name";
    private static final String MARK_ROLES_LIST = "role_list";
    private static final String MARK_ROLES_LIST_FOR_USER = "user_role_list";
    private static final String MARK_GROUPS_LIST = "group_list";
    private static final String MARK_GROUPS_LIST_FOR_USER = "user_group_list";
    private static final Object MARK_EXTERNAL_APPLICATION_EXIST = "external_application_exist";

    // Templates
    private static final String TEMPLATE_CREATE_USER = "admin/plugins/mylutece/modules/openiddatabase/create_user.html";
    private static final String TEMPLATE_MODIFY_USER = "admin/plugins/mylutece/modules/openiddatabase/modify_user.html";
    private static final String TEMPLATE_MANAGE_USERS = "admin/plugins/mylutece/modules/openiddatabase/manage_users.html";
    private static final String TEMPLATE_MANAGE_ROLES_USER = "admin/plugins/mylutece/modules/openiddatabase/manage_roles_user.html";
    private static final String TEMPLATE_MANAGE_GROUPS_USER = "admin/plugins/mylutece/modules/openiddatabase/manage_groups_user.html";
    private static Plugin _plugin;

    /**
     * Returns the User creation form
     *
     * @param request The Http request
     * @return Html creation form
     */
    public String getCreateUser( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_CREATE_USER );

        HashMap<String, Object> model = new HashMap<String, Object>(  );

        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CREATE_USER, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Process user's creation
     *
     * @param request The Http request
     * @return The user's Displaying Url
     */
    public String doCreateUser( HttpServletRequest request )
    {
        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }

        String strLogin = request.getParameter( PARAMETER_LOGIN );
        String strPassword = request.getParameter( PARAMETER_PASSWORD );
        String strLastName = request.getParameter( PARAMETER_LAST_NAME );
        String strFirstName = request.getParameter( PARAMETER_FIRST_NAME );
        String strEmail = request.getParameter( PARAMETER_EMAIL );

        if ( ( strLogin.length(  ) == 0 ) || ( strPassword.length(  ) == 0 ) || ( strLastName.length(  ) == 0 ) ||
                ( strFirstName.length(  ) == 0 ) || ( strEmail.length(  ) == 0 ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        if ( !StringUtil.checkEmail( strEmail ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_EMAIL_INVALID, AdminMessage.TYPE_STOP );
        }

        OpenIdDatabaseUser databaseUser = new OpenIdDatabaseUser(  );
        databaseUser.setEmail( strEmail );
        databaseUser.setFirstName( strFirstName );
        databaseUser.setLastName( strLastName );
        databaseUser.setLogin( strLogin );
        databaseUser.setAuthentificationType( PROPERTY_DATABASE_TYPE );

        if ( OpenIdDatabaseUserHome.findDatabaseUsersListForLogin( strLogin, _plugin ).size(  ) != 0 )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_USER_EXIST, AdminMessage.TYPE_STOP );
        }

        OpenIdDatabaseUserHome.create( databaseUser, strPassword, _plugin );

        return MANAGE_USERS + "?" + PARAMETER_PLUGIN_NAME + "=" + _plugin.getName(  );
    }

    /**
     * Returns the User modification form
     *
     * @param request The Http request
     * @return Html modification form
     */
    public String getModifyUser( HttpServletRequest request )
    {
        setPageTitleProperty( PROPERTY_PAGE_TITLE_MODIFY_USER );

        OpenIdDatabaseUser databaseUser = getDatabaseUserFromRequest( request );

        if ( databaseUser == null )
        {
            return getCreateUser( request );
        }

        HashMap model = new HashMap(  );

        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( MARK_USER, databaseUser );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MODIFY_USER, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Process user's modification
     *
     * @param request The Http request
     * @return The user's Displaying Url
     */
    public String doModifyUser( HttpServletRequest request )
    {
        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }

        String strLogin = request.getParameter( PARAMETER_LOGIN );
        String strLastName = request.getParameter( PARAMETER_LAST_NAME );
        String strFirstName = request.getParameter( PARAMETER_FIRST_NAME );
        String strEmail = request.getParameter( PARAMETER_EMAIL );

        if ( ( strLogin.length(  ) == 0 ) || ( strLastName.length(  ) == 0 ) || ( strFirstName.length(  ) == 0 ) ||
                ( strEmail.length(  ) == 0 ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.MANDATORY_FIELDS, AdminMessage.TYPE_STOP );
        }

        OpenIdDatabaseUser databaseUser = getDatabaseUserFromRequest( request );

        if ( databaseUser == null )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_MODIFY_USER, AdminMessage.TYPE_ERROR );
        }

        if ( !databaseUser.getLogin(  ).equalsIgnoreCase( strLogin ) &&
                ( OpenIdDatabaseUserHome.findDatabaseUsersListForLogin( strLogin, _plugin ).size(  ) != 0 ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_USER_EXIST, AdminMessage.TYPE_STOP );
        }

        if ( !StringUtil.checkEmail( strEmail ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_EMAIL_INVALID, AdminMessage.TYPE_STOP );
        }

        databaseUser.setEmail( strEmail );
        databaseUser.setFirstName( strFirstName );
        databaseUser.setLastName( strLastName );
        databaseUser.setLogin( strLogin );

        OpenIdDatabaseUserHome.update( databaseUser, _plugin );

        return MANAGE_USERS + "?" + PARAMETER_PLUGIN_NAME + "=" + _plugin.getName(  );
    }

    /**
     * Returns removal user's form
     *
     * @param request The Http request
     * @return Html form
     */
    public String getRemoveUser( HttpServletRequest request )
    {
        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }

        UrlItem url = new UrlItem( JSP_DO_REMOVE_USER );
        url.addParameter( PARAMETER_PLUGIN_NAME, _plugin.getName(  ) );
        url.addParameter( PARAMETER_MYLUTECE_DATABASE_USER_ID,
            request.getParameter( PARAMETER_MYLUTECE_DATABASE_USER_ID ) );

        return AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_REMOVE_USER, url.getUrl(  ),
            AdminMessage.TYPE_CONFIRMATION );
    }

    /**
     * Process user's removal
     *
     * @param request The Http request
     * @return The Jsp management URL of the process result
     */
    public String doRemoveUser( HttpServletRequest request )
    {
        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }

        OpenIdDatabaseUser user = getDatabaseUserFromRequest( request );

        if ( user == null )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_REMOVE_USER, AdminMessage.TYPE_ERROR );
        }

        OpenIdDatabaseUserHome.remove( user, _plugin );

        OpenIdDatabaseHome.removeRolesForUser( user.getUserId(  ), _plugin );

        return MANAGE_USERS + "?" + PARAMETER_PLUGIN_NAME + "=" + _plugin.getName(  );
    }

    /**
     * Returns users management form
     *
     * @param request The Http request
     * @return Html form
     */
    public String getManageUsers( HttpServletRequest request )
    {
        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_MANAGE_USERS );

        Boolean applicationsExist = Boolean.FALSE;

        Collection userList = OpenIdDatabaseUserHome.findDatabaseUsersList( _plugin );
        HashMap model = new HashMap(  );
        model.put( MARK_USERS_LIST, userList );
        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );
        model.put( MARK_EXTERNAL_APPLICATION_EXIST, applicationsExist );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_USERS, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Returns roles management form for a specified user
     *
     * @param request The Http request
     * @return Html form
     */
    public String getManageRolesUser( HttpServletRequest request )
    {
        AdminUser adminUser = getUser(  );

        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }

        setPageTitleProperty( PROPERTY_PAGE_TITLE_MANAGE_ROLES_USER );

        OpenIdDatabaseUser user = getDatabaseUserFromRequest( request );

        if ( user == null )
        {
            return getManageUsers( request );
        }

        Collection<Role> allRoleList = RoleHome.findAll(  );
        allRoleList = (ArrayList<Role>) RBACService.getAuthorizedCollection( allRoleList,
                RoleResourceIdService.PERMISSION_ASSIGN_ROLE, adminUser );

        ArrayList<String> userRoleKeyList = OpenIdDatabaseHome.findUserRolesFromLogin( user.getLogin(  ), _plugin );
        Collection<Role> userRoleList = new ArrayList<Role>(  );

        for ( String strRoleKey : userRoleKeyList )
        {
            for ( Role role : allRoleList )
            {
                if ( role.getRole(  ).equals( strRoleKey ) )
                {
                    userRoleList.add( RoleHome.findByPrimaryKey( strRoleKey ) );
                }
            }
        }

        HashMap model = new HashMap(  );
        model.put( MARK_ROLES_LIST, allRoleList );
        model.put( MARK_ROLES_LIST_FOR_USER, userRoleList );
        model.put( MARK_USER, user );
        model.put( MARK_PLUGIN_NAME, _plugin.getName(  ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_MANAGE_ROLES_USER, getLocale(  ), model );

        return getAdminPage( template.getHtml(  ) );
    }

    /**
     * Process assignation roles for a specified user
     *
     * @param request The Http request
     * @return Html form
     */
    public String doAssignRoleUser( HttpServletRequest request )
    {
        if ( _plugin == null )
        {
            String strPluginName = request.getParameter( PARAMETER_PLUGIN_NAME );
            _plugin = PluginService.getPlugin( strPluginName );
        }

        //get User
        OpenIdDatabaseUser user = getDatabaseUserFromRequest( request );

        if ( user == null )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_ERROR_MANAGE_ROLES, AdminMessage.TYPE_ERROR );
        }

        String[] roleArray = request.getParameterValues( PARAMETER_MYLUTECE_DATABASE_ROLE_ID );

        OpenIdDatabaseHome.removeRolesForUser( user.getUserId(  ), _plugin );

        if ( roleArray != null )
        {
            for ( int i = 0; i < roleArray.length; i++ )
            {
                OpenIdDatabaseHome.addRoleForUser( user.getUserId(  ), roleArray[i], _plugin );
            }
        }

        return MANAGE_USERS + "?" + PARAMETER_PLUGIN_NAME + "=" + _plugin.getName(  );
    }

    /**
     *
     * @param request The http request
     * @return The Database User
     */
    private OpenIdDatabaseUser getDatabaseUserFromRequest( HttpServletRequest request )
    {
        String strUserId = request.getParameter( PARAMETER_MYLUTECE_DATABASE_USER_ID );

        if ( ( strUserId == null ) || !strUserId.matches( REGEX_DATABASE_USER_ID ) )
        {
            return null;
        }

        int nUserId = Integer.parseInt( strUserId );

        OpenIdDatabaseUser user = OpenIdDatabaseUserHome.findByPrimaryKey( nUserId, _plugin );

        return user;
    }
}

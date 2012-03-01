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
package fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.business;

import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.sql.DAOUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class PasswordRecoveryByLinkDAO implements IPasswordRecoveryService
{
    private static final String TEMPLATE_EMAIL_BODY_LINK = "skin/plugins/mylutece/modules/openiddatabase/email_body_link.html";
    private static final String PROPERTY_EMAIL_OBJECT_LINK = "module.mylutece.openiddatabase.email_link.object";
    private static final String PROPERTY_MYLUTECE_RECOVERY_LINK_VALIDITY = "mylutece-openiddatabase.email.link.validity";
    private static final String PROPERTY_NOREPLY_EMAIL = "mail.noreply.email";
    private static final String MARK_UNIQUE_ID = "operation_id";
    private static final String MARK_USER = "user";
    private static final String PROPERTY_PROD_BASE_URL = "lutece.prod.url";
    private static final String MARK_PROD_URL = "prod_url";
    private static final String SQL_QUERY_INSERT_USER_OPERATION_DEPENDENCY = "INSERT INTO mylutece_database_openid_recovery_user ( mylutece_database_openid_user_id, id_recovery_operation ) VALUES ( ? , ? )";
    private static final String SQL_QUERY_INSERT = "INSERT  INTO `mylutece_database_openid_recovery`(`id_recovery_operation`,`date_recovery_creation`,`date_recovery_expiration`,`operation_recovery_accomplished`) VALUES ( ? , ? , ? , ? )";
    private static final String SQL_QUERY_SELECT_BY_TOKEN = "SELECT mylutece_database_openid_user_id FROM mylutece_database_openid_recovery_user where id_recovery_operation= ?";
    private static final String SQL_QUERY_SELECT_EXPIRATION_DATE_BY_TOKEN = "SELECT date_recovery_expiration FROM mylutece_database_openid_recovery WHERE id_recovery_operation= ? ";
    
    /** This class implements the Singleton design pattern. */
    private static PasswordRecoveryByLinkDAO _dao = new PasswordRecoveryByLinkDAO(  );

    /**
     * Returns the unique instance of the singleton.
     *
     * @return the instance
     */
    static PasswordRecoveryByLinkDAO getInstance(  )
    {
        return _dao;
    }

    public boolean verifyOperationValid( String strOperationId, Plugin plugin )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public String newOperationKey(  )
    {
        return java.util.UUID.randomUUID(  ).toString(  );
    }

    /**
     * Inserts the depedency between the user and the operation
     * @param nUserId The id of the user
     * @param strIdOperation The id of the operation
     * @param plugin The plugin
     */
    public void insertDependency( int nUserId, String strIdOperation, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT_USER_OPERATION_DEPENDENCY, plugin );

        daoUtil.setInt( 1, nUserId );
        daoUtil.setString( 2, strIdOperation );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    public void storeOperation( OpenIdDatabaseUser user, String strOperationId, Plugin plugin )
    {
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin );
        Date dateNow = Calendar.getInstance(  ).getTime(  );
        int nDaysValidity = AppPropertiesService.getPropertyInt( PROPERTY_MYLUTECE_RECOVERY_LINK_VALIDITY, 1 );
        Calendar calExpiration = Calendar.getInstance(  );
        calExpiration.add( Calendar.DATE, nDaysValidity );

        Date dateExpiration = calExpiration.getTime(  );

        daoUtil.setString( 1, strOperationId );

        daoUtil.setDate( 2, new java.sql.Date( dateNow.getTime(  ) ) );
        daoUtil.setDate( 3, new java.sql.Date( dateExpiration.getTime(  ) ) );
        daoUtil.setBoolean( 4, true );
        insertDependency( user.getUserId(  ), strOperationId, plugin );

        daoUtil.executeUpdate(  );
        daoUtil.free(  );
    }

    public void processOperations( OpenIdDatabaseUser user, Locale locale, Plugin plugin )
    {
        String strNewOperationKey = newOperationKey(  );
        storeOperation( user, strNewOperationKey, plugin ); //Store

        HashMap<String, Object> model = new HashMap<String, Object>(  );
        String strSender = AppPropertiesService.getProperty( PROPERTY_NOREPLY_EMAIL );
        model.put( MARK_UNIQUE_ID, strNewOperationKey );
        model.put( MARK_USER, user );
        model.put( MARK_PROD_URL, AppPropertiesService.getProperty( PROPERTY_PROD_BASE_URL ));

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_EMAIL_BODY_LINK, locale, model );
        MailService.sendMailHtml( user.getEmail(  ), user.getFirstName(  ), strSender, getMailSubject( locale ),
            template.getHtml(  ) ); //Send Mail
    }

    private String getMailSubject( Locale locale )
    {
        return I18nService.getLocalizedString( PROPERTY_EMAIL_OBJECT_LINK, locale );
    }

    public int getUserId( String strIdToken, Plugin plugin )
    {
        int nUserId = 0;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_TOKEN, plugin );
        daoUtil.setString( 1, strIdToken );

        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            nUserId = daoUtil.getInt( 1 );
        }

        daoUtil.free(  );

        return nUserId;
    }

    public boolean isExpired( String strIdToken, Plugin plugin )
    {
        boolean bIsExpired = false;
        Date dateExpiration = null;
        DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_EXPIRATION_DATE_BY_TOKEN, plugin );
        daoUtil.setString( 1, strIdToken );
        daoUtil.executeQuery(  );

        if ( daoUtil.next(  ) )
        {
            dateExpiration = daoUtil.getDate( 1 );
        }

        daoUtil.free(  );

        Date dateNow = Calendar.getInstance(  ).getTime(  );

        if ( dateNow.after( dateExpiration ) )
        {
            bIsExpired = true;
        }

        return bIsExpired;
    }
}

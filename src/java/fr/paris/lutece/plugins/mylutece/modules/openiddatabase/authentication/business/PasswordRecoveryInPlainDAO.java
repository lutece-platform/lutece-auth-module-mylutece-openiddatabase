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
package fr.paris.lutece.plugins.mylutece.modules.openiddatabase.authentication.business;

import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.util.HashMap;
import java.util.Locale;


public class PasswordRecoveryInPlainDAO implements IPasswordRecoveryService
{
    private static final String MARK_USER = "user";
    private static final String MARK_PASSWORD = "password";
    private static final String TEMPLATE_EMAIL_BODY = "skin/plugins/mylutece/modules/openiddatabase/email_body.html";
    private static final String PROPERTY_NOREPLY_EMAIL = "mail.noreply.email";
    private static final String PROPERTY_EMAIL_OBJECT_PLAIN = "module.mylutece.openiddatabase.email.object.plain";

    public void processOperations( OpenIdDatabaseUser user, Locale locale, Plugin plugin )
    {
        HashMap<String, Object> model = new HashMap<String, Object>(  );
        String strSender = AppPropertiesService.getProperty( PROPERTY_NOREPLY_EMAIL );
        model.put( MARK_USER, user );
        model.put( MARK_PASSWORD, OpenIdDatabaseUserHome.findPasswordByPrimaryKey( user.getUserId(  ), plugin ) );
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_EMAIL_BODY, locale, model );
    
        MailService.sendMailHtml( user.getEmail(  ), user.getFirstName(  ), strSender, getMailSubject( locale ),
            template.getHtml(  ) ); //Send Mail
    }

    private String getMailSubject( Locale locale )
    {
        return I18nService.getLocalizedString( PROPERTY_EMAIL_OBJECT_PLAIN, locale );
    }

    public int getUserId( String strIdToken, Plugin plugin )
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isExpired( String strIdToken, Plugin plugin )
    {
        // TODO Auto-generated method stub
        return false;
    }
}

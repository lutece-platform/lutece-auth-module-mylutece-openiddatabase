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

import fr.paris.lutece.portal.service.plugin.Plugin;

import java.util.Collection;


/**
 *
 * @author Etienne
 */
public interface IOpenIdDatabaseUserDAO
{
    /**
     * Generates a new primary key
     * @param plugin The Plugin using this data access service
     * @return The new primary key
     */
    int newPrimaryKey( Plugin plugin );

    /**
     * Insert a new record in the table.
     *
     * @param databaseUser The databaseUser object
     * @param strPassword The user password
     * @param plugin The Plugin using this data access service
     */
    void insert( OpenIdDatabaseUser databaseUser, String strPassword, Plugin plugin );

    /**
     * Load the data of DatabaseUser from the table
     *
     * @param nDatabaseUserId The identifier of databaseUser
     * @param plugin The Plugin using this data access service
     * @return the instance of the DatabaseUser
     */
    OpenIdDatabaseUser load( int nDatabaseUserId, Plugin plugin );

    /**
     * Delete a record from the table
     * @param databaseUser The databaseUser object
     * @param plugin The Plugin using this data access service
     */
    void delete( OpenIdDatabaseUser databaseUser, Plugin plugin );

    /**
     * Update the record in the table
     * @param databaseUser The reference of databaseUser
     * @param plugin The Plugin using this data access service
     */
    void store( OpenIdDatabaseUser databaseUser, Plugin plugin );

    /**
     * Update the record in the table
     * @param databaseUser The reference of databaseUser
     * @param plugin The Plugin using this data access service
     */
    void storeByLogin( OpenIdDatabaseUser databaseUser, Plugin plugin );

    /**
     * Update the record in the table
     * @param databaseUser The reference of databaseUser
     * @param strNewPassword The new password to store
     * @param plugin The Plugin using this data access service
     */
    void updatePassword( OpenIdDatabaseUser databaseUser, String strNewPassword, Plugin plugin );

    /**
     * Load the password of the specified user
     *
     * @param nDatabaseUserId The Primary key of the databaseUser
     * @param plugin The current plugin using this method
     * @return String the user password
     */
    String selectPasswordByPrimaryKey( int nDatabaseUserId, Plugin plugin );

    /**
     * Load the list of databaseUsers
     * @param plugin The Plugin using this data access service
     * @return The Collection of the databaseUsers
     */
    Collection<OpenIdDatabaseUser> selectDatabaseUserList( Plugin plugin );

    /**
     * Load the list of DatabaseUsers for a login
     * @param strLogin The login of DatabaseUser
     * @param plugin The Plugin using this data access service
     * @return The Collection of the DatabaseUsers
     */
    Collection<OpenIdDatabaseUser> selectDatabaseUserListForLogin( String strLogin, Plugin plugin );

    /**
     * Load the list of a user by a email
     * @param strEmail The email of DatabaseUser
     * @param plugin The Plugin using this data access service
     * @return The Collection of the DatabaseUsers
     */
    OpenIdDatabaseUser selectDatabaseUserByEmail( String strEmail, Plugin plugin );

    /**
     * Check the password for a DatabaseUser
     *
     * @param strLogin The user login of DatabaseUser
     * @param strPassword The password of DatabaseUser
     * @param plugin The Plugin using this data access service
     * @return true if password is ok
     */
    boolean checkPassword( String strLogin, String strPassword, Plugin plugin );

    /**
     * Checks whether the login exists
     *
     * @param strLogin The user login of DatabaseUser
     * @param plugin The Plugin using this data access service
     * @return true if password is ok
     */
    boolean checkUserLogin( String strLogin, Plugin plugin );
}
